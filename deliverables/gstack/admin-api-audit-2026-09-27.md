# STP 项目管理端（Admin）全量接口审计报告

**日期**：2026-09-27
**场景**：上线前全流程审计（安全审计 + 代码审查 + 数据真实性/性能调查）
**审计范围**：各微服务 `**/admin/**` 包下全部管理端接口（13 个 Controller / 768 行）+ 支撑的 application/infrastructure 代码（admin 相关 3799 行 / 84 文件）+ `frontend-admin` 前端服务层 + 网关与公共鉴权链路
**参与成员**：安全官（gstack-security-officer） + 产品评审员（gstack-product-reviewer） + 调查员（gstack-investigator）
**主理人**：沽思航（Gu）· 软件工坊 CEO —— 负责编排、交叉验证与汇编

---

## 📌 TL;DR（执行摘要）

- **整体结论：🔴 不通过（No-Go）**。管理端当前处于「鉴权形同虚设 + KPI 可能是假数据 + 分页不生效」的三重风险叠加状态，**不具备上线条件**。
- **阻塞项 7 条**，其中 3 条安全类可直接导致**匿名者获得完整管理员权限**（4 个 Controller 无鉴权注解 / 网关不剥离伪造头 / 全部微服务端口暴露）。
- **数据可信度问题严重**：后端硬编码 3 个系统指标为 0、前端在接口异常时用**编造的 KPI**（128,560 用户 / ¥894,520 GMV）冒充成功响应，管理者无法分辨真假。
- **性能侧系统性缺陷**：全仓库**缺失 MyBatis-Plus 分页拦截器**，管理端所有列表接口分页失效并可能全表加载。
- **下一步**：先做 3 项安全止血（补注解 + 网关剥离头 + 端口收敛），再做 4 项数据/性能修复，然后复测。

---

## 🎯 核心结论卡片

| 项目 | 内容 |
|------|------|
| **Go / No-Go** | 🔴 **No-Go**（存在可直接利用的越权与数据可信性问题） |
| **严重度分布** | 🔴 严重 **7** / 🟠 高 **8** / 🟡 中 **19** / 🟢 低 **4** |
| **阻塞项数量** | **7 条**（见行动清单 P0） |
| **关键行动项** | 12 条（P0×7 / P1×5） |
| **建议负责人** | 后端负责人（安全 3 项 + 数据/性能 4 项）、前端负责人（Mock 兜底 + 幽灵接口）、运维（端口收敛 + 密钥轮换） |
| **审计覆盖** | 13 个管理端 Controller、3799 行 admin 后端代码、12 个前端 service 文件、网关鉴权链、SqlConfig/缓存/线程池 |
| **交叉验证** | 主理人已独立复核 7 条 🔴 中的 7 条（全部证实） |

---

## 1. 各成员核心结论

### 🛡️ 安全官（OWASP Top 10 + STRIDE 审计）
- **核心判断**：管理端**没有真正的服务端鉴权边界**。唯一授权机制是 Spring AOP 注解，而 4 个 admin Controller 完全没加注解（可匿名调用封禁/发通知/发券/删审计日志）；网关对无 Token 请求直接放行且**不剥离客户端伪造的 `X-User-*` 头**，下游 `Interceptor` 无条件信任这些头构建 `UserSession`（含 `admin` 等级）→ **任意匿名者可完整冒充管理员**。整体评级 **F**。
- **关键建议**：网关入口**无条件 remove 所有 `X-User-*` / `X-Internal-Request` 头**，再由网关统一注入并签名；4 个 Controller 补齐类级 `@Admin @Login`；生产环境仅暴露网关端口；`/internal/**` 改用 mTLS 或网关签名令牌。
- **已确认无问题**：MyBatis XML 无 `${}` 拼接（无 SQL 注入）；JWT 用 JJWT `verifyWith` 强制验签、拒绝 none 算法（**JWT 本体安全**）；异常处理器不回传堆栈；未配置 CORS `*`。

### 🔍 产品评审员（代码臃肿 & 可维护性）
- **核心判断**：管理端是典型的「薄业务、厚脚手架」——单条接口普遍穿透 4-5 层，但真实业务分支极薄，3799 行里大量是手写 builder 映射、`Result` 包装、日志刷屏与复制粘贴。**最致命的不是"乱"而是"假"与"漏"**。整体可维护性评级 **Developing（偏弱）**。
- **关键建议**：①补齐 4 个漏标注解的 Controller（安全强关联）；②消灭假接口与静默 Mock（`refund()` 空实现、前端 107 行 Mock、6 个幽灵接口）；③启用 MapStruct 统一映射（pom 已引入但**使用 0 处**），删除 DTO=Command 重复层；④抽泛型 `parallelFetch` 消除 6 段复制粘贴；⑤建测试基线（全项目仅 5 个测试文件，admin 业务单测 **0**）。

### 🔧 调查员（数据真实性 & 性能）
- **核心判断**：最严重的是**「假数据」而非「没数据」**——前端 `dashboard.ts` 内置整套 MOCK 兜底，只要后端报错/超时/code≠1 就静默把假数当真实 KPI 渲染，且统一返回 `code:1`。后端同源问题：Feign 失败 → `return null` → Assembler 补 0 → **再写入 Redis 缓存 180s**，一次下游抖动让大盘显示"0 用户/¥0.00 GMV"长达 3 分钟。
- **关键建议**：①删除前端 MOCK 兜底改显式错误态；②**补上 MyBatis-Plus 分页拦截器**（`SqlConfig` 手写 `SqlSessionFactory` 未注册 `MybatisPlusInterceptor`）；③修复 `refund()` 空实现；④硬编码指标接真实数据源或从前端隐藏；⑤Feign 失败不得补 0 缓存、超时后 `cancel(true)`、给列表接口加 size 上限。

---

## 2. 综合审查发现（去重合并后按严重度排序）

> 标注「✅主理人复核」的条目已由主理人独立读取源码证实。

### 🔴 严重（7）

| # | 严重度 | 类别 | 位置 | 问题描述 | 建议 | 来源 |
|---|--------|------|------|---------|------|------|
| 1 | 🔴 | 安全·鉴权缺失 | `AdminPostController.java:15-18`、`AdminSystemMessageController.java:16-19`、`AdminCouponActivityController.java:14-17`、`OperationLogController.java:11-14` | **4 个管理端 Controller 无 `@Admin`/`@Login`**，全项目无 Spring Security → 匿名可调 `/a/post/ban\|bypass`、`/a/notification/create\|publish\|delete`、`/a/coupon/activity/save\|start\|close\|delete`、`/a/logs/list\|delete`（可删审计日志） ✅主理人复核 | 4 个类补类级 `@Admin @Login`；网关对 `/a/**` 强制鉴权兜底，勿只依赖注解 | 安全官 + 产品评审员（交叉确认） |
| 2 | 🔴 | 安全·认证绕过 | `AuthorizeGatewayFilter.java:67-75` + `Interceptor.java:86-118` | 网关无 Token 时**原样放行**且**不删除** `X-User-*` 头；下游据 `X-User-Id`/`X-User-Admin`/`X-User-Token-Type` 直接构造含 `admin` 等级的 session → 伪造头即冒充管理员 ✅主理人复核 | 网关入口无条件剥离所有 `X-User-*` 头后统一注入；服务端校验头来源（内部签名） | 安全官 |
| 3 | 🔴 | 安全·网络暴露 | `docker-compose.yml:134-406` | **全部微服务端口发布到宿主机**（8081/8082/8083/8085/8087/8088/8089/8090/8091/8092），网关可被完全绕过，直接命中服务即可复用 #2 伪造头 ✅主理人复核 | 生产仅暴露网关 9090；内部服务走 Docker 内网不映射端口 | 安全官 |
| 4 | 🔴 | 性能·分页失效 | `common/common-core/.../config/SqlConfig.java:29-63` | 手写 `MybatisSqlSessionFactoryBean` **未 `setPlugins`**，全仓库 grep 无 `MybatisPlusInterceptor`/`PaginationInnerInterceptor` → `selectPage` 不注入 LIMIT、不执行 count → **total=0、返回全表** ✅主理人复核（grep 全仓库命中 0） | 注册 `MybatisPlusInterceptor` + `PaginationInnerInterceptor(DbType.MYSQL)`；先运行时确认 total 是否恒为 0 | 调查员 |
| 5 | 🔴 | 数据造假 | `frontend-admin/src/services/dashboard.ts:87-193, 197-272` | 内置整套 MOCK 兜底（总用户 128560、GMV 894520、网关 QPS 3420、命中率 98.6%），后端异常时 `catch{}` 后 `return { code:1, data: MOCK_OVERVIEW }` **伪装成成功响应** ✅主理人复核 | 删除 MOCK，改为显式错误态/空态，禁止用假数冒充成功 | 调查员 + 产品评审员（交叉确认） |
| 6 | 🔴 | 数据造假 | `DashboardDataAssembler.java:122-130` | `gatewayQps(0)`、`avgResponseTimeMs(0)`、`mqBacklogCount(0)` 三个系统指标**硬编码为 0**，无任何数据源 ✅主理人复核 | 接真实数据源（网关/MQ 指标），或从前端隐藏该面板 | 调查员（主理人线索证实） |
| 7 | 🔴 | 假接口 | `stp-order-service/.../AdminOrderServiceImpl.java:37-40` | `refund()` 空实现 `return null`，Controller 原样透传 → HTTP 200 空 body；**资金操作静默无效** ✅主理人复核 | 补真实退款编排或显式 `throw new UnsupportedOperationException`，并下线前端入口 | 产品评审员 + 调查员（交叉确认） |

### 🟠 高（8）

| # | 严重度 | 类别 | 位置 | 问题描述 | 建议 | 来源 |
|---|--------|------|------|---------|------|------|
| 8 | 🟠 | 安全·信息泄露 | `AdminController.java:58-61` + `WebConfig.java:55` | `GET /u/admin/internal/is/{uid}` 被 `excludePathPatterns` **完全排除**在拦截器之外、网关 `/*/internal/**` 又不匹配（internal 位于第 3 段）→ **公网无鉴权、无需任何请求头即可访问**，可枚举任意 uid 是否为管理员及其等级 ✅主理人复核 | 加 `@Admin` 或纳入内部鉴权；黑名单改 `/**/internal/**` | 安全官 |
| 9 | 🟠 | 安全·基础设施 | `docker-compose.yml:2-16` | Redis 6379 暴露且**无 requirepass**，而会话/Token 全存 Redis → 可直接读写会话伪造登录 | 设强密码 + 绑定内网 + ACL | 安全官 |
| 10 | 🟠 | 安全·弱口令 | `.env` | MySQL `Root@123456`、MinIO `admin/12345678`、RabbitMQ/Nacos `*@123456`、JWT 密钥弱可猜。注：`.env` 已被 `.gitignore` 忽略**未入库**，但部署即弱口令 | 全部改强随机；密钥走 Vault/K8s Secret；JWT 密钥 ≥256bit 随机 | 安全官 |
| 11 | 🟠 | 数据造假·降级放大 | `DashboardQueryAppServiceImpl.java:86-204` + `DashboardDataAssembler.java:32-45` + `AdminDashboardServiceImpl.java:29-94` | Feign 失败 → `return null` → Assembler 补 0 → **再写 Redis 缓存 180s** → 下游一次抖动使 KPI 归零并持续 3 分钟 | 失败不补 0 缓存；改为不缓存或标记降级 | 调查员 |
| 12 | 🟠 | 性能·无上限 | `AdminOrderController.java:26-35`、`AdminUserController.java:28-47`、`AdminPostController.java:22-35`、`AdminUserReportController.java:27-36` | `size/pageSize` 直接透传无 `@Max`/clamp，可传 `size=100000` | 全局分页 `maxLimit` + 参数校验 | 调查员 |
| 13 | 🟠 | 性能·线程池 | `DashboardQueryAppServiceImpl.java:61-74,111,151,176,199` | 线程池 8/16/queue200/`CallerRunsPolicy`；`allOf().get(3s)` 超时**不 cancel** future（孤儿任务按 Feign 默认超时约 60s 继续占线程）；仓库未配 Feign 超时 | 超时后 `cancel(true)`；显式配置 Feign 超时；评估 `CallerRunsPolicy` 对 Tomcat 线程的拖垮风险 | 调查员 |
| 14 | 🟠 | 性能·缓存失效 | `DashboardCacheProviderImpl.java:37-59,70-92` | ①`finally lockMap.remove` 使等待线程与新线程持有**不同 lock 对象** → 双检锁失效 ②JVM 本地锁**多实例部署完全无效** ③空结果不缓存反复穿透 | 改用 Redisson 分布式锁；空值短 TTL 缓存 | 调查员 + 产品评审员（交叉确认） |
| 15 | 🟠 | 可维护性·审计缺失 | 13 个 admin Controller 仅 3 个用 `@OperationLog` | 写操作（post 封禁/审核、comment 封禁、coupon 增删、notification 发布、order 退款、member 套餐保存、activity 创建）**全部无审计** | 写接口统一补 `@OperationLog` | 产品评审员 |

### 🟡 中（19，合并同类）

| # | 类别 | 位置 | 问题 | 来源 |
|---|------|------|------|------|
| 16 | 安全·防护可绕过 | `Interceptor.java:52-66`、`GatewayConfig.java:24-35` | 全部 `/internal/**` 接口仅靠**静态可伪造头** `X-Internal-Request: true` 保护（无共享密钥、无网络隔离校验）；管理端相关的 `/u/admin/internal/stats/**` 聚合统计接口借此头即可被外部读取（返回聚合业务数据，不含 PII）。网关虽写了 remove-header filter，但路由实际由 Nacos `DynamicRouteLoader` 加载，该 filter **不生效**（置信度 9/10） | 安全官 |
| 17 | 数据口径 | `UserDashSupport.java:198`、`OrderDashSupport.java:39`、`PostDashSupport.java:25` | `period` 仅识别 `30d`，其余（90d/1d）静默按 7 天处理且无提示 | 调查员 |
| 18 | 数据口径 | `DashboardQueryAppServiceImpl.java:267-287` | "Redis 命中率"取的是**服务器全局** `keyspace_hits/misses`，非本应用缓存命中率 | 调查员 |
| 19 | 数据一致性 | `AdminPostServiceImpl.java:44-47` | total 来自 ES、记录回 DB 查且 `IN` 不保序 → 列表条数与顺序可能错位 | 调查员 |
| 20 | 数据契约 | `AdminUserServiceImpl.java:84` | `esQuery` 在无匹配时 `return null`，破坏 `Result` 契约且上层 NPE 隐患 | 产品评审员 + 调查员 |
| 21 | 安全·PII | `UserProfileVO.java:24-31`、`AdminOrderVO.java:16-17` | 管理端响应回传明文 `phone`/`email`/`ip`，订单 VO 回传 `to`/`sign`（支付签名） | 安全官 |
| 22 | 安全·存储 | `MinIoConfig.java:29-39,56`、`FileUploadUtil.java:33-51` | 桶策略 `Principal:*` + `s3:GetObject` → 所有文件公网可读；上传未校验 content-type → 可传 SVG/HTML 造成存储型 XSS | 安全官 |
| 23 | 安全·前端 | `frontend-admin/src/views/auth/store.ts:8-9,17-21` | access/refresh token 存 `localStorage`，XSS 可窃取长效 refresh token | 安全官 |
| 24 | 安全·设计 | 全部 admin 写接口 | 无幂等、无限流（`DistributedLockUtil` 未被 admin 使用）→ 可重放/刷（重复发券、重复退款） | 安全官 |
| 25 | 分层违规 | `AdminNotificationServiceImpl.java:20,28-29,35,42` | web 注解污染应用层：`@Admin/@Login` 标在 ServiceImpl 上，service 方法参数用 `@RequestBody` | 产品评审员 |
| 26 | 依赖倒置 | `AdminServiceImpl.java:29`、`AdminCouponItemsServiceImpl.java:8` | 直接依赖具体实现 `AdminRepositoryImpl`/`CouponActivityRepositoryImpl` 而非接口 | 产品评审员 |
| 27 | 重复代码 | `DashboardQueryAppServiceImpl.java:86-204` | 6 个方法复制粘贴同构 `CompletableFuture.supplyAsync(try{feign}catch{null})`，约 90 行 | 产品评审员 |
| 28 | 吞异常 | `DashboardQueryAppServiceImpl.java:152,177,200` | 3 处 `catch (Exception ignored) {}` 静默吞掉超时/异常 | 产品评审员 |
| 29 | 重复代码 | `SystemActivityAppService.java:44-54,66-76` + `DashboardDataAssembler.java:140-150` | 同一 `SystemActivity→VO` 映射写 **3 遍** | 产品评审员 |
| 30 | 重复代码 | `DashboardCacheProviderImpl.java:29-93` | `getOrSet` 与 `getOrSetList` 双重校验锁逻辑 ~90% 重复 | 产品评审员 |
| 31 | 重复代码 | `AdminUserQueryDTO.java` vs `AdminUserQueryCommand.java` 等 4 对 | DTO 与 Command 15 字段逐字重复；MapStruct 已引入但**使用 0 处** | 产品评审员 |
| 32 | 幽灵接口 | `frontend-admin/src/services/member.ts:121,146-169` | 6 个前端接口后端**无实现**（`/a/member/config`、`/a/member/decoration/*`），调用必 404 ✅主理人复核（后端仅 level-config/package/user 三类路由） | 产品评审员 |
| 33 | 事务/一致性 | `AdminPostServiceImpl.java:51-69`、`AdminCouponItemsServiceImpl.java:60-63` | `@Transactional` 与 `TransactionTemplate` 双重事务、部分写方法无事务；更新分支复用对象覆盖 `createTime` | 产品评审员 |
| 34 | 可维护性 | 全局 | 新增一个 admin 接口需手写 5-7 个文件；`@Admin/@Login` 覆盖 4/13 缺失、`@OperationLog` 仅 3/13；测试全项目仅 5 文件、admin 业务单测 0 | 产品评审员 |

### 🟢 低（4）

| # | 类别 | 位置 | 问题 | 来源 |
|---|------|------|------|------|
| 35 | 日志泄露 | `GuestSessionRepository.java:41`、`RequestLoggingInterceptor.java:16` | 日志打印 token 与含 query 的完整 URL | 安全官 |
| 36 | 语义错误 | `AdminUserController.java:55-66`、`AdminController.java:30-42` | 封禁/启用等状态变更用 `GET`，可被预取/爬虫触发 | 安全官 |
| 37 | 纵深防御 | `AuthAspect.java:55` | `if (admin != null && !isAdmin(...))` —— 取不到注解时**跳过**管理员校验（应拒绝而非放行） | 安全官 |
| 38 | 死代码 | `AdminServiceImpl.java:15,16,32`、`AdminUserServiceImpl.java:16` | 未使用字段/import + 空白刷行 | 产品评审员 |

---

## ✅ 行动清单

### P0 — 上线阻塞项（必须完成）

| # | 行动 | 负责方 | 紧急度 | 关联发现 |
|---|------|--------|--------|---------|
| 1 | 给 4 个无鉴权 Controller（AdminPost/AdminSystemMessage/AdminCouponActivity/OperationLog）补类级 `@Admin @Login`，并在网关对 `/a/**` 增加强制鉴权兜底 | 后端 | **P0** | #1 |
| 2 | 网关入口**无条件剥离** `X-User-Id/X-User-Admin/X-User-Token-Type/X-User-Session/X-Internal-Request`，改由网关签名注入 | 后端/网关 | **P0** | #2 |
| 3 | docker-compose 移除全部业务服务 `ports` 映射，仅暴露网关 9090 | 运维 | **P0** | #3 |
| 4 | `SqlConfig` 注册 `MybatisPlusInterceptor` + `PaginationInnerInterceptor(DbType.MYSQL)`；先实测一次列表接口确认 `total` | 后端 | **P0** | #4 |
| 5 | 删除 `dashboard.ts` 全部 MOCK 兜底（第 87-193 行）与各 API 的静默 fallback，改为显式错误态 | 前端 | **P0** | #5 |
| 6 | `assembleSystemStatus` 三个硬编码指标接真实数据源或从前端隐藏 | 后端 | **P0** | #6 |
| 7 | 修复 `AdminOrderServiceImpl.refund()` 空实现（接真实退款或显式报错），同步下线前端入口 | 后端 | **P0** | #7 |

### P1 — 尽快修复

| # | 行动 | 负责方 | 紧急度 | 关联发现 |
|---|------|--------|--------|---------|
| 8 | 内部接口改 mTLS/签名令牌；网关黑名单改 `/**/internal/**`；给 `/u/admin/internal/is/{uid}` 加鉴权 | 后端 | P1 | #8 #16 |
| 9 | Redis 设强密码 + 内网绑定；轮换 `.env` 全部弱口令与 JWT 密钥 | 运维 | P1 | #9 #10 |
| 10 | 大盘健壮性：Feign 失败不补 0 缓存、`allOf().get()` 超时后 `cancel(true)`、显式配置 Feign 超时、列表接口加 size 上限 | 后端 | P1 | #11 #12 #13 |
| 11 | 缓存防击穿改 Redisson 分布式锁，空值短 TTL 缓存 | 后端 | P1 | #14 |
| 12 | 启用 MapStruct 统一映射，删除 DTO=Command 重复层（4 对）与 `SystemActivityVO` 三处手写映射；抽 `parallelFetch` 模板；建测试基线 + ArchUnit 护栏 | 后端 | P1 | #27 #29 #31 #34 |

---

## ⚠️ 待完善 / 已知局限

- **需运行时实测确认**（静态分析无法定论）：
  - 伪造头越权（#2）：建议向 `/a/dashboard/overview` 带 `X-User-Id:1, X-User-Admin:1, X-User-Token-Type:ACCESS` 且**不带 Token** 发请求，若返回数据即确认。
  - 分页失效（#4）：调一次管理端列表接口看 `total` 是否恒为 0 / 是否全表返回。
  - 匿名可达（#1）：取决于线上是否存在未纳入代码库的网关/WAF 强制鉴权层。
- **置信度说明**：
  - 「内部统计接口靠伪造 `X-Internal-Request` 头可读」（#16）——证据充分（`Interceptor.java:52-66` 只比对字符串 `"true"`；`GatewayConfig` 的 remove-header filter 遍历 `gatewayProperties.getRoutes()` 而路由实由 Nacos `DynamicRouteLoader` 加载，filter 不生效），置信度 **9/10**。
  - `/u/admin/internal/is/{uid}` 公网可达（#8）——被 `excludePathPatterns` 排除 + 网关黑名单不匹配，双证据，置信度 **9/10**。
  - `X-User-Session` base64 伪造路径依赖 Jackson 对 `UserSession`（final 字段 + Builder）的反序列化行为，置信度约 **6/10**；`X-User-Id/X-User-Admin` 路径无需此依赖，优先利用。
  - `@within` 对 CGLIB 代理类的注解匹配在边缘场景（注解在父类）未逐一实测，置信度约 **5/10**。
- **未覆盖范围**：C 端（frontend-client）接口、支付/IM 业务逻辑深度、运行期性能压测、数据库实际索引命中率（基于 `mysql-schema.sql` 静态推断）。
- **`.env` 未入库**：已确认 `.gitignore` 忽略且 `git ls-files` 无该文件，故弱口令属「部署配置风险」而非源码泄露。

---

## 📚 成员产出索引

- **gstack-security-officer（安全官）**：OWASP Top 10 + STRIDE 双路径威胁建模，14 项发现（F-001~F-014），含「未发现项」清单与置信度标注；收口后补充一轮证据细化——将 F-004/F-005 拆分（`/u/admin/internal/is/{uid}` 提为公网可达 🟠；`/u/admin/internal/stats/**` 归为「伪造头可读聚合数据」🟡）并将内部接口可达性置信度上调至 9/10。
- **gstack-product-reviewer（产品评审员）**：代码臃肿与可维护性评审，28 项发现，量化指标（3799 行/84 文件、最大类 288 行、重复映射 ×3、MapStruct 使用 0 处、测试 5 文件）+ Top 5 重构优先级。
- **gstack-investigator（调查员）**：数据真实性 9 项（DF-1~DF-9）+ 性能 7 项（PF-1~PF-7），含关键路径耗时/调用次数估算与无索引全表扫明细。
- **主理人交叉验证**：独立复核 #1 #2 #3 #4 #5 #6 #7 #8 #9 #32 共 10 项，全部证实。

---

> 本报告由软件工坊 AI 协作生成，关键决策请由工程负责人复核。
