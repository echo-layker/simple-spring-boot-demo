// jenkins pipeline语法官方文档：

pipeline {
    agent any

    //定时检测分支 15分钟一次，有变动触发构建 H/15 * * * *
//    triggers {
//        pollSCM 'H/1 * * * *'
////        upstream(upstreamProjects: "spring-data-commons/master", threshold: hudson.model.Result.SUCCESS)
//    }

    post {
        // failure {
        //     updateGitlabCommitStatus name: 'build', state: 'failed'
        // }
        // success {
        //     updateGitlabCommitStatus name: 'build', state: 'success'
        // }
        changed {
            script {
                dingTalk(accessToken: "${accessToken}", notifyPeople: '', message: " 当前构建结果为 ${currentBuild.currentResult}", imageUrl: 'https://i.loli.net/2019/06/13/5d025c99b76de60359.jpeg', jenkinsUrl: 'http://10.76.79.50:8080')
            }
        }
    }

    triggers {
        //每月周一到周五每天9-12点2分钟执行一次
        pollSCM 'H/15 * * * *'
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

        stage("部署参数测试") {
            steps {
                echo "本次部署环境 : ${params.ENVIRONMENT}"

                echo "是否同步更新服务 : ${params.UPDATE}"

                echo "自定义镜像名称 : ${params.IMAGENAME}"
            }
        }

        stage('maven build') {
            steps {
                //构建命令
                sh 'mvn clean package'
                archiveArtifacts(artifacts: 'target/*.jar', excludes: 'target/*.source.jar', onlyIfSuccessful: true)
            }
        }
        stage('docker build image') {
            steps {

                script {
                    dir('./') {
                        docker.withRegistry("https://${registry}", "${registry}") {
                            docker.build("${imageName}").push()
                        }
                    }
                }

                sh '''docker images
                      docker rmi -f ${imageName}'''

                //构建镜像名称归档
                sh '''echo "${imageName}" > imageName.txt'''
                archiveArtifacts(artifacts: "imageName.txt", onlyIfSuccessful: true)
            }
        }
        stage('deploy to k8s') {
            when {
                beforeInput true
                branch "master"
                environment name: 'ENVIRONMENT', value: 'PROD'
            }
            input {
                message "Should we continue?"
                ok "Yes, we should."
                submitter "alice,bob"
                parameters {
                    string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
                }
            }
            steps {
                echo "开始部署生产服务"
//   备份             withKubeConfig(credentialsId: 'hulushuju-uat', serverUrl: 'https://rc.hulushuju.com/k8s/clusters/c-z5qq9', namespace: 'devops-k8s-example', clusterName: 'hulushuju-uat', contextName: 'hulushuju-uat') {
                withKubeConfig(credentialsId: 'hulushuju-uat') {
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
        //harbor域名
        registry = "hub.hulushuju.com"
        //镜像名称
        imageName = "${registry}/${namespace}/${deployment}:${BRANCH_NAME}-${BUILD_NUMBER}"
        //钉钉
        accessToken = "e66e0cd9e155c15bb89ccb881f015e4391efe7f7ad66e63518aca06d97beb187"
    }

    //输入参数
    parameters {
        string(name: 'IMAGENAME', defaultValue: 'BY_JENKINS', description: '自定义构建镜像名称，eg: hub.hulushuju.com/namespace/deployname:tag（默认jenkins自动生成）')

        string(name: "VERSION", defaultValue: 'BY_JENKINS', description: '自定义版本号，eg: v1.1.0（默认jenkins自动生成）')

        booleanParam(name: 'UPDATE', defaultValue: true, description: '构建完成是否更新服务')

        choice(name: 'ENVIRONMENT', choices: ['UAT', 'PROD', '全部'], description: '选择部署目标环境')

    }


}