pipeline {
    agent any
    tools {
        maven 'Maven3'
        jdk 'JDK21'
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
                script {
                    def image = docker.build("${DOCKER_HUB_REPO}:${IMAGE_TAG}")
                    bat "echo ${DOCKER_CREDS_PSW} | docker login -u ${DOCKER_CREDS_USR} --password-stdin"
                    image.push()
                    image.push('latest')
                }
            }
        }
    }
    post { always { sh 'docker logout' } }
}