def pluginName = args.pluginName
def pluginKey = getProject("/plugins/$pluginName/project").pluginKey
def pluginDir = getProperty("/server/settings/pluginsDirectory").value + "/" + pluginName

// Remove content created by promotion
// Use Default project because pluginName project will be deleted if this is run due to uninstall
def clean = getProperty("/projects/$pluginName/clean").value

def stepNumber = 0
project "Default",{
	property "ec_visibility", value: "hidden" // Legal values: pickListOnly, hidden, all
	procedure "Clean Release Model",{
		clean.tokenize('\n').each {
			step "Clean " + stepNumber++,
				command: it
			step "Remove clean property",
				command: "ectool deleteProperty \"/projects/$pluginName/clean\""
			step "Delete this procedure",
				command: 'ectool deleteProcedure "$[/myProject/projectName]" "$[/myProcedure/procedureName]"'
		}
	} // procedure "Clean Release Model"
} // project "Default"

transaction {
	runProcedure(projectName: "Default", procedureName: "Clean Release Model")
}
