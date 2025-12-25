package com.sqlviewer.controller;

import com.sqlviewer.service.ApiForwardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * API转发控制器
 * 将前端API请求转发到后端服务器
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private ApiForwardService apiForwardService;

    /**
     * 用户登录
     */
    @PostMapping("/authenticate")
    public ResponseEntity<String> login(@RequestParam String username,
                                        @RequestParam String password,
                                        HttpSession session) {
        try {
            logger.info("Login request for user: {}", username);
            String response = apiForwardService.login(username, password, session);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\": -1, \"msg\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 查询服务器列表
     */
    @GetMapping("/servers")
    public ResponseEntity<String> queryServers(HttpSession session) {
        try {
            logger.info("Query servers request");
            String response = apiForwardService.queryServers(session);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Query servers error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\": -1, \"msg\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 查询数据库列表
     */
    @GetMapping("/databases")
    public ResponseEntity<String> queryDatabases(@RequestParam String instanceName,
                                                  HttpSession session) {
        try {
            logger.info("Query databases for instance: {}", instanceName);
            String response = apiForwardService.queryDatabases(instanceName, session);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Query databases error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\": -1, \"msg\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 查询表列表
     */
    @GetMapping("/tables")
    public ResponseEntity<String> queryTables(@RequestParam String instanceName,
                                               @RequestParam String dbName,
                                               HttpSession session) {
        try {
            logger.info("Query tables for database: {}", dbName);
            String response = apiForwardService.queryTables(instanceName, dbName, session);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Query tables error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\": -1, \"msg\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 查询表结构
     */
    @PostMapping("/table/structure")
    public ResponseEntity<String> showTableStructure(@RequestParam String instanceName,
                                                      @RequestParam String dbName,
                                                      @RequestParam String tableName,
                                                      HttpSession session) {
        try {
            logger.info("Show table structure: {}.{}", dbName, tableName);
            String response = apiForwardService.showTableStructure(instanceName, dbName, tableName, session);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Show table structure error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\": -1, \"msg\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 执行SQL查询
     */
    @PostMapping("/query")
    public ResponseEntity<String> querySql(@RequestParam String instanceName,
                                            @RequestParam String dbName,
                                            @RequestParam String tableName,
                                            @RequestParam String sqlContent,
                                            @RequestParam String limitNum,
                                            HttpSession session) {
        try {
            logger.info("Execute SQL query: {}", sqlContent);
            String response = apiForwardService.querySql(instanceName, dbName, tableName, 
                                                         sqlContent, limitNum, session);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Query SQL error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\": -1, \"msg\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 保存配置到Session
     */
    @PostMapping("/config/save")
    public ResponseEntity<Map<String, Object>> saveConfig(@RequestParam String key,
                                                           @RequestParam String value,
                                                           HttpSession session) {
        try {
            session.setAttribute(key, value);
            Map<String, Object> result = new HashMap<>();
            result.put("status", 0);
            result.put("msg", "Success");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Save config error", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", -1);
            result.put("msg", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 从Session获取配置
     */
    @GetMapping("/config/get")
    public ResponseEntity<Map<String, Object>> getConfig(@RequestParam String key,
                                                          HttpSession session) {
        try {
            Object value = session.getAttribute(key);
            Map<String, Object> result = new HashMap<>();
            result.put("status", 0);
            result.put("data", value);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Get config error", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", -1);
            result.put("msg", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
