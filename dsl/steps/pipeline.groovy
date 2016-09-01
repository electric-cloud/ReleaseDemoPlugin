def pipe  = $[pipe]
def projName = "$[projName]"

project projName, {

	procedure "UpdateTicket"
	procedure "SeleniumTests"

	pipeline pipe.name, {
		pipe.stages.eachWithIndex { st, index ->
			stage st,{
				task "Batch Deploy",
					taskType: "DEPLOYER"
				task "Update ticket",
					taskType: 'PROCEDURE',
					subproject: projName,
					subprocedure: "UpdateTicket"
				task "Test Automation",
					taskType: 'PROCEDURE',
					subproject: projName,
					subprocedure: "SeleniumTests"
				if (index == 0) {
					task "Test Automation"
					task "Manual Validation",
						taskType: "MANUAL",
						approvers: "quincy",
						instruction: "Verify that business requirements are met"
					}
				if (index > 0) task "Entry gate approval",  // Don't create a gate for first stage
					taskType: 'APPROVAL',
					approver: ['admin'],
					gateType: 'PRE',
					notificationTemplate: 'ec_default_pipeline_notification_template'
			} // stage
		} // Each stage
	} // Pipeline
} // Project