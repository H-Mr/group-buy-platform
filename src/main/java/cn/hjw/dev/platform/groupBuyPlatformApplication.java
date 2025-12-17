package cn.hjw.dev.platform;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication // 标记为Spring Boot应用
@Configurable // 标记为配置类
@EnableAsync // 开启异步任务
@EnableScheduling //开启定时任务
 public class groupBuyPlatformApplication {
  public static void main(String[] args) {
   SpringApplication.run(groupBuyPlatformApplication.class, args);
  }
 }