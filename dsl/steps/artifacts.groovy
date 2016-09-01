def artifactGroup = "$[artifactGroup]"
def apps = $[apps]

new File("/tmp/empty").createNewFile()

def artifacts = []

apps.each { app ->
	artifacts.push("${artifactGroup}:${app.artifactKey}")
	app.versions.each { ver -> 
		runProcedure (
			projectName: '/plugins/EC-Artifact/project',
			procedureName: 'Publish',
			actualParameter : [
				artifactName: (String) "${artifactGroup}:${app.artifactKey}",
				artifactVersionVersion: ver,  // required
				fromLocation: '/tmp',
				includePatterns: 'empty',
				repositoryName: 'default',  // required
			]
		)
	}
}

def clean = getProperty("/myProject/clean").value
artifacts.each {
	clean += "ectool deleteArtifact \"$it\"\n"
}
setProperty "/myProject/clean", value: clean