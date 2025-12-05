package com.lei.springbootpractice4;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.lei.springbootpractice4.mapper")
@EnableScheduling
public class SpringBootPractice4Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootPractice4Application.class, args);
    }

}
