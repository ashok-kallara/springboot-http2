package com.manh.rest.http2;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class DemoController {

    @RequestMapping("/hello")
    public String index(@RequestParam(name = "waitTimeSec", required = false) Integer waitTimeSec) throws Exception {
        if(waitTimeSec != null) {
            Thread.sleep(waitTimeSec * 1000 );
        }
        return "Greetings from Spring Boot!";
    }
}