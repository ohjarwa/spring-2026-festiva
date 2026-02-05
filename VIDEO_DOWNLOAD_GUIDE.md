# 视频下载功能说明

## ✅ 现在您有两个视频下载接口

### 1️⃣ 正式接口（推荐）
```
GET /api/video/download?recordId={recordId}&userId={userId}
```

### 2️⃣ 测试接口
```
GET /api/oss/test/download?key={objectKey}
```

---

## 📋 正式接口详细说明

### 接口信息

**路径：** `/api/video/download`

**方法：** `GET`

**参数：**
- `recordId` - 视频记录ID
- `userId` - 用户ID（用于权限验证）

**返回：** 视频文件流（直接下载）

### 功能特性

1. ✅ **权限验证** - 验证用户是否为视频所有者
2. ✅ **状态检查** - 检查视频是否生成完成
3. ✅ **自动识别OSS账户** - 自动判断使用default还是cv账户
4. ✅ **文件名生成** - 自动命名为 `{recordId}_final.mp4`
5. ✅ **流式下载** - 支持大文件流式传输
6. ✅ **错误处理** - 完善的错误提示和日志

### 使用示例

```bash
# 示例1：下载视频
curl "http://localhost:8080/api/video/download?recordId=record_123&userId=user_001" \
  --output video.mp4

# 示例2：浏览器直接访问
http://localhost:8080/api/video/download?recordId=record_123&userId=user_001
```

### 响应示例

**成功（200 OK）：**
```http
HTTP/1.1 200 OK
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="record_123_final.mp4"
Content-Length: 5242880

<视频二进制数据>
```

**失败场景：**

1. **视频不存在（404）：**
```json
HTTP/1.1 404 Not Found
```

2. **视频未生成完成（400）：**
```json
HTTP/1.1 400 Bad Request
```

3. **无权限（404）**：返回404而不是403，避免信息泄露

---

## 🔧 下载流程

```
用户请求下载
    ↓
验证recordId和userId
    ↓
查询数据库获取视频记录
    ↓
检查status是否为2（已完成）
    ↓
获取resultUrl
    ↓
从URL提取fileKey
    ↓
判断OSS账户（default或cv）
    ↓
检查文件是否存在
    ↓
从OSS获取文件流
    ↓
流式传输给用户
```

---

## 🧪 测试接口说明

### 接口信息

**路径：** `/api/oss/test/download`

**方法：** `GET`

**参数：**
- `key` - OSS对象Key（如：`spring2026/record_123/videos/final_result.mp4`）

**返回：** 文件流

### 使用示例

```bash
# 直接下载OSS文件
curl "http://localhost:8080/api/oss/test/download?key=spring2026/record_123/videos/final_result.mp4" \
  --output test_video.mp4
```

---

## 📊 两种接口的区别

| 特性 | /api/video/download | /api/oss/test/download |
|------|---------------------|------------------------|
| **用途** | 正式视频下载 | OSS测试接口 |
| **权限验证** | ✅ 验证userId | ❌ 无验证 |
| **参数** | recordId + userId | objectKey |
| **状态检查** | ✅ 检查视频状态 | ❌ 不检查 |
| **URL识别** | ✅ 自动识别 | ❌ 需手动指定 |
| **文件名** | ✅ recordId_final.mp4 | ❌ 原始文件名 |
| **推荐场景** | 生产环境 | 开发测试 |

---

## 🎯 使用建议

### 生产环境（前端调用）

使用 **正式接口**：`/api/video/download`

**前端调用示例：**

```javascript
// 方式1：直接下载链接
const downloadUrl = `/api/video/download?recordId=${recordId}&userId=${userId}`;
window.location.href = downloadUrl;

// 方式2：fetch下载
fetch(`/api/video/download?recordId=${recordId}&userId=${userId}`)
  .then(response => response.blob())
  .then(blob => {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${recordId}_final.mp4`;
    a.click();
  });
```

### 开发测试

使用 **测试接口**：`/api/oss/test/download`

```bash
# 查看帮助
curl "http://localhost:8080/api/oss/test/help"

# 下载指定文件
curl "http://localhost:8080/api/oss/test/download?key=spring2026/record_123/videos/final_result.mp4" \
  -O test.mp4
```

---

## ⚠️ 注意事项

### 1. Bucket访问权限

- 如果Bucket是**私有**，文件会通过服务器流式传输
- 如果Bucket是**公共**，可以考虑直接返回URL重定向

### 2. 文件大小限制

- 使用流式传输，理论上支持任意大小文件
- 建议前端显示下载进度（大文件）

### 3. 并发下载

- 服务端使用流式传输，内存占用低
- 支持多个用户同时下载

### 4. 下载统计

建议添加下载统计功能：
```java
// 在下载成功后增加下载次数
recordMapper.incrementDownloadCount(recordId);
```

---

## 🔍 故障排查

### 问题1：下载404

**检查项：**
- recordId是否正确
- 视频是否生成完成（status=2）
- resultUrl是否为空

**解决：**
```sql
SELECT record_id, status, result_url
FROM spring_2026_creation_record
WHERE record_id = 'record_123';
```

### 问题2：下载失败

**检查日志：**
```bash
tail -f logs/spring.log | grep "下载视频"
```

**常见错误：**
- OSS文件不存在 → 检查fileKey是否正确
- OSS账户错误 → 检查bucket名称匹配
- 网络超时 → 检查OSS访问速度

### 问题3：下载的视频无法播放

**可能原因：**
- 文件损坏 → 重新生成视频
- 编码不支持 → 检查视频编码格式

---

## 📝 代码位置

**VideoController.java**
- 路径：`src/main/java/org/example/newyear/controller/VideoController.java`
- 方法：`downloadVideo()`
- 行数：第74-147行

**OssTestController.java**
- 路径：`src/main/java/org/example/newyear/controller/OssTestController.java`
- 方法：`download()`
- 行数：第82-117行

---

**更新时间：** 2026-02-05
**功能状态：** ✅ 已实现
