// jenkins pipeline语法官方文档：

pipeline {
    agent any

    //定时检测分支 15分钟一次，有变动触发构建 H/15 * * * *
//    triggers {
//        pollSCM 'H/1 * * * *'
////        upstream(upstreamProjects: "spring-data-commons/master", threshold: hudson.model.Result.SUCCESS)
//    }

    post {
        always {
            echo "清理构建产生的镜像"
            sh '''docker images
                  docker rmi -f ${imageName} ${releaseImageName} ${uat_imageName} ${uat_releaseImageName}
               '''
        }
        // failure {
        //     updateGitlabCommitStatus name: 'build', state: 'failed'
        // }
        // success {
        //     updateGitlabCommitStatus name: 'build', state: 'success'
        // }
        unsuccessful {
            // One or more steps need to be included within each condition's block.
            echo "本次构建不成功"
            script {
                dingTalk(accessToken: "${accessToken}", notifyPeople: '', message: "本次构建不成功，注意查看", imageUrl: 'https://i.loli.net/2019/06/13/5d025c99b76de60359.jpeg', jenkinsUrl: 'http://10.76.79.50:8080')
            }
        }
//        success {
//            script {
//                dingTalk(accessToken: "${accessToken}", notifyPeople: '', message: "", imageUrl: 'https://i.loli.net/2019/06/13/5d025c99b76de60359.jpeg', jenkinsUrl: 'http://10.76.79.50:8080')
//            }
//        }
    }

    triggers {
//        cron('H */4 * * 1-5')
        //每月周一到周五每天9-12点2分钟执行一次

        pollSCM 'H/2 H(9-19)/1 * * 1-5'
        //上游依赖项目
//        upstream(upstreamProjects: "spring-data-commons/master", threshold: hudson.model.Result.SUCCESS)

        //推送到master分支 触发构建,需要配置gitlab webhook， http://10.76.81.200/devops-k8s-example/simple-tomcat-demo/hooks
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }

    options {
        //构建超时60分钟
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '20', daysToKeepStr: '', numToKeepStr: '20')
        disableConcurrentBuilds()
    }

    stages {

        stage("display build params") {
            steps {
                //                echo "ENVIRONMENT : ${params.ENVIRONMENT}"

                echo "UPDATE : ${params.UPDATE}"

//                echo "IMAGE : ${params.IMAGE}"

                echo "imageName : ${imageName}"

                sh "env"
            }
        }

        stage('build image for uat') {
            when {
                not {
                    environment name: 'DEPLOY_TO_PRODUCTION', value: 'true'
                }
            }
            environment {
                RUN_ARGS = "${UAT_RUN_ARGS}"
                imageName = "${uat_imageName}"
                releaseImageName = "${uat_releaseImageName}"
            }
            steps {
                //构建命令
                echo "开始 maven : ${UAT_BUILD_CMD}"
                sh "${UAT_BUILD_CMD}"
                sh 'mkdir -p docker'
                sh "cp -r ${workdir}/target/*.jar docker/${deployment}.jar"
                echo "prepare Dockerfile for uat start"
                sh '''
                eval "cat << $(< Dockerfile.tmpl)"  > docker/Dockerfile
                '''
                echo "prepare Dockerfile for uat end"
                script {
                    dir('docker') {
                        docker.withRegistry("https://${registry}", "${registry}") {
                            def image = docker.build("${imageName}")
                            image.push()
                            image.push("${BRANCH_NAME}")
                        }
                    }
                }
                //构建镜像名称归档
                sh '''echo "${imageName}\n${releaseImageName}" > uat_imageName.txt'''
                archiveArtifacts(artifacts: "uat_imageName.txt", onlyIfSuccessful: true)
//                archiveArtifacts(artifacts: 'docker/*.jar', excludes: 'docker/*.source.jar', onlyIfSuccessful: true)
            }
            post {
                always {
                    echo "清理docker dir"
                    sh "rm -rf docker"
                }
            }
        }

        stage("directly deploy to k8s 【uat】") {
            when {
                allOf {
                    not {
                        environment name: 'DEPLOY_TO_PRODUCTION', value: 'true'
                    }
                    not {
                        environment name: 'UPDATE', value: 'false'
                    }
                    not {
                        environment name: 'UAT_CANARY', value: 'true'
                    }
                }
            }
            environment {
//                imageName = sh(script: '[[ "${IMAGE}" ==  "BY_JENKINS" ]] && echo "${uat_imageName}" || echo "${IMAGE}"', returnStdout: true).trim()
                imageName = "${uat_imageName}"
//                DEPLOY_CMD = "sed -i -e \"s#<IMAGE>#${uat_imageName}#g\" docker/deployment.yaml   && kubectl apply -f docker"
            }
            steps {
                echo "开始部署UAT环境"
//   备份             withKubeConfig(credentialsId: 'hulushuju-uat', serverUrl: 'https://rc.hulushuju.com/k8s/clusters/c-z5qq9', namespace: 'devops-k8s-example', clusterName: 'hulushuju-uat', contextName: 'hulushuju-uat') {
                withKubeConfig(credentialsId: 'hulushuju-uat') {
//                    sh "sed -i 's/<BUILD_TAG>/${build_tag}/' docker/deployment.yaml"
                    sh 'kubectl -n ${namespace} set image deployment/${deployment}  ${deployment}=${imageName}'
//                    sh "${DEPLOY_CMD}"
                }
            }
            post {
                success {
                    script {
                        dingTalk(accessToken: "${accessToken}", notifyPeople: '', message: "${tag}，UAT已部署，更新:${MSG}", imageUrl: 'https://i.loli.net/2019/06/13/5d025c99b76de60359.jpeg', jenkinsUrl: 'http://10.76.79.50:8080')
                    }
                }
            }
        }


        stage("canary deploy to k8s 【uat】") {
            when {
                allOf {
                    not {
                        environment name: 'DEPLOY_TO_PRODUCTION', value: 'true'
                    }
                    not {
                        environment name: 'UPDATE', value: 'false'
                    }
                    not {
                        environment name: 'UAT_CANARY', value: 'false'
                    }
                }
            }
            environment {
//                imageName = sh(script: '[[ "${IMAGE}" ==  "BY_JENKINS" ]] && echo "${uat_imageName}" || echo "${IMAGE}"', returnStdout: true).trim()
                imageName = "${uat_imageName}"
//                DEPLOY_CMD = "sed -i -e \"s#<IMAGE>#${uat_imageName}#g\" docker/deployment.yaml   && kubectl apply -f docker"
            }
            steps {
                echo "UAT环境 开始 金丝雀部署"
                echo "******************* 开始checkout yaml配置文件 *******************"
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CleanBeforeCheckout'], [$class: 'RelativeTargetDirectory', relativeTargetDir: 'yaml']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'gitadmin', url: 'http://10.76.81.200/devops-k8s-example/yaml.git']]])
                echo "******************* 成功checkout yaml配置文件 *******************"

//                echo "cmd: helm install yaml/${namespace}/${deployment}  --name ${deployment} --namespace ${namespace} --set image.repository=${registry}/${namespace}/${deployment}  --set image.tag=${tag}"
//   备份             withKubeConfig(credentialsId: 'hulushuju-uat', serverUrl: 'https://rc.hulushuju.com/k8s/clusters/c-z5qq9', namespace: 'devops-k8s-example', clusterName: 'hulushuju-uat', contextName: 'hulushuju-uat') {
                withKubeConfig(credentialsId: 'hulushuju-uat') {
                    sh 'chmod 750 yaml/${namespace}/${deployment}.sh'
                    sh 'yaml/${namespace}/${deployment}.sh'
//                    helm template yaml/${namespace}/${deployment}  --name ${deployment} --namespace ${namespace} --set image.repository=${registry}/${namespace}/${deployment}  --set image.tag=${tag} |  kubectl apply -f -
//                    sh 'kubectl -n ${namespace} set image deployment/${deployment}  ${deployment}=${imageName}'
//                    sh "${DEPLOY_CMD}"
                }
                archiveArtifacts(artifacts: "yaml/${namespace}/${deployment}/${deployment}-${tag}.yaml", onlyIfSuccessful: true)
            }
            post {
                success {
                    script {
                        dingTalk(accessToken: "${accessToken}", notifyPeople: '', message: "${tag}，UAT环境金丝雀版本已部署，更新:${MSG}", imageUrl: 'https://i.loli.net/2019/06/13/5d025c99b76de60359.jpeg', jenkinsUrl: 'http://10.76.79.50:8080')
                    }
                }
            }
        }


        stage('build image for production') {
            when {
//                beforeInput true
                environment name: 'DEPLOY_TO_PRODUCTION', value: 'true'
            }
            environment {
                RUN_ARGS = "${PROD_RUN_ARGS}"
                BUILD_CMD = "${PROD_BUILD_CMD}"
            }
//            input {
//                message "继续构建生产环境?"
//                ok "是的，继续."
//                submitter "admin"
////                parameters {
////                    string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
////                }
//            }

            steps {
                //构建命令
                echo "开始 maven : ${BUILD_CMD}"
                sh "${BUILD_CMD}"
                sh 'mkdir -p docker'
                sh "cp -r ${workdir}/target/*.jar docker/${deployment}.jar"

                echo "prepare Dockerfile for production start"
                sh '''
                eval "cat << $(< Dockerfile.tmpl)"  > docker/Dockerfile
                '''
                echo "prepare Dockerfile for production end"

                script {
                    dir('./docker') {
                        docker.withRegistry("https://${registry}", "${registry}") {
                            def image = docker.build("${imageName}")
                            image.push()
                            image.push("${BRANCH_NAME}")
                            image.push("latest")
                        }
                    }
                }
                echo "构建镜像名: ${imageName}"
                //构建镜像名称归档
                sh '''echo "${imageName}\n${releaseImageName}" > imageName.txt
                    pwd
                    ls -l
                    '''
                archiveArtifacts(artifacts: 'target/*.jar', excludes: '', onlyIfSuccessful: true)
                archiveArtifacts(artifacts: "imageName.txt", onlyIfSuccessful: true)
            }
            post {
                always {
                    echo "清理 docker dir"
                    sh "rm -rf docker"
                }
            }
        }

        stage("deploy to k8s 【production】") {
            when {
                environment name: 'DEPLOY_TO_PRODUCTION', value: "true"
                not {
                    environment name: 'UPDATE', value: 'false'
                }
            }
            environment {
//                imageName = sh(script: '[[ "${IMAGE}" ==  "BY_JENKINS" ]] && echo "${imageName}" || echo "${IMAGE}"', returnStdout: true).trim()
                imageName = "${releaseImageName}"
//                DEPLOY_CMD = "sed -i -e \"s#<IMAGE>#${imageName}#g\" docker/deployment.yaml   && kubectl apply -f docker"
            }

            steps {
                echo "******************* 开始checkout yaml配置文件 *******************"
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CleanBeforeCheckout'], [$class: 'RelativeTargetDirectory', relativeTargetDir: 'yaml']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'gitadmin', url: 'http://10.76.81.200/devops-k8s-example/yaml.git']]])
                echo "******************* 成功checkout yaml配置文件 *******************"

                echo "开始部署生产服务"

//                echo "cmd: helm install yaml/${namespace}/${deployment}  --name ${deployment} --namespace ${namespace} --set image.repository=${registry}/${namespace}/${deployment}  --set image.tag=${tag}"
//   备份             withKubeConfig(credentialsId: 'hulushuju-uat', serverUrl: 'https://rc.hulushuju.com/k8s/clusters/c-z5qq9', namespace: 'devops-k8s-example', clusterName: 'hulushuju-uat', contextName: 'hulushuju-uat') {
                withKubeConfig(credentialsId: 'hulushuju-wuxi') {
                    sh 'chmod 750 yaml/${namespace}/${deployment}.sh'
                    sh 'yaml/${namespace}/${deployment}.sh'
//                    helm template yaml/${namespace}/${deployment}  --name ${deployment} --namespace ${namespace} --set image.repository=${registry}/${namespace}/${deployment}  --set image.tag=${tag} |  kubectl apply -f -
//                    sh 'kubectl -n ${namespace} set image deployment/${deployment}  ${deployment}=${imageName}'
//                    sh "${DEPLOY_CMD}"
                }
                archiveArtifacts(artifacts: "yaml/${namespace}/${deployment}/${deployment}-${tag}.yaml", onlyIfSuccessful: true)
            }

            post {
                success {
                    script {
                        dingTalk(accessToken: "${accessToken}", notifyPeople: '', message: "${tag}，生产金丝雀版本已部署，更新:${MSG}", imageUrl: 'https://i.loli.net/2019/06/13/5d025c99b76de60359.jpeg', jenkinsUrl: 'http://10.76.79.50:8080')
                    }
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
        registry = "layker"
        //worddir
        workdir = "."
        //UAT环境构建命令
        UAT_BUILD_CMD = "mvn clean package -Puat"
        //PROD环境构建命令
        PROD_BUILD_CMD = "mvn clean package -Pprod"
        //tag
//        sh(script: 'echo "BRANCH_NAME:${BRANCH_NAME}"')
//        sh(script: 'echo "VERSION:$VERSION"')
        //根据是否是tag自动判断是否更新生产环境: 已 'v' 开头的会触发更新生产环境
        DEPLOY_TO_PRODUCTION = sh(script: "if echo $BRANCH_NAME|grep -qe '^v' ;then echo 'true';else echo 'false' ;fi;", returnStdout: true).trim()
        //镜像名称 for 生产环境
        tag = "${BRANCH_NAME}-rc${BUILD_NUMBER}"
        imageName = "${registry}/${namespace}/${deployment}:${tag}"
        releaseImageName = "${registry}/${namespace}/${deployment}:${BRANCH_NAME}"
        //测试环境构建的镜像名称
        uat_imageName = "${registry}/${namespace}/${deployment}:${tag}"
        uat_releaseImageName = "${registry}/${namespace}/${deployment}:${BRANCH_NAME}"
        //钉钉
        accessToken = "fe14f8222b866060a96d80c1db25eafc44b361c8bbebc58544464b885c52152e"
        //构建提示消息
        MSG = sh(script: 'git log --pretty=format:"%s" $(git rev-parse HEAD) -1', returnStdout: true).trim()
    }


    //输入参数
    parameters {
//        string(name: 'IMAGE', defaultValue: 'BY_JENKINS', description: '直接部署此镜像，eg: layker/namespace/deployname:tag（默认jenkins自动生成）')

//        string(name: "VERSION", defaultValue: "BY_JENKINS", description: '自定义版本号，eg: v1.1.0（默认jenkins自动生成）ps: IMAGE优先')

        string(name: "UAT_RUN_ARGS", defaultValue: "", description: 'UAT环境服务启动参数,eg: -Dspring.profile.active=uat')

        string(name: "PROD_RUN_ARGS", defaultValue: "", description: 'PROD环境服务启动参数,eg: -Dspring.profile.active=prod')

        booleanParam(name: 'UPDATE', defaultValue: true, description: '构建完成是否更新服务')

        booleanParam(name: 'UAT_CANARY', defaultValue: true, description: 'UAT环境是否启用金丝雀部署')

        booleanParam(name: 'PROD_CANARY', defaultValue: true, description: '生产环境是否启用金丝雀部署')

//        choice(name: 'ENVIRONMENT', choices: ['all', 'uat', 'production'], description: '选择部署目标环境:all=所有环境，uat=测试环境，production=生产环境')

//        listGitBranches branchFilter: '.*', credentialsId: 'gitadmin', defaultValue: '', name: 'VERSION', quickFilterEnabled: false, remoteURL: 'https://github.com/jenkinsci/list-git-branches-parameter-plugin.git', selectedValue: 'TOP', sortMode: 'DESCENDING_SMART', tagFilter: '.*', type: 'PT_BRANCH_TAG'
//        gitParameter branch: '', branchFilter: '.*', defaultValue: 'latest', description: '构建版本号', listSize: '10', name: 'VERSION', quickFilterEnabled: false, selectedValue: 'TOP', sortMode: 'DESCENDING_SMART', tagFilter: '*', type: 'PT_TAG'

    }
}
