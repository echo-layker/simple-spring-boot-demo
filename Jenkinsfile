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
        sh '''docker build --rm -f "Dockerfile" -t  ${imageName} .

                      docker login hub.hulushuju.com -u ${harbor_user} -p ${harbor_password}
                        
                      docker push ${imageName}
                        
                      docker rmi -f ${imageName}'''
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
    imageName = "hub.hulushuju.com/${namespace}/${deployment}:${BRANCH_NAME}-${BUILD_NUMBER}"
    harbor_user = 'admin'
    harbor_password = 'Harbor12345'
  }
  post {
    changed {
      script {
        dingTalk(accessToken: 'e66e0cd9e155c15bb89ccb881f015e4391efe7f7ad66e63518aca06d97beb187', notifyPeople: '', message: "${currentBuild.fullDisplayName} is reported as ${currentBuild.currentResult}", imageUrl: 'https://i.loli.net/2019/06/13/5d025c99b76de60359.jpeg', jenkinsUrl: 'http://10.76.79.50:8080')
      }


    }

  }
}