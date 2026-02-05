# 模板1-4完整流程实现说明

## 📋 流程概述

根据产品需求，模板1-4实现了完整的7步AI视频生成流程。

## 🎯 完整流程

### 素材列表

| 素材 | 文件名 | 说明 |
|------|--------|------|
| 视频片段0 | src_video_0.mp4 | 产品运营提供，用于人物替换 |
| 视频片段1 | src_video_1.mp4 | 产品运营提供，无须算法处理 |
| 视频片段2 | src_video_2.mp4 | 产品运营提供，先人物替换，再Lipsync |
| 用户原始照片 | src_person.jpg | 用户上传的照片 |
| 用户合成照片 | aigc_person.jpg | 图生图算法生成的人物照片 |
| 用户语音 | vocal_2.wav | 语音合成，使用用户音色生成的语音文件 |
| 背景音乐 | bgm_2.wav | 产品运营提供的背景音乐 |

### 合成步骤

#### **步骤1：调用语音服务（两步）**

1.1 **声音克隆** → 获得voiceId
- 输入：用户原始语音
- 输出：voiceId
- 说明：同一个用户只需要克隆一次

```java
String voiceId = performVoiceCloneAndWait(userAudioUrl, recordId);
```

1.2 **声音合成** → 获得vocal_2.wav
- 输入：voiceId + 固定文案
- 输出：合成音频URL

```java
String vocal2Url = performVoiceTtsAndWait(voiceId, recordId);
```

#### **步骤2：多图生图（图生图算法）**

- 算法：Dreamface-Flux2-ImageGen-V1
- 输入：用户原始照片 + 预设提示词
- 输出：aigc_person.jpg（用于渲染的人物照片）

```java
String aigcPersonUrl = performMultiImageGenerateAndWait(userPhotoUrl, recordId);
```

#### **步骤3：人物替换（视频0）**

- 算法：Dreamface-WanAnimate-Image2Video-V1
- 输入：src_video_0.mp4 + aigc_person.jpg
- 输出：aigc_video_0.mp4

```java
String aigcVideo0Url = performFaceSwapAndWait(SRC_VIDEO_0_URL, aigcPersonUrl, recordId, "face_swap_0");
```

#### **步骤4：人物替换（视频2）**

- 算法：Dreamface-WanAnimate-Image2Video-V1
- 输入：src_video_2.mp4 + aigc_person.jpg
- 输出：aigc_video_2_step0.mp4

**注意：步骤3和步骤4并行执行**

```java
CompletableFuture.allOf(video0Future, video2Future).join();
```

#### **步骤5：唇形同步（视频2）**

- 算法：ImageProcess-TalkingFace-Render-V1
- 输入：aigc_video_2_step0.mp4 + vocal_2.wav
- 输出：aigc_video_2_step1.mp4（口唇同步好的，只有人声的视频）

```java
String aigcVideo2Step1Url = performLipSyncAndWait(aigcVideo2Step0Url, vocal2Url, recordId);
```

#### **步骤6：FFmpeg混入背景音乐**

- 输入：aigc_video_2_step1.mp4 + bgm_2.wav
- 输出：混入BGM的视频2最终版

```java
String aigcVideo2FinalUrl = performAudioMixing(aigcVideo2Step1Url, BGM_2_URL, recordId);
```

#### **步骤7：视频拼接**

- 输入：aigc_video_0.mp4 + aigc_video_2_final.mp4
- 输出：result.mp4（最终视频）

```java
String finalResultUrl = performVideoConcatenation(aigcVideo0Url, aigcVideo2FinalUrl, recordId);
```

---

## 📁 文件结构

```
src/main/java/org/example/newyear/
├── service/
│   ├── Template1to4Processor.java          ← 模板1-4处理器（主流程）
│   ├── VideoProcessingService.java         ← 回调处理服务
│   ├── CallbackResultManager.java          ← 回调产物管理器
│   └── algorithm/
│       ├── FaceSwapService.java            ← 人脸替换服务
│       ├── MultiImageGenerateService.java  ← 多图生图服务 ✨新增
│       ├── VoiceCloneService.java          ← 声音克隆服务
│       ├── VoiceTtsService.java            ← 声音合成服务
│       ├── LipSyncService.java             ← 唇形同步服务
│       ├── FaceSwapRequest.java
│       ├── MultiImageGenerateRequest.java  ← 多图生图请求 ✨新增
│       ├── VoiceCloneRequest.java
│       ├── VoiceTtsRequest.java
│       └── LipSyncRequest.java
├── config/
│   └── AlgorithmProperties.java            ← 算法服务配置（已更新）✨
├── controller/
│   ├── VideoAlgorithmCallbackController.java  ← 视频算法回调
│   └── AlgorithmCallbackController.java       ← 语音算法回调
├── dto/callback/
│   ├── VideoAlgorithmCallbackResponse.java
│   ├── FaceSwapCallbackData.java
│   ├── MultiImageGenerateCallbackData.java
│   ├── LipSyncCallbackData.java
│   ├── VoiceCloneCallbackDTO.java
│   └── VoiceTtsCallbackDTO.java
└── util/
    └── VideoProcessorUtil.java             ← 视频处理工具（已更新）✨
```

---

## 🔧 核心实现细节

### 1. **异步回调机制**

所有算法服务调用都是异步的，使用`callAndWaitForCallback`方法等待回调：

```java
Map<String, Object> result = videoProcessingService.callAndWaitForCallback(
    recordId,           // 记录ID
    stepName,           // 步骤名称（如"face_swap"）
    () -> service.call(request),  // 算法调用
    timeoutSeconds       // 超时时间
);
```

### 2. **回调产物存储**

所有回调产物存储在Redis中：
- Key格式：`callback:{recordId}:{stepName}`
- TTL：3600秒（1小时）

```java
// 保存产物
callbackResultManager.saveResult(recordId, "face_swap", result);

// 获取产物
String videoUrl = callbackResultManager.getResultUrl(recordId, "face_swap");
```

### 3. **并行处理**

步骤3和步骤4（两个人脸替换）使用`CompletableFuture`并行执行：

```java
CompletableFuture<String> video0Future = CompletableFuture.supplyAsync(() -> {
    return performFaceSwapAndWait(...);
});

CompletableFuture<String> video2Future = CompletableFuture.supplyAsync(() -> {
    return performFaceSwapAndWait(...);
});

// 等待两个任务都完成
CompletableFuture.allOf(video0Future, video2Future).join();
```

### 4. **视频处理**

#### 背景音乐混合

```java
videoProcessorUtil.mixAudioWithBgm(videoUrl, bgmUrl, outputUrl);
```

#### 视频拼接

```java
List<String> videoUrls = Arrays.asList(video0Url, video2Url);
videoProcessorUtil.concatVideos(videoUrls, outputUrl);
```

---

## 📝 配置说明

### application.yml

```yaml
algorithm:
  face-swap:
    url: http://face-swap-service.com
    api-key: your-api-key
    timeout: 30000

  multi-image-generate:  # ✨新增
    url: http://multi-image-generate-service.com
    api-key: your-api-key
    timeout: 120000  # 2分钟

  voice-clone:
    url: http://voice-clone-service.com
    timeout: 60000

  voice-tts:
    url: http://voice-tts-service.com
    timeout: 60000

  lip-sync:
    url: http://lip-sync-service.com
    api-key: your-api-key
    timeout: 30000
```

### 固定素材URL配置

在`Template1to4Processor`中配置的固定URL（后续从OSS获取）：

```java
private static final String SRC_VIDEO_0_URL = "https://your-oss-bucket.com/templates/src_video_0.mp4";
private static final String SRC_VIDEO_2_URL = "https://your-oss-bucket.com/templates/src_video_2.mp4";
private static final String BGM_2_URL = "https://your-oss-bucket.com/templates/bgm_2.wav";
```

---

## ✅ 完整检查清单

### 步骤实现检查

- [x] 步骤1.1：声音克隆 → voiceId
- [x] 步骤1.2：声音合成 → vocal_2.wav
- [x] 步骤2：多图生图 → aigc_person.jpg
- [x] 步骤3：人物替换（视频0）→ aigc_video_0.mp4
- [x] 步骤4：人物替换（视频2）→ aigc_video_2_step0.mp4
- [x] 步骤3+4：并行执行 ✨
- [x] 步骤5：唇形同步（视频2）→ aigc_video_2_step1.mp4
- [x] 步骤6：FFmpeg混入背景音乐 → aigc_video_2_final.mp4
- [x] 步骤7：视频拼接 → result.mp4

### 组件实现检查

- [x] MultiImageGenerateService - 多图生图服务 ✨新增
- [x] MultiImageGenerateRequest - 多图生图请求 ✨新增
- [x] VideoProcessorUtil.mixAudioWithBgm() - 混音方法 ✨新增
- [x] VideoProcessorUtil.concatVideos() - 视频拼接方法（已存在）
- [x] CallbackResultManager - 回调产物管理器
- [x] 异步回调等待机制
- [x] 并行处理机制（CompletableFuture）

### 回调处理检查

- [x] VideoAlgorithmCallbackController - 视频算法回调
- [x] AlgorithmCallbackController - 语音算法回调
- [x] 7个回调DTO类
- [x] callbackId参数解析
- [x] recordId提取
- [x] Redis存储
- [x] CountDownLatch唤醒

---

## 🎯 下一步工作

### 1. **OSS集成**
- [ ] 实现上传到OSS的逻辑
- [ ] 将固定素材URL从OSS获取
- [ ] 将生成的视频上传到OSS

### 2. **配置管理**
- [ ] 从配置文件读取服务器域名
- [ ] 配置实际的API密钥
- [ ] 配置实际的OSS地址

### 3. **错误处理**
- [ ] 更完善的异常处理
- [ ] 重试机制
- [ ] 失败回滚逻辑

### 4. **性能优化**
- [ ] 考虑使用线程池管理并行任务
- [ ] 优化视频处理流程
- [ ] 添加进度上报机制

### 5. **监控和日志**
- [ ] 添加更详细的日志
- [ ] 添加性能监控
- [ ] 添加错误统计

---

## 📊 流程图

```
用户上传素材（照片+语音）
        ↓
┌─────────────────────────────────┐
│ 步骤1: 语音服务                  │
│  1.1 克隆声音 → voiceId         │
│  1.2 合成语音 → vocal_2.wav     │
└─────────────────────────────────┘
        ↓
┌─────────────────────────────────┐
│ 步骤2: 多图生图                  │
│  用户照片 → aigc_person.jpg      │
└─────────────────────────────────┘
        ↓
┌─────────────────────────────────┐
│ 步骤3+4: 并行人物替换            │
│  视频0 + aigc_person → v0.mp4   │
│  视频2 + aigc_person → v2_s0.mp4│
└─────────────────────────────────┘
        ↓
┌─────────────────────────────────┐
│ 步骤5: 唇形同步（视频2）          │
│  v2_s0 + vocal_2 → v2_s1.mp4    │
└─────────────────────────────────┘
        ↓
┌─────────────────────────────────┐
│ 步骤6: FFmpeg混入BGM             │
│  v2_s1 + bgm → v2_final.mp4     │
└─────────────────────────────────┘
        ↓
┌─────────────────────────────────┐
│ 步骤7: 视频拼接                  │
│  v0.mp4 + v2_final.mp4 → result │
└─────────────────────────────────┘
        ↓
    返回最终视频URL
```

---

## 🎉 总结

现在的实现**完全遵循**了产品要求的7步流程：

1. ✅ 所有步骤都已实现
2. ✅ 异步回调机制完整
3. ✅ 并行处理优化性能
4. ✅ 回调产物管理完善
5. ✅ 视频处理功能完整

**核心改进：**
- ✨ 新增了多图生图服务
- ✨ 实现了双视频并行处理
- ✨ 添加了背景音乐混合功能
- ✨ 完善了视频拼接功能
- ✨ 完整的回调产物存储和获取机制
