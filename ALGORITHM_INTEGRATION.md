# 算法接口配置说明

## 📋 概述

本项目集成了多个算法服务，实现AI视频生成功能。

### 算法服务列表

1. **人脸替换** - 需要鉴权
2. **唇形同步** - 需要鉴权
3. **声音克隆** - 无需鉴权
4. **声音合成** - 无需鉴权

---

## 🔧 环境配置

### 1. 配置文件

在 `application.yml` 中配置算法服务地址：

```yaml
algorithm:
  # 人脸替换服务
  face-swap:
    url: http://face-swap-service.com  # 替换为实际地址
    api-key: your-api-key              # 替换为实际API密钥
    timeout: 30000

  # 唇形同步服务
  lip-sync:
    url: http://lip-sync-service.com   # 替换为实际地址
    api-key: your-api-key              # 替换为实际API密钥
    timeout: 30000

  # 声音克隆服务（无需鉴权）
  voice-clone:
    url: http://voice-clone-service.com # 替换为实际地址
    timeout: 60000

  # 声音合成服务（无需鉴权）
  voice-tts:
    url: http://voice-tts-service.com   # 替换为实际地址
    timeout: 60000
```

### 2. 环境变量（推荐）

为了安全，建议使用环境变量：

```bash
# 人脸替换
export FACE_SWAP_URL=http://face-swap-service.com
export FACE_SWAP_API_KEY=your_api_key

# 唇形同步
export LIP_SYNC_URL=http://lip-sync-service.com
export LIP_SYNC_API_KEY=your_api_key

# 声音克隆
export VOICE_CLONE_URL=http://voice-clone-service.com

# 声音合成
export VOICE_TTS_URL=http://voice-tts-service.com
```

---

## 📝 模板配置

### 标准流程模板示例

数据库表 `spring_2026_template.task_config` 字段配置：

```json
{
  "steps": [
    {
      "step_name": "face_swap",
      "step_type": "video_process",
      "service": "faceSwapService",
      "method": "swapFace",
      "description": "人脸替换",
      "input_mapping": {
        "videoUrl": "{{template_video_url}}",
        "faceImageUrl": "{{user_photo_url}}"
      },
      "output_key": "face_swap_result",
      "timeout_seconds": 30
    },
    {
      "step_name": "voice_clone",
      "step_type": "audio_process",
      "service": "voiceCloneService",
      "method": "cloneVoice",
      "description": "声音克隆",
      "input_mapping": {
        "audioUrl": "{{user_audio_url}}"
      },
      "output_key": "voice_id",
      "timeout_seconds": 60
    },
    {
      "step_name": "voice_tts",
      "step_type": "audio_process",
      "service": "voiceTtsService",
      "method": "synthesizeVoice",
      "description": "声音合成",
      "input_mapping": {
        "voiceId": "{{voice_clone_result.voiceId}}",
        "text": "春节快乐，万事如意，恭喜发财！"
      },
      "output_key": "tts_audio_url",
      "timeout_seconds": 60,
      "depends_on": ["voice_clone"]
    },
    {
      "step_name": "lip_sync",
      "step_type": "video_process",
      "service": "lipSyncService",
      "method": "syncLip",
      "description": "唇形同步",
      "input_mapping": {
        "videoUrl": "{{face_swap_result.resultUrl}}",
        "audioUrl": "{{tts_audio_url}}"
      },
      "output_key": "final_video_url",
      "timeout_seconds": 30,
      "depends_on": ["face_swap", "voice_tts"]
    }
  ],
  "estimated_time_seconds": 180,
  "parallel_groups": [
    ["face_swap", "voice_clone"],
    ["voice_tts"],
    ["lip_sync"]
  ]
}
```

### 配置字段说明

| 字段 | 说明 | 示例 |
|-----|------|------|
| `step_name` | 步骤名称（唯一标识） | `face_swap` |
| `step_type` | 步骤类型 | `video_process` / `audio_process` |
| `service` | Spring Bean名称 | `faceSwapService` |
| `method` | 调用的方法名 | `swapFace` |
| `input_mapping` | 输入参数映射 | 见下方说明 |
| `output_key` | 输出结果的键名 | `face_swap_result` |
| `timeout_seconds` | 超时时间（秒） | `30` |
| `depends_on` | 依赖的前置步骤 | `["face_swap"]` |

### 输入参数映射

支持的变量格式：

- `{{template_video_url}}` - 模板视频URL（从template表获取）
- `{{user_photo_url}}` - 用户上传的照片URL
- `{{user_audio_url}}` - 用户上传的音频URL
- `{{step_name.output_key}}` - 前面步骤的输出结果

示例：
```json
{
  "input_mapping": {
    "videoUrl": "{{template_video_url}}",           // 使用模板视频
    "faceImageUrl": "{{user_photo_url}}",          // 使用用户照片
    "audioUrl": "{{voice_clone_result.resultUrl}}"  // 使用声音克隆结果
  }
}
```

---

## 🔄 异步回调

### 回调接口

算法服务完成处理后，会回调以下接口：

1. **视频处理回调**
   ```
   POST /api/callback/video
   ```

2. **音频处理回调**
   ```
   POST /api/callback/audio
   ```

### 回调请求格式

```json
{
  "callbackId": "callback_123",
  "taskId": "record_456",
  "stepName": "face_swap",
  "callbackType": "video_process",
  "status": "success",
  "resultUrl": "https://result.com/video.mp4",
  "errorMessage": null,
  "timestamp": 1738588888000
}
```

### 回调处理流程

1. 算法服务完成处理
2. 调用回调接口通知结果
3. 系统将结果存储到Redis（1小时过期）
4. `VideoProcessingService` 检测到结果，继续下一步骤
5. 所有步骤完成后，更新数据库记录状态

---

## 📦 服务包装方法

所有算法服务都已封装，使用方法：

```java
@Autowired
private FaceSwapService faceSwapService;

// 调用人脸替换
FaceSwapRequest request = new FaceSwapRequest();
request.setVideoUrl("https://video.com/template.mp4");
request.setFaceImageUrl("https://user.com/face.jpg");
request.setCallbackUrl("https://your-domain.com/api/callback/video");

AlgorithmResponse response = faceSwapService.swapFace(request);
```

---

## 🚀 完整流程示例

### 1. 用户创建视频任务

```bash
POST /api/video/create?userId=user_123
{
  "template_id": "tpl_001",
  "materials": {
    "photos": ["https://oss.xxx.com/face.jpg"],
    "audios": ["https://oss.xxx.com/voice.mp3"]
  }
}
```

### 2. 后端处理流程

```
① 扣减配额
② 创建创作记录（status=0排队）
③ 异步处理开始：
   - 人脸替换（调用算法服务） → 等待回调
   - 声音克隆（调用算法服务） → 等待回调
   - 声音合成（调用算法服务） → 等待回调
   - 唇形同步（调用算法服务） → 等待回调
④ 所有步骤完成 → 更新数据库（status=2完成）
```

### 3. 用户查询进度

```bash
GET /api/user/works?userId=user_123

# 返回当前进度
{
  "record_id": "record_456",
  "status": "processing",
  "progress": 50,
  "current_step": "voice_tts"
}
```

---

## ⚠️ 注意事项

1. **鉴权**
   - 人脸替换和唇形同步需要配置API密钥
   - 声音克隆和声音合成无需鉴权

2. **超时时间**
   - 视频处理建议30秒超时
   - 音频处理建议60秒超时

3. **异步回调**
   - 确保回调地址可从算法服务访问
   - 回调接口需要幂等性处理

4. **并发控制**
   - 使用线程池控制并发数
   - 避免同时处理过多任务导致资源耗尽

---

## 📚 相关文档

- 算法接口文档：`src/main/resources/接口文档/算法接口文档/`
- 模板配置示例：`src/main/resources/template-config-examples.json`