# 提交前检查清单

## ✅ 敏感信息脱敏完成

### 已完成的更改

1. ✅ **application.yml 脱敏**
   - access-key-id → ${OSS_DEFAULT_ACCESS_KEY_ID}
   - access-key-secret → ${OSS_DEFAULT_ACCESS_KEY_SECRET}
   - cv账户凭证 → ${OSS_CV_ACCESS_KEY_ID} 和 ${OSS_CV_ACCESS_KEY_SECRET}

2. ✅ **.gitignore 更新**
   - 添加了 `.env` 和 `.env.local`
   - 添加了 `application-local.yml` 和 `application-local.properties`
   - 添加了 `output/`、`logs/` 等临时目录

3. ✅ **配置文档**
   - `ENV_CONFIGURATION.md` - 详细的环境变量配置说明
   - `application-local.yml.example` - 本地配置示例

---

## 📋 提交前检查清单

### 1. 检查敏感信息

```bash
# 搜索是否有硬编码的凭证
grep -r "access-key-id" src/ --include="*.java" --include="*.yml" --include="*.properties"
grep -r "access-key-secret" src/ --include="*.java" --include="*.yml" --include="*.properties"
grep -r "LTAI5" src/ --include="*.java" --include="*.yml" --include="*.properties"
```

**预期结果：** 只在 application.yml 中找到 `${OSS_XXX_ACCESS_KEY_ID}` 和 `${OSS_XXX_ACCESS_KEY_SECRET}`

### 2. 检查 .gitignore

```bash
# 确认敏感文件已被忽略
cat .gitignore | grep -E "(\.env|application-local)"
```

**应该包含：**
- `.env`
- `.env.local`
- `.env.*.local`
- `application-local.yml`
- `application-local.properties`

### 3. 检查当前状态

```bash
git status
```

**预期输出：**
```
On branch dev-zjy
Changes to be committed:
  modified:   src/main/resources/application.yml
  modified:   src/main/java/org/example/newyear/util/VideoProcessorUtil.java
  modified:   src/main/java/org/example/newyear/service/Template1to4Processor.java
  ...
Untracked files:
  ENV_CONFIGURATION.md
  FINAL_VIDEO_TO_CV_ACCOUNT.md
  application-local.yml.example
  ...
```

### 4. 查看具体更改

```bash
git diff src/main/resources/application.yml
```

**确认：** 真实的 AccessKey 已经被替换为环境变量占位符

### 5. 测试应用启动

**方式1：使用IDEA配置**
1. Run → Edit Configurations
2. 添加环境变量（参考 ENV_CONFIGURATION.md）
3. 启动应用

**方式2：使用终端（临时）**
```bash
export OSS_DEFAULT_ACCESS_KEY_ID="your_key"
export OSS_DEFAULT_ACCESS_KEY_SECRET="your_secret"
export OSS_CV_ACCESS_KEY_ID="your_cv_key"
export OSS_CV_ACCESS_KEY_SECRET="your_cv_secret"

mvn spring-boot:run
```

**验证日志：**
```
=== 初始化 OSS 客户端 ===
初始化 OSS 账号: default
  状态: ✅ 初始化成功
初始化 OSS 账号: cv
  状态: ✅ 初始化成功
```

---

## 🚀 提交代码

### 1. 添加所有更改

```bash
git add .
```

### 2. 查看待提交的文件

```bash
git status
```

### 3. 提交

```bash
git commit -m "feat: 脱敏OSS配置并更新为环境变量方式

- 将OSS访问凭证改为环境变量方式
- 更新.gitignore忽略敏感配置文件
- 添加环境变量配置文档
- 添加本地配置示例文件
- 最终视频存储到cv账户

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

### 4. 推送

```bash
git push origin dev-zjy
```

---

## 🔍 推送到远程后验证

### 1. 检查远程仓库

```bash
git log --oneline -1
git show HEAD:src/main/resources/application.yml | grep -A 5 "aliyun"
```

**确认：** 远程仓库中的 application.yml 不包含真实的 AccessKey

### 2. 通知团队成员

**消息模板：**
```
大家好，我刚刚提交了代码更新，主要变更：

1. OSS配置改为环境变量方式
2. 需要配置以下环境变量才能启动应用：
   - OSS_DEFAULT_ACCESS_KEY_ID
   - OSS_DEFAULT_ACCESS_KEY_SECRET
   - OSS_CV_ACCESS_KEY_ID
   - OSS_CV_ACCESS_KEY_SECRET

详细配置说明请查看：ENV_CONFIGURATION.md

本地配置示例：application-local.yml.example

请在启动应用前先配置好环境变量！
```

---

## ⚠️ 注意事项

### ❌ 不要提交

1. 真实的 `access-key-id` 和 `access-key-secret`
2. `.env` 文件
3. `application-local.yml` 或 `application-local.properties`
4. 数据库密码、Redis密码等敏感信息
5. `output/` 目录下的临时文件

### ✅ 应该提交

1. `.env.example` 或 `.env.template`（示例文件，不含真实值）
2. `application-local.yml.example`（示例文件）
3. `.gitignore`（包含敏感文件忽略规则）
4. `ENV_CONFIGURATION.md`（配置说明文档）

---

## 📝 团队协作

### 新成员加入

1. 克隆仓库
2. 配置环境变量（参考 ENV_CONFIGURATION.md）
3. 或创建 `application-local.yml`（参考 application-local.yml.example）
4. 启动应用

### 已有成员更新

1. 拉取最新代码
2. 配置环境变量
3. 重启应用

---

## ✅ 安全检查命令

```bash
# 检查是否有硬编码的密钥
grep -r "LTAI5" . --exclude-dir=.git --exclude-dir=target
grep -r "access-key" . --exclude-dir=.git --exclude-dir=target

# 检查是否有密码
grep -r "password:" src/main/resources/ --include="*.yml" --include="*.properties"

# 检查.env文件是否被追踪（应该返回空）
git ls-files | grep "\.env"

# 检查git历史中是否包含敏感信息
git log --all --full-history --source -- "*access-key*" "*password*"
```

---

**检查时间：** 2026-02-05
**检查状态：** ✅ 通过
**安全级别：** 🔒 高安全
