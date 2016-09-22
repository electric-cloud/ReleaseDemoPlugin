// print date in yyyy-mm-dd format
def formatDate(d) {
	year = d.year+1900
	date = d.date
	month = d.month+1
	return "${year}-${month}-${date}"
}

// Create an array of start and stop dates for releases
startStopDates = []
(0..2).each {
    def start = new Date() + 2 + (it * 33)
    def stop = new Date()  + (it * 33 + 33)
    startStopDates.push(
        start: formatDate(start),
        stop: formatDate(stop),
        month: new java.text.DateFormatSymbols().months[start.month]
    )
}

runProcedure(projectName: "/plugins/ReleaseDemoPlugin/project",
	procedureName: "Create Release Model",
	actualParameter: [
		projName: "Online Banking",
		artifactGroup: "com.acmebank",
		apps: """\
			[
				[
					name: "Register User",
					artifactKey: "userRegistration",
					versions: ["2.1","2.2","2.3","2.4"]
				],
				[
					name: "Open Account",
					artifactKey: "openAccount",
					versions: ["5.0","5.1","5.5","5.7"]
				],
				[
					name: "Bankground Check",
					artifactKey: "backgroundCheck",
					versions: ["1.7","1.8","2.0","2.1"]
				]
			]
		""".stripIndent(),
		pipe: """\
			[
				name: "Monthly Online Banking",
				stages: ["Training","UAT","PROD"]
			]
		""".stripIndent(),
		releases: """\
			[
				[
					name: "${startStopDates[0].month} Online Banking",
					plannedStartDate: "${startStopDates[0].start}",
					plannedEndDate: "${startStopDates[0].stop}",
					versionIndex: 1
				],
				[
					name: "${startStopDates[1].month} Online Banking",
					plannedStartDate: "${startStopDates[1].start}",
					plannedEndDate: "${startStopDates[1].stop}",
					versionIndex: 2
				],		
				[
					name: "${startStopDates[2].month} Online Banking",
					plannedStartDate: "${startStopDates[2].start}",
					plannedEndDate: "${startStopDates[2].stop}",
					versionIndex: 3
				],
			]
		""".stripIndent()
	]
)
