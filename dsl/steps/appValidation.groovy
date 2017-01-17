def projName = "$[/myJob/projName]"

project projName, {
	procedure "Application Validation",{
		step "URL Test",
			command: "echo testing URL"
	}
}