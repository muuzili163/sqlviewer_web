# Nginx 反向代理部署指南

本文档详细说明如何使用Nginx反向代理部署MySQL数据查看器，以绕过浏览器的CORS限制。

## 为什么需要Nginx反向代理？

浏览器的同源策略（CORS）阻止从 `localhost` 或其他域访问 `https://sql-out.sdcreditech.com` 的API。通过Nginx反向代理，可以：
- 将前端和API请求统一到同一域下
- 避免CORS跨域问题
- 提供更好的安全性和性能

## 部署架构

```
浏览器
  ↓
Nginx (http://your-domain.com)
  ├─→ /             → 静态文件 (index.html, app.js, styles.css)
  └─→ /api/         → 反向代理到 https://sql-out.sdcreditech.com/
```

## 完整配置步骤

### 步骤 1: 安装 Nginx

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install nginx
```

#### CentOS/RHEL
```bash
sudo yum install nginx
```

#### macOS
```bash
brew install nginx
```

### 步骤 2: 准备静态文件

将项目文件部署到服务器：

```bash
# 创建部署目录
sudo mkdir -p /var/www/sqlviewer

# 复制文件到部署目录
sudo cp index.html /var/www/sqlviewer/
sudo cp app.js /var/www/sqlviewer/
sudo cp styles.css /var/www/sqlviewer/
sudo cp demo.html /var/www/sqlviewer/  # 可选

# 设置权限
sudo chown -R www-data:www-data /var/www/sqlviewer
sudo chmod -R 755 /var/www/sqlviewer
```

### 步骤 3: 修改 app.js 配置

编辑 `/var/www/sqlviewer/app.js`，修改 `baseUrl`：

```javascript
const config = {
    baseUrl: '/api',  // 改为相对路径，指向Nginx代理
    csrftoken: '0w8mYnqK82gNrkNmgs9CIn3UaaHpmaxY',
    sessionid: '',
    username: '',
    instanceName: '',
    pageSize: 100
};
```

### 步骤 4: 配置 Nginx

创建Nginx配置文件：

```bash
sudo nano /etc/nginx/sites-available/sqlviewer
```

**完整配置示例：**

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为您的域名或IP
    
    # 静态文件根目录
    root /var/www/sqlviewer;
    index index.html;
    
    # 启用gzip压缩
    gzip on;
    gzip_types text/css application/javascript application/json;
    
    # 静态文件缓存
    location ~* \.(css|js|jpg|jpeg|png|gif|ico|svg)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
    
    # 主页面
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API 反向代理配置
    location /api/ {
        # 重写路径，去掉 /api 前缀
        rewrite ^/api/(.*) /$1 break;
        
        # 代理到后端API服务器
        proxy_pass https://sql-out.sdcreditech.com;
        
        # 转发原始Host头
        proxy_set_header Host sql-out.sdcreditech.com;
        
        # 转发客户端真实IP
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 转发自定义请求头
        proxy_set_header x-csrftoken $http_x_csrftoken;
        proxy_set_header User-Agent $http_user_agent;
        
        # Cookie 处理
        proxy_cookie_domain sql-out.sdcreditech.com $host;
        proxy_cookie_path / /api/;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # 缓冲设置
        proxy_buffering off;
        proxy_request_buffering off;
        
        # SSL 验证（如果后端是HTTPS）
        proxy_ssl_verify off;
        proxy_ssl_server_name on;
    }
    
    # 日志配置
    access_log /var/log/nginx/sqlviewer_access.log;
    error_log /var/log/nginx/sqlviewer_error.log;
}
```

### 步骤 5: 启用配置

```bash
# 创建符号链接启用站点
sudo ln -s /etc/nginx/sites-available/sqlviewer /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 如果测试通过，重启Nginx
sudo systemctl restart nginx
```

### 步骤 6: 配置防火墙（如需要）

```bash
# Ubuntu UFW
sudo ufw allow 'Nginx HTTP'

# CentOS/RHEL firewalld
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --reload
```

### 步骤 7: 访问应用

打开浏览器访问：
```
http://your-domain.com
```

使用测试账号登录：
- 账号：`liweihan`
- 密码：`li13625306340`

## HTTPS 配置（推荐）

生产环境建议配置HTTPS：

### 使用 Let's Encrypt 免费证书

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx  # Ubuntu/Debian
# 或
sudo yum install certbot python3-certbot-nginx  # CentOS/RHEL

# 自动配置 HTTPS
sudo certbot --nginx -d your-domain.com
```

Certbot会自动修改Nginx配置，添加SSL证书并设置重定向。

### 手动 HTTPS 配置

如果有自己的SSL证书：

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    # SSL 证书配置
    ssl_certificate /path/to/your/certificate.crt;
    ssl_certificate_key /path/to/your/private.key;
    
    # SSL 安全配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    # ... 其他配置同上 ...
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

## 故障排查

### 1. 502 Bad Gateway

**可能原因**：无法连接到后端API服务器

**解决方法**：
```bash
# 检查后端API是否可访问
curl -I https://sql-out.sdcreditech.com

# 检查 Nginx 错误日志
sudo tail -f /var/log/nginx/sqlviewer_error.log
```

### 2. Cookie 不工作

**解决方法**：确保 `proxy_cookie_domain` 和 `proxy_cookie_path` 配置正确

```nginx
# 在 location /api/ 中添加或修改
proxy_cookie_domain sql-out.sdcreditech.com $host;
proxy_cookie_path / /;
```

### 3. CORS 错误依然存在

**检查**：
- 确认 `app.js` 中 `baseUrl` 已修改为 `/api`
- 清除浏览器缓存
- 检查浏览器开发者工具的Network面板，确认请求是发送到 `/api/` 而非原域名

### 4. 静态文件 404

**解决方法**：
```bash
# 检查文件权限
ls -la /var/www/sqlviewer

# 检查 Nginx 配置的 root 路径
sudo nginx -T | grep root
```

## 性能优化建议

### 1. 启用缓存

```nginx
# 在 http 块中添加
proxy_cache_path /var/cache/nginx/sqlviewer levels=1:2 keys_zone=api_cache:10m max_size=1g inactive=60m;

# 在 location /api/ 中添加
proxy_cache api_cache;
proxy_cache_valid 200 5m;
proxy_cache_key "$scheme$request_method$host$request_uri";
add_header X-Cache-Status $upstream_cache_status;
```

### 2. 启用 HTTP/2

```nginx
listen 443 ssl http2;
```

### 3. 配置连接池

```nginx
upstream api_backend {
    server sql-out.sdcreditech.com:443;
    keepalive 32;
}

location /api/ {
    proxy_pass https://api_backend;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    # ... 其他配置 ...
}
```

## Docker 部署（可选）

如果使用Docker部署：

**Dockerfile:**
```dockerfile
FROM nginx:alpine

# 复制静态文件
COPY index.html /usr/share/nginx/html/
COPY app.js /usr/share/nginx/html/
COPY styles.css /usr/share/nginx/html/

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

**docker-compose.yml:**
```yaml
version: '3'
services:
  sqlviewer:
    build: .
    ports:
      - "80:80"
    restart: unless-stopped
```

**运行:**
```bash
docker-compose up -d
```

## 安全建议

1. **限制访问IP（如需要）**
   ```nginx
   location / {
       allow 192.168.1.0/24;  # 允许内网访问
       deny all;
   }
   ```

2. **添加基本认证（双重保护）**
   ```nginx
   location / {
       auth_basic "Restricted Access";
       auth_basic_user_file /etc/nginx/.htpasswd;
   }
   ```

3. **限制请求速率**
   ```nginx
   limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
   
   location /api/ {
       limit_req zone=api_limit burst=20;
       # ... 其他配置 ...
   }
   ```

## 总结

通过以上配置，您可以成功部署MySQL数据查看器并绕过CORS限制。关键步骤：
1. ✅ 安装并配置Nginx
2. ✅ 修改 `app.js` 中的 `baseUrl` 为 `/api`
3. ✅ 配置反向代理转发到 `https://sql-out.sdcreditech.com`
4. ✅ 正确处理Cookie和请求头
5. ✅ （推荐）配置HTTPS证书

如有问题，请查看Nginx日志：
```bash
sudo tail -f /var/log/nginx/sqlviewer_error.log
sudo tail -f /var/log/nginx/sqlviewer_access.log
```
