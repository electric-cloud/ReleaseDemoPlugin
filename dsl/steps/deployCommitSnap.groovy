def apps = $[/myJob/apps]
def projName = "$[/myJob/projName]"

def appTier = "App"

apps.each { app ->
	def verParam = (String) "ec_${app.artifactKey}-version"
	app.versions.each { ver ->
		transaction {
			runProcess projectName: projName,
				applicationName: app.name,
				processName: "Deploy",
				environmentName: "Commit",
				actualParameter : [
					(verParam): ver
				]
		}
		transaction {sleep(10000)} // Wait for deploy
		transaction {
			snapshot projectName: projName,
				applicationName: app.name,
				snapshotName: ver,
				environmentName: "Commit",
				environmentprojectName: projName
		}
	}
}
