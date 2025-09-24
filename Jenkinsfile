pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Starting the Spring Boot build...'
                sh 'chmod +x gradlew' // gradlew 파일에 실행 권한 부여
                sh './gradlew clean build'
            }
        }
    }
}