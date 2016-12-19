def apps = $[/myJob/apps]
def projName = "$[/myJob/projName]"
def artifactGroup = "$[/myJob/artifactGroup]"
def pipe  = $[/myJob/pipe]

def envs = pipe.stages
envs.push("Commit")

apps.each { app -> 
	project projName, {
		// transaction {
			def appName = app.name
			application applicationName: appName, {
			
				app.tiers.each { appTier, envTier ->
					applicationTier appTier, {

						component appTier, pluginKey: "EC-Artifact", {
							ec_content_details.with { 
								pluginprojName = "EC-Artifact"
								pluginProcedure = "Retrieve"
								artifactName = "${artifactGroup}.${app.artifactName}:${appTier}"
								filterList = ""
								overwrite = "update"
								//versionRange = app.versions[1]
								versionRange = ""
								artifactVersionLocationProperty = "/myJob/retrievedArtifactVersions/\$" + "[assignedResourceName]"
							}

							process processName: "Install",
								processType: "DEPLOY",
								componentApplicationName: appName,
								applicationName: null,
								{
								processStep processStepName: "Retrieve Artifact",
									processStepType: "component",
									subprocedure: "Retrieve",
									errorHandling: "failProcedure",
									subproject: "/plugins/EC-Artifact/project",
									applicationName: null,
									applicationTierName: null,
									actualParameter: [ 
										artifactName : "\$" + "[/myComponent/ec_content_details/artifactName]",
										artifactVersionLocationProperty : "\$" + "[/myComponent/ec_content_details/artifactVersionLocationProperty]",
										filterList : "\$" + "[/myComponent/ec_content_details/filterList]",
										overwrite : "\$" + "[/myComponent/ec_content_details/overwrite]",
										versionRange : "\$" + "[ec_" + app.artifactName + "-version]"
									]
								processStep processStepName: "Deploy Artifact",
									processStepType: 'command',
									subproject: '/plugins/EC-Core/project',
									subprocedure: 'RunCommand',
									actualParameter: [
										// Linux: sh "fullPathToFile/installer.sh"
										// Windows: "fullPathToFile\installer.bat"
										commandToRun: '$' + '[/javascript myResource.hostPlatform=="linux"?"sh ":""]' +
										'\"' + // Quote entire command
										'$' + '[/myJob/retrievedArtifactVersions/$' + '[assignedResourceName]/' + 
										'$' + '[/myComponent/ec_content_details/artifactName]/cacheLocation]' + 
										'$' + '[/javascript myResource.hostPlatform=="linux"?"/":"\\\\"]' + // Slash direction 
										'installer.' + '$' + '[/javascript myResource.hostPlatform=="linux"?"sh":"bat"]' + '\"'
										],
									applicationName: null,
									applicationTierName: null,
									componentApplicationName: appName		
									
								createProcessDependency componentApplicationName: appName,
									processStepName: "Retrieve Artifact",
									targetProcessStepName: "Deploy Artifact"
									
									
							} // process processName: "Install"
						} // component componentName
					} // applicationTier appTier
				} // each appTier
				
				process "Deploy",{
				
					formalParameter "changeType"
					
					processStep 'Validation', {
						applicationTierName = 'DB'
						errorHandling = 'failProcedure'
						processStepType = 'procedure'
						subprocedure = 'Application Validation'
						subproject = projName
					}

					processStep 'Rollback', {
						processStepType = 'rollback'
						rollbackType = 'environment'
						smartRollback = '0'
						dependencyJoinType = 'or'
					}
					
					processDependency 'Validation', targetProcessStepName: "Rollback", branchType: 'ERROR',
						branchCondition: '$'+'[/javascript myJob.outcome=="error"]',
						branchConditionName:  'On error',
						branchConditionType:  'CUSTOM'					
					
					app.tiers.each { appTier, envTier ->
						processStep  processStepName: appTier,
							processStepType: 'process',
							componentName: null,
							applicationName: appName,
							componentApplicationName: appName,
							errorHandling: 'failProcedure',
							subcomponent: appTier,
							subcomponentApplicationName: appName,
							subcomponentProcess: "Install",
							applicationTierName: appTier
							
						processDependency appTier, targetProcessStepName: 'Validation', branchType: 'ALWAYS'
					}
				}
				
				envs.each { env ->
					app.tiers.each { appTier, envTier ->
						tierMap tierMapName: "${appName}-$env",
							environmentProjectName: projectName,
							environmentName: env,
							tierMapping: [(appTier):envTier]
					}
				}

			} // Applications
		// } // transaction

	} // project projName
} // each app