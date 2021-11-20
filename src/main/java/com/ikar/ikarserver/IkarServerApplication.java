package com.ikar.ikarserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.ikar.ikarserver.domain.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.ikar.ikarserver.repository.jpa"
})
public class IkarServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(IkarServerApplication.class, args);
    }

}
