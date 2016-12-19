def projName = "$[/myJob/projName]"

aclEntry projectName: "/plugins/EC-Artifact/project",
	objectType: "project",
	principalName: "project: $projName",
	principalType: "user",
	executePrivilege: "allow"

aclEntry projectName: "/plugins/EC-Core/project",
	objectType: "project",
	principalName: "project: $projName",
	principalType: "user",
	executePrivilege: "allow"