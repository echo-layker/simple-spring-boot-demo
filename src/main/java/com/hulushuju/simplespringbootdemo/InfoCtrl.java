package com.hulushuju.simplespringbootdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: layker
 * @Date: 2019-08-08 16:08
 * @Description:
 */
@RestController
@RequestMapping(value = "api/v1")
public class InfoCtrl {

    @Value("${service.tomcat.host}")
    private String host;

    @Value("${service.tomcat.port}")
    private int port;


    @Autowired
    private IndexCtrl indexCtrl;

    /**
     * 返回tomcat信息
     *
     * @return
     */
    @RequestMapping(value = "tomcat")
    public Object tomcat(@RequestParam(value = "path") String path, HttpServletRequest request) {
        //headers
        HttpHeaders requestHeaders = new HttpHeaders();

        requestHeaders.add("x-ot-span-context", request.getHeader("x-ot-span-context"));
        requestHeaders.add("x-b3-flags", request.getHeader("x-b3-flags"));
        requestHeaders.add("x-b3-sampled", request.getHeader("x-b3-sampled"));
        requestHeaders.add("x-b3-parentspanid", request.getHeader("x-b3-parentspanid"));
        requestHeaders.add("x-b3-spanid", request.getHeader("x-b3-spanid"));
        requestHeaders.add("x-b3-traceid", request.getHeader("x-b3-traceid"));
        requestHeaders.add("x-request-id", request.getHeader("x-request-id"));
        //HttpEntity
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(requestHeaders);

        RestTemplate restTemplate = new RestTemplate();
        if (StringUtils.isEmpty(path)) {
            path = "api/v1";
        }
        String url = MessageFormat.format("http://{0}:{1}/{2}", host, port, path);
        ResponseEntity<Map> mapResponseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);

        Map<String, Object> resp = new HashMap<>();
        Object info = indexCtrl.index(request);
        resp.put("boot", info);
        resp.put("tomcat", mapResponseEntity);
        return resp;
    }


}
