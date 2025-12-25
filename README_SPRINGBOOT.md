# Spring Boot版本运行指南

## 项目说明

本项目已从纯HTML版本升级为Spring Boot项目，使用Java后端转发API请求，解决了浏览器CORS和CSRF限制问题。

## 项目结构

```
sqlviewer-web/
├── pom.xml                                    # Maven配置文件
├── src/
│   └── main/
│       ├── java/com/sqlviewer/
│       │   ├── SqlViewerApplication.java      # Spring Boot启动类
│       │   ├── controller/
│       │   │   ├── PageController.java         # 页面控制器
│       │   │   └── ApiController.java          # API转发控制器
│       │   ├── service/
│       │   │   └── ApiForwardService.java      # API转发服务
│       │   └── util/
│       │       └── HttpClientUtil.java         # HTTP工具类
│       └── resources/
│           ├── application.properties          # 应用配置
│           ├── templates/                      # HTML模板
│           │   ├── index.html
│           │   └── demo.html
│           └── static/                         # 静态资源
│               ├── app.js                      # 前端JavaScript
│               └── styles.css                  # CSS样式
└── README_SPRINGBOOT.md                        # 本文档
```

## 系统要求

- **JDK**: 1.8 或更高版本
- **Maven**: 3.6 或更高版本（可选，可使用IDE内置Maven）

## 运行步骤

### 方式1: 使用Maven命令行

#### 1. 编译打包

```bash
cd /path/to/sqlviewer_web
mvn clean package
```

#### 2. 运行JAR包

```bash
java -jar target/sqlviewer-web-1.0.0.jar
```

#### 3. 访问应用

打开浏览器访问：
```
http://localhost:8080
```

### 方式2: 使用Maven直接运行

```bash
cd /path/to/sqlviewer_web
mvn spring-boot:run
```

### 方式3: 使用IDE运行

#### IntelliJ IDEA
1. 打开项目：File -> Open -> 选择项目目录
2. 等待Maven自动下载依赖
3. 找到 `SqlViewerApplication.java`
4. 右键 -> Run 'SqlViewerApplication'

#### Eclipse
1. 导入项目：File -> Import -> Existing Maven Projects
2. 等待Maven自动下载依赖
3. 找到 `SqlViewerApplication.java`
4. 右键 -> Run As -> Java Application

## 配置说明

配置文件位于 `src/main/resources/application.properties`

```properties
# 服务器端口
server.port=8080

# 后端API服务器地址
api.base.url=https://sql-out.sdcreditech.com
```

### 修改端口

如果8080端口被占用，可以修改为其他端口：
```properties
server.port=9090
```

### 修改后端API地址

如果后端API地址变更，修改此配置：
```properties
api.base.url=https://your-api-server.com
```

## 打包为可执行JAR

### 1. 打包

```bash
mvn clean package
```

生成的JAR文件位于：`target/sqlviewer-web-1.0.0.jar`

### 2. 部署运行

将JAR文件复制到服务器，运行：

```bash
# 前台运行
java -jar sqlviewer-web-1.0.0.jar

# 后台运行
nohup java -jar sqlviewer-web-1.0.0.jar > app.log 2>&1 &

# 指定端口运行
java -jar sqlviewer-web-1.0.0.jar --server.port=9090
```

### 3. 停止应用

```bash
# 查找进程
ps aux | grep sqlviewer-web

# 停止进程
kill <PID>
```

## 登录测试

使用以下测试账号登录：
- 账号：`liweihan`
- 密码：`li13625306340`

## 功能特性

✅ 所有功能与HTML版本一致：
- 用户登录认证
- 服务器实例选择
- 数据库和表的树形导航
- 多标签页支持
- 表结构查看
- 数据查询和展示
- WHERE条件查询
- 列排序功能
- 分页大小设置
- 总数计算
- SQL语句复制

✅ 新增优势：
- **无CORS限制**：通过Java后端转发请求
- **无CSRF限制**：Session由Spring Boot管理
- **更好的安全性**：凭据不暴露在前端
- **易于部署**：打包为单个JAR文件
- **跨平台**：只需JDK即可运行

## 技术栈

- **后端**: Spring Boot 2.7.18
- **前端**: HTML5 + CSS3 + JavaScript (ES6+)
- **HTTP客户端**: Apache HttpClient 4.5.14
- **模板引擎**: Thymeleaf
- **构建工具**: Maven 3

## 日志查看

应用运行日志会输出到控制台，包含：
- API请求日志
- 错误信息
- 调试信息

日志级别配置：
```properties
logging.level.root=INFO
logging.level.com.sqlviewer=DEBUG
```

## 常见问题

### 1. 端口被占用

错误信息：`Port 8080 was already in use`

解决方法：
- 修改 `application.properties` 中的 `server.port`
- 或在启动时指定端口：`java -jar app.jar --server.port=9090`

### 2. 连接后端API失败

错误信息：`Connection refused` 或 `SSLException`

解决方法：
- 检查后端API地址配置是否正确
- 确认网络连接正常
- 检查防火墙设置

### 3. Maven依赖下载失败

解决方法：
- 配置Maven镜像（如阿里云Maven仓库）
- 检查网络连接
- 清理Maven缓存：`mvn clean`

## 开发说明

### 添加新的API转发

1. 在 `ApiForwardService.java` 中添加新方法
2. 在 `ApiController.java` 中添加对应的Controller方法
3. 在前端 `app.js` 的 `api` 对象中添加调用方法

### 修改前端代码

前端代码位于：
- JavaScript: `src/main/resources/static/app.js`
- CSS: `src/main/resources/static/styles.css`
- HTML: `src/main/resources/templates/index.html`

修改后重新打包或重启应用即可生效。

## 性能优化建议

1. **启用Gzip压缩**
```properties
server.compression.enabled=true
server.compression.mime-types=text/html,text/css,application/javascript
```

2. **调整JVM参数**
```bash
java -Xms512m -Xmx1024m -jar sqlviewer-web-1.0.0.jar
```

3. **使用反向代理**
在生产环境建议使用Nginx作为反向代理。

## 生产部署建议

1. 使用systemd管理服务
2. 配置日志持久化
3. 启用HTTPS
4. 设置合理的JVM参数
5. 配置Nginx反向代理
6. 启用健康检查

## 支持与反馈

如遇到问题，请查看应用日志或提交Issue。
