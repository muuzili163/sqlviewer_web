package com.sqlviewer.service;

import com.sqlviewer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * API转发服务
 * 负责将前端请求转发到后端API服务器
 */
@Service
public class ApiForwardService {

    private static final Logger logger = LoggerFactory.getLogger(ApiForwardService.class);

    @Value("${api.base.url}")
    private String apiBaseUrl;

    /**
     * 登录认证
     */
    public String login(String username, String password, HttpSession session) throws IOException {
        String url = apiBaseUrl + "/authenticate/";
        
        // 获取或初始化csrftoken
        String csrftoken = (String) session.getAttribute("csrftoken");
        if (csrftoken == null || csrftoken.isEmpty()) {
            csrftoken = "0w8mYnqK82gNrkNmgs9CIn3UaaHpmaxY";
        }
        
        // 构建请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("x-csrftoken", csrftoken);
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        headers.put("Cookie", "csrftoken=" + csrftoken);
        
        // 构建请求体
        String body = "username=" + URLEncoder.encode(username, "UTF-8") +
                     "&password=" + URLEncoder.encode(password, "UTF-8");
        
        String response = HttpClientUtil.doPost(url, headers, body);
        
        // 保存session信息（这里简化处理，实际应该从响应头中提取）
        session.setAttribute("username", username);
        session.setAttribute("csrftoken", csrftoken);
        
        return response;
    }

    /**
     * 查询服务器列表
     */
    public String queryServers(HttpSession session) throws IOException {
        String csrftoken = getSessionAttribute(session, "csrftoken");
        String sessionid = getSessionAttribute(session, "sessionid");
        
        String url = apiBaseUrl + "/group/user_all_instances/?tag_codes%5B%5D=can_read";
        
        Map<String, String> headers = buildHeaders(csrftoken, sessionid);
        return HttpClientUtil.doGet(url, headers);
    }

    /**
     * 查询数据库列表
     */
    public String queryDatabases(String instanceName, HttpSession session) throws IOException {
        String csrftoken = getSessionAttribute(session, "csrftoken");
        String sessionid = getSessionAttribute(session, "sessionid");
        
        String url = apiBaseUrl + "/instance/instance_resource/?instance_name=" + 
                    URLEncoder.encode(instanceName, "UTF-8") + "&resource_type=database";
        
        Map<String, String> headers = buildHeaders(csrftoken, sessionid);
        return HttpClientUtil.doGet(url, headers);
    }

    /**
     * 查询表列表
     */
    public String queryTables(String instanceName, String dbName, HttpSession session) throws IOException {
        String csrftoken = getSessionAttribute(session, "csrftoken");
        String sessionid = getSessionAttribute(session, "sessionid");
        
        String url = apiBaseUrl + "/instance/instance_resource/?instance_name=" + 
                    URLEncoder.encode(instanceName, "UTF-8") + 
                    "&db_name=" + URLEncoder.encode(dbName, "UTF-8") + 
                    "&resource_type=table";
        
        Map<String, String> headers = buildHeaders(csrftoken, sessionid);
        return HttpClientUtil.doGet(url, headers);
    }

    /**
     * 查询表结构
     */
    public String showTableStructure(String instanceName, String dbName, String tableName, HttpSession session) throws IOException {
        String csrftoken = getSessionAttribute(session, "csrftoken");
        String sessionid = getSessionAttribute(session, "sessionid");
        
        String url = apiBaseUrl + "/instance/describetable/";
        
        Map<String, String> headers = buildHeaders(csrftoken, sessionid);
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        
        String body = "instance_name=" + URLEncoder.encode(instanceName, "UTF-8") +
                     "&db_name=" + URLEncoder.encode(dbName, "UTF-8") +
                     "&schema_name=" +
                     "&tb_name=" + URLEncoder.encode(tableName, "UTF-8");
        
        return HttpClientUtil.doPost(url, headers, body);
    }

    /**
     * 执行SQL查询
     */
    public String querySql(String instanceName, String dbName, String tableName, 
                          String sqlContent, String limitNum, HttpSession session) throws IOException {
        String csrftoken = getSessionAttribute(session, "csrftoken");
        String sessionid = getSessionAttribute(session, "sessionid");
        
        String url = apiBaseUrl + "/query/";
        
        Map<String, String> headers = buildHeaders(csrftoken, sessionid);
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        
        String body = "instance_name=" + URLEncoder.encode(instanceName, "UTF-8") +
                     "&db_name=" + URLEncoder.encode(dbName, "UTF-8") +
                     "&schema_name=" +
                     "&tb_name=" + URLEncoder.encode(tableName, "UTF-8") +
                     "&sql_content=" + URLEncoder.encode(sqlContent, "UTF-8") +
                     "&limit_num=" + limitNum;
        
        return HttpClientUtil.doPost(url, headers, body);
    }

    /**
     * 构建请求头
     */
    private Map<String, String> buildHeaders(String csrftoken, String sessionid) {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-csrftoken", csrftoken);
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        
        StringBuilder cookie = new StringBuilder();
        if (csrftoken != null && !csrftoken.isEmpty()) {
            cookie.append("csrftoken=").append(csrftoken);
        }
        if (sessionid != null && !sessionid.isEmpty()) {
            if (cookie.length() > 0) cookie.append("; ");
            cookie.append("sessionid=").append(sessionid);
        }
        if (cookie.length() > 0) {
            headers.put("Cookie", cookie.toString());
        }
        
        return headers;
    }

    /**
     * 获取Session属性
     */
    private String getSessionAttribute(HttpSession session, String key) {
        Object value = session.getAttribute(key);
        return value != null ? value.toString() : "";
    }
}
