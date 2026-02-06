# 管理员接口使用指南

## 🔐 认证说明

所有管理员接口都需要在请求头中携带管理员ID：

```
X-User-UUID: admin_user_id
```

---

## 📋 管理员接口列表

### 1. 封禁用户

**接口：** `POST /admin/users/ban`

**权限要求：** 管理员及以上（adminLevel >= 2）

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| targetId | String | 是 | 目标用户ID |
| banReason | String | 是 | 封禁原因 |
| banDays | Integer | 否 | 封禁天数（默认0，0表示永久封禁） |

**请求示例：**
```bash
# 永久封禁用户
curl -X POST "http://localhost:8080/api/admin/users/ban" \
  -H "X-User-UUID: admin" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "targetId=user123&banReason=违规内容&banDays=0"

# 临时封禁7天
curl -X POST "http://localhost:8080/api/admin/users/ban" \
  -H "X-User-UUID: admin" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "targetId=user123&banReason=多次违规&banDays=7"
```

**封禁效果：**
- ✅ 账号状态设置为禁用（accountStatus = 0）
- ✅ 禁止上传文件（canUpload = 0）
- ✅ 禁止创建视频（canCreateVideo = 0）
- ✅ 记录封禁原因和结束时间

---

### 2. 解封用户

**接口：** `POST /admin/users/unban`

**权限要求：** 管理员及以上（adminLevel >= 2）

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| targetId | String | 是 | 目标用户ID |

**请求示例：**
```bash
curl -X POST "http://localhost:8080/api/admin/users/unban" \
  -H "X-User-UUID: admin" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "targetId=user123"
```

**解封效果：**
- ✅ 恢复账号状态（accountStatus = 1）
- ✅ 恢复上传权限（canUpload = 1）
- ✅ 恢复创建视频权限（canCreateVideo = 1）
- ✅ 清除封禁原因和结束时间

---

### 3. 下线作品

**接口：** `POST /admin/works/take-down`

**权限要求：** 管理员及以上（adminLevel >= 2）

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| recordId | String | 是 | 创作记录ID |
| reason | String | 是 | 下线原因 |

**请求示例：**
```bash
curl -X POST "http://localhost:8080/api/admin/works/take-down" \
  -H "X-User-UUID: admin" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "recordId=rec:xxx&reason=违规内容"
```

**下线效果：**
- ✅ 作品状态设置为已下线（status = 4）
- ✅ 记录下线原因和操作者信息

---

### 4. 设置管理员级别

**接口：** `POST /admin/users/set-admin`

**权限要求：** 超级管理员（adminLevel = 3）

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| targetId | String | 是 | 目标用户ID |
| adminLevel | Integer | 是 | 管理员级别（0=普通用户 1=审核员 2=管理员 3=超级管理员） |

**请求示例：**
```bash
# 设置为管理员
curl -X POST "http://localhost:8080/api/admin/users/set-admin" \
  -H "X-User-UUID: admin" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "targetId=user456&adminLevel=2"

# 设置为审核员
curl -X POST "http://localhost:8080/api/admin/users/set-admin" \
  -H "X-User-UUID: admin" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "targetId=user789&adminLevel=1"
```

---

## 🎯 管理员级别说明

| 级别代码 | 级别名称 | 权限描述 |
|---------|---------|---------|
| 0 | 普通用户 | 无管理权限 |
| 1 | 审核员 | 可以审核内容 |
| 2 | 管理员 | 可以封禁用户、下线作品 |
| 3 | 超级管理员 | 可以设置管理员级别 |

---

## 🛡️ 权限控制规则

### 封禁用户规则
- ✅ 管理员可以封禁普通用户和审核员
- ❌ 管理员不能封禁同级或更高级别的管理员
- ✅ 超级管理员可以封禁所有级别的用户

### 设置管理员级别规则
- ❌ 只有超级管理员可以设置管理员级别
- ❌ 不能降低自己的管理员级别

---

## 📱 前端集成示例

### JavaScript 封装

```javascript
const ADMIN_API = 'http://localhost:8080/api/admin';

// 设置管理员ID（从登录信息获取）
const adminId = 'admin';

// 封禁用户
async function banUser(targetId, banReason, banDays = 0) {
  const params = new URLSearchParams({
    targetId: targetId,
    banReason: banReason,
    banDays: banDays.toString()
  });

  const response = await fetch(`${ADMIN_API}/users/ban`, {
    method: 'POST',
    headers: {
      'X-User-UUID': adminId,
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: params
  });

  return await response.json();
}

// 解封用户
async function unbanUser(targetId) {
  const params = new URLSearchParams({ targetId });

  const response = await fetch(`${ADMIN_API}/users/unban`, {
    method: 'POST',
    headers: {
      'X-User-UUID': adminId,
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: params
  });

  return await response.json();
}

// 下线作品
async function takeDownWork(recordId, reason) {
  const params = new URLSearchParams({
    recordId: recordId,
    reason: reason
  });

  const response = await fetch(`${ADMIN_API}/works/take-down`, {
    method: 'POST',
    headers: {
      'X-User-UUID': adminId,
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: params
  });

  return await response.json();
}

// 设置管理员级别
async function setAdminLevel(targetId, adminLevel) {
  const params = new URLSearchParams({
    targetId: targetId,
    adminLevel: adminLevel.toString()
  });

  const response = await fetch(`${ADMIN_API}/users/set-admin`, {
    method: 'POST',
    headers: {
      'X-User-UUID': adminId,
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: params
  });

  return await response.json();
}
```

### 使用示例

```javascript
// 封禁用户7天
await banUser('user123', '多次发布违规内容', 7);

// 永久封禁用户
await banUser('user456', '严重违规', 0);

// 解封用户
await unbanUser('user123');

// 下线作品
await takeDownWork('rec:abc123', '涉及敏感内容');

// 设置为管理员
await setAdminLevel('user789', 2);
```

---

## ⚠️ 注意事项

1. **权限验证**：所有接口都会验证操作者的管理员级别
2. **操作日志**：所有管理操作都会记录日志，便于审计
3. **不可逆操作**：封禁和解封都是即时生效的，请谨慎操作
4. **级别限制**：不能封禁同级或更高级别的管理员
5. **初始化管理员**：数据库中已初始化超级管理员账号（user_id = 'admin'），请及时修改密码

---

## 🚀 后续扩展

- [ ] 添加操作日志查询接口
- [ ] 添加批量封禁功能
- [ ] 添加封禁原因分类
- [ ] 添加用户申诉功能