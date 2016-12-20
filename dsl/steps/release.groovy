def pipe  = $[/myJob/pipe]
def projName = "$[/myJob/projName]"
def apps = $[/myJob/apps]
def releases = $[/myJob/releases]

project projName, {
	releases.each { rel ->
		release rel.name,
			pipelineName: pipe.name,
			pipelineprojectName: projName,
			plannedStartDate: rel.plannedStartDate,
			plannedEndDate: rel.plannedEndDate,{
				deployer "Main Applications",{
					apps.each { app ->
						deployerApplication app.name,
							applicationprojectName: projName,
							//orderIndex: ,
							processName: "Deploy",
							snapshotName: app.versions[rel.versionIndex], 
							releaseName: rel.name, {
							pipe.stages.each { st ->
								def isProd = st.toLowerCase().contains("prod")
								deployerConfiguration environmentName: st,
								environmentprojectName: projName,
								stageName: st,
								actualParameter: [
									changeType: "full"  
								], {
									if (isProd) {
										insertRollingDeployManualStep = '1'
										rollingDeployEnabled = '1'
									}
								}
							} // Each stage
						} // Deployer application
					} // Each application
				} // Deployer
			} // Release
	} // releases.each
} // Project