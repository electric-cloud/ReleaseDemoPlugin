def apps = $[/myJob/apps]
def projName = "$[/myJob/projName]"
def artifactGroup = "$[/myJob/artifactGroup]"
def pipe  = $[/myJob/pipe]

def appTier = "App"
def envs = pipe.stages
envs.push("Dev")

apps.each { app -> 
	project projName, {
		application applicationName: app.name, {
			applicationTier appTier, {

				component app.artifactKey, pluginKey: "EC-Artifact", {
					ec_content_details.with { 
						pluginprojName = "EC-Artifact"
						pluginProcedure = "Retrieve"
						artifactName = "${artifactGroup}:${app.artifactKey}"
						filterList = ""
						overwrite = "update"
						versionRange = app.versions[1]
						artifactVersionLocationProperty = "retrievedArtifactVersions/\$" + "[assignedResourceName]"
					}

					process processName: "Install",
						processType: "DEPLOY",
						componentApplicationName: app.name,
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
								versionRange : "\$" + "[ec_" + app.artifactKey + "-version]"
							]
						/* No actual artifact as this point
						processStep processStepName: "Deploy Artifact",
							processStepType: 'command',
							subproject: '/plugins/EC-Core/project',
							subprocedure: 'RunCommand',
							actualParameter: [
								// Linux: sh "fullPathToFile/installer.sh"
								// Windows: "fullPathToFile\installer.bat"
								commandToRun: '$' + '[/javascript myResource.hostPlatform=="linux"?"sh ":""]' +
								'\"' + // Quote entire command
								'$' + '[retrievedArtifactVersions/$' + '[assignedResourceName]/' + 
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
						*/
					} // process processName: "Install"
				} // component componentName
			} // applicationTier appTier
			
			process "Deploy",{
			
				formalParameter "changeType"
			
				processStep  processStepName: app.artifactKey,
					processStepType: 'process',
					componentName: null,
					applicationName: app.name,
					componentApplicationName: app.name,
					errorHandling: 'failProcedure',
					subcomponent: app.artifactKey,
					subcomponentApplicationName: app.name,
					subcomponentProcess: "Install",
					applicationTierName: appTier
			}
			
			envs.each { env ->
				tierMap tierMapName: "${app.name}-$env",
					environmentProjectName: projectName,
					environmentName: env,
					tierMapping: ["App":"App Server"]			
			}

		} // Applications

	} // project projName
} // each app