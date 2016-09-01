project "$[/myProject/projectName]",{
	property "clean", value: """\
		ectool modifyProject "$[projName]" --newName  "deleteMeProject"
		ectool deleteProject "deleteMeProject"
	""".stripIndent()
}