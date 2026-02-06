# 素材管理功能使用指南

## 📋 功能概述

新增用户素材管理功能，支持：
1. ✅ 用户上传图片/音频素材，自动落库记录
2. ✅ 查询用户的所有素材
3. ✅ 通过素材ID（materialId）选择素材生成视频
4. ✅ 向后兼容：仍支持直接传URL

---

## 🗄️ 数据库变更

执行 SQL 文件：
```bash
mysql -u root -p spring_2026_festival < src/main/resources/sql/add_user_material_table.sql
```

新增表：`spring_2026_user_material`

---

## 🔧 API 接口

### 1. 上传素材（复用现有接口）

**上传图片：**
```bash
POST /api/upload/image
Content-Type: multipart/form-data

userId: user123
file: [图片文件]
```

**上传音频：**
```bash
POST /api/upload/audio
Content-Type: multipart/form-data

userId: user123
file: [音频文件]
```

**响应（新增 materialId 字段）：**
```json
{
  "code": 200,
  "data": {
    "url": "https://oss.example.com/user123/image/xxx.jpg",
    "name": "photo.jpg",
    "size": 1234567,
    "materialId": "mat:3f9a1b2c3d4e5f6g"  // 新增字段
  }
}
```

---

### 2. 查询素材（新增接口）

**查询用户的所有图片：**
```bash
GET /api/material/photos/{userId}
```

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "materialId": "mat:3f9a1b2c3d4e5f6g",
      "materialType": "photo",
      "fileUrl": "https://oss.example.com/user123/image/xxx.jpg",
      "originalFilename": "photo.jpg",
      "fileSize": 1234567,
      "uploadTime": "2026-02-06T10:30:00"
    },
    // ... 更多素材
  ]
}
```

**查询用户的所有音频：**
```bash
GET /api/material/audios/{userId}
```

**查询素材详情：**
```bash
GET /api/material/info/{materialId}
```

---

### 3. 生成视频（修改接口）

**方式1：使用 materialId（推荐）**
```bash
POST /api/video/create
Content-Type: application/json

{
  "templateId": "tpl_001",
  "materials": {
    "photoMaterialIds": ["mat:xxx", "mat:yyy"],
    "audioMaterialIds": ["mat:zzz"]
  }
}
```

**方式2：直接传URL（向后兼容）**
```bash
POST /api/video/create
Content-Type: application/json

{
  "templateId": "tpl_001",
  "materials": {
    "photos": ["https://oss.example.com/xxx.jpg"],
    "audios": ["https://oss.example.com/yyy.mp3"]
  }
}
```

---

## 📱 前端集成示例

### 完整流程示例

```javascript
// 1. 上传图片素材
async function uploadPhoto(userId, file) {
  const formData = new FormData();
  formData.append('userId', userId);
  formData.append('file', file);

  const response = await fetch('/api/upload/image', {
    method: 'POST',
    body: formData
  });

  const result = await response.json();
  return result.data.materialId;  // 返回素材ID
}

// 2. 上传音频素材
async function uploadAudio(userId, file) {
  const formData = new FormData();
  formData.append('userId', userId);
  formData.append('file', file);

  const response = await fetch('/api/upload/audio', {
    method: 'POST',
    body: formData
  });

  const result = await response.json();
  return result.data.materialId;  // 返回素材ID
}

// 3. 查询用户的所有图片
async function getUserPhotos(userId) {
  const response = await fetch(`/api/material/photos/${userId}`);
  const result = await response.json();
  return result.data;  // 返回素材列表
}

// 4. 查询用户的所有音频
async function getUserAudios(userId) {
  const response = await fetch(`/api/material/audios/${userId}`);
  const result = await response.json();
  return result.data;  // 返回素材列表
}

// 5. 选择素材并生成视频
async function createVideo(userId, photoMaterialId, audioMaterialId, templateId) {
  const response = await fetch('/api/video/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      templateId: templateId,
      materials: {
        photoMaterialIds: [photoMaterialId],
        audioMaterialIds: [audioMaterialId]
      }
    })
  });

  const result = await response.json();
  return result.data.recordId;
}
```

---

## 🔄 数据流转

```
用户上传 → OSS存储 → 素材表记录 → 返回materialId
                         ↓
                   前端展示素材列表
                         ↓
                 用户选择素材 → 生成视频请求
                         ↓
                后端根据materialId获取URL
                         ↓
                     调用算法处理
```

---

## ⚠️ 注意事项

1. **向后兼容**：旧的直接传URL方式仍然有效
2. **优先级**：materialId 优先于直接URL
3. **删除功能**：当前不支持删除，可添加 status=0 软删除
4. **权限控制**：用户只能查询和使用自己的素材
5. **素材审核**：后续可扩展素材审核功能

---

## 🚀 后续扩展

- [ ] 素材删除接口
- [ ] 素材编辑接口（重命名）
- [ ] 素材分组功能
- [ ] 素材使用统计
- [ ] 素材审核机制
- [ ] 素材过期清理