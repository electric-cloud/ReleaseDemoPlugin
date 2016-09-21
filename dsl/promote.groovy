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
	
		
	procedure "createArtifacts",{
		formalParameter "_versions", defaultValue: '$[/myJob/versions]'
		formalParameter "_artifactKeys", defaultValue: '$[/myJob/artifactKeys]'
		formalParameter "_artifactGroup", defaultValue: '$[/myJob/artifactGroup]'
		
		step "createArtifacts", shell: "ec-perl",
			command: new File(pluginDir + "/dsl/steps/createArtifacts.pl").text
	}
	
	procedure "Create Release Model",{
	
		formalParameter "projName", required: "true"
		formalParameter "artifactGroup", required: "true"
		formalParameter "apps", required: "true"
		formalParameter "pipe", required: "true"
		formalParameter "releases", required: "true"
		
		property "ec_parameterForm", value: '''\
			<editor>
				<formElement>
					<property>projName</property>
					<label>Project Name</label>
					<documentation>The name of the project where the release and supporting assets are to be stored.</documentation>
					<type>entry</type>
					<required>true</required>
					<value>$[/myProject/config/projName]</value>
				</formElement>
				<formElement>
					<property>artifactGroup</property>
					<label>Artifact Group Name</label>
					<documentation>The group name for artifacts used, for example, com.mycompany.myapp</documentation>
					<type>entry</type>
					<required>true</required>
					<value>$[/myProject/config/artifactGroup]</value>
				</formElement>
				<formElement>
					<property>apps</property>
					<label>Application definitions</label>
					<documentation>A Groovy data structure describing the applications to be created</documentation>
					<type>textarea</type>
					<required>true</required>
					<value>$[/myProject/config/apps]</value>
				</formElement>
				<formElement>
					<property>pipe</property>
					<label>Release pipeline definition</label>
					<documentation>A Groovy data structure describing the release pipeline to be created</documentation>
					<type>textarea</type>
					<required>true</required>
					<value>$[/myProject/config/pipe]</value>
				</formElement>
				<formElement>
					<property>releases</property>
					<label>Release definitions</label>
					<documentation>A Groovy data structure describing the releases to be created</documentation>
					<type>textarea</type>
					<required>true</required>
					<value>$[/myProject/config/releases]</value>
				</formElement>
			</editor>
		'''.stripIndent()
	
		//step "Set up properties", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "properties.groovy").text
		step "Set initial clean property", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "initialClean.groovy").text
		step "Add project to clean list", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "cleanProject.groovy").text
		step "Set up permissions", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "permissions.groovy").text
		step "Set up artifacts", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "artifacts.groovy").text
		step "Publish artifacts", subproject: projectName, subprocedure: "createArtifacts"
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

/*
transaction {
	runProcedure projectName: pluginName, procedureName: "Create Release Model"
} 
*/
