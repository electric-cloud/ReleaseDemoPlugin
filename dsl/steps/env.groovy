def pipe  = $[/myJob/pipe]
def projName = "$[/myJob/projName]"
def apps = $[/myJob/apps]

def envs = pipe.stages
envs.push("Commit")

def resources = []

project projName, {
	pipe.stages.each { env ->
		environment env, {
			// Only in Prod
			def isProd = env.toLowerCase().contains("prod")
			if (isProd) {				
				rollingDeployEnabled = '1'
				rollingDeployType = 'phase'

				["Green","Blue"].each { phase ->
					rollingDeployPhase phase, {
						orderIndex = '1'
						phaseExpression = null
						rollingDeployPhaseType = 'tagged'
					}
				}
			} // if isProd
			
			// Using only first app's tier definition
			// TODO: iterate over all apps just in case other tiers defined
			apps[0].tiers.each { appTier, envTier ->
		
				environmentTier envTier, {
					// create and add resource to the Tier
					def resCount = isProd?5:1
					(1..resCount).each { resNum ->
						def resName = (String) "${env}_${projName}_${envTier}_${resNum}"
						resources.push(resName)
						resource resourceName: resName, hostName : "localhost"
						def phase = (resNum==1)?'Blue':'Green'
						if (isProd) {
							environmentTier envTier, resourcePhaseMapping: [(resName) : phase]
						}
					} // each resource
				} // environmentTier
			} // each app
		} // environment
	} // Environments
} // Project projName

def clean = getProperty("/myProject/clean").value
resources.each {
	clean += "ectool deleteResource \"$it\"\n"
}
setProperty "/myProject/clean", value: clean