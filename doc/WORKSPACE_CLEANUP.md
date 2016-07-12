# Workspace cleanup

:exclamation: You must have the [Workspace Cleanup Plugin](http://wiki.jenkins-ci.org/display/JENKINS/Workspace+Cleanup+Plugin)
installed to use the workspace cleanup features shown bellow.

## Examples

### Delete workspace regardless the build result

The workspace can be deleted without taking into the consideration the build result, by using the
_try-finally_ block.

```groovy
def extWorkspace = exwsAllocate diskPoolId: 'diskpool1'

node ('linux') {
    exws (extWorkspace) {
        try {
            checkout scm
            sh 'mvn clean install'
        } finally {
            step ([$class: 'WsCleanup'])
        }
    }
}
```

### Delete workspace unless the build has failed

There may be cases when you want to keep the workspace only if the build has failed, for debug purposes,
and delete it otherwise.
For this, it may come in hand the _try-catch-finally_ block, along with the _cleanWhenFailure_ parameter set to _false_.

```groovy
def extWorkspace = exwsAllocate diskPoolId: 'diskpool1'

node ('linux') {
    exws (extWorkspace) {
        try {
            checkout scm
            sh 'mvn clean install'
        } catch (err) {
            currentBuild.result = 'FAILURE'
        } finally {
            step ([$class: 'WsCleanup', cleanWhenFailure: false])
        }
    }
}
```

### Delete workspace files based on a pattern
You have the option to specify which files from the workspace to be deleted, based on a pattern (using Ant syntax).
Pattern examples may be found in the
[DirectoryScanner javadoc](http://www.docjar.org/docs/api/org/apache/tools/ant/DirectoryScanner.html).
The following example deletes all the _.class_ files/dirs in a directory tree.

```groovy
def extWorkspace = exwsAllocate diskPoolId: 'diskpool1'

node ('linux') {
    exws (extWorkspace) {
        try {
            checkout scm
            sh 'mvn clean install'
        } finally {
            step ([$class: 'WsCleanup', patterns: [[pattern: '**/*.class', type: 'INCLUDE']]])
        }
    }
}
```

### Delete workspace in the downstream job
The following use case is addressed when you have one upstream job, followed by a downstream job.
You may want to keep the workspace in the upstream job (to be reused by the downstream job), but afterwards you
want to delete the workspace in the downstream job.

**Pipeline for the upstream job**

```groovy
def extWorkspace = exwsAllocate diskPoolId: 'diskpool1'

node ('linux') {
    exws (extWorkspace) {
        checkout scm
        sh 'mvn clean install -DskipTests'
    }
}
```

**Pipeline for the downstream job**

```groovy
def extWorkspace = exwsAllocate upstream: 'upstream-full-name'

node ('test') {
    exws (extWorkspace) {
        try {
            checkout scm
            sh 'mvn test'
        } finally {
            step ([$class: 'WsCleanup'])
        }
    }
}
```
