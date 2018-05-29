package com.gs.spider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ImportResource(value= {"classpath:spider-core.xml"})
@ServletComponentScan
@EnableScheduling
public class SpiderApplication {

    /**
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(SpiderApplication.class, args);
    }
}


