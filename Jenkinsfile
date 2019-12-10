timestamps {
    ansiColor('xterm') {
        node ('bcd-794') {
            stage('Checkout') {
                checkout scm
            }
            stage('Build') {
                try {
                    if(env.BRANCH_NAME.equals('master')){
                      sh "./mvnw -B clean install -Djvm=${env.JAVA_HOME_11}/bin/java -DaltDeploymentRepository=${env.ALT_DEPLOYMENT_REPOSITORY_SNAPSHOTS}"
                    }else{
                       sh "./mvnw -B clean verify -Djvm=${env.JAVA_HOME_11}/bin/java"
                    }
                    archiveArtifacts 'target/bonita-connector-email-*.zip'
                } finally {
                    junit allowEmptyResults: true, testResults: '**/target/*-reports/*.xml'
                }
            }
            stage('Archive') {
                archiveArtifacts artifacts: "target/*.zip, target/*.jar", fingerprint: true
            }
        }
    }
}
