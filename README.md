# Release Demo Plugin
This plugin provide a procedure that can be run with user-configurable parameters to generate a ready-to-run set of release models.

* Packaged as a plugin (using PluginLite)
* Customizable through procedure parameters
* Default parameters create three releases, each with own dates and snap shot versions
* Can be run multiple times with different parameters
* Snapshots for multiple version, makes showing path to production compliance easy
* Automatic dates and release names (monthly)
* Prefilled environments (though release doesn’t currently show these in its inventory)
* Manual steps with instructions
* Configuration parameter exposed per environment
* Cleans up after itself on demotion, deleting all projects, artifacts and resources created from run procedures

## Installation and Configuration Instructions
* Upload the plugin file (jar file provide or your own zip file)
* Promote the plugin
* Run the procedure, Release Demo :: Create Release Model and edit parameter values as desired:
	* Project name
	* Artifact group name
	* Names, number of applications, artifacts and version
	* Pipeline name and stages
	* Release names and dates (yyyy-mm-dd format)

## Demonstration Instructions	
* Run one of the releases
* Act on manual tasks or promotion prompts
* Note inventory

## Known issues
Timeouts may occur on some of the steps due to DSL process; try increasing the DSL timeout (Server settings "Max DSL duration")
