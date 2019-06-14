package com.hulushuju.simplespringbootdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Slf4j
public class SimpleSpringBootDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleSpringBootDemoApplication.class, args);
    }


    @Configuration
    public class EnvInfo implements CommandLineRunner {

        @Override
        public void run(String... args) throws Exception {
            log.info("======== 当前环境是 =========");
            log.info("======== 项目启动成功 =========");
        }
    }

}

