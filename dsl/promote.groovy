// Variables available for use in DSL code
def pluginName = args.pluginName
def pluginKey = getProject("/plugins/$pluginName/project").pluginKey
def pluginDir = getProperty("/server/settings/pluginsDirectory").value + "/" + pluginName
// END Variables

// print date in yyyy-mm-dd format
def formatDate(d) {
	year = d.year+1900
	date = d.date
	month = d.month+1
	return "${year}-${month}-${date}"
}

// Create an array of start and stop dates for releases
startStopDates = []
(0..2).each {
    def start = new Date() + 2 + (it * 33)
    def stop = new Date()  + (it * 33 + 33)
    startStopDates.push(
        start: formatDate(start),
        stop: formatDate(stop),
        month: new java.text.DateFormatSymbols().months[start.month]
    )
}

/********* User editable properties ************/
property "/projects/$pluginName/config/projName", value: "Online banking"
property "/projects/$pluginName/config/artifactGroup", value: "com.acmebank.apps"
/*
	versionIndex selects the version from apps.versions[].  Use apps.version[0]
	for initial environment snapshots.  Make sure that there are as many versions
	in the apps data structure as there are releases in the releases data structure
	plus one (index 0).
*/

/*
	plannedStartDate: (String) formatDate(new Date()),
	plannedEndDate: (String) formatDate(new Date()+14),
*/

property "/projects/$pluginName/config/releases", value: """\
	[
		[
			name: "${startStopDates[0].month} Online Banking",
			plannedStartDate: "${startStopDates[0].start}",
			plannedEndDate: "${startStopDates[0].stop}",
			versionIndex: 1
		],
		[
			name: "${startStopDates[1].month} Online Banking",
			plannedStartDate: "${startStopDates[1].start}",
			plannedEndDate: "${startStopDates[1].stop}",
			versionIndex: 2
		],		
		[
			name: "${startStopDates[2].month} Online Banking",
			plannedStartDate: "${startStopDates[2].start}",
			plannedEndDate: "${startStopDates[2].stop}",
			versionIndex: 3
		],
	]
""".stripIndent()
property "/projects/$pluginName/config/pipe", value: '''\
	[
		name: "Monthly Online Banking",
		stages: ["UAT", "STG", "PROD"]
	]
'''.stripIndent()
property "/projects/$pluginName/config/apps", value: '''\
	[
		[
			name: "Account Statements",
			artifactName: "statements",
			versions: ["2.1","2.2","2.3","2.4"],
			tiers: ["app":"Apache","db":"Oracle","web":"JBoss"]
		],
		[
			name: "Credit Card Accounts",
			artifactName: "cards",
			versions: ["5.0","5.1","5.5","5.7"],
			tiers: ["app":"Apache","db":"Oracle","web":"JBoss"]
		],
		[
			name: "Fund Transfer",
			artifactName: "fund",
			versions: ["1.7","1.8","2.0","2.1"],
			tiers: ["app":"Apache","db":"Oracle","web":"JBoss"]
		]
	]
'''.stripIndent()
/********* End of user editable properties ************/


// Build resource model
def stepDir = pluginDir + "/dsl/steps/"
project pluginName,{
	property "clean" // commands to clean up after this plugin
	property "ec_visibility", value: "all" // Legal values: pickListOnly, hidden, all
		
	procedure "Create Release Model",
		description: "Run me to create a Release model", {
	
		formalParameter "projName", required: "true"
		formalParameter "artifactGroup", required: "true"
		formalParameter "apps", required: "true"
		formalParameter "pipe", required: "true"
		formalParameter "releases", required: "true"
		
		property "ec_parameterForm", value: new File(pluginDir + "/dsl/ec_parameterForm.xml").text
		
		property "runModelSteps", "1"
		
		//step "Set up properties", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "properties.groovy").text
		step "Set initial clean property", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "initialClean.groovy").text
		step "Add project to clean list", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "cleanProject.groovy").text
		step "Set up permissions", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "permissions.groovy").text
		step "Create Procedures", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "procedures.groovy").text
		step "Create Application Validation Procedure", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "appValidation.groovy").text
		//step "Set up artifacts", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "artifacts.groovy").text
		//step "Publish artifacts", subproject: projectName, subprocedure: "createArtifacts"
		step "Set up environments", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "env.groovy").text, condition: '$[/myProcedure/runModelSteps]'
		step "Set up applications", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "app.groovy").text, condition: '$[/myProcedure/runModelSteps]'
		step "Create Commit Pipeline", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "commitPipeline.groovy").text, condition: '$[/myProcedure/runModelSteps]'
		step "Run Commit Pipeline", description: "Builds all versions and creates snapshots for them",
			subproject: '$[/myJob/projName]',
			subprocedure: 'Run pipeline',
			actualParameter: [
				projName: '$[/myJob/projName]',
				pipeName: 'Commit'
			]
			condition: '$[/myProcedure/runModelSteps]'
		//step "Deploy snaphot versions[0] to upper environments", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "deployUpper.groovy").text, condition: '$[/myProcedure/runModelSteps]'
		//step "Set up snapshots", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "snapshots.groovy").text
		step "Set up release pipeline", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "pipeline.groovy").text, condition: '$[/myProcedure/runModelSteps]'
		step "Set up releases", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "release.groovy").text, condition: '$[/myProcedure/runModelSteps]'
	} // procedure "Create Release Model"

	procedure "Generate Reports",{
		step 'Create Report',
			subproject : '/plugins/EC-FileOps/project',
			subprocedure : 'AddTextToFile',
			actualParameter : [
				Path: 'summary.html',
				Content: '''\
					<html>
						<head/>
						<body>
							<h1>Test Results</h1>
							<h2>Pipeline Name: $[/myPipelineRuntime/pipelineName]</h2>
							<h2>Stage: $[/myPipelineStageRuntime/stageName]</h2>
						</body>
					</html>
				'''.stripIndent()
			]
		step 'Create pipeline stage link', shell: 'ec-perl',
			command: '''\
				use strict;
				use ElectricCommander();
				my $ec = new ElectricCommander->new({format=>"json"});
				$ec->setProperty("/myJob/artifactsDirectory", ".");
				$ec->setProperty("/myPipelineStageRuntime/ec_summary/Application Deployment Summary",
					"<html><a href=\\"/commander/jobSteps/$[jobStepId]/summary.html\\">HTML report</a></html");
			'''.stripIndent()
		step 'Update release summary', shell: 'ec-perl',
			command: '''\
				use strict;
				use ElectricCommander();
				my $ec = new ElectricCommander->new({format=>"json"});
				$ec->setProperty("/myJob/artifactsDirectory", ".");
				$ec->setProperty("/myReleaseRuntime/ec_releaseSummary/$[/myPipelineStageRuntime/stageName]",
					"<html><li><a href=\\"/commander/jobSteps/$[jobStepId]/summary.html\\">$[/myPipelineStageRuntime/stageName] Deployment Report</a></li></html");
			'''.stripIndent()
	} // procedure "Generate Reports"	
} // project pluginName

/*
transaction {
	runProcedure projectName: pluginName, procedureName: "Create Release Model"
} 
*/
