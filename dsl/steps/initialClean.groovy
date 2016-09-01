project "$[/myProject/projectName]",{
	property "clean", value: """\
		ectool modifyProject "$[/myProject/config/projName]" --newName  "deleteMeProject"
		ectool deleteProject "deleteMeProject"
	""".stripIndent()
}