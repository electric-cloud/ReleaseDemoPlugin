def projName = "$[/myProject/config/projName]"
def apps = $[/myProject/config/apps]
def pipe = $[/myProject/config/pipe]

pipe.stages.each { env ->
	apps.each { app ->
		runProcess projectName: projName,
			applicationName: app.name,
			processName: "Deploy",
			environmentName: env,
			snapshotName : app.versions[0]
	} // apps.each
} // pipe.stages.each

