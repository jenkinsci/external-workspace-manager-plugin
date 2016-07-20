# Custom workspace path

By default, the workspace path on disk is computed based on the following pattern: 
_${physicalPathOnDisk}/${JOB_NAME}/${BUILD_NUMBER}_.
The _$physicalPathOnDisk_ is defined in the Jenkins global config, _External Workspace Definitions_ section, 
for each _Disk_ entry. 

If required, you can change the default workspace computation pattern. 
The plugin offers two alternatives for this:
 1. [Define a global workspace template for each Disk Pool](#define-a-global-workspace-template-for-each-disk-pool)
 1. [Define a custom workspace path in the Pipeline script](#define-a-custom-workspace-path-in-the-pipeline-script)

## Define a global workspace template for each Disk Pool

In the Jenkins global config, under the _External Workspace Definitions_ section, you can provide the 
_Workspace path template_ parameter. 

**Example**

Let's assume that we want the workspace path to be computed on disk based on the next template:
_${JOB_NAME}/${CUSTOM_BUILD_PARAM}/${BUILD_NUMBER}_.

Bellow is a Jenkins global config example:

![Global Workspace Template](img/global-workspace-template.png)

And the Pipeline script:

```groovy
def extWorkspace
withEnv(['CUSTOM_BUILD_PARAM=100']) {
    stage ('Stage 1. Allocate workspace')
    extWorkspace = exwsAllocate 'diskpool1'
}

node ('linux') {
    exws (extWorkspace) {
        stage ('Stage 2. Build')
        checkout scm
        sh 'mvn clean install -DskipTests'
    }
}

node ('test') {
    exws (extWorkspace) {
        stage ('Stage 3. Test')
        sh 'mvn test'
    }
}
```

**Stage 1. Allocate workspace**

For Disk Pool identified by _diskpool1_ we have provided the *Workspace path template* parameter. 
Therefore, the `exwsAllocate` step will try to resolve the parameters provided in the template:
 - It will replace the _${JOB_NAME}_ with the actual name of the project. 
Let's say the job is named _foobar_.
 - Then, it will resolve the _${CUSTOM_BUILD_PARAM}_ argument that we have provided at `withEnv([''])` step, 
replacing it with _100_.
 - Finally, the _${BUILD_NUMBER}_ will have the value of the current build number, e.g. _20_.

The resulting workspace path to be allocated on the disk is: _foobar/100/20_.

**Stage 2. Build** and **Stage 3. Test**

The `exws` step will compute the complete workspace path, appending the mounting point from the 
Jenkins node with the allocated path on disk.
The resulting path will be: _/mounting-point-from-node-to-disk/foobar/100/20_.

## Define a custom workspace path in the Pipeline script

A second alternative for defining a custom workspace path is to configure it within Build DSL.
For this, the `exwsAllocate` step defines a new parameter: `path`.

**Example**

In this example, we are allocating a specific workspace for each pull request. 
The Pipeline syntax may look like:

```groovy
stage ('Stage 0. Configure custom path')
def customPath = "${env.JOB_NAME}/${PULL_REQUEST_NUMBER}/${env.BUILD_NUMBER}"

stage ('Stage 1. Allocate workspace')
def extWorkspace = exwsAllocate diskPoolId: 'diskpool1', path: customPath

node ('linux') {
    exws (extWorkspace) {
        stage ('Stage 2. Build')
        checkout scm
        sh 'mvn clean install -DskipTests'
    }
}

node ('test') {
    exws (extWorkspace) {
        stage ('Stage 3. Test')
        sh 'mvn test'
    }
}
```
**Stage 0. Configure custom path**

:exclamation: You must use double quotes for String interpolation, otherwise the parameters will not be resolved.

We can make use of the Build DSL to configure the workspace path.
In our example the path is made up of the following:
 - The _${env.JOB_NAME}_ that will be resolved to the actual name of the project, for example _foobar_.
 - The _${PULL_REQUEST_NUMBER}_ is a Build parameter, and can be the considered as the pull request number.
Let's say it's _30_
 - The _${env.BUILD_NUMBER}_ is the number of the current build: _20_.

Resulting the _customPath_ variable to have to value: _foobar/30/20_.

**Stage 1. Allocate workspace**

By providing the `path` parameter, the `exwsAllocate` will allocate that custom path on the Disk.
If this parameter is provided, the *Workspace path template* from Jenkins global config will be ignored.

**Stage 2. Build** and **Stage 3. Test**

Similarly as in the previous example, the `exws` step will compute the complete workspace path.
The resulting path will be: _/mounting-point-from-node-to-disk/foobar/30/20_.
