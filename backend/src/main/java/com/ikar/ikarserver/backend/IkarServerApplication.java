package com.ikar.ikarserver.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.ikar.ikarserver.backend.domain.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.ikar.ikarserver.backend.repository.jpa"
})
public class IkarServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(IkarServerApplication.class, args);
    }

}
