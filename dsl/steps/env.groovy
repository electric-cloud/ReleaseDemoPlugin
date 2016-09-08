def pipe  = $[/myJob/pipe]
def projName = "$[/myJob/projName]"

def envTier = "App Server"

def envs = pipe.stages
envs.push("Dev")

def resources = []

project projName, {
	pipe.stages.each { env ->
		environment env, {
			environmentTier envTier, {
				// create and add resource to the Tier
				res = "${env}_${envTier}"
				resources.push(res)
				resource resourceName: res, hostName : "localhost"
			} // environmentTier
		} // environment
	} // Environments
} // Project projName


def clean = getProperty("/myProject/clean").value
resources.each {
	clean += "ectool deleteResource \"$it\"\n"
}
setProperty "/myProject/clean", value: clean