def projName = "$[/myJob/projName]"
def apps = $[/myJob/apps]

resource "Builder", hostName: "localhost"

project projName,{
	procedure "Build artifacts", resourceName: "Builder",
		description: "Generate artifacts", {
		
		formalParameter "appName", description: "Application Name", required: "true"
		formalParameter "artGroup", description: "Artifact Group Name", required: "true"
		formalParameter "artVersion", description: "Artifact Version", required: "true"
		formalParameter "artName", description: "Artifact Key", required: "true"
		formalParameter "buildErrorLevel", defaultValue: "0"
		formalParameter "causeRollback", defaultValue: "0"

		emailNotifier 'On Build Failure', {
			condition = '$[/javascript (getProperty("outcome") == "error")'
			configName = 'default'
			destinations = 'raj'
			eventType = 'onCompletion'
			formattingTemplate = '''\
				Subject: Job "$[jobName]" from procedure  "$[procedureName]" $[/myEvent/type] FAILED - ElectricFlow notification

				$[/server/ec_notifierTemplates/Html_JobTempl/body]
			'''.stripIndent()
		} // email notifier
		
		apps[0].tiers.each { artKey, envTier ->
		// Only looking at first application's tier names for artifact keys
			step "Build ${artKey}", errorHandling: "abortJob",
				command: """\
					echo running build \$[appName] \$[artGroup].\$[artName]:${artKey}:\$[artVersion]-\$[/increment /myProject/buildCount]
					echo creating installer.sh
					echo echo Installing \$[appName] \$[artGroup].\$[artName]:${artKey}:\$[artVersion]-\$[/myProject/buildCount] > installer.sh
					echo exit \$[causeRollback] >> installer.sh
					
					echo creating installer.bat
					echo echo Installing \$[appName] \$[artGroup].\$[artName]:${artKey}:\$[artVersion]-\$[/myProject/buildCount] > installer.bat
					echo exit \$[causeRollback] >> installer.bat
					
					exit \$[buildErrorLevel]
				""".stripIndent()
				
			step "Publish ${artKey}", description: "Publish artifacts",
				subproject : '/plugins/EC-Artifact/project',
				subprocedure : 'Publish',
				actualParameter : [
					artifactName: (String) "\$[artGroup].\$[artName]:${artKey}",
					artifactVersionVersion: '$[artVersion]-$[/myProject/buildCount]',
					includePatterns: 'installer.sh;installer.bat',
					repositoryName: 'default'
				]
		} // each tier (artifact key)


	} // procedure "Build artifacts"
	procedure "snapshot", description: "Create environment snapshot", {
	
		formalParameter "appName", required: "true"
		formalParameter "version", required: "true"
	
		step "Create Snapshot", shell: "ectool evalDsl --dslFile {0}",
			command: """\
				snapshot projectName: "$projectName",
					applicationName: '\$[appName]',
					snapshotName: '\$[version]',
					environmentName: "Commit",
					environmentprojectName: "$projectName"				
			""".stripIndent()
	} // procedure "snapshot"
	
	procedure "Run pipeline", description: "Run and wait for pipeline to complete",{
	
		formalParameter "projName"
		formalParameter "pipeName"
		
		step "Run pipeline", shell: "ec-perl",
			command: '''\
				use strict;
				use ElectricCommander ();
				$| = 1;
				my $ec = new ElectricCommander->new({format=>"json"});
				my $resp = $ec->runPipeline({projectName=>"$[projName]", pipelineName=>"$[pipeName]"});
				my $flowRuntimeId = $resp->{responses}[0]->{flowRuntime}->{flowRuntimeId};
				sub getStatus($) {
						my $status = $ec->getPipelineRuntimeDetails({flowRuntimeId=>$flowRuntimeId});
						return $status->{responses}[0]->{flowRuntime}[0]->{status};
				}

				while (getStatus($flowRuntimeId) eq "running") {
						print "Waiting for pipeline to complete\\n";
						sleep 5;
				}
				print getStatus($flowRuntimeId);

			'''.stripIndent()
	}
	
}