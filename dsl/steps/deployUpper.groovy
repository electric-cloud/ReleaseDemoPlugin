def projName = "$[projName]"
def apps = $[apps]
def pipe = $[pipe]

pipe.stages.each { env ->
	apps.each { app ->
		runProcess projectName: projName,
			applicationName: app.name,
			processName: "Deploy",
			environmentName: env,
			snapshotName : app.versions[0]
	} // apps.each
} // pipe.stages.each

