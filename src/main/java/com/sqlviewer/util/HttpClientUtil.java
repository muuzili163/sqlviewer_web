package com.sqlviewer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP请求工具类
 * 用于转发请求到后端API服务器
 */
@Component
public class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int TIMEOUT = 60000; // 60秒超时

    /**
     * 发送GET请求
     */
    public static String doGet(String url, Map<String, String> headers) throws IOException {
        CloseableHttpClient httpClient = createHttpClient();
        HttpGet httpGet = new HttpGet(url);
        
        // 设置请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }
        }
        
        return executeRequest(httpClient, httpGet);
    }

    /**
     * 发送POST请求
     */
    public static String doPost(String url, Map<String, String> headers, String body) throws IOException {
        CloseableHttpClient httpClient = createHttpClient();
        HttpPost httpPost = new HttpPost(url);
        
        // 设置请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        
        // 设置请求体
        if (body != null && !body.isEmpty()) {
            StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
        }
        
        return executeRequest(httpClient, httpPost);
    }

    /**
     * 执行HTTP请求
     */
    private static String executeRequest(CloseableHttpClient httpClient, HttpRequestBase request) throws IOException {
        CloseableHttpResponse response = null;
        try {
            // 设置超时
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(TIMEOUT)
                    .setSocketTimeout(TIMEOUT)
                    .setConnectionRequestTimeout(TIMEOUT)
                    .build();
            request.setConfig(requestConfig);
            
            logger.debug("Sending request to: {}", request.getURI());
            
            response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            
            if (entity != null) {
                String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                logger.debug("Response: {}", result);
                return result;
            }
            return null;
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    logger.error("Error closing response", e);
                }
            }
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.error("Error closing http client", e);
                }
            }
        }
    }

    /**
     * 创建HTTP客户端（支持HTTPS，忽略证书验证）
     */
    private static CloseableHttpClient createHttpClient() {
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true)
                    .build();
            
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslContext,
                    NoopHostnameVerifier.INSTANCE
            );
            
            return HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build();
        } catch (Exception e) {
            logger.error("Error creating HTTP client", e);
            return HttpClients.createDefault();
        }
    }

    /**
     * 将JSON字符串转换为JsonNode
     */
    public static JsonNode parseJson(String json) throws IOException {
        return objectMapper.readTree(json);
    }
}
