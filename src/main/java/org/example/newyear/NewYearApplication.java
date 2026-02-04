package org.example.newyear;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 春节2026 AI视频生成 - 主应用类
 *
 * @author Claude
 * @since 2026-02-04
 */
@SpringBootApplication
@MapperScan("org.example.newyear.mapper")
public class NewYearApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewYearApplication.class, args);
    }
}