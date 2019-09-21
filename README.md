simple-spring-boot-demo

1.改造步骤 @过时
    
    复制 Dockerfile
    
```
#基础镜像
FROM layker/jre/jre-8:8u191
#可以通过环境变量 自定义JVM参数
ENV JAVA_OPTIONS " "
WORKDIR /data/java
COPY <你的jar包全路径> <容器中jar名称[可以与from名称相同]>
#挂载出日志目录
VOLUME /data/logs
CMD java -Djava.security.egd=file:/dev/./urandom ${JAVA_OPTIONS} -jar <容器中jar名称>
```

    添加Jenkinsfile

    Jenkinsfile
    
```
pipeline {
    agent any

//    triggers {
//        pollSCM 'H/1 * * * *'
////        upstream(upstreamProjects: "spring-data-commons/master", threshold: hudson.model.Result.SUCCESS)
//    }


    triggers {
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }

    options { timeout(time: 1, unit: 'HOURS') }

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

                      docker login layker -u ${harbor_user} -p ${harbor_password}
                        
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
        imageName = "layker/${namespace}/${deployment}:${BRANCH_NAME}-${BUILD_NUMBER}"
        harbor_user = 'admin'
        harbor_password = 'Harbor12345'
    }
}
```

```

修改对应构建命令和环境变量配置

```

#  测试命令
```bash
curl 'http://localhost:8080/api/v1?a=b' -H 'sec-fetch-mode: cors' -H 'accept-encoding: gzip, deflate, br' -H 'accept-language: zh-CN,zh;q=0.9,en;q=0.8' -H 'cookie: R_USERNAME=admin; CSRF=d3ed9d42fb; Hm_lvt_a42ef6994d2c0d900b5738317637cdea=1564627968,1565082890; Hm_lpvt_a42ef6994d2c0d900b5738317637cdea=1565083014; R_SESS=token-8nlv8:btl2c8fbpmbbhz2nwb925smpb4rm6x5gm7r7wsj6626n4xvdj2889d; grafana_sess=e4f5fde03112147b' -H 'pragma: no-cache' -H 'x-api-action-links: actionLinks' -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.87 Safari/537.36' -H 'content-type: application/json' -H 'x-api-no-challenge: true' -H 'accept: application/json' -H 'cache-control: no-cache' -H 'authority: k8s.hulushuju.com' -H 'referer: https://k8s.hulushuju.com/p/c-8l4bp:p-qn5x9/workloads' -H 'sec-fetch-site: same-origin' -H 'x-api-csrf: d3ed9d42fb' --compressed
```



