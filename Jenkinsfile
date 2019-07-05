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
        unsuccessful {
            // One or more steps need to be included within each condition's block.
            sh "本次构建不成功"
        }
        changed {
            script {
                dingTalk(accessToken: "${accessToken}", notifyPeople: '', message: "${ENVIRONMENT} build : ${currentBuild.currentResult}", imageUrl: 'https://i.loli.net/2019/06/13/5d025c99b76de60359.jpeg', jenkinsUrl: 'http://10.76.79.50:8080')
            }
        }
    }

    triggers {
        //每月周一到周五每天9-12点2分钟执行一次
        pollSCM 'H/2 H(9-19)/2 * * 1-5'
        //上游依赖项目
//        upstream(upstreamProjects: "spring-data-commons/master", threshold: hudson.model.Result.SUCCESS)

        //推送到master分支 触发构建,需要配置gitlab webhook， http://10.76.81.200/devops-k8s-example/simple-tomcat-demo/hooks
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }

    options {
        //构建超时30分钟
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '20', daysToKeepStr: '', numToKeepStr: '20')
    }

    stages {

        stage("部署参数测试") {
            steps {
                echo "本次部署环境 : ${params.ENVIRONMENT}"

                echo "是否同步更新服务 : ${params.UPDATE}"

                echo "直接部署镜像 : ${params.IMAGE}"

                echo "构建镜像名称 : ${imageName}"
            }
        }

        stage('maven build') {
            when {
                environment name: 'IMAGE', value: 'BY_JENKINS'
            }
            environment {
                BUILD_CMD = sh(script: '[[ "${ENVIRONMENT}" ==  "PROD" ]] && echo "${PROD_BUILD_CMD}" || echo "${UAT_BUILD_CMD}"', returnStdout: true).trim()
            }
            steps {
                //构建命令
                echo "开始 maven 构建${ENVIRONMENT}环境"
                sh "${BUILD_CMD}"
                sh 'mkdir -p docker'
                sh "cp target/*.jar docker/${deployment}.jar"
                archiveArtifacts(artifacts: 'target/*.jar', excludes: 'target/*.source.jar', onlyIfSuccessful: true)
            }
        }
        stage("准备Dockerfile构建环境") {
            when {
                environment name: 'IMAGE', value: 'BY_JENKINS'
            }
            environment {
                RUN_ARGS = sh(script: '[[ "${ENVIRONMENT}" ==  "PROD" ]] && echo "${PROD_RUN_ARGS}" || echo "${UAT_RUN_ARGS}"', returnStdout: true).trim()
            }
            steps {
                sh '''
cat > docker/Dockerfile <<EOF
#基础镜像
FROM hub.hulushuju.com/jre/jre-8:8u191
#可以通过环境变量 自定义JVM参数
ENV JAVA_OPTIONS " "
WORKDIR /data/java
COPY ${deployment}.jar ${deployment}.jar
#挂载出日志目录
VOLUME /data/logs
CMD java -Djava.security.egd=file:/dev/./urandom  ${RUN_ARGS} ${JAVA_OPTIONS} -jar ${deployment}.jar
EOF
'''
                sh '''
                    ls docker
                    echo docker/Dockerfile
                    '''
            }
        }

        stage('docker build image') {
            when {
                environment name: 'IMAGE', value: 'BY_JENKINS'
            }
            steps {
                script {
                    dir('./docker') {
                        docker.withRegistry("https://${registry}", "${registry}") {
                            docker.build("${imageName}").push()
                        }
                    }
                }
                echo "构建镜像名: ${imageName}"
                sh '''docker images
                      docker rmi -f ${imageName}'''

                //构建镜像名称归档
                sh '''echo "${imageName}" > imageName.txt'''
                archiveArtifacts(artifacts: "imageName.txt", onlyIfSuccessful: true)
            }
        }

        stage("deploy to k8s 【UAT】") {
            when {
                environment name: 'ENVIRONMENT', value: 'UAT'
                environment name: 'UPDATE', value: "true"
            }
            environment {
                imageName = sh(script: '[[ "${IMAGE}" ==  "BY_JENKINS" ]] && echo "${imageName}" || echo "${IMAGE}"', returnStdout: true).trim()
                DEPLOY_CMD = "sed -i \"s/<IMAGE>/${imageName}/g\" docker/deployment.yaml   && kubectl apply -f docker"
            }
            steps {
                echo "开始部署UAT环境"
//   备份             withKubeConfig(credentialsId: 'hulushuju-uat', serverUrl: 'https://rc.hulushuju.com/k8s/clusters/c-z5qq9', namespace: 'devops-k8s-example', clusterName: 'hulushuju-uat', contextName: 'hulushuju-uat') {
                withKubeConfig(credentialsId: 'hulushuju-uat') {
//                    sh "sed -i 's/<BUILD_TAG>/${build_tag}/' docker/deployment.yaml"
                    sh "${DEPLOY_CMD}"
                }
            }
        }

        stage("deploy to k8s 【PROD】") {
            when {
                beforeInput true
                environment name: 'ENVIRONMENT', value: 'PROD'
                environment name: 'UPDATE', value: "true"
            }
            environment {
                imageName = sh(script: '[[ "${IMAGE}" ==  "BY_JENKINS" ]] && echo "${imageName}" || echo "${IMAGE}"', returnStdout: true).trim()
                DEPLOY_CMD = "sed -i \"s/<IMAGE>/${imageName}/g\" docker/deployment.yaml   && kubectl apply -f docker"
            }
            input {
                message "确定更新生产环境?"
                ok "是的，继续."
                submitter "admin"
//                parameters {
//                    string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
//                }
            }
            steps {
                echo "开始部署生产服务"
//   备份             withKubeConfig(credentialsId: 'hulushuju-uat', serverUrl: 'https://rc.hulushuju.com/k8s/clusters/c-z5qq9', namespace: 'devops-k8s-example', clusterName: 'hulushuju-uat', contextName: 'hulushuju-uat') {
                withKubeConfig(credentialsId: 'hulushuju-prod') {
//                    sh 'kubectl -n ${namespace} set image deployment/${deployment}  ${deployment}=${imageName}'
                    sh "${DEPLOY_CMD}"
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
        //tag
        tag = sh(script: '[[ "$VERSION" ==  "BY_JENKINS" ]] && echo "${BUILD_NUMBER}" || echo "${VERSION}"', returnStdout: true).trim()
        //镜像名称
        imageName = "${registry}/${namespace}/${deployment}:${BRANCH_NAME}-${ENVIRONMENT}-${tag}"
        //UAT环境构建命令
        UAT_BUILD_CMD = "mvn clean package -Puat"
        //PROD环境构建命令
        PROD_BUILD_CMD = "mvn clean package -Pprod"
        //钉钉
        accessToken = "e66e0cd9e155c15bb89ccb881f015e4391efe7f7ad66e63518aca06d97beb187"
    }


    //输入参数
    parameters {
        string(name: 'IMAGE', defaultValue: 'BY_JENKINS', description: '直接部署此镜像，eg: hub.hulushuju.com/namespace/deployname:tag（默认jenkins自动生成）')

        string(name: "VERSION", defaultValue: "BY_JENKINS", description: '自定义版本号，eg: v1.1.0（默认jenkins自动生成）ps: IMAGE优先')

        string(name: "UAT_RUN_ARGS", defaultValue: "", description: 'UAT环境服务启动参数,eg: -Dspring.profile.active=uat')

        string(name: "PROD_RUN_ARGS", defaultValue: "", description: 'PROD环境服务启动参数,eg: -Dspring.profile.active=prod')

        booleanParam(name: 'UPDATE', defaultValue: true, description: '构建完成是否更新服务')

        choice(name: 'ENVIRONMENT', choices: ['UAT', 'PROD'], description: '选择部署目标环境')
    }
}