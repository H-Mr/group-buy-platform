package cn.hjw.dev.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
 public class groupBuyMallApplication {
  public static void main(String[] args) {
   SpringApplication.run(groupBuyMallApplication.class, args);
  }
 }