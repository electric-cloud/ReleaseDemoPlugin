def projName = "$[projName]"

aclEntry projectName: "/plugins/EC-Artifact/project",
	objectType: "project",
	principalName: "project: $projName",
	principalType: "user",
	executePrivilege: "allow"
