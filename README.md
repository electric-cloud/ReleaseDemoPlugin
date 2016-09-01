# Release Demo Plugin
This plugin provide an out-of-the box ready-to-run set of release models.  These models can be easily customized for particular demo situations.

## Installation and Configuration Instructions
* (Optional) Customize the project, artifact names, applications, pipeline and/or release details
	* Edit the file dsl/promote.groovy
	* Scroll to the "User editable properties" section
	* Adjust any of the names
	* Add/remove applications, pipeline stages, and/or releases
	* Adjust release dates (yyyy-mm-dd format)
	* Save your changes
	* Zip up the folders in the ReleaseDemoPlugin
* Upload the plugin file (jar file provide or your own zip file)
* Promote the plugin.  This will take about 4.5 minutes.  To monitor the progress, look at the job launched by the plugin promotion
* Run one of the releases
* Act on manual tasks or promotion prompts
* Note inventory

## Known issues
Current the plugin only works on Linux type EF servers
When demoting plugin, give it time to complete clean up (see Default project job) before promoting again

