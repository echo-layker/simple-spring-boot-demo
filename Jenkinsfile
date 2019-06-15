pipeline {
    agent any

//    triggers {
//        pollSCM 'H/1 * * * *'
////        upstream(upstreamProjects: "spring-data-commons/master", threshold: hudson.model.Result.SUCCESS)
//    }

    post {
        failure {
            updateGitlabCommitStatus name: 'build', state: 'failed'
        }
        success {
            updateGitlabCommitStatus name: 'build', state: 'success'
        }
        changed {
            script {
                dingTalk(accessToken: 'e66e0cd9e155c15bb89ccb881f015e4391efe7f7ad66e63518aca06d97beb187', notifyPeople: '', message: " 当前构建结果为 ${currentBuild.currentResult}", imageUrl: 'https://i.loli.net/2019/06/13/5d025c99b76de60359.jpeg', jenkinsUrl: 'http://10.76.79.50:8080')
            }
        }
    }

    triggers {
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }

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
        //项目组名称
        namespace = 'devops-k8s-example'
        //项目名称
        deployment = 'simple-spring-boot-demo'
        //镜像名称
        imageName = "hub.hulushuju.com/${namespace}/${deployment}:${BRANCH_NAME}-${BUILD_NUMBER}"
        harbor_user = 'admin'
        harbor_password = 'Harbor12345'
    }
}