def pipe  = $[pipe]
def projName = "$[projName]"
def apps = $[apps]
def releases = $[releases]

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
									deployerConfiguration environmentName: st,
										environmentprojectName: projName,
										stageName: st,{
											actualParameter 'changeType', "full"
										}
								} // Each stage
							} // Deployer application
						} // Each application
					} // Deployer
			} // Release
	} // releases.each
} // Project