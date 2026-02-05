# 回调产物存储和使用说明

## 📦 问题：回调产物存在哪里？

### 📍 存储位置

回调产物存储在**Redis**中：

```
Key格式: callback:{recordId}:{stepName}
TTL: 3600秒（1小时）
```

### 📊 数据结构

```json
// 人脸替换产物
{
  "success": true,
  "targetVideoUrl": "https://xxx.com/face_swap_result.mp4",
  "timestamp": 17387777888888
}

// 声音克隆产物
{
  "success": true,
  "voiceId": "voice_abc123",
  "timestamp": 17387777889999
}

// 声音合成产物
{
  "success": true,
  "audioUrl": "https://xxx.com/tts_audio.mp3",
  "timestamp": 1738777889000
}
```

---

## 🔄 完整的产物传递流程

### 示例：模板1-4流程

```
1. 人脸替换回调
   ↓
   notifyFaceSwapCallback()
   ↓
   callbackResultManager.saveResult(
       recordId, "face_swap",
       { success: true, targetVideoUrl: "url1" }
   )
   ↓
   Redis: callback:record_123:face_swap
```

```
2. 声音克隆回调
   ↓
   notifyVoiceCloneCallback()
   ↓
   callbackResultManager.saveResult(
       recordId, "voice_clone",
       { success: true, voiceId: "voice_abc" }
   )
   ↓
   Redis: callback:record_123:voice_clone
```

```
3. 唇形同步需要使用前面两步的产物
   ↓
   performLipSync() 需要获取：
   - face_swap 的 targetVideoUrl
   - voice_tts 的 audioUrl
   ↓
   从Redis获取：
   callbackResultManager.getResultUrl(recordId, "face_swap")
   callbackResultManager.getResultUrl(recordId, "voice_tts")
```

---

## 🎯 如何在模板处理器中使用

### 方式1：直接在处理器中使用CallbackResultManager

```java
@Service("template1to4Processor")
public class Template1to4Processor implements ITemplateProcessor {

    @Autowired
    private CallbackResultManager callbackResultManager;

    @Override
    public String process(String recordId, Spring2026Template template, VideoCreateDTO dto) {

        // ========== 步骤1：人脸替换 ==========
        FaceSwapRequest request = new FaceSwapRequest();
        request.setVideoUrl(templateVideoUrl);
        request.setFaceImageUrl(userPhotoUrl);
        request.setCallbackUrl(buildCallbackUrl(recordId, "face_swap"));

        AlgorithmResponse response = faceSwapService.swapFace(request);

        // 等待回调...
        // 回调会自动保存到Redis

        // ========== 步骤2：声音克隆 ==========
        VoiceCloneRequest vcRequest = new VoiceCloneRequest();
        vcRequest.setAudioUrl(userAudioUrl);
        vcRequest.setCallbackUrl(buildCallbackUrl(recordId, "voice_clone"));

        AlgorithmResponse vcResponse = voiceCloneService.cloneVoice(vcRequest);

        // ========== 步骤3：声音合成 ==========
        // 需要获取声音克隆的产物：voiceId
        String voiceId = callbackResultManager.getResultId(recordId, "voice_clone");

        VoiceTtsRequest ttsRequest = new VoiceTtsRequest();
        ttsRequest.setVoiceId(voiceId);
        ttsRequest.setText("春节快乐！");
        ttsRequest.setCallbackUrl(buildCallbackUrl(recordId, "voice_tts"));

        AlgorithmResponse ttsResponse = voiceTtsService.synthesizeVoice(ttsRequest);

        // ========== 步骤4：唇形同步 ==========
        // 需要获取前面两步的产物
        String faceSwapVideoUrl = callbackResultManager.getResultUrl(recordId, "face_swap");
        String audioUrl = callbackResultManager.getResultUrl(recordId, "voice_tts");

        LipSyncRequest lsRequest = new LipSyncRequest();
        lsRequest.setVideoUrl(faceSwapVideoUrl);
        lsRequest.setAudioUrl(audioUrl);
        lsRequest.setCallbackUrl(buildCallbackUrl(recordId, "lip_sync"));

        AlgorithmResponse lsResponse = lipSyncService.syncLip(lsRequest);

        // 等待回调完成后获取最终视频
        String finalVideoUrl = callbackResultManager.getResultUrl(recordId, "lip_sync");
        return finalVideoUrl;
    }
}
```

### 方式2：通用等待方法

```java
// 调用并等待
Map<String, Object> faceSwapResult = videoProcessingService.callAndWaitForCallback(
    recordId, "face_swap",
    () -> faceSwapService.swapFace(request),
    30
);

// 直接获取产物
String videoUrl = (String) faceSwapResult.get("targetVideoUrl");
```

---

## 📝 CallbackResultManager API

### 保存产物
```java
// 保存回调产物
callbackResultManager.saveResult(
    recordId,      // 记录ID
    stepName,      // 步骤名称: "face_swap"
    result         // Map<String, Object>
);
```

### 获取产物

```java
// 1. 获取完整产物Map
Map<String, Object> result = callbackResultManager.getResult(recordId, "face_swap");

// 2. 获取URL字段
String url = callbackResultManager.getResultUrl(recordId, "face_swap");
// 会自动尝试: targetVideoUrl, videoUrl, audioUrl, resultUrl

// 3. 获取ID字段
String id = callbackResultManager.getResultId(recordId, "voice_clone");
// 会自动尝试: voiceId, taskId

// 4. 检查是否成功
boolean success = callbackResultManager.isSuccess(recordId, "face_swap");
```

---

## 🔍 当前代码需要修复的地方

### 问题：当前代码缺少recordId传递

**问题**：回调接口中无法知道是哪个recordId的回调

**解决方案**：在调用算法服务时，在callbackId中包含recordId

### 修改：设置callbackUrl

```java
// 修改buildCallbackUrl方法
private String buildCallbackUrl(String recordId, String stepName) {
    String callbackId = recordId + ":" + UUID.randomUUID().toString();
    return "http://your-domain.com/api/callback/video/face-swap?callbackId=" + callbackId;
}
```

### 修改：VideoAlgorithmCallbackController

```java
@PostMapping("/face-swap")
public Map<String, Object> handleFaceSwapCallback(@RequestBody VideoAlgorithmCallbackResponse response,
                                              @RequestParam String callbackId) {

    // 从callbackId中提取recordId
    String[] parts = callbackId.split(":");
    String recordId = parts[0];
    String stepName = parts[1];

    // 处理回调...
}
```

---

## ✅ 完整的数据流

```
1. 用户创建视频
   recordId = "record_123"

2. 调用人脸替换算法
   callbackUrl = "http://xxx.com/api/callback/video/face-swap?callbackId=record_123:uuid"

3. 算法服务处理完成，回调URL
   POST /api/callback/video/face-swap?callbackId=record_123:uuid
   Body: { code: 0, message: "success", data: { targetVideoUrl: "xxx" } }

4. VideoAlgorithmCallbackController
   ├─ 从callbackId提取recordId: "record_123"
   └─ 调用VideoProcessingService.notifyFaceSwapCallback()

5. VideoProcessingService
   └─ 保存到Redis: callback:record_123:face_swap
      └─ 唤醒等待中的线程

6. 唇形同步需要使用
   callbackResultManager.getResultUrl("record_123", "face_swap")
   → 获取: "https://xxx.com/face_swap_result.mp4"
```

---

## 💡 建议

### 1. 统一使用CallbackResultManager
所有产物获取都通过CallbackResultManager，便于管理和追踪：

```java
// 推荐
String videoUrl = callbackResultManager.getResultUrl(recordId, "face_swap");

// 不推荐
String videoUrl = (String) callbackResults.get(...);
```

### 2. 产物过期时间
当前设置1小时TTL，可根据需要调整：
- 短期流程（10分钟）：600秒
- 中期流程（1小时）：3600秒
- 长期流程（1天）：86400秒

### 3. 产物清理
在流程完成后，可以选择性清理产物：
```java
// 产物保留1小时后自动过期
// 或手动删除
callbackResultManager.deleteResult(recordId, "face_swap");
```

---

## 🎯 总结

| 问题 | 答案 |
|-----|------|
| 产物存在哪？ | **Redis**，key格式：`callback:{recordId}:{stepName}` |
| 怎么获取？ | `callbackResultManager.getResultUrl(recordId, stepName)` |
| TTL多久？ | 1小时（3600秒），可调整 |
| 会丢失吗？ | 不会，Redis持久化 |
| 如何使用？ | 通过CallbackResultManager统一管理 |
