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
1. [Basic usage](#basic-usage)
    2. [Example one](#example-one)
    2. [Example two](#example-two)

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

![External Workspace Definitions](doc/images/external-workspace-definitions.png)

##### Node Properties

In each Node configuration, we have to define the mounting point from the current node to each disk.
Let's assume that we have two nodes, one labeled _linux_, and the other one labeled _test_.
A common node configuration is shown below:

![Linux Node Properties](doc/images/linux-node-config.png)
___
![Test Node Properties](doc/images/test-node-config.png)

##### External Workspace Templates

There may be cases when you have more than one Node with the same label.
Instead of specifying the same External Workspace Node properties for multiple Nodes that share the same label,
you can make use of the _External Workspace Templates_ from the Jenkins global config.

Below is an example of such config.
All the Nodes that are labeled _linux_ will use the properties that are defined in this config.
When the `exws` step is called, it will firstly try to find a matching in the External Workspace Templates for a given disk.
If no entries are defined, it will fallback to External Workspace properties from the Node config.

![External Workspace Templates](doc/images/external-workspace-templates.png)

### Basic usage

This plugin is currently written for Pipeline jobs, so the examples are for Pipeline script jobs.

##### Example one

Let’s assume that we have one Pipeline job.
In this job, we want to have the same workspace available from multiple Jenkins nodes.

```groovy
stage ('Stage 1. Allocate workspace')
def extWorkspace = exwsAllocate diskPoolId: 'diskpool1'

node ('linux') {
    exws (extWorkspace) {
        stage('Stage 2. Build on the build server')

        git url: 'https://github.com/alexsomai/dummy-hello-world.git'

        def mvnHome = tool 'M3'
        sh '${mvnHome}/bin/mvn clean install -DskipTests'
    }
}

node ('test') {
    exws (extWorkspace) {
        stage('Stage 3. Run tests on a test machine')

        def mvnHome = tool 'M3'
        sh '${mvnHome}/bin/mvn test'
    }
}
```

_Note: The `stage` steps are optional from the External Workspace Manager plugin perspective._

###### Stage 1. Allocate workspace

The `exwsAllocate` step selects a disk from _diskpool1_ (default behavior - the disk with the most available size).
On that disk, let’s say _disk1_, it allocates a directory.
The pattern for the computed directory path is: _physicalPathOnDisk/$JOB_NAME/$BUILD_NUMBER_.

For example, let’s assume that the _$JOB_NAME_ is integration and the _$BUILD_NUMBER_ is _14_.
Then, the resulting path is: _jenkins-project/disk1/integration/14_.

###### Stage 2. Build on the build server

All the nodes labeled _linux_ must have access to the disks defined in the disk pool.
In the Jenkins Node configurations we have defined the local paths that are the mounting points to each disk.

The `exws` step concatenates the node’s local path with the path returned by the `exwsAllocate` step.
In our case, the node labeled _linux_ has its local path to _disk1_ defined as: _/mount-from-linux-node/to/disk-one_.
So, the complete workspace path is: _/mount-from-linux-node/to/disk-one/jenkins-project/disk1/integration/14_.

###### Stage 3. Run tests on a test machine

Further, we want to run our tests on a different node, but we want to reuse the previously created workspace.

In the node labeled test we have defined the local path to _disk1_ as: _/mount-from-test-node/to/disk-one_.
By applying the `exws` step, our tests will be able to run in the same workspace as the build.
Therefore, the path is: _/mount-from-test-node/to/disk-one/jenkins-project/disk1/integration/14_.

**Demo 1. Workspace reuse in same job**
![Example one demo](doc/gifs/demo1-workspace-reuse-in-same-job.gif)

##### Example two

Let’s assume that we have two Jenkins jobs, one called _upstream_ and the other one called _downstream_.
In the _upstream_ job, we clone the repository and build the project, and in the _downstream_ job we run the tests.
In the _downstream_ job we don’t want to clone and re-build the project, we need to use the same workspace created in
the _upstream_ job.
We have to be able to do so without copying the workspace content from one location to another.

The pipeline code in the _upstream_ job is the following:

```groovy
stage ('Stage 1. Allocate workspace in the upstream job')
def extWorkspace = exwsAllocate diskPoolId: 'diskpool1'

node ('linux') {
    exws (extWorkspace) {
        stage('Stage 2. Build in the upstream job')

        git url: 'https://github.com/alexsomai/dummy-hello-world.git'

        def mvnHome = tool 'M3'
        sh '${mvnHome}/bin/mvn clean install -DskipTests'
    }
}
```

And the _downstream_'s Pipeline code is:

```groovy
stage ('Stage 3. Allocate workspace in the downstream job')
def extWorkspace = exwsAllocate upstream: 'upstream'

node ('test') {
    exws (extWorkspace) {
        stage('Stage 4. Run tests in the downstream job')

        def mvnHome = tool 'M3'
        sh '${mvnHome}/bin/mvn test'
    }
}
```

_Note: If the `upstream` parameter is provided, `exwsAllocate` step will ignore the  `diskPoolId` parameter._

###### Stage 1. Allocate workspace in the upstream job

The functionality is the same as in example one - stage 1.
In our case, the allocated directory on the physical disk is: _jenkins-project/disk1/upstream/14_.

###### Stage 2. Build in the upstream job

Same functionality as example one - stage 2.
The final workspace path is: _/mount-from-linux-node/to/disk-one/jenkins-project/disk1/upstream/14_.

###### Stage 3. Allocate workspace in the downstream job

By passing the `upstream` parameter to the `exwsAllocate` step, it selects the most recent stable upstream
workspace (default behavior).
The workspace path pattern is like this: _physicalPathOnDisk/$UPSTREAM_NAME/$MOST_RECENT_STABLE_BUILD_.
Let’s assume that the last stable build number is _12_, then the resulting path is: _jenkins-project/disk1/upstream/12_.

###### Stage 4. Run tests in the downstream job

The `exws` step concatenates the node’s local path with the path returned by the `exwsAllocate` step in stage 3.
In this scenario, the complete path for running tests is: _/mount-from-test-node/to/disk-one/jenkins-project/disk1/upstream/12_.
It will reuse the workspace defined in the _upstream_ job.

**Demo 2. Upstream job**
![Example two upstream](doc/gifs/demo2-upstream-job.gif)

**Demo 2. Downstream job**
![Example two downstream](doc/gifs/demo2-downstream-job.gif)
