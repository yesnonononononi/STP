# STP 管理端重构——后端需提供接口契约规范文档 (API Specification)

本文档由前端根据本次管理端重构需求全盘整理生成，列出了**会员管理**、**优惠券及活动管理**、**帖子管理**、**评论管理**、**订单管理**及**系统通知管理** 6 大模块所需后端服务提供的高级管理 API 规范。所有会员管理接口均严密对齐后端现有的数据库表结构（`member_level_config`、`member_package`、`user_member`），不要求后端新增或修改数据库表。

---

## 1. 会员管理模块 (Member Management - 对齐后端数据库表)

### 1.1 等级配置查询 (`member_level_config` 表)
- **URL**: `GET /a/member/level-config/list`
- **说明**: 查询会员等级配置列表。
- **响应数据格式 (`Result<List<MemberLevelConfigVO>>`)**:
```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "level": 1,               // bigint, 主键
      "levelName": "普通会员",   // varchar
      "minRecharge": 0.0,      // double, 最低充值门槛
      "privilegesJson": "{}",  // varchar, 特权配置JSON
      "iconUrl": "https://...",// varchar, 图标URL
      "sortOrder": 1           // bigint, 排序
    }
  ]
}
```

### 1.2 保存/更新等级配置 (`member_level_config` 表)
- **URL**: `POST /a/member/level-config/save`
- **Body (`MemberLevelConfigDTO`)**:
```json
{
  "level": 2,
  "levelName": "黄金 VIP",
  "minRecharge": 200.0,
  "privilegesJson": "{\"desc\":\"9.5折优惠\"}",
  "iconUrl": "https://...",
  "sortOrder": 2
}
```

### 1.3 删除等级配置 (`member_level_config` 表)
- **URL**: `POST /a/member/level-config/delete/{level}`

---

### 1.4 会员套餐配置列表查询 (`member_package` 表)
- **URL**: `GET /a/member/package/list`
- **说明**: 查询后台配置的会员购买套餐（月卡、年卡等）。
- **响应数据格式 (`Result<List<MemberPackageVO>>`)**:
```json
{
  "code": 1,
  "data": [
    {
      "id": 1,                 // bigint, 自增主键
      "name": "VIP 连续包月套餐",// varchar
      "price": 25.00,          // decimal, 售价
      "duration": 30,          // int, 有效天数
      "description": "自动续费",// varchar, 描述
      "typeId": 1,             // bigint, 关联会员类型ID
      "stock": 9999,           // int, 库存数量
      "discount": 8.5,         // double, 折扣比例
      "dailyRate": 0.83,       // decimal, 日均单价
      "priority": 1,           // int, 排序优先级
      "createTime": "2026-01-01 00:00:00"
    }
  ]
}
```

### 1.5 保存/更新会员套餐 (`member_package` 表)
- **URL**: `POST /a/member/package/save`
- **Body (`MemberPackageDTO`)**:
```json
{
  "id": 1, // 新增时为空
  "name": "VIP 连续包月套餐",
  "price": 25.00,
  "duration": 30,
  "description": "自动续费",
  "typeId": 1,
  "stock": 9999,
  "discount": 8.5,
  "priority": 1
}
```

### 1.6 删除会员套餐 (`member_package` 表)
- **URL**: `POST /a/member/package/delete/{id}`

---

### 1.7 用户会员记录查询 (`user_member` 表)
- **URL**: `GET /a/member/user/list`
- **Params**: `userId` (可选), `page`, `size`
- **响应数据格式 (`Result<PageResult<UserMemberVO>>`)**:
```json
{
  "code": 1,
  "data": {
    "list": [
      {
        "userId": 10086,                       // bigint, 主键
        "totalRecharge": 580.00,               // double, 累计充值金额
        "vipLevel": 2,                          // bigint, VIP等级
        "packageTypeId": 1,                    // bigint, 套餐类型ID
        "expireTime": "2026-12-31 23:59:59",    // timestamp, 到期时间
        "levelUpgradeTime": "2026-06-01 10:00:00" // timestamp, 升级时间
      }
    ],
    "total": 1
  }
}
```

### 1.8 手工调整用户 VIP 等级与到期时间 (`user_member` 表)
- **URL**: `POST /a/member/user/update-level`
- **Body**:
```json
{
  "userId": 10086,
  "vipLevel": 3,
  "expireTime": "2027-12-31 23:59:59"
}
```

---

## 2. 优惠券与营销活动模块 (Coupon & Activity Management)

### 2.1 分页查询优惠券列表
- **URL**: `GET /a/coupon/list`
- **Params**: `keyword`, `status`, `page`, `size`
- **响应格式 (`Result<PageResult<CouponVO>>`)**:
```json
{
  "code": 1,
  "data": {
    "list": [
      {
        "id": 1001,
        "title": "双11全场无门槛立减券",
        "couponType": "CASH", // CASH:满减, DISCOUNT:折扣
        "discountValue": 15.0,
        "minThreshold": 0.0,
        "totalCount": 2000,
        "remainCount": 1450,
        "perUserLimit": 1,
        "status": 1, // 1:正常, 0:封禁/作废
        "validStartTime": "2026-11-01 00:00:00",
        "validEndTime": "2026-11-12 23:59:59"
      }
    ],
    "total": 1
  }
}
```

### 2.2 保存/更新优惠券
- **URL**: `POST /a/coupon/save`
- **Body (`CouponSaveDTO`)**:
```json
{
  "id": 1001,
  "title": "双11全场无门槛立减券",
  "couponType": "CASH",
  "discountValue": 15.0,
  "minThreshold": 0.0,
  "totalCount": 2000,
  "perUserLimit": 1
}
```

### 2.3 封禁 / 解封优惠券
- **URL**: `POST /a/coupon/toggle-status`
- **Body**: `{ "id": 1001, "status": 0 }` // 0:封禁, 1:解封

### 2.4 删除优惠券
- **URL**: `POST /a/coupon/delete/{id}`

---

### 2.5 营销活动列表查询
- **URL**: `GET /a/coupon/activity/list`
- **Params**: `keyword`, `status`, `page`, `size`

### 2.6 创建/修改营销活动
- **URL**: `POST /a/coupon/activity/save`
- **Body (`CouponActivitySaveDTO`)**:
```json
{
  "id": 201,
  "activityName": "暑期冲浪季·满减券大发放",
  "description": "全员可领，限时3天",
  "associatedCouponId": 1001
}
```

### 2.7 开启 / 暂停营销活动
- **URL**: `POST /a/coupon/activity/toggle-status`
- **Body**: `{ "id": 201, "status": 0 }` // 1:开启/进行中, 0:暂停

---

## 3. 帖子管理模块 (Post Management)

### 3.1 多条件组合筛选查询帖子列表
- **URL**: `POST /a/post/list`
- **Body (`PostQueryPayload`)**:
```json
{
  "page": 1,
  "size": 10,
  "keyword": "Spring Boot",
  "topicName": "Java后端",
  "status": 1, // 1:通过, 0:驳回, 2:待审核
  "authorId": 10086,
  "minLikes": 10,
  "minComments": 2
}
```

### 3.2 审核通过 / 驳回不通过 Toggle 帖子
- **URL**: `POST /a/post/toggle-status`
- **说明**: 当驳回不通过 (`status=0`) 时，带上前端弹窗输入的 `rejectReason` 驳回原由。
- **Body**:
```json
{
  "id": 502,
  "status": 0, // 0:驳回不通过, 1:审核通过
  "rejectReason": "包含违规违法/敏感内容"
}
```

### 3.3 删除帖子
- **URL**: `POST /a/post/delete/{id}`

---

## 4. 评论管理模块 (Comment Management)

### 4.1 评论列表查询 (按被举报状态等筛选)
- **URL**: `POST /a/comment/list`
- **Body (`CommentQueryPayload`)**:
```json
{
  "page": 1,
  "size": 10,
  "keyword": "",
  "reportStatus": 1, // 0:未被举报, 1:已被举报(待处理), 2:举报已处理
  "status": 1
}
```

### 4.2 审核通过 / 屏蔽 Toggle 评论
- **URL**: `POST /a/comment/toggle-status`
- **Body**: `{ "id": 802, "status": 0 }` // 0:屏蔽违规, 1:正常显示

### 4.3 忽略/处理评论举报
- **URL**: `POST /a/comment/report/ignore/{id}`

---

## 5. 订单交易管理模块 (Order Management)

### 5.1 多条件筛选查询订单列表
- **URL**: `POST /a/order/list`
- **Body (`OrderQueryPayload`)**:
```json
{
  "page": 1,
  "size": 10,
  "orderNo": "ORD202608169901",
  "userId": 10086,
  "orderType": "MEMBER", // MEMBER | COUPON_PACKAGE | OTHER
  "payStatus": "PAID"   // PAID | UNPAID | REFUNDED | FAILED
}
```

### 5.2 异常订单手工补单
- **URL**: `POST /a/order/reissuance`
- **说明**: 针对第三方渠道显示支付成功，但系统未完成权益履约发放的订单，重新触发履约流程。
- **Body**: `{ "orderNo": "ORD202608169901" }`

### 5.3 订单退单退款
- **URL**: `POST /a/order/refund`
- **Body**:
```json
{
  "orderNo": "ORD202608169901",
  "reason": "用户误购申请退款"
}
```

### 5.4 订单渠道状态实时核对
- **URL**: `POST /a/order/verify-status`
- **说明**: 主动向微信/支付宝等三方渠道发起交易流水核对，并自动同步补全内部状态。
- **Body**: `{ "orderNo": "ORD202608169901" }`

---

## 6. 系统通知管理模块 (Notification Management)

### 6.1 系统通知列表查询
- **URL**: `POST /a/notification/list`
- **Body**:
```json
{
  "page": 1,
  "size": 10,
  "keyword": "维护公告",
  "noticeType": "MAINTENANCE"
}
```

### 6.2 发布系统通知/广播
- **URL**: `POST /a/notification/create`
- **Body (`NotificationCreateDTO`)**:
```json
{
  "title": "STP 社区 2.0 系统升级维护公告",
  "content": "为了提供更好的服务体验，系统将于今晚 02:00 进行例行维护升级...",
  "noticeType": "MAINTENANCE", // SYSTEM | ACTIVITY | MAINTENANCE
  "targetType": "ALL",        // ALL | MEMBER_ONLY | SPECIFIC_USER
  "targetUserId": null
}
```

### 6.3 撤回系统通知
- **URL**: `POST /a/notification/revoke/{id}`

### 6.4 删除系统通知
- **URL**: `POST /a/notification/delete/{id}`
