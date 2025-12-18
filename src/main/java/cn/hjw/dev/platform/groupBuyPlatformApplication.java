package cn.hjw.dev.platform;

import io.github.cdimascio.dotenv.Dotenv;
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

   // 加载根目录的 .env 文件到系统环境变量
   Dotenv dotenv = Dotenv.load();
   dotenv.entries().forEach(entry ->
           System.setProperty(entry.getKey(), entry.getValue())
   );
   SpringApplication.run(groupBuyPlatformApplication.class, args);
  }
 }