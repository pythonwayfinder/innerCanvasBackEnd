pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Starting the Spring Boot build...'
                sh './gradlew clean build'
            }
        }
    }
}