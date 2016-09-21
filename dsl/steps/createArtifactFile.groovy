createJobStep jobStepName: "Create file",
	parentPath: "/jobSteps/$[/myJobStep/jobStepId]",
	subproject : '/plugins/EC-FileOps/project',
	subprocedure : 'AddTextToFile',
	actualParameter : [
		AddNewLine: '1',
		Append: '1',
		Content: '',  // required
		Path: 'empty',  // required
	]