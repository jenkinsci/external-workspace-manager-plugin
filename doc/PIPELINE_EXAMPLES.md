# Pipeline examples

This plugin is currently written for Pipeline jobs, so the examples are for Pipeline script jobs.

1. [Workspace reuse in same job](#workspace-reuse-in-same-job)
1. [Workspace reuse in two different jobs](#workspace-reuse-in-two-different-jobs)

## Workspace reuse in same job

Let’s assume that we have one Pipeline job.
In this job, we want to have the same workspace available from multiple Jenkins nodes.

```groovy
stage ('Stage 1. Allocate workspace')
def extWorkspace = exwsAllocate 'diskpool1'

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

**Stage 1. Allocate workspace**

The `exwsAllocate` step selects a disk from _diskpool1_ (default behavior - the disk with the most available size).
On that disk, let’s say _disk1_, it allocates a directory.
The pattern for the computed directory path is: _physicalPathOnDisk/$JOB_NAME/$BUILD_NUMBER_.

For example, let’s assume that the _$JOB_NAME_ is integration and the _$BUILD_NUMBER_ is _14_.
Then, the resulting path is: _jenkins-project/disk1/integration/14_.

**Stage 2. Build on the build server**

All the nodes labeled _linux_ must have access to the disks defined in the disk pool.
In the Jenkins Node configurations we have defined the local paths that are the mounting points to each disk.

The `exws` step concatenates the node’s local path with the path returned by the `exwsAllocate` step.
In our case, the node labeled _linux_ has its local path to _disk1_ defined as: _/mount-from-linux-node/to/disk-one_.
So, the complete workspace path is: _/mount-from-linux-node/to/disk-one/jenkins-project/disk1/integration/14_.

**Stage 3. Run tests on a test machine**

Further, we want to run our tests on a different node, but we want to reuse the previously created workspace.

In the node labeled test we have defined the local path to _disk1_ as: _/mount-from-test-node/to/disk-one_.
By applying the `exws` step, our tests will be able to run in the same workspace as the build.
Therefore, the path is: _/mount-from-test-node/to/disk-one/jenkins-project/disk1/integration/14_.

**Demo 1. Workspace reuse in same job**
![Example one demo](gif/demo1-workspace-reuse-in-same-job.gif)

## Workspace reuse in two different jobs

Let’s assume that we have two Jenkins jobs, one called _upstream_ and the other one called _downstream_.
In the _upstream_ job, we clone the repository and build the project, and in the _downstream_ job we run the tests.
In the _downstream_ job we don’t want to clone and re-build the project, we need to use the same workspace created in
the _upstream_ job.
We have to be able to do so without copying the workspace content from one location to another.

The pipeline code in the _upstream_ job is the following:

```groovy
stage ('Stage 1. Allocate workspace in the upstream job')
def extWorkspace = exwsAllocate 'diskpool1'

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
stage ('Stage 3. Select the upstream run')
def run = selectRun 'upstream'

stage ('Stage 4. Allocate workspace in the downstream job')
def extWorkspace = exwsAllocate selectedRun: run

node ('test') {
    exws (extWorkspace) {
        stage('Stage 5. Run tests in the downstream job')

        def mvnHome = tool 'M3'
        sh '${mvnHome}/bin/mvn test'
    }
}
```

_Note: If the `selectedRun` parameter is provided, `exwsAllocate` step will ignore the `diskPoolId` parameter._

**Stage 1. Allocate workspace in the upstream job**

The functionality is the same as in the previous example, Stage 1.
In our case, the allocated directory on the physical disk is: _jenkins-project/disk1/upstream/14_.

**Stage 2. Build in the upstream job**

Same functionality as the previous example, Stage 2.
The final workspace path is: _/mount-from-linux-node/to/disk-one/jenkins-project/disk1/upstream/14_.

**Stage 3. Select the upstream run**

:exclamation: You must have the [Run Selector Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Run+Selector+Plugin)
installed to use the `selectRun` step described bellow.

We can use the `selectRun` step from the *Run Selector plugin* to select the upstream run.
By default, if no selectors are provided, the step will select the last stable build from the given Jenkins job.
By providing the `selector` parameter, we can specify different build selection strategies.
More details and examples of how the `selectRun` step works may be found at the
Run Selector plugin's [documentation page](https://github.com/jenkinsci/run-selector-plugin/blob/master/README.md), 
Pipeline examples section.

**Stage 4. Allocate workspace in the downstream job**

By passing the `selectedRun` parameter to the `exwsAllocate` step, it allocates the first workspace used by the 
selected run.
If the `selectRun` step selected the build number _14_, then the resulting path 
is: _jenkins-project/disk1/upstream/14_.

**Stage 5. Run tests in the downstream job**

The `exws` step concatenates the node’s local path with the path returned by the `exwsAllocate` step in stage 3.
In this scenario, the complete path for running tests is: 
_/mount-from-test-node/to/disk-one/jenkins-project/disk1/upstream/14_.
It will reuse the workspace defined in the _upstream_ job.

**Demo 2. Upstream job**
![Example two upstream](gif/demo2-upstream-job.gif)

**Demo 2. Downstream job**
![Example two downstream](gif/demo2-downstream-job.gif)
