# Continuous Integration with Toad Edge

## Introduction

This plugin allows user to automate Database Lifecycle Management tasks from within Jenkins. Following tasks are available:

1. Compare database schemas - Compare two different schemas (from database, model or snapshot). Based on it you can do in the next steps further actions (like generating report or alter script)
2. Compare database with baseline - Compare your current schema with baseline. In case the schema is different the build step will fail.
3. Create baseline - Set the current state of your schema as the baseline to detect any changes that has been made into the schema since now.
4. Create snapshot - Save your database schema to a file to have the possibility to return to it later.
5. Deploy SQL script - Execute any arbitrary SQL script on a selected database.
6. Generate Jenkins HTML comparison report - Generate a HTML report based on a previous schema compare build step.
7. Generate standalone HTML comparison report - Generate a HTML report based on a previous schema compare build step. This report is accessible from build result in Jenkins.
8. Generate change SQL script - Generate an alter SQL script based on a previous schema compare build step.
9. Generate create SQL script - Generate a create SQL script from your VCS repository, saved snapshot or database.

## Installing
First you need to install the plugin.

1. Open your Jenkins.
2. Go to Manage Jenkins > Manage Plugins > Available and search for Continuous Integration with Toad Edge.
3. Check the Continuous Integration with Toad Edge, and finish installation

In order for plugin to work properly it needs Continuous Integration with Toad Edge CLI libraries. Those are distributed as .zip file. To make it work:

1. Upload libraries to your jenkins master node
2. Go to Manage Jenkins -> Configure System and look for Continuous Integration with Toad Edge section
3. Fill-in path to libraries to "Continuous Integration with Toad Edge libraries .zip file" field
4. Save