# 多图生图实现更新说明

## 📋 更新内容

根据实际API请求示例，更新了多图生图服务的实现。

## 🔍 实际API格式

### 请求示例
```bash
curl --location "http://127.0.0.1:${PORT}/invoke/sync" \
--header 'content-type: application/json' \
--data '{
    "targetDir": "temp/",
    "images": ["https://dreamface-resource-cv.oss-us-east-1.aliyuncs.com/dsw-dev/2026-01-27/01.png",
               "https://dreamface-resource-cv.oss-us-east-1.aliyuncs.com/dsw-dev/2026-01-27/11.png"],
    "prompt": "The person in Figure 1 wears the clothes in Figure 2",
    "width": 1440,
    "height": 1440,
    "num": 1
}'
```

### 关键发现

1. **同步接口** - 使用 `/invoke/sync`，不是异步回调
2. **images 是数组** - 支持多个输入图片
3. **包含 width/height** - 指定生成图片的尺寸
4. **使用 num** 不是 `count`
5. **包含 targetDir** - 输出目录

## 🔄 代码更新

### 1. MultiImageGenerateRequest

**更新前：**
```java
@Data
public class MultiImageGenerateRequest {
    private String imageUrl;      // ❌ 单个图片
    private String prompt;
    private Integer count = 1;    // ❌ 错误字段名
    private String callbackUrl;   // ❌ 不需要
}
```

**更新后：**
```java
@Data
public class MultiImageGenerateRequest {
    private String targetDir = "temp/";
    private List<String> images;  // ✅ 多个图片URL
    private String prompt;
    private Integer width = 1440;
    private Integer height = 1440;
    private Integer num = 1;      // ✅ 正确字段名
}
```

### 2. MultiImageGenerateService

**更新前：**
- 使用异步回调模式
- 调用 `/api/multi_image_generate`
- 返回 `AlgorithmResponse`

**更新后：**
```java
public List<String> generate(MultiImageGenerateRequest request) {
    String url = properties.getMultiImageGenerate().getUrl() + "/invoke/sync";

    HttpResponse response = HttpRequest.post(url)
        .header("Content-Type", "application/json")
        .body(JSONUtil.toJsonStr(request))
        .timeout(120000)  // 2分钟
        .execute();

    // 解析响应，返回图片URL列表
    List<String> imageUrls = ...;
    return imageUrls;
}
```

### 3. Template1to4Processor

**更新前：**
```java
// 使用 callAndWaitForCallback 等待异步回调
Map<String, Object> result = videoProcessingService.callAndWaitForCallback(
    recordId, "multi_image_generate",
    () -> multiImageGenerateService.generate(request),
    120
);
```

**更新后：**
```java
// 直接同步调用
MultiImageGenerateRequest request = new MultiImageGenerateRequest();
request.setImages(Arrays.asList(userPhotoUrl));
request.setPrompt(IMAGE_GEN_PROMPT);
request.setWidth(1440);
request.setHeight(1440);
request.setNum(1);

List<String> generatedImages = multiImageGenerateService.generate(request);
return generatedImages.get(0);  // 返回第一张生成的图片
```

### 4. buildCallbackUrl 方法

移除了 `multi_image_generate` 的分支，因为它是同步接口，不需要回调URL。

## ⚠️ 重要说明

### 关于图片输入

从示例看，多图生图支持多张图片输入，例如：
- 图片1：人物照片
- 图片2：衣服/配饰参考
- 提示词："The person in Figure 1 wears the clothes in Figure 2"

**当前实现：**
- 只使用用户上传的单张照片
- 提示词：描述生成风格（春节主题，喜庆氛围）

**如果需要两张图片：**
```java
// 示例：用户照片 + 参考图片
String userPhotoUrl = dto.getMaterials().getPhotos().get(0);
String referenceImageUrl = "https://your-oss-bucket.com/templates/reference_clothes.jpg";

request.setImages(Arrays.asList(userPhotoUrl, referenceImageUrl));
request.setPrompt("The person in Figure 1 wears the clothes in Figure 2");
```

### 提示词建议

**当前提示词：**
```
"Spring Festival theme, festive atmosphere, high quality portrait, Chinese New Year celebration"
```

**根据实际需求调整：**

1. **只生成风格化人物照：**
   ```
   "Spring Festival theme, festive atmosphere, high quality portrait, red and gold colors, traditional Chinese style"
   ```

2. **换装/换动作（需要参考图）：**
   ```
   "The person in Figure 1 wears the clothes in Figure 2, maintains facial features, festive background"
   ```

3. **春节主题特效：**
   ```
   "Chinese New Year celebration, festive atmosphere, lanterns and decorations background, happy expression"
   ```

## 📊 响应格式假设

根据API推测，响应格式可能是：

### 格式1：直接返回图片列表
```json
{
    "images": ["https://oss-url.com/generated_1.jpg"],
    "status": "success"
}
```

### 格式2：统一格式
```json
{
    "code": 0,
    "message": "success",
    "data": {
        "images": ["https://oss-url.com/generated_1.jpg"]
    }
}
```

**代码中已兼容这两种格式。**

## ✅ 测试建议

### 1. 测试单图输入
```java
MultiImageGenerateRequest request = new MultiImageGenerateRequest();
request.setImages(Arrays.asList("https://user-photo.jpg"));
request.setPrompt("Spring Festival theme");
request.setNum(1);
```

### 2. 测试双图输入
```java
MultiImageGenerateRequest request = new MultiImageGenerateRequest();
request.setImages(Arrays.asList(
    "https://user-photo.jpg",
    "https://reference-clothes.jpg"
));
request.setPrompt("The person in Figure 1 wears the clothes in Figure 2");
request.setNum(1);
```

### 3. 测试尺寸参数
```java
request.setWidth(720);   // 尝试不同尺寸
request.setHeight(720);
```

## 🎯 下一步

1. **确认实际响应格式** - 测试后根据实际响应调整解析逻辑
2. **配置实际URL** - 更新 `AlgorithmProperties` 中的服务地址
3. **调整提示词** - 根据实际效果优化提示词
4. **处理多图输出** - 如果 `num > 1`，处理多张生成的图片

## 📝 配置示例

```yaml
algorithm:
  multi-image-generate:
    url: http://127.0.0.1:8080  # 实际服务地址
    timeout: 120000  # 2分钟
```

---

**更新时间：** 2026-02-05
**更新原因：** 根据实际API请求示例调整实现
