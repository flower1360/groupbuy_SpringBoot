package com.xj.groupbuy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.xj.groupbuy.mapper")
public class GroupbuyApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupbuyApplication.class, args);
    }

}
