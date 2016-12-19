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
					exit \$[buildErrorLevel]
				""".stripIndent()
				
			step "Publish ${artKey}", description: "Publish artifacts",
				subproject : '/plugins/EC-Artifact/project',
				subprocedure : 'Publish',
				actualParameter : [
					artifactName: (String) "\$[artGroup].\$[artName]:${artKey}",
					artifactVersionVersion: '$[artVersion]-$[/myProject/buildCount]',
					includePatterns: 'installer.sh',
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
}