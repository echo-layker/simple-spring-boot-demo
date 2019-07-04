package com.hulushuju.simplespringbootdemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by layker on 2018-12-10
 */
@RequestMapping("/")
@RestController
public class IndexCtrl {

    @Value("${spring.profiles.active:未知}")
    private String env;

    @RequestMapping("")
    public String index() {
        return env;
    }
}
