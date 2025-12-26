pipeline {
    agent any

    environment {
        AWS_REGION = 'ap-southeast-2'           // change to your region
        ECR_REPO   = '074955808689.dkr.ecr.ap-southeast-2.amazonaws.com/ecom/product-service'  // replace with your AWS ECR repo URI
        IMAGE_TAG  = 'latest'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/AshanHimantha/product-service',
                    credentialsId: 'github'
            }
        }

        stage('Build Spring Boot') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat "docker build -t %ECR_REPO%:%IMAGE_TAG% ."
            }
        }

        stage('Login to ECR') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'aws-ecr',
                    usernameVariable: 'AWS_ACCESS_KEY_ID',
                    passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                )]) {
                    bat """
                    aws configure set aws_access_key_id %AWS_ACCESS_KEY_ID%
                    aws configure set aws_secret_access_key %AWS_SECRET_ACCESS_KEY%
                    aws configure set default.region %AWS_REGION%
                    aws ecr get-login-password --region %AWS_REGION% | docker login --username AWS --password-stdin %ECR_REPO%
                    """
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                bat "docker push %ECR_REPO%:%IMAGE_TAG%"
            }
        }
    }

    post {
        success {
            echo '✅ Docker image built and pushed to ECR successfully'
        }
        failure {
            echo '❌ Pipeline failed'
        }
    }
}
