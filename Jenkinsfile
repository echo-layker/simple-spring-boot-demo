// jenkins pipeline语法官方文档：

pipeline {
    agent any

    //定时检测分支 15分钟一次，有变动触发构建 H/15 * * * *
    triggers {
        //每月周一到周五每天9-12点2分钟执行一次
        pollSCM '0 0/2 9-21 0 0 1/5 *'
        //上游依赖项目
//        upstream(upstreamProjects: "spring-data-commons/master", threshold: hudson.model.Result.SUCCESS)

        //推送到master分支 触发构建,需要配置gitlab webhook， http://10.76.81.200/devops-k8s-example/simple-tomcat-demo/hooks
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }

    options {
        //构建超时30分钟
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('maven build') {
            steps {
                //构建命令
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


    post {
        //构建状态通知


//        always
//        无论流水线或阶段的完成状态如何，都允许在 post 部分运行该步骤。
//
//        changed
//        只有当前流水线或阶段的完成状态与它之前的运行不同时，才允许在 post 部分运行该步骤。
//
//        failure
//        只有当前流水线或阶段的完成状态为"failure"，才允许在 post 部分运行该步骤, 通常web UI是红色。
//
//        success
//        只有当前流水线或阶段的完成状态为"success"，才允许在 post 部分运行该步骤, 通常web UI是蓝色或绿色。
//
//        unstable
//        只有当前流水线或阶段的完成状态为"unstable"，才允许在 post 部分运行该步骤, 通常由于测试失败,代码违规等造成。通常web UI是黄色。
//
//        aborted
//        只有当前流水线或阶段的完成状态为"aborted"，才允许在 post 部分运行该步骤, 通常由于流水线被手动的aborted。通常web UI是灰色。

        always {
            echo 'I will always say Hello again!'
        }

        failure {
            updateGitlabCommitStatus name: 'build', state: 'failed'
            echo 'failure!'
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