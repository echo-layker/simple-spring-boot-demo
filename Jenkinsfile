pipeline {
    agent any
    stages {
        stage('maven build') {
            steps {
                sh 'mvn clean package'
                archiveArtifacts(artifacts: 'target/*.jar', excludes: 'target/*.source.jar', onlyIfSuccessful: true)
            }
        }
        stage('docker build') {
            steps {

                docker.withRegistry("https://${registry}", "${registry}") {
                    docker.build(imageName).push()
                }
                sh '''docker rmi -f hub.hulushuju.com/${namespace}/${deployment}:${BUILD_NUMBER}'''
            }
        }
        stage('deploy to k8s') {
            steps {
                withKubeConfig(credentialsId: 'hulushuju-uat', serverUrl: 'https://rc.hulushuju.com/k8s/clusters/c-z5qq9', namespace: 'devops-k8s-example', clusterName: 'hulushuju-uat', contextName: 'hulushuju-uat') {
                    sh 'kubectl -n ${namespace} set image deployment/${deployment}  ${deployment}=${imageName}'
                }

            }
        }
    }
    environment {
        namespace = 'devops-k8s-example'
        deployment = 'simple-spring-boot-demo'
        imageName = 'hub.hulushuju.com/devops-k8s-example/simple-spring-boot-demo:${BUILD_NUMBER}'
    }
}