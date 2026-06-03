package com.guandian.bidding;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 观点科技电子招投标交易平台 - 后端启动类。
 */
@SpringBootApplication
@MapperScan("com.guandian.bidding.**.mapper")
public class BiddingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiddingApplication.class, args);
    }
}
