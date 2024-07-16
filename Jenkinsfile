pipeline {
    agent any

    environment {
        GIT_REPO_URL = ''
        BRANCH = 'main'
        CREDENTIALS_ID = ''
    }

    tools {
        maven 'javax.mail:1.6.2' //changes according to the maven name on Jenskin
        //jdk 'jdk-17.0.2.jdk' // Ensure JDK is installed on Jenkins and replace with your installation name
    }
    stages {
        stage('Clone Repository') {
            steps {
                git branch: "${BRANCH}", url: "${GIT_REPO_URL}", credentialsId: "${CREDENTIALS_ID}"
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -f track2/pom.xml clean install'
            }
        }

        stage('Run Application') {
            steps {
                sh 'java -jar...' //
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }

        failure {
            script {
                echo 'Build or execution failed.'
            }
        }
    }
}
