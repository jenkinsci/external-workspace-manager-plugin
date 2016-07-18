# External Workspace Manager Plugin

[![Join the chat at https://gitter.im/jenkinsci/external-workspace-manager-plugin](https://badges.gitter.im/jenkinsci/external-workspace-manager-plugin.svg)](https://gitter.im/jenkinsci/external-workspace-manager-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/external-workspace-manager-plugin/master)](https://ci.jenkins.io/job/Plugins/job/external-workspace-manager-plugin/job/master/)

This plugin provides an external workspace management system.
It facilitates workspace share and reuse across multiple Jenkins jobs.
It eliminates the need to copy, archive or move files.

### Table of contents
1. [Current status](#current-status)
1. [Design document](#design-document)
1. [Prerequisites](#prerequisites)
    2. [External Workspace Definitions](#external-workspace-definitions)
    2. [Node Properties](#node-properties)
    2. [External Workspace Templates](#external-workspace-templates)
1. [Pipeline examples](doc/PIPELINE_EXAMPLES.md)
    2. [Workspace reuse in same job](doc/PIPELINE_EXAMPLES.md#workspace-reuse-in-same-job)
    2. [Workspace reuse in two different jobs](doc/PIPELINE_EXAMPLES.md#workspace-reuse-in-two-different-jobs)
1. [Workspace cleanup](doc/WORKSPACE_CLEANUP.md)

### Current status

Currently the plugin is under development. See the links to get more info about the current status.

### Design document

The design document may be accessed [here](https://docs.google.com/document/d/1yiisnsR7qg3XEEvch8vocWbitSUCZcoQ-pfzEVFg1eA/edit?usp=sharing).

### Prerequisites
A set of prerequisites needs to be set to have this plugin usable.

 - One ore more physical disks accessible from Jenkins Master, most commonly via mounting points.
The disks may also be shared directories via NFS or Samba.
 - The same disks must be accessible from Jenkins Nodes.

##### External Workspace Definitions

In the Jenkins global configuration, we need to define a Disk Pool (or more) that will contain the physical disks.
An example of such config is shown in the following image:

![External Workspace Definitions](doc/img/external-workspace-definitions.png)

##### Node Properties

In each Node configuration, we have to define the mounting point from the current node to each disk.
Let's assume that we have two nodes, one labeled _linux_, and the other one labeled _test_.
A common node configuration is shown below:

![Linux Node Properties](doc/img/linux-node-config.png)
___
![Test Node Properties](doc/img/test-node-config.png)

##### External Workspace Templates

There may be cases when you have more than one Node with the same label.
Instead of specifying the same External Workspace Node properties for multiple Nodes that share the same label,
you can make use of the _External Workspace Templates_ from the Jenkins global config.

Below is an example of such config.
All the Nodes that are labeled _linux_ will use the properties that are defined in this config.
When the `exws` step is called, it will firstly try to find a matching in the External Workspace Templates for a given disk.
If no entries are defined, it will fallback to External Workspace properties from the Node config.

![External Workspace Templates](doc/img/external-workspace-templates.png)
