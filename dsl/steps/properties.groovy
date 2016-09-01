// print date in yyyy-mm-dd format
def formatDate(d) {
	year = d.year+1900
	date = d.date
	month = d.month+1
	return "${year}-${month}-${date}"
}

/********* User editable properties ************/
property "/myJob/projName", value: "On line bank Release"
property "/myJob/artifactGroup", value: "com.mybank.apps"
/*
property "/myJob/plannedStartDate", value: (String) formatDate(new Date())
property "/myJob/plannedEndDate", value: (String) formatDate(new Date()+14)
property "/myJob/releaseName", value: "Quarterly Online Banking Release"
*/

// versionIndex selects the version from apps.versions[].  Use apps.version[0] for initial environment snapshots.
property "/myJob/releases", value: """\
	[
		[
			name: "September 2016 Online Banking",
			plannedStartDate: "2016-09-01",
			plannedEndDate: "2016-09-30",
			versionIndex: 1
		],
		[
			name: "October 2016 Online Banking",
			plannedStartDate: "2016-10-01",
			plannedEndDate: "2016-10-31",
			versionIndex: 2
		],		
		[
			name: "November 2016 Online Banking",
			plannedStartDate: "2016-11-01",
			plannedEndDate: "2016-11-30",
			versionIndex: 3
		],
	]
""".stripIndent()
property "/myJob/pipe", value: '''\
	[
		name: "Quarterly Online Banking",
		stages: ["UAT", "STG", "PROD"]
	]
'''.stripIndent()
property "/myJob/apps", value: '''\
	[
		[
			name: "Account Statements",
			artifactKey: "statements",
			versions: ["2.1","2.2","2.3","2.4"]
		],
		[
			name: "Credit Card Accounts",
			artifactKey: "cards",
			versions: ["5.0","5.1","5.5","5.7"]
		],
		[
			name: "Fund Transfer",
			artifactKey: "fund",
			versions: ["1.7","1.8","2.0","2.1"]
		]
	]
'''.stripIndent()
/********* End of user editable properties ************/