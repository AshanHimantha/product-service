pipeline {
    agent any

    environment {
        AWS_REGION   = 'ap-southeast-2'
        AWS_ACCOUNT  = '074955808689'
        ECR_REGISTRY = "${AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        ECR_REPO     = 'ecom/product-service'
        IMAGE_TAG    = "${env.BUILD_NUMBER}" // Dynamic tag per build
    }

    stages {

        stage('Checkout') {
            steps {
                // Reliable Git checkout
                checkout([$class: 'GitSCM',
                    branches: [[name: '*/master']],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [],
                    userRemoteConfigs: [[
                        url: 'https://github.com/AshanHimantha/product-service',
                        credentialsId: 'github' // Remove if repo is public
                    ]]
                ])
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                docker build -t ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG} .
                """
            }
        }

        stage('Login to ECR') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'aws-ecr',
                    usernameVariable: 'AWS_ACCESS_KEY_ID',
                    passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                )]) {
                    sh """
                    # Login to AWS ECR
                    aws ecr get-login-password --region ${AWS_REGION} \
                        --access-key $AWS_ACCESS_KEY_ID \
                        --secret-key $AWS_SECRET_ACCESS_KEY \
                        | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    """
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh "docker push ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"
            }
        }
    }

    post {
        success {
            echo "✅ Docker image built and pushed: ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"
        }
        failure {
            echo '❌ Pipeline failed'
        }
        always {
            cleanWs() // Clean workspace to avoid old repo conflicts
        }
    }
}
