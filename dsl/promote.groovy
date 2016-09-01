// Variables available for use in DSL code
def pluginName = args.pluginName
def pluginKey = getProject("/plugins/$pluginName/project").pluginKey
def pluginDir = getProperty("/server/settings/pluginsDirectory").value + "/" + pluginName
// END Variables

// Build resource model
def stepDir = pluginDir + "/dsl/steps/"
project pluginName,{
	property "clean" // commands to clean up after this plugin
	property "ec_visibility", value: "hidden" // Legal values: pickListOnly, hidden, all
	procedure "Create Release Model",{
		step "Set up properties", shell: "ectool evalDsl --dslFile {0}", command: new File(stepDir + "properties.groovy").text
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
