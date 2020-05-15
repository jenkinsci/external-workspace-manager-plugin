buildPlugin()

stage("UI tests") {
    node('docker && highmem') {
        checkout scm
        docker.image('jenkins/ath:acceptance-test-harness-1.69').inside('-v /var/run/docker.sock:/var/run/docker.sock --shm-size 2g') {
            sh """
                mvn clean package -DskipTests # Build .hpi before running ATH so the snapshot is consumed instead of latest released
                eval \$(vnc.sh)
                mvn test -B -Dmaven.test.failure.ignore=true -DforkCount=1 -Ptest-ath
            """
        }
        junit '**/target/surefire-reports/**/*.xml'
    }
}
