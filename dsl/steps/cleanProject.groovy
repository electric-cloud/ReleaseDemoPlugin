project "$[/myProject/projectName]",{
	property "clean", value: """\
		$[/myProject/clean]
		ectool deleteProject "$[/myJob/projName]"
	""".stripIndent()
}