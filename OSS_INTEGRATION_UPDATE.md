# OSS集成更新说明

## 📋 更新概述

已完成代码中所有OSS操作的集成，使用新的`OssService`替代旧的`OssUtil`，所有视频和音频处理结果现在都会自动上传到OSS并返回访问URL。

---

## 🆕 新的OSS服务结构

### 核心组件

1. **OssService（接口）**
   - 定义了所有OSS操作的接口
   - 支持多OSS账号切换
   - 文件位置：`service/oss/OssService.java`

2. **OssServiceImpl（实现）**
   - 完整的OSS操作实现
   - 文件位置：`service/oss/OssServiceImpl.java`

3. **OssClientFactory（工厂）**
   - 管理多个OSS账号的客户端实例
   - 自动初始化和销毁
   - 文件位置：`util/OssClientFactory.java`

4. **MultiOssConfig（配置）**
   - 从application.yml读取配置
   - 支持多个OSS账号配置
   - 文件位置：`config/MultiOssConfig.java`

5. **OssUploadResult（结果对象）**
   - 上传结果的统一封装
   - 包含fileKey、accessUrl、文件信息等
   - 文件位置：`service/oss/OssUploadResult.java`

---

## 📝 application.yml配置

```yaml
aliyun:
  oss:
    accounts:
      # 默认账号
      default:
        endpoint: oss-cn-hangzhou.aliyuncs.com
        access-key-id: your_default_access_key_id_here
        access-key-secret: your_default_access_key_secret_here
        bucket: ths-newyear-2026
        private-access: true      # 私有Bucket，生成签名URL
        signed-url-expire: 600     # 签名URL过期时间（秒）

      # CV专用账号
      cv:
        endpoint: oss-cn-hangzhou.aliyuncs.com
        access-key-id: your_cv_access_key_id_here
        access-key-secret: your_cv_access_key_secret_here
        bucket: cv-springfestval-2026
        private-access: true
        signed-url-expire: 600
```

### 配置说明

- **accounts**: 支持多个OSS账号配置
- **endpoint**: OSS服务地址（如：oss-cn-hangzhou.aliyuncs.com）
- **access-key-id / access-key-secret**: 阿里云访问凭证
- **bucket**: OSS存储桶名称
- **private-access**:
  - `true`: 私有Bucket，生成签名URL（有时效性）
  - `false`: 公共Bucket，生成永久URL
- **signed-url-expire**: 签名URL有效期（秒），默认600秒（10分钟）

---

## 🔄 更新的文件

### 1. VideoProcessorUtil

**更新内容：**
- ✅ 注入`OssService`依赖
- ✅ 新增`uploadLocalFileToOss()`方法 - 上传本地文件到OSS
- ✅ 新增`deleteLocalFile()`方法 - 删除本地临时文件
- ✅ 新增`FileMultipartFile`适配器 - 将File转为MultipartFile
- ✅ 更新`mixAudioWithBgm()`方法 - 上传混音结果到OSS
- ✅ 更新`concatVideos()`方法 - 上传拼接结果到OSS

**方法签名变更：**
```java
// 更新前：
public String mixAudioWithBgm(String videoUrl, String bgmUrl, String outputUrl)
public String concatVideos(List<String> videoUrls, String outputUrl)

// 更新后：
public String mixAudioWithBgm(String videoUrl, String bgmUrl, String recordId)
public String concatVideos(List<String> videoUrls, String recordId)
```

**返回值：**
- 更新前：返回本地路径
- 更新后：返回OSS访问URL

### 2. Template1to4Processor

**更新内容：**
- ✅ 删除`buildOutputUrl()`方法（不再需要）
- ✅ 更新`performAudioMixing()`方法调用
- ✅ 更新`performVideoConcatenation()`方法调用

**更新前：**
```java
private String performAudioMixing(String videoUrl, String bgmUrl, String recordId) {
    String outputUrl = buildOutputUrl(recordId, "video2_with_bgm");
    videoProcessorUtil.mixAudioWithBgm(videoUrl, bgmUrl, outputUrl);
    return outputUrl;
}
```

**更新后：**
```java
private String performAudioMixing(String videoUrl, String bgmUrl, String recordId) {
    String ossUrl = videoProcessorUtil.mixAudioWithBgm(videoUrl, bgmUrl, recordId);
    return ossUrl;
}
```

---

## 🔍 OSS文件路径规则

### 生成规则

```
spring2026/{recordId}/{category}/{fileName}{extension}
```

### 示例

```
# 混入BGM的视频
spring2026/record_123/videos/mix_bgm.mp4

# 拼接的最终视频
spring2026/record_123/videos/final_result.mp4

# 用户上传的照片（使用OssService.upload()）
spring2026/user123/photos/20260205/abc123.jpg
```

### fileKey示例

```
spring2026/record_123/videos/mix_bgm.mp4
```

---

## 🔧 OssService API使用

### 上传文件（使用默认账号）

```java
@Autowired
private OssService ossService;

// 上传MultipartFile
OssUploadResult result = ossService.upload(multipartFile, "user123/photos");

// 返回结果包含：
// - result.getFileKey()      → "spring2026/user123/photos/xxx.jpg"
// - result.getAccessUrl()    → "https://bucket.oss-cn-hangzhou.aliyuncs.com/..."
// - result.getFileSize()     → 文件大小
// - result.getOriginalFilename() → 原始文件名
```

### 上传文件（指定账号）

```java
// 上传到cv账号
OssUploadResult result = ossService.upload(multipartFile, "output/images", "cv");
```

### 获取访问URL

```java
// 获取URL（默认账号）
String url = ossService.getAccessUrl("spring2026/record_123/videos/final.mp4");

// 获取URL（指定账号）
String url = ossService.getAccessUrl("spring2026/record_123/videos/final.mp4", "cv");
```

### 检查文件是否存在

```java
boolean exists = ossService.exists("spring2026/record_123/videos/final.mp4");
```

### 删除文件

```java
boolean success = ossService.deleteFile("spring2026/record_123/videos/temp.mp4");
```

---

## 📊 完整的文件处理流程

### 示例：视频混入BGM

```java
// 1. 本地处理（JavaCV）
String localPath = "output/videos/mix_bgm_" + uuid + ".mp4";
// ... 混音处理 ...

// 2. 上传到OSS
FileMultipartFile fileAdapter = new FileMultipartFile(localFile);
String ossPath = recordId + "/videos/mix_bgm";
OssUploadResult uploadResult = ossService.upload(fileAdapter, ossPath);

// 3. 获取访问URL
String ossUrl = uploadResult.getAccessUrl();
// 私有Bucket：https://bucket.oss-cn-hangzhou.aliyuncs.com/spring2026/...?signature=...
// 公共Bucket：https://bucket.oss-cn-hangzhou.aliyuncs.com/spring2026/...

// 4. 删除本地临时文件
localFile.delete();

// 5. 返回URL给调用者
return ossUrl;
```

---

## ✅ 优势

### 1. 自动URL生成
- **私有Bucket**：自动生成签名URL（带时效性）
- **公共Bucket**：自动生成永久URL
- 无需手动拼接URL

### 2. 多账号支持
- 可配置多个OSS账号（default、cv等）
- 根据业务需求切换账号
- 自动管理客户端生命周期

### 3. 统一错误处理
- 使用`OssException`统一异常处理
- 详细的错误信息和日志

### 4. 临时文件自动清理
- 本地处理后立即上传OSS
- 上传成功后删除本地文件
- 节省磁盘空间

---

## 🎯 下一步建议

### 1. 配置外部化
将敏感信息（access-key-id、access-key-secret）移到环境变量：
```yaml
access-key-id: ${OSS_ACCESS_KEY_ID}
access-key-secret: ${OSS_ACCESS_KEY_SECRET}
```

### 2. 监控和日志
- 添加OSS上传失败的告警
- 记录上传耗时和文件大小统计
- 监控OSS存储使用量

### 3. 缓存优化
- 对生成的视频URL进行缓存
- 避免重复处理相同的请求

### 4. 异步上传
- 考虑使用异步方式上传大文件
- 提升用户体验

---

## 📝 相关文档

- **阿里云OSS文档**: https://help.aliyun.com/product/31815.html
- **Java SDK文档**: https://help.aliyun.com/document_detail/32068.html
- **签名URL说明**: https://help.aliyun.com/document_detail/32016.html

---

**更新时间：** 2026-02-05
**更新内容：** 集成新的OssService，所有视频处理结果自动上传OSS
