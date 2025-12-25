# MySQL 数据查看器 (HTML版)

这是一个基于Web的MySQL数据查看器，重构自C#版本的SQLViewer。它提供了现代化的用户界面，用于查看和分析MySQL数据库数据。

## 功能特性

### 核心功能 (与C#版一致)
- ✅ 用户登录认证
- ✅ 服务器实例选择
- ✅ 数据库和表的树形导航
- ✅ 多标签页支持（可打开多个表）
- ✅ 表结构查看
- ✅ 数据查询和展示
- ✅ 自定义WHERE条件查询
- ✅ 列排序功能（点击列头）
- ✅ 分页大小设置（50/100/200/500/1000）
- ✅ 总数计算
- ✅ SQL语句复制
- ✅ 查询时间和结果条数显示

### 界面特色
- 🎨 现代化扁平设计
- 🌈 渐变色彩搭配
- 📱 响应式布局
- ⚡ 流畅的动画效果
- 🎯 直观的操作体验

## 技术栈

- **HTML5**: 页面结构
- **CSS3**: 现代样式设计（Flexbox, Grid, 动画等）
- **JavaScript (ES6+)**: 业务逻辑实现
- **Fetch API**: HTTP请求处理
- **LocalStorage**: 本地配置存储

## 文件结构

```
sqlviewer_web/
├── index.html          # 主页面
├── styles.css          # 样式表
├── app.js              # 应用逻辑
├── README.md           # 说明文档
└── SQLViewer.zip       # C#版源代码（参考）
```

## 使用方法

### 1. 直接打开
在浏览器中打开 `index.html` 文件即可使用。

### 2. 使用本地服务器（推荐）
```bash
# 使用 Python
python -m http.server 8000

# 使用 Node.js
npx http-server

# 使用 PHP
php -S localhost:8000
```

然后在浏览器访问: `http://localhost:8000`

### 3. 登录
1. 输入账号和密码
2. 点击"登录"按钮
3. 登录成功后会自动跳转到主界面

### 4. 查看数据
1. 选择服务器实例
2. 在左侧树形菜单中展开数据库
3. 双击表名打开新标签页
4. 在标签页中可以：
   - 查看表结构
   - 输入WHERE条件查询
   - 点击列头排序
   - 计算总数
   - 复制SQL语句

## 界面说明

### 顶部工具栏
- **服务器选择**: 选择要连接的MySQL服务器实例
- **分页大小**: 设置每次查询返回的最大记录数
- **用户按钮**: 显示当前登录用户，点击可重新登录

### 左侧导航栏
- 展示所有数据库的树形结构
- 点击数据库名展开查看表列表
- 双击表名打开数据查看标签页

### 主内容区
- **标签页**: 支持同时打开多个表，点击"×"关闭标签
- **查询面板**: 
  - 表结构按钮：显示/隐藏CREATE TABLE语句
  - 条件输入框：输入WHERE子句条件
  - 查询按钮：执行查询
  - 清空排序：重置排序状态
  - 复制SQL：复制当前执行的SQL语句
  - 计算总数：统计表的总记录数
- **数据表格**: 显示查询结果，点击列头可排序

## 设计特点

### 1. 现代化UI设计
- 使用渐变色彩和阴影效果
- 圆角边框和柔和的过渡动画
- 清晰的视觉层次

### 2. 用户体验优化
- 加载状态提示
- 错误信息友好展示
- 操作响应流畅
- 数据自动缓存

### 3. 功能完整性
- 完全保留C#版的所有核心功能
- 支持多标签工作流
- 灵活的查询和排序能力

## 浏览器兼容性

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## 注意事项

1. **CORS限制**: 
   - ⚠️ 由于浏览器的同源策略（CORS），从本地文件或localhost访问远程API会被浏览器阻止
   - **推荐部署方式**: 将此HTML应用部署到与API服务器同域的Web服务器上，或配置API服务器允许跨域请求
   - **替代方案**: 使用浏览器扩展（如CORS Unblock）临时禁用CORS检查进行测试（仅用于开发测试）
   - 本地测试时可查看 `demo.html` 了解界面效果

2. **Cookie和Session**: 应用使用Cookie进行身份认证，请确保浏览器允许Cookie。

3. **数据安全**: 本应用连接到 `https://sql-out.sdcreditech.com`，请确保在安全的网络环境下使用。

4. **本地存储**: 应用使用LocalStorage存储用户配置（服务器选择、分页大小等），不会存储敏感信息。

## 部署建议

为了避免CORS问题，推荐以下部署方式：

### 方式1: 部署到同域服务器
将HTML文件部署到与API服务器 `sql-out.sdcreditech.com` 相同域名下的某个路径。

### 方式2: 配置Nginx反向代理（推荐）

使用Nginx反向代理，将API请求转发到后端服务器，避免CORS问题。

**📖 查看完整配置指南**: [NGINX_DEPLOYMENT.md](./NGINX_DEPLOYMENT.md)

简要步骤：
1. 安装Nginx
2. 部署静态文件到 `/var/www/sqlviewer`
3. 修改 `app.js` 中的 `baseUrl` 为 `/api`
4. 配置Nginx反向代理（参见详细文档）
5. 访问 `http://your-domain.com`

快速配置示例：
```nginx
location /api/ {
    rewrite ^/api/(.*) /$1 break;
    proxy_pass https://sql-out.sdcreditech.com;
    proxy_set_header Host sql-out.sdcreditech.com;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header x-csrftoken $http_x_csrftoken;
    proxy_cookie_domain sql-out.sdcreditech.com $host;
}
```

### 方式3: API服务器配置CORS
在API服务器端添加CORS响应头：
```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Content-Type, x-csrftoken
Access-Control-Allow-Credentials: true
```

## 与C#版本的对比

| 功能 | C#版 | HTML版 |
|------|------|--------|
| 登录认证 | ✅ | ✅ |
| 服务器选择 | ✅ | ✅ |
| 数据库导航 | ✅ | ✅ |
| 多标签页 | ✅ | ✅ |
| 表结构查看 | ✅ | ✅ |
| 数据查询 | ✅ | ✅ |
| 列排序 | ✅ | ✅ |
| 条件过滤 | ✅ | ✅ |
| 总数计算 | ✅ | ✅ |
| SQL复制 | ✅ | ✅ |
| 跨平台 | ❌ | ✅ |
| 无需安装 | ❌ | ✅ |
| 现代化UI | 普通 | ⭐️ |

## 开发说明

### 代码结构

**app.js** 主要模块：
- `config`: 应用配置
- `storage`: 本地存储工具
- `http`: HTTP请求工具
- `api`: API调用封装
- `UIController`: UI控制器（主要业务逻辑）

## 自定义配置

修改 `app.js` 中的 `config` 对象：

```javascript
const config = {
    baseUrl: 'https://sql-out.sdcreditech.com',  // API服务器地址
    csrftoken: '0w8mYnqK82gNrkNmgs9CIn3UaaHpmaxY',  // 初始CSRF令牌（登录后会更新）
    sessionid: '',      // 会话ID（登录后自动设置）
    username: '',       // 用户名（登录后自动设置）
    instanceName: '',   // 当前选择的实例
    pageSize: 100       // 默认分页大小
};
```

**配置说明**：
- `baseUrl`: API服务器地址，根据实际部署环境修改
- `csrftoken`: 初始CSRF令牌，登录时会从服务器响应中获取新的token
- `pageSize`: 默认每页显示的记录数，可在页面上通过下拉框修改

## 测试账号

可使用以下测试账号（由用户提供）：
- 账号：`liweihan`
- 密码：`li13625306340`

**注意**：需要在正确的环境下部署（解决CORS问题）才能成功登录。

## 许可证

本项目基于C#版SQLViewer重构，仅供学习和内部使用。

## 更新日志

### v1.0.0 (2025-12-25)
- ✨ 初始版本发布
- 🎨 现代化UI设计
- ⚡️ 完整功能实现
- 📱 响应式布局支持
