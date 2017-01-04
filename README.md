# Release Demo Plugin
This plugin provide a procedure that can be run with user-configurable parameters to generate a ready-to-run set of release models.

* Packaged as a plugin (using PluginLite)
* Customizable through procedure parameters or DSL template
* Default parameters create three releases, each with own dates and snap shot versions
* Can be run multiple times with different parameters
* Snapshots for multiple version, makes showing path to production compliance easy
* Automatic dates and release names (monthly)
* Prefilled environments
* Manual steps with instructions
* Configuration parameter exposed per environment
* Includes rolling deploys in prod environments with manual steps after phases (blue/green)
* Includes rollback
* Cleans up after itself on demotion, deleting all projects, artifacts and resources created from run procedures

## Installation and Configuration Instructions
* Upload the plugin file (jar file provide or your own zip file)
* Promote the plugin
* (option 1) Run the procedure, Release Demo-3.0 :: Create Release Model and edit parameter values as desired:
	* Project name
	* Artifact group name
	* Names, number of applications, artifacts and version
	* Release Pipeline name and stages
	* Release names and dates (yyyy-mm-dd format)
* (option 2) Copy the contents of the plugin property, dslRunProcedureTemplate to DSLIDE and edit as desired
* Note that this procedure creates
	* Environment models: "Commit" plus the user-specified ones
	* Application: includes tiers and components specified by the user, these are deployed in parallel with a dummy validation and rollback 
	* Commit pipeline: builds, deploys and snapshots all versions of the applications
	* build procedure: Run as part of the commit pipeline
	* Release and Release pipeline

## Demonstration Instructions	
* Run one of the releases
* Act on manual tasks or promotion prompts
* Note inventory
* To force a rollback, do a deploy and select one of the "bad" artifact versions
* When running in Prod, the demonstration is configured to do Blue/Green rolling deploys. There is a manual approval after each phase.

## Known issues
* Demonstration ~should~ work on Windows, though it hasn't been tested there