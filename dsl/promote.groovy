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

/********* User editable properties ************/
property "/projects/$pluginName/config/projName", value: "On line bank Release"
property "/projects/$pluginName/config/artifactGroup", value: "com.mybank.apps"
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
			name: "September 2016 Online Banking",
			plannedStartDate: "${(String) formatDate(new Date())}",
			plannedEndDate: "2016-09-30",
			versionIndex: 1
		],
		[
			name: "October 2016 Online Banking",
			plannedStartDate: "2016-10-01",
			plannedEndDate: "2016-10-31",
			versionIndex: 2
		],		
		[
			name: "November 2016 Online Banking",
			plannedStartDate: "2016-11-01",
			plannedEndDate: "2016-11-30",
			versionIndex: 3
		],
	]
""".stripIndent()
property "/projects/$pluginName/config/pipe", value: '''\
	[
		name: "Quarterly Online Banking",
		stages: ["UAT", "STG", "PROD"]
	]
'''.stripIndent()
property "/projects/$pluginName/config/apps", value: '''\
	[
		[
			name: "Account Statements",
			artifactKey: "statements",
			versions: ["2.1","2.2","2.3","2.4"]
		],
		[
			name: "Credit Card Accounts",
			artifactKey: "cards",
			versions: ["5.0","5.1","5.5","5.7"]
		],
		[
			name: "Fund Transfer",
			artifactKey: "fund",
			versions: ["1.7","1.8","2.0","2.1"]
		]
	]
'''.stripIndent()
/********* End of user editable properties ************/


// Build resource model
def stepDir = pluginDir + "/dsl/steps/"
project pluginName,{
	property "clean" // commands to clean up after this plugin
	property "ec_visibility", value: "hidden" // Legal values: pickListOnly, hidden, all
	procedure "Create Release Model",{
		//step "Set up properties", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "properties.groovy").text
		step "Set initial clean property", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "initialClean.groovy").text
		step "Set up permissions", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "permissions.groovy").text
		step "Set up artifacts", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "artifacts.groovy").text
		step "Set up environments", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "env.groovy").text
		step "Set up applications", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "app.groovy").text
		step "Deploy and snapshot all apps versions to Dev", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "deployDevSnap.groovy").text
		step "Deploy snaphot versions[0] to upper environments", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "deployUpper.groovy").text
		//step "Wait for deployments", shell: "ec-perl", command: "sleep 10"
		//step "Set up snapshots", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "snapshots.groovy").text
		step "Set up pipeline", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "pipeline.groovy").text
		step "Set up releases", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "release.groovy").text
	} // procedure "Create Release Model"
} // project pluginName

transaction {
	runProcedure projectName: pluginName, procedureName: "Create Release Model"
} 
