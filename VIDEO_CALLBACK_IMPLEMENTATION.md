# 视频算法回调接口完整实现

## 📋 回调接口结构总结

根据API文档，三个视频算法的回调结构：

### 1️⃣ **人脸替换回调**

**请求结构**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "targetVideoUrl": "https://example.com/result.mp4"
  }
}
```

**回调接口**：`POST /api/callback/video/face-swap`

**DTO**：
```java
// 最外层
VideoAlgorithmCallbackResponse {
    Integer code;
    String message;
    Object data;  // 实际是FaceSwapCallbackData
}

// Data部分
FaceSwapCallbackData {
    String targetVideoUrl;
}
```

---

### 2️⃣ **多图生图回调**

**请求结构**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "fileUrls": [
      "https://example.com/image1.jpg",
      "https://example.com/image2.jpg",
      "https://example.com/image3.jpg"
    ]
  }
}
```

**回调接口**：`POST /api/callback/video/multi-image-generate`

**DTO**：
```java
// Data部分
MultiImageGenerateCallbackData {
    List<String> fileUrls;
}
```

---

### 3️⃣ **唇形同步回调**

**请求结构**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "videoUrl": "https://example.com/lipsync.mp4",
    "code": 0,
    "message": "处理成功"
  }
}
```

**回调接口**：`POST /api/callback/video/lip-sync`

**DTO**：
```java
// Data部分
LipSyncCallbackData {
    String videoUrl;
    Integer code;
    String message;
}
```

---

## 📁 已创建的文件结构

```
src/main/java/org/example/newyear/
├── controller/
│   ├── AlgorithmCallbackController.java       ← 语音算法回调
│   └── VideoAlgorithmCallbackController.java   ← 视频算法回调
├── dto/
│   └── callback/
│       ├── VideoAlgorithmCallbackResponse.java ← 视频算法回调外层
│       ├── FaceSwapCallbackData.java          ← 人脸替换data
│       ├── MultiImageGenerateCallbackData.java ← 多图生图data
│       ├── LipSyncCallbackData.java            ← 唇形同步data
│       ├── VoiceCloneCallbackDTO.java          ← 声音克隆
│       ├── VoiceTtsCallbackDTO.java             ← 声音合成
│       └── SongFeatureExtractCallbackDTO.java   ← 特征提取
└── service/
    └── VideoProcessingService.java              ← 流程编排+回调处理
```

---

## 🔄 完整的回调处理流程

```
┌────────────────────────────────────────────┐
│  算法服务处理完成                            │
│  (10s-3min)                                  │
└────────────────────────────────────────────┘
                    ↓ POST
┌────────────────────────────────────────────┐
│  回调接口                                    │
│  /api/callback/video/face-swap            │
│  /api/callback/video/multi-image-generate   │
│  /api/callback/video/lip-sync               │
│  /api/callback/voice-clone                 │
│  /api/callback/voice-tts                    │
│  /api/callback/song-feature-extract         │
└────────────────────────────────────────────┘
                    ↓
┌────────────────────────────────────────────┐
│  VideoProcessingService                    │
│  1. 解析回调数据                            │
│  2. 存储到Redis（持久化）                   │
│  3. 存储到内存（快速访问）                   │
│  4. 唤醒CountDownLatch                     │
└────────────────────────────────────────────┘
                    ↓
┌────────────────────────────────────────────┐
│  等待中的线程被唤醒                         │
│  - 获取回调结果                            │
│  - 继续执行下一步骤                         │
└────────────────────────────────────────────┘
```

---

## 💡 使用示例

### 模板1-4处理器中的使用

```java
@Service("template1to4Processor")
public class Template1to4Processor implements ITemplateProcessor {

    @Autowired
    private VideoProcessingService videoProcessingService;

    @Override
    public String process(String recordId, Spring2026Template template, VideoCreateDTO dto) {

        // ========== 步骤1: 人脸替换 ==========
        Map<String, Object> faceSwapResult = videoProcessingService.callAndWaitForCallback(
            recordId, "face_swap",
            () -> faceSwapService.swapFace(request),
            30  // 等待30秒
        );

        String targetVideoUrl = (String) faceSwapResult.get("targetVideoUrl");

        // ========== 步骤2: 声音克隆 ==========
        Map<String, Object> voiceCloneResult = videoProcessingService.callAndWaitForCallback(
            recordId, "voice_clone",
            () -> voiceCloneService.cloneVoice(request),
            60  // 等待60秒
        );

        String voiceId = (String) voiceCloneResult.get("voiceId");

        // ========== 步骤3: 声音合成 ==========
        Map<String, Object> voiceTtsResult = videoProcessingService.callAndWaitForCallback(
            recordId, "voice_tts",
            () -> voiceTtsService.synthesizeVoice(request),
            60
        );

        String audioUrl = (String) voiceTtsResult.get("audioUrl");

        // ========== 步骤4: 唇形同步 ==========
        Map<String, Object> lipSyncResult = videoProcessingService.callAndWaitForCallback(
            recordId, "lip_sync",
            () -> lipSyncService.syncLip(request),
            60
        );

        return (String) lipSyncResult.get("videoUrl");
    }
}
```

---

## 🎯 关键点

### 1. **数据解析**
```java
// 在Controller中解析
FaceSwapCallbackData data = objectMapper.convertValue(
    response.getData(), FaceSwapCallbackData.class
);

String targetVideoUrl = data.getTargetVideoUrl();
```

### 2. **唤醒等待**
```java
// 存储回调结果时唤醒CountDownLatch
callbackResults.put(taskId + ":" + stepName, result);
CountDownLatch latch = callbackLatches.remove(latchKey);
if (latch != null) {
    latch.countDown();  // ← 唤醒等待中的线程
}
```

### 3. **等待回调**
```java
CountDownLatch latch = new CountDownLatch(1);
callbackLatches.put(latchKey, latch);

// 调用算法服务后等待
latch.await(60, TimeUnit.SECONDS);  // 最多等60秒

// 获取结果
Map<String, Object> result = callbackResults.remove(latchKey);
```

---

## ✅ 已完成的功能

### 回调接口（7个）
1. ✅ `/api/callback/video/face-swap` - 人脸替换
2. ✅ `/api/callback/video/multi-image-generate` - 多图生图
3. ✅ `/api/callback/video/lip-sync` - 唇形同步
4. ✅ `/api/callback/voice-clone` - 声音克隆
5. ✅ `/api/callback/voice-tts` - 声音合成
6. ✅ `/api/callback/song-feature-extract` - 特征提取

### DTO类（7个）
1. ✅ `VideoAlgorithmCallbackResponse` - 视频算法回调外层
2. ✅ `FaceSwapCallbackData` - 人脸替换data
3. ✅ `MultiImageGenerateCallbackData` - 多图生图data
4. ✅ `LipSyncCallbackData` - 唇形同步data
5. ✅ `VoiceCloneCallbackDTO` - 声音克隆
6. ✅ `VoiceTtsCallbackDTO` - 声音合成
7. ✅ `SongFeatureExtractCallbackDTO` - 特征提取

### 核心服务
1. ✅ `VideoProcessingService` - 完整的流程编排和回调处理

---

## 🔧 下一步

1. **设置回调URL**
   在调用算法服务时，设置正确的回调URL：
   ```java
   request.setCallbackUrl("http://your-domain.com/api/callback/video/face-swap");
   ```

2. **测试回调接口**
   使用Postman或curl测试回调接口是否正常工作

3. **实现模板处理器**
   更新`Template1to4Processor`使用新的回调机制

---

## ⚠️ 注意事项

1. **taskId传递**：需要确保算法服务调用时能返回taskId
2. **超时设置**：根据实际处理时间设置合理的超时时间
3. **错误处理**：检查回调中的success字段，失败时抛出异常
4. **Redis持久化**：即使服务重启，回调结果不丢失
