timestamps {
    ansiColor('xterm') {
        node ('bcd-7101') {
            stage('Checkout') {
                checkout scm
            }
            stage('Build') {
                try {
                    sh "./mvnw -B clean verify "
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
