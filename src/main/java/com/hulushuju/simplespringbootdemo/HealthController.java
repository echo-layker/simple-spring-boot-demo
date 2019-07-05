package com.hulushuju.simplespringbootdemo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by layker on 2018-12-10
 */
@RestController
@RequestMapping(value = "actuator")
public class HealthController {
    @RequestMapping(value = "healthz", method = RequestMethod.GET)
    public String healthz() {
        System.out.println("==================健康检查healthz==================");
        return "ok";
    }

    @RequestMapping(value = "ready", method = RequestMethod.GET)
    public String ready() {
        System.out.println("==================就绪检查ready==================");
        return "ready";
    }
}
