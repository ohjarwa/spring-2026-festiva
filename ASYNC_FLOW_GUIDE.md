# 异步流程编排架构说明

## 📋 问题解答

### ❓ 问题1：回调接口应该写多个吗？

**答：** 只需要**1个通用回调接口**，原因：

1. **统一的数据格式**
   - 所有算法服务的回调格式都相同
   - 都有：callbackId, taskId, stepName, status, resultUrl等字段

2. **通过stepName区分**
   ```java
   @PostMapping("/algorithm")
   public Object handleCallback(@RequestBody AlgorithmCallbackRequest request) {
       String stepName = request.getStepName(); // face_swap, voice_clone, lip_sync等
       // 根据 stepName 做不同处理
   }
   ```

3. **便于维护和监控**
   - 统一日志记录
   - 统一错误处理
   - 统一响应格式

---

### ❓ 问题2：用CompletableFuture编排是否还需要回调接口？

**答：** **仍然需要回调接口**！原因：

1. **算法服务是外部异步服务**
   ```
   你 → 调用算法服务 → 立即返回taskId → [10s-3min处理] → 算法服务回调你
   ```

2. **CompletableFuture的作用**
   - ❌ 不能让算法服务同步返回（它处理需要很长时间）
   - ✅ 只能编排**你自己的异步任务**
   - ✅ 可以**等待**回调完成

3. **正确的架构**
   ```
   算法服务（外部）
       ↓ 调用
   [立即返回taskId]
       ↓
   CompletableFuture.wait() ← 你的服务等待
       ↓
   [回调接口] ← 算法服务处理完后调用
       ↓
   CompletableFuture.complete() ← 唤醒等待
   ```

---

## ✅ 推荐方案：CountDownLatch + CompletableFuture

### 核心思想

```
┌─────────────────────────────────────────────┐
│  模板处理器（你的代码）                        │
│  使用 CompletableFuture 编排流程               │
│  使用 CountDownLatch 等待回调                 │
└─────────────────────────────────────────────┘
                    ↓ 调用
┌─────────────────────────────────────────────┐
│  算法服务（外部）                            │
│  人脸替换、声音克隆、声音合成、唇形同步         │
└─────────────────────────────────────────────┘
                    ↓ 回调
┌─────────────────────────────────────────────┐
│  通用回调接口（/api/callback/algorithm）     │
│  1. 接收回调                                 │
│  2. 存储到Redis                              │
│  3. 唤醒CountDownLatch                       │
└─────────────────────────────────────────────┘
```

---

## 🎯 模板1-4的完整流程

### 流程图

```
用户上传照片 + 音频
        ↓
┌───────────────────────────────────────────┐
│  阶段1：并行处理（可同时进行）              │
│  ┌──────────────┐  ┌──────────────┐        │
│  │ 人脸替换      │  │ 声音克隆      │        │
│  │ 30秒        │  │ 60秒        │        │
│  └──────────────┘  └──────────────┘        │
│         ↓                  ↓               │
│  face_swap_result   voice_clone_result    │
└───────────────────────────────────────────┘
        ↓                  ↓
┌───────────────────────────────────────────┐
│  阶段2：串行处理（必须按顺序）              │
│         ↓                                  │
│  ┌──────────────┐  voice_clone_result      │
│  │ 声音合成      │  (voice_id)              │
│  │ 60秒        │         ↓                 │
│  └──────────────┘  ┌──────────────┐        │
│         ↓         │ 唇形同步      │        │
│  audio_result    │ 60秒        │        │
│         ↓         └──────────────┘        │
│  ┌──────────────────────────┐             │
│  │ 最终视频                  │             │
│  └──────────────────────────┘             │
└───────────────────────────────────────────┘
```

### 代码实现

```java
@Override
public String process(String recordId, Spring2026Template template, VideoCreateDTO dto) {

    // ====== 阶段1：并行执行 ======
    CompletableFuture<Callback> faceSwap = CompletableFuture.supplyAsync(() ->
        performFaceSwap(templateVideoUrl, userPhotoUrl, recordId)
    );

    CompletableFuture<Callback> voiceClone = CompletableFuture.supplyAsync(() ->
        performVoiceClone(userAudioUrl, recordId)
    );

    // 等待两个并行任务都完成
    CompletableFuture.allOf(faceSwap, voiceClone).join();

    String faceSwapVideoUrl = faceSwap.get().getResultUrl();
    String voiceId = voiceClone.get().getResultUrl();

    // ====== 阶段2：串行执行 ======
    Callback voiceTts = performVoiceTts(voiceId, recordId);
    Callback lipSync = performLipSync(faceSwapVideoUrl, voiceTts.getResultUrl(), recordId);

    return lipSync.getResultUrl();
}
```

---

## 🔧 关键代码：等待回调

### 调用算法服务并等待

```java
public AlgorithmCallbackRequest callAndWaitForCallback(
    String recordId,
    String stepName,
    Supplier<AlgorithmResponse> callSupplier,
    int timeoutSeconds) throws Exception {

    // 1. 创建CountDownLatch
    CountDownLatch latch = new CountDownLatch(1);
    callbackLatches.put(recordId + ":" + stepName, latch);

    try {
        // 2. 调用算法服务（立即返回taskId）
        AlgorithmResponse response = callSupplier.get();

        // 3. 等待回调（阻塞当前线程，最多等timeoutSeconds秒）
        boolean success = latch.await(timeoutSeconds, TimeUnit.SECONDS);

        if (!success) {
            throw new RuntimeException("等待超时");
        }

        // 4. 获取回调结果
        return callbackResults.get(recordId + ":" + stepName);

    } finally {
        callbackLatches.remove(recordId + ":" + stepName);
    }
}
```

### 回调接口处理

```java
@PostMapping("/algorithm")
public Object handleCallback(@RequestBody AlgorithmCallbackRequest request) {
    String recordId = request.getTaskId();
    String stepName = request.getStepName();

    // 1. 存储结果到内存（供CountDownLatch等待获取）
    callbackResults.put(recordId + ":" + stepName, request);

    // 2. 唤醒CountDownLatch
    CountDownLatch latch = callbackLatches.remove(recordId + ":" + stepName);
    if (latch != null) {
        latch.countDown(); // ← 这里会唤醒等待中的线程
    }

    // 3. 持久化到Redis（防止重启丢失）
    redisTemplate.opsForValue().set(cacheKey, request, 1, TimeUnit.HOURS);

    return Map.of("code", 0, "message", "success");
}
```

---

## 📊 时间线示例

```
T=0s    调用人脸替换算法 → 立即返回taskId
        调用声音克隆算法 → 立即返回taskId

T=1s    faceSwap线程等待CountDownLatch...
        voiceClone线程等待CountDownLatch...

T=30s   算法服务回调: face_swap完成
        → CountDownLatch.countDown()
        → faceSwap线程继续执行

T=60s   算法服务回调: voice_clone完成
        → CountDownLatch.countDown()
        → voiceClone线程继续执行

T=60s   阶段1完成，两个线程都return结果
        → 开始阶段2：声音合成
```

---

## 🎨 两种使用模式

### 模式1：同步等待模式（推荐用于你当前场景）

```java
// 调用并等待回调完成
AlgorithmCallbackRequest callback = videoProcessingService.callAndWaitForCallback(
    recordId, "face_swap",
    () -> faceSwapService.swapFace(request),
    30  // 最多等30秒
);

// 可以直接使用结果
String videoUrl = callback.getResultUrl();
```

**优点**：
- ✅ 代码逻辑清晰
- ✅ 异常处理简单
- ✅ 适合你的场景（一个用户的完整流程）

**缺点**：
- ❌ 占用线程（但用线程池控制数量）
- ❌ 如果回调延迟，线程会阻塞

---

### 模式2：纯异步模式（适合高并发）

```java
// 只调用，不等待
AlgorithmResponse response = faceSwapService.swapFace(request);
String taskId = response.getData().getTaskId();

// 后续逻辑在回调接口中处理
@PostMapping("/algorithm")
public void handleCallback(AlgorithmCallbackRequest callback) {
    // 1. 根据taskId找到recordId
    // 2. 查询当前状态（当前执行到哪个步骤了）
    // 3. 继续执行下一步
    // 4. 更新数据库
}
```

**优点**：
- ✅ 不占用线程
- ✅ 更高并发

**缺点**：
- ❌ 流程复杂，需要状态机
- ❌ 调试困难
- ❌ 错误处理复杂

---

## 🚀 你的场景建议

**使用模式1（同步等待模式）**，原因：

1. **一次完整的视频生成**（不是批处理）
2. **实时反馈需求**（需要进度更新）
3. **可控的并发**（用线程池限制并发数）
4. **代码可读性**（便于维护和调试）

---

## ⚠️ 注意事项

1. **线程池配置**
```java
@Bean("videoTaskExecutor")
public Executor videoTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);    // 核心线程数
    executor.setMaxPoolSize(50);     // 最大线程数
    executor.setQueueCapacity(100);  // 队列容量
    return executor;
}
```

2. **超时设置**
- 人脸替换：30秒
- 声音克隆：60秒
- 声音合成：60秒
- 唇形同步：60秒

3. **Redis持久化**
- 即使重启，回调结果不丢失
- 支持查询历史状态

4. **错误重试**
- 超时后可以重试
- 失败后记录错误信息

---

## 📝 完整示例

参见：
- `VideoProcessingServiceV2.java` - 核心编排服务
- `Template1to4ProcessorV2.java` - 模板1-4处理器
- `AlgorithmCallbackController.java` - 回调接口
