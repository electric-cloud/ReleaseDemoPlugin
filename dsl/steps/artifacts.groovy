def projName = "$[/myJob/projName]"
def artifactGroup = "$[/myJob/artifactGroup]"
def apps = $[/myJob/apps]

new File("installer.sh").createNewFile()
new File("installer.bat").createNewFile()

def artifacts = []
def versions = []
def artifactKeys = []
def appTiers = []

apps.each { app ->
	app.tiers.each { appTier, envTier ->
		artifacts.push("${artifactGroup}.${app.artifactKey}:${appTier}")
		app.versions.each { ver -> 
			versions.push(ver)
			artifactKeys.push(app.artifactKey)
			appTiers.push(appTier)
		} // each artifact version
	} // each tier
} // each app

property "/myJob/versions", value: versions.join(",")
property "/myJob/artifactKeys", value: artifactKeys.join(",")
property "/myJob/appTiers", value: appTiers.join(",")

def clean = getProperty("/myProject/clean").value
artifacts.each {
	clean += "ectool deleteArtifact \"$it\"\n"
}
setProperty "/myProject/clean", value: clean