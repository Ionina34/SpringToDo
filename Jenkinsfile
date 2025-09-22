pipeline {
    agent any
    tools {
        maven 'Maven3'
        jdk 'JDK21'
        docker 'Docker'
    }
    environment {
        DOCKER_HUB_REPO = 'dasha499/spring-todo'
        DOCKER_CREDS = credentials('docker-hub-creds')
        IMAGE_TAG = "${env.BUILD_ID}-${env.BRANCH_NAME}"
    }
    stages {
        stage('Checkout') { steps { checkout scm } }
        stage('Maven Build & Test') {
            when { anyOf { branch 'dev'; changeRequest target: 'dev'; branch 'main' } }
            steps { bat 'mvn clean install -DskipTests' }
            post { always { junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml' } }
        }
        stage('Build & Push Docker') {
            when { branch 'main' }
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                     script {
                       def image = docker.build("${DOCKER_HUB_REPO}:${IMAGE_TAG}")
                       bat "echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin"
                       image.push()
                       image.push('latest')
                     }
                }
            }
        }
    }
    post { always { bat 'docker logout' } }
}