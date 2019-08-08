package com.hulushuju.simplespringbootdemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by layker on 2018-12-10
 */
@RestController
@RequestMapping("/api/v1")
public class IndexCtrl {

    @Value("${spring.profiles.active:未知}")
    private String environment;


    private static Map<String, String> app = new HashMap<>();

    static {
        app.put("app", System.getenv("app"));
        app.put("version", System.getenv("version"));
    }


    @RequestMapping("")
    public Object index(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();

        String requestURI = request.getRequestURI();
        Cookie[] cookies = request.getCookies();
        String queryString = request.getQueryString();
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> httpHeaders = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String hname = headerNames.nextElement();
            String hvalue = request.getHeader(hname);
            httpHeaders.put(hname, hvalue);
        }
        String contentType = request.getContentType();
        int contentLength = request.getContentLength();

        info.put("requestURI", requestURI);
        info.put("cookies", cookies);
        info.put("queryString", queryString);
        info.put("httpHeaders", httpHeaders);
        info.put("contentType", contentType);
        info.put("contentLength", contentLength);
        info.put("app", app);
        info.put("environment", environment);

        return info;
    }


    @RequestMapping("environment")
    public String environment() {
        return environment;
    }

    @RequestMapping("app")
    public Object app() {
        return app;
    }

    @RequestMapping("version")
    public String version() {
        return app.get("version");
    }
}
