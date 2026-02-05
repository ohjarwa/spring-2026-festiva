# 环境变量配置说明

## 🔐 敏感信息配置

本项目使用环境变量来管理敏感配置信息（如OSS访问凭证），**请勿将真实凭证提交到Git仓库**。

---

## 📋 需要配置的环境变量

### 阿里云OSS凭证

#### Default账户
```bash
export OSS_DEFAULT_ACCESS_KEY_ID="your_access_key_id_here"
export OSS_DEFAULT_ACCESS_KEY_SECRET="your_access_key_secret_here"
```

#### CV账户
```bash
export OSS_CV_ACCESS_KEY_ID="your_cv_access_key_id_here"
export OSS_CV_ACCESS_KEY_SECRET="your_cv_access_key_secret_here"
```

---

## 🛠️ 配置方式

### 方式1：IDEA环境变量配置（开发环境推荐）

#### IntelliJ IDEA / GoLand

1. **Run → Edit Configurations**
2. 选择您的Spring Boot启动配置
3. **Environment variables** 字段点击 **📝** 按钮
4. 添加以下环境变量：
   ```
   OSS_DEFAULT_ACCESS_KEY_ID=your_access_key_id_here
   OSS_DEFAULT_ACCESS_KEY_SECRET=3ZqOx0X6NlJnVJfSqEtj2cFQLy2VfJ
   OSS_CV_ACCESS_KEY_ID=your_access_key_id_here
   OSS_CV_ACCESS_KEY_SECRET=o48HH33M5YRPa2zT908mrnSgTstkJK
   ```

#### VS Code

在 `.vscode/launch.json` 中配置：
```json
{
  "env": {
    "OSS_DEFAULT_ACCESS_KEY_ID": "your_access_key_id_here",
    "OSS_DEFAULT_ACCESS_KEY_SECRET": "3ZqOx0X6NlJnVJfSqEtj2cFQLy2VfJ",
    "OSS_CV_ACCESS_KEY_ID": "your_access_key_id_here",
    "OSS_CV_ACCESS_KEY_SECRET": "o48HH33M5YRPa2zT908mrnSgTstkJK"
  }
}
```

### 方式2：系统环境变量

#### macOS / Linux

在 `~/.bashrc` 或 `~/.zshrc` 中添加：
```bash
# 阿里云OSS凭证
export OSS_DEFAULT_ACCESS_KEY_ID="your_access_key_id_here"
export OSS_DEFAULT_ACCESS_KEY_SECRET="3ZqOx0X6NlJnVJfSqEtj2cFQLy2VfJ"
export OSS_CV_ACCESS_KEY_ID="your_access_key_id_here"
export OSS_CV_ACCESS_KEY_SECRET="o48HH33M5YRPa2zT908mrnSgTstkJK"
```

然后执行：
```bash
source ~/.bashrc  # 或 source ~/.zshrc
```

#### Windows (PowerShell)

在 PowerShell 配置文件中添加：
```powershell
$env:OSS_DEFAULT_ACCESS_KEY_ID="your_access_key_id_here"
$env:OSS_DEFAULT_ACCESS_KEY_SECRET="3ZqOx0X6NlJnVJfSqEtj2cFQLy2VfJ"
$env:OSS_CV_ACCESS_KEY_ID="your_access_key_id_here"
$env:OSS_CV_ACCESS_KEY_SECRET="o48HH33M5YRPa2zT908mrnSgTstkJK"
```

或者设置系统环境变量：
1. 右键 **此电脑** → **属性**
2. **高级系统设置** → **环境变量**
3. 添加上述环境变量

### 方式3：使用 .env 文件（需要配置）

#### 1. 添加依赖（pom.xml）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
```

#### 2. 创建 .env 文件

在项目根目录创建 `.env` 文件：
```properties
# 阿里云OSS凭证
OSS_DEFAULT_ACCESS_KEY_ID=your_access_key_id_here
OSS_DEFAULT_ACCESS_KEY_SECRET=3ZqOx0X6NlJnVJfSqEtj2cFQLy2VfJ
OSS_CV_ACCESS_KEY_ID=your_access_key_id_here
OSS_CV_ACCESS_KEY_SECRET=o48HH33M5YRPa2zT908mrnSgTstkJK
```

#### 3. 添加到 .gitignore

```
.env
```

### 方式4：使用JVM参数

启动时添加：
```bash
java -jar app.jar \
  -DOSS_DEFAULT_ACCESS_KEY_ID=your_access_key_id_here \
  -DOSS_DEFAULT_ACCESS_KEY_SECRET=3ZqOx0X6NlJnVJfSqEtj2cFQLy2VfJ \
  -DOSS_CV_ACCESS_KEY_ID=your_access_key_id_here \
  -DOSS_CV_ACCESS_KEY_SECRET=o48HH33M5YRPa2zT908mrnSgTstkJK
```

---

## ✅ 验证配置

启动应用后，检查日志输出：

```
=== 初始化 OSS 客户端 ===
初始化 OSS 账号: default
  Endpoint: oss-cn-hangzhou.aliyuncs.com
  Bucket: ths-newyear-2026
  状态: ✅ 初始化成功
初始化 OSS 账号: cv
  Endpoint: oss-cn-hangzhou.aliyuncs.com
  Bucket: cv-springfestval-2026
  状态: ✅ 初始化成功
=== OSS 客户端初始化完成，共 2 个账号 ===
```

如果看到 **"✅ 初始化成功"**，说明配置正确。

---

## 🚫 安全注意事项

### ❌ 不要做

1. **不要**将真实的 `access-key-id` 和 `access-key-secret` 提交到Git
2. **不要**在代码中硬编码凭证信息
3. **不要**在公开的文档或注释中泄露凭证
4. **不要**在生产环境使用默认凭证

### ✅ 应该做

1. ✅ 使用环境变量管理敏感信息
2. ✅ 将 `.env` 文件添加到 `.gitignore`
3. ✅ 定期轮换（更换）访问密钥
4. ✅ 为不同环境使用不同的OSS账户
5. ✅ 使用RAM子账号（而不是主账号）
6. ✅ 设置最小权限原则

---

## 🔐 RAM子账号最佳实践

### 1. 创建RAM子账号

1. 登录阿里云控制台
2. 访问 **RAM访问控制** → **用户**
3. 创建新用户（如：spring-2026-app）
4. **不要**启用控制台登录（只需编程访问）

### 2. 授予权限

为用户添加OSS权限策略：
- **AliyunOSSFullAccess** - 完整权限（测试环境）
- 或自定义权限（生产环境，只读/只写特定Bucket）

### 3. 生成AccessKey

1. 创建用户后，点击用户名
2. **创建AccessKey**
3. **保存** AccessKey ID 和 AccessKey Secret（只显示一次！）

### 4. 使用子账号凭证

```bash
export OSS_DEFAULT_ACCESS_KEY_ID="your_access_key_id_here"  # 子账号
export OSS_DEFAULT_ACCESS_KEY_SECRET="YYYYYYYYYYYY"      # 子账号
```

---

## 📝 其他敏感配置

除了OSS凭证，以下配置也应该使用环境变量：

### 数据库密码
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}
```

### Redis密码
```yaml
spring:
  data:
    redis:
      password: ${REDIS_PASSWORD}
```

### 算法服务密钥
```yaml
algorithm:
  face-swap:
    api-key: ${FACE_SWAP_API_KEY}
  lip-sync:
    api-key: ${LIP_SYNC_API_KEY}
```

---

## 🎯 快速配置检查清单

- [ ] 环境变量已配置
- [ ] `.gitignore` 包含 `.env`
- [ ] application.yml 已脱敏
- [ ] 启动应用验证初始化日志
- [ ] 测试OSS上传功能
- [ ] 代码中没有硬编码凭证

---

**更新时间：** 2026-02-05
**配置方式：** 环境变量
**安全级别：** 🔒 高安全
