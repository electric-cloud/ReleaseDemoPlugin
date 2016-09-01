def apps = $[apps]
def projName = "$[projName]"

def appTier = "App"

apps.each { app ->
	def verParam = (String) "ec_${app.artifactKey}-version"
	app.versions.each { ver ->
		transaction {
			runProcess projectName: projName,
				applicationName: app.name,
				processName: "Deploy",
				environmentName: "Dev",
				actualParameter : [
					(verParam): ver
				]
		}
		transaction {sleep(10000)} // Wait for deploy
		transaction {
			snapshot projectName: projName,
				applicationName: app.name,
				snapshotName: ver,
				environmentName: "Dev",
				environmentprojectName: projName
		}
	}
}
