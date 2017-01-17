def projName = "$[/myJob/projName]"
def apps = $[/myJob/apps]
def pipe = $[/myJob/pipe]

pipe.stages.each { env ->
	apps.each { app ->
		runProcess projectName: projName,
			applicationName: app.name,
			processName: "Deploy",
			environmentName: env,
			snapshotName : app.versions[0]
	} // apps.each
} // pipe.stages.each

