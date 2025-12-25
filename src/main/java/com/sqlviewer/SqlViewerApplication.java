package com.sqlviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SQLViewer Web Application
 * MySQL数据查看器 - Spring Boot应用入口
 */
@SpringBootApplication
public class SqlViewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqlViewerApplication.class, args);
    }
}
