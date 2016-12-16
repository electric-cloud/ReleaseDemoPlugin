def apps = $[/myJob/apps]
def projName = "$[/myJob/projName]"

apps.each { app ->
	app.versions.each { ver ->
		transaction {
			def actualParam = [:]
			app.tier.each { appTier, envTier ->
				def verParam = (String) "ec_${appTier}-version"
				actualParam.push((verParam):ver)
			}			
			runProcess projectName: projName,
				applicationName: app.name,
				processName: "Deploy",
				environmentName: "Commit",
				actualParameter : actualParam
		}
		transaction {sleep(20000)} // Wait for deploy
		transaction {
			snapshot projectName: projName,
				applicationName: app.name,
				snapshotName: ver,
				environmentName: "Commit",
				environmentprojectName: projName
		}
	}
}
