pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    environment {
        DOCKER_HUB_REPO = 'yourusername/spring-app'
        DOCKER_CREDS = credentials('docker-hub-creds')
        IMAGE_TAG = "${env.BUILD_ID}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Maven Build & Test') {
            when {
                anyOf {
                    branch 'dev';
                    changeRequest target: 'dev'
                    branch 'master'
                }
            }
            steps {
                sh 'mvn clean install'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            when {
                branch 'master'
            }
            steps {
                script {
                    def image = docker.build("${DOCKER_HUB_REPO}:${IMAGE_TAG}")
                    image.push('latest')
                }
            }
        }

        stage('Push to Docker Hub') {
            when {
                branch 'master'
            }
            steps {
                script {
                    sh "echo ${DOCKER_CREDS_PSW} | docker login -u ${DOCKER_CREDS_USR} --password-stdin"
                    docker.image("${DOCKER_HUB_REPO}:${IMAGE_TAG}").push()
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout'
        }
        success {
            echo 'Build successful!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}