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
                sh '''docker build --rm -f "Dockerfile" -t  hub.hulushuju.com/${namespace}/${deployment}:${BUILD_NUMBER} .

                      docker login hub.hulushuju.com -u ${harbor_user} -p ${harbor_password}
                        
                      docker push hub.hulushuju.com/${namespace}/${deployment}:${BUILD_NUMBER}
                        
                      docker rmi -f hub.hulushuju.com/${namespace}/${deployment}:${BUILD_NUMBER}'''
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
        imageName = "hub.hulushuju.com/${namespace}/${deployment}:${BUILD_NUMBER}"
        harbor_user = 'admin'
        harbor_password = 'Harbor12345'
    }
}