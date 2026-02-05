# 异步回调接口完整说明

## 📋 回调接口列表

根据算法服务文档，每个接口的回调数据结构不同，需要创建独立的回调接口：

| 接口路径 | 算法服务 | 关键字段 |
|---------|---------|---------|
| `POST /api/callback/voice-clone` | 声音克隆 | `voiceId` |
| `POST /api/callback/voice-tts` | 声音合成 | `audioUrl` |
| `POST /api/callback/song-feature-extract` | 歌曲特征提取 | `feature[]` (float数组) |
| `POST /api/callback/face-swap` | 人脸替换 | `videoUrl` |
| `POST /api/callback/lip-sync` | 唇形同步 | `videoUrl` |

---

## 🔧 创建的DTO类

### 1. VoiceCloneCallbackDTO.java
```java
@Data
public class VoiceCloneCallbackDTO {
    private String callbackId;
    private String taskId;
    private String status;        // success/failed
    private String voiceId;       // ← 克隆后的声音ID
    private String errorMsg;
    private Long timestamp;
}
```

### 2. VoiceTtsCallbackDTO.java
```java
@Data
public class VoiceTtsCallbackDTO {
    private String callbackId;
    private String taskId;
    private String status;        // success/failed
    private String audioUrl;      // ← 合成的音频URL
    private String errorMsg;
    private Long timestamp;
}
```

### 3. SongFeatureExtractCallbackDTO.java
```java
@Data
public class SongFeatureExtractCallbackDTO {
    private String callbackId;
    private String taskId;
    private String status;        // success/failed
    private List<Float> feature; // ← 特征向量（float数组）
    private String errorMsg;
    private Long timestamp;
}
```

### 4. VideoProcessCallbackDTO.java
```java
@Data
public class VideoProcessCallbackDTO {
    private String callbackId;
    private String taskId;
    private String status;        // success/failed
    private String videoUrl;      // ← 处理后的视频URL
    private String errorMsg;
    private Long timestamp;
}
```

---

## 🔌 风控异常码

根据歌曲特征提取接口文档，新增异常码：

| 错误码 | 含义 | 处理建议 |
|-------|------|---------|
| **11000** | 风控校验失败 | 内容不合规，拒绝处理 |

已添加到 `BusinessCode.java`：
```java
ERROR_RISK_CONTROL_FAILED(41001, "风控校验失败"),
ERROR_CONTENT_UNSAFE(41002, "内容不合规"),
ERROR_AUDIO_VIOLATION(41003, "音频违规"),
ERROR_IMAGE_VIOLATION(41004, "图片违规")
```

---

## 💡 使用示例

### 模板1-4流程配置

```java
@Service("template1to4Processor")
public class Template1to4Processor implements ITemplateProcessor {

    // 阶段1：并行执行人脸替换和声音克隆
    CompletableFuture<VoiceCloneCallbackDTO> voiceClone =
        CompletableFuture.supplyAsync(() -> {
            return performVoiceClone(audioUrl, recordId);
        });

    CompletableFuture<VideoProcessCallbackDTO> faceSwap =
        CompletableFuture.supplyAsync(() -> {
            return performFaceSwap(videoUrl, photoUrl, recordId);
        });

    // 等待两个并行任务完成
    CompletableFuture.allOf(voiceClone, faceSwap).join();

    // 获取结果
    String voiceId = voiceClone.get().getVoiceId();
    String faceSwapVideoUrl = faceSwap.get().getVideoUrl();

    // 阶段2：串行执行声音合成和唇形同步
    VoiceTtsCallbackDTO voiceTts = performVoiceTts(voiceId, recordId);
    VideoProcessCallbackDTO lipSync = performLipSync(
        faceSwapVideoUrl, voiceTts.getAudioUrl(), recordId
    );

    return lipSync.getVideoUrl();
}
```

### 调用算法服务（设置回调URL）

```java
// 声音克隆
VoiceCloneRequest request = new VoiceCloneRequest();
request.setAudioUrl(audioUrl);
request.setCallbackUrl("http://your-domain.com/api/callback/voice-clone");

AlgorithmResponse response = voiceCloneService.cloneVoice(request);
String taskId = response.getData().getTaskId();

// 等待回调...
VoiceCloneCallbackDTO callback = waitForCallback(taskId, "voice-clone", 60);
String voiceId = callback.getVoiceId();
```

---

## 📊 回调处理流程

```
算法服务处理完成
    ↓
POST http://your-domain.com/api/callback/voice-clone
Body: {
  "callbackId": "callback_123",
  "taskId": "task_456",
  "status": "success",
  "voiceId": "voice_abc",
  "timestamp": 1738496231
}
    ↓
AlgorithmCallbackController.handleVoiceCloneCallback()
    ↓
VideoProcessingService.notifyVoiceCloneCallback()
    ↓
1. 存储结果到Redis
2. 唤醒CountDownLatch
3. 更新任务执行详情
```

---

## ⚠️ 重要提示

### 1. 回调URL配置

在调用算法服务时，需要设置正确的回调URL：

```java
// 声音克隆
String callbackUrl = "http://your-domain.com/api/callback/voice-clone";

// 声音合成
String callbackUrl = "http://your-domain.com/api/callback/voice-tts";

// 歌曲特征提取
String callbackUrl = "http://your-domain.com/api/callback/song-feature-extract";

// 人脸替换
String callbackUrl = "http://your-domain.com/api/callback/face-swap";

// 唇形同步
String callbackUrl = "http://your-domain.com/api/callback/lip-sync";
```

### 2. 回调数据映射

每个算法服务的回调关键字段：

| 算法服务 | 结果字段 | 含义 |
|---------|---------|------|
| 声音克隆 | `voiceId` | 克隆后的声音ID |
| 声音合成 | `audioUrl` | 合成的音频URL |
| 歌曲特征提取 | `feature[]` | 特征向量 |
| 人脸替换 | `videoUrl` | 替换后的视频URL |
| 唇形同步 | `videoUrl` | 同步后的视频URL |

### 3. 错误处理

当 `status = "failed"` 时：
```json
{
  "status": "failed",
  "errorMsg": "风控校验失败"  // ← 11000异常
}
```

需要在代码中检查status并处理错误：
```java
if ("failed".equals(callback.getStatus())) {
    if (callback.getErrorMsg().contains("11000")) {
        throw new BusinessException(BusinessCode.ERROR_RISK_CONTROL_FAILED);
    }
}
```

---

## 🎯 完整工作流程

```
1. 用户提交素材（照片+音频）
       ↓
2. 调用算法服务（设置回调URL）
       ↓
3. 算法服务立即返回taskId
       ↓
4. 使用CountDownLatch等待回调（最多等60秒）
       ↓
5. 算法服务处理完成 → 调用回调接口
       ↓
6. 回调接口存储结果 → 唤醒CountDownLatch
       ↓
7. 获取结果 → 继续下一步骤
       ↓
8. 所有步骤完成 → 返回最终视频URL
```

---

## 📁 文件结构

```
src/main/java/org/example/newyear/
├── controller/
│   └── AlgorithmCallbackController.java  ← 5个回调接口
├── dto/
│   └── callback/
│       ├── VoiceCloneCallbackDTO.java      ← 声音克隆回调
│       ├── VoiceTtsCallbackDTO.java         ← 声音合成回调
│       ├── SongFeatureExtractCallbackDTO.java ← 特征提取回调
│       └── VideoProcessCallbackDTO.java      ← 视频处理回调
└── service/
    └── VideoProcessingService.java          ← 流程编排服务
```

---

## 🚀 下一步

1. **更新VideoProcessingService**：添加各个回调处理方法
2. **更新模板处理器**：使用新的回调DTO
3. **测试回调接口**：使用Postman或curl测试
4. **配置回调URL**：确保算法服务能访问你的回调接口

需要我实现VideoProcessingService中的回调处理方法吗？
