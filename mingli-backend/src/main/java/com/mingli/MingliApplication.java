package com.mingli;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mingli.mapper")
public class MingliApplication {

    public static void main(String[] args) {
        SpringApplication.run(MingliApplication.class, args);
    }
}
