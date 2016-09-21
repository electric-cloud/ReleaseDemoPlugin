def projName = "$[/myJob/projName]"
def artifactGroup = "$[/myJob/artifactGroup]"
def apps = $[/myJob/apps]

new File("empty").createNewFile()

def artifacts = []
def versions = []
def artifactKeys = []

apps.each { app ->
	artifacts.push("${artifactGroup}:${app.artifactKey}")
	app.versions.each { ver -> 
		versions.push(ver)
		artifactKeys.push(app.artifactKey)
	}
}

property "/myJob/versions", value: versions.join(",")
property "/myJob/artifactKeys", value: artifactKeys.join(",")

def clean = getProperty("/myProject/clean").value
artifacts.each {
	clean += "ectool deleteArtifact \"$it\"\n"
}
setProperty "/myProject/clean", value: clean