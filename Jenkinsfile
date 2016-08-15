#!groovy

/* Only keep the 10 most recent builds */
properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '10']]])

node ('kelp') {
  stage 'Checkout'
  checkout scm

  stage 'Build'

  /* Call the maven build */
  mvn 'clean install -B -V'

  /* Save Results */
  stage 'Results'

  /* Archive the test results */
  step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
}

/* Run maven from tool 'mvn' */
void mvn(def args) {
  /* Get jdk tool */
  String jdktool = tool name: 'jdk7', type: 'hudson.model.JDK'

  /* Get the maven tool */
  def mvnHome = tool name: 'mvn'

  /* Set JAVA_HOME, and special PATH variables */
  List javaEnv = [
    "PATH+JDK=${jdktool}/bin", "JAVA_HOME=${jdktool}"
  ]

  /* Call maven tool with java envVars */
  withEnv(javaEnv) {
    timeout(time: 60, unit: 'MINUTES') {
      if (isUnix()) {
        sh "${mvnHome}/bin/mvn ${args}"
      } else {
        bat "${mvnHome}\\bin\\mvn ${args}"
      }
    }
  }
}
