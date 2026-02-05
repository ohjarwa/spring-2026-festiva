# 最终视频存储到CV账户更新

## 📋 更新说明

根据需求，**最终拼接的视频结果现在会存储到cv的OSS账户**，而不是default账户。

---

## 🔄 代码变更

### 1. VideoProcessorUtil - 新增方法重载

#### concatVideos方法签名

**新增：**
```java
// 指定OSS账号类型
public String concatVideos(
    List<String> videoUrls,    // 视频URL列表
    String recordId,            // 记录ID
    String accountType          // OSS账号类型（default、cv）
) throws Exception

// 完整版本
public String concatVideos(
    List<String> videoUrls,    // 视频URL列表
    String outputUrl,          // 输出路径
    String recordId,           // 记录ID
    String accountType         // OSS账号类型
) throws Exception
```

#### uploadLocalFileToOss方法签名

**新增：**
```java
// 指定OSS账号类型
private String uploadLocalFileToOss(
    String localFilePath,      // 本地文件路径
    String recordId,           // 记录ID
    String category,           // 文件分类
    String fileName,           // 文件名
    String accountType         // OSS账号类型
) throws Exception
```

**调用方式：**
```java
// 上传到cv账户
var uploadResult = ossService.upload(fileAdapter, ossPath, "cv");

// 上传到default账户
var uploadResult = ossService.upload(fileAdapter, ossPath, "default");
```

### 2. Template1to4Processor - 更新调用

#### 步骤7：视频拼接

**更新前：**
```java
private String performVideoConcatenation(String video0Url, String video2Url, String recordId) {
    List<String> videoUrls = Arrays.asList(video0Url, video2Url);
    String ossUrl = videoProcessorUtil.concatVideos(videoUrls, recordId);  // 使用default
    return ossUrl;
}
```

**更新后：**
```java
private String performVideoConcatenation(String video0Url, String video2Url, String recordId) {
    List<String> videoUrls = Arrays.asList(video0Url, video2Url);
    String ossUrl = videoProcessorUtil.concatVideos(videoUrls, recordId, "cv");  // 使用cv
    return ossUrl;
}
```

---

## 📊 存储分配

### Default账户（ths-newyear-2026）

存储内容：
- ✅ 步骤5：唇形同步后的视频（aigc_video_2_step1.mp4）
- ✅ 步骤6：混入BGM的视频（aigc_video_2_final.mp4）

OSS路径示例：
```
spring2026/record_123/videos/mix_bgm.mp4
```

### CV账户（cv-springfestval-2026）

存储内容：
- ✅ 步骤7：最终拼接的视频（result.mp4）← **用户的最终结果**

OSS路径示例：
```
spring2026/record_123/videos/final_result.mp4
```

---

## 🎯 为什么这样设计？

### 1. **分离存储策略**
- **中间产物** → default账户（临时文件，可能定期清理）
- **最终结果** → cv账户（用户实际需要的视频，永久保存）

### 2. **便于管理**
- cv账户专门存储用户创作的最终视频
- 便于统计、审核、备份
- 不同的生命周期管理策略

### 3. **成本优化**
- 中间产物可以设置较短的生命周期
- 最终结果长期保存
- 降低存储成本

---

## 🔍 配置验证

### application.yml中的cv账户配置

```yaml
aliyun:
  oss:
    accounts:
      cv:
        endpoint: oss-cn-hangzhou.aliyuncs.com
        access-key-id: your_cv_access_key_id_here
        access-key-secret: your_cv_access_key_secret_here
        bucket: cv-springfestval-2026      # CV专用Bucket
        private-access: true
        signed-url-expire: 600
```

### 访问URL格式

**私有Bucket（签名URL）：**
```
https://cv-springfestval-2026.oss-cn-hangzhou.aliyuncs.com/spring2026/record_123/videos/final_result.mp4?OSSAccessKeyId=xxx&Expires=xxx&Signature=xxx
```

**有效期：** 600秒（10分钟）

---

## ✅ 测试验证

### 验证步骤

1. **执行完整流程**
   ```java
   // 步骤1-6正常执行
   // ...

   // 步骤7：视频拼接
   String finalVideoUrl = performVideoConcatenation(video0Url, video2FinalUrl, recordId);
   ```

2. **检查日志**
   ```
   开始上传到OSS[cv]: localFile=..., ossPath=...
   OSS上传成功[cv]: fileKey=..., accessUrl=...
   拼接视频已上传到OSS[cv]: https://cv-springfestval-2026.oss-cn-hangzhou.aliyuncs.com/...
   ```

3. **验证访问**
   - 访问返回的URL是否能正常下载
   - 检查视频内容是否完整
   - 验证URL签名是否有效

### 预期结果

- ✅ 中间视频在default账户
- ✅ 最终视频在cv账户
- ✅ 返回的URL可以访问
- ✅ 本地临时文件已删除

---

## 📝 总结

### 存储分配表

| 步骤 | 内容 | OSS账户 | Bucket |
|------|------|---------|--------|
| 步骤1-4 | 中间产物 | - | -（算法服务处理） |
| 步骤5 | 唇形同步视频 | default | ths-newyear-2026 |
| 步骤6 | 混入BGM视频 | default | ths-newyear-2026 |
| **步骤7** | **最终拼接视频** | **cv** | **cv-springfestval-2026** |

### 优势

1. ✅ **清晰分离** - 中间产物和最终结果分开存储
2. ✅ **便于管理** - CV账户专门管理用户创作
3. ✅ **成本优化** - 不同文件不同生命周期
4. ✅ **扩展性强** - 后续可添加更多账户

---

**更新时间：** 2026-02-05
**更新内容：** 最终视频存储到cv账户
