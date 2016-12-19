def apps = $[/myJob/apps]
def projName = "$[/myJob/projName]"
def artifactGroup = "$[/myJob/artifactGroup]"

project projName, {

	pipeline "Commit", {
		stage "Commit", {
		
			apps.each { app ->
				app.versions.each { ver ->
					task "Build $app.name $ver",
						taskType: 'PROCEDURE',
						subproject: projName,
						subprocedure: "Build artifacts",
						actualParameter: [
							appName: app.name,
							artGroup: artifactGroup,
							artVersion: ver,
							artName: app.artifactName
						]
					task "Deploy $app.name $ver", {
						// TODO: add version explicitly, don't rely on latest
						environmentName = 'Commit'
						environmentProjectName = projectName
						errorHandling = 'stopOnError'
						subapplication = app.name
						subprocess = 'Deploy'
						subproject = projectName
						taskType = 'PROCESS'
						}
					task "Snapshot $app.name $ver",
						description: '',
						actualParameter: [
							appName: app.name,
							version: ver
						],
						errorHandling: 'stopOnError',
						subprocedure: 'snapshot',
						subproject: projectName,
						taskType: 'PROCEDURE'
				} // version
			} // app
		} // stage
	} // Pipeline
} // Project