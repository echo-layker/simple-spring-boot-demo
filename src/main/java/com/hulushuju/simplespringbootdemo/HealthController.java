package com.hulushuju.simplespringbootdemo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by layker on 2018-12-10
 */
@RestController
@RequestMapping(value = "healthz")
public class HealthController {
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String healthz() {
        return "ok";
    }
}
