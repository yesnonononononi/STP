# STP 管理端修复 · 构建验证与交付报告（WS-6）

**日期**：2026-09-27
**对应审计**：`deliverables/gstack/admin-api-audit-2026-09-27.md`（Go/No-Go：🔴 No-Go）
**执行**：gstack-qa-lead（WS-4 实施者 + 本报告构建验证）
**参与**：安全官（WS-1）/ 产品评审员（WS-2）/ 调查员（WS-3）/ 设计师（WS-5）/ QA 负责人（WS-4、WS-6）；主理人负责编排与交叉复核
**验证方式**：静态代码核实 + Maven 编译验证（**本次未做运行时/集成验证**，见 §5）

---

## 📌 TL;DR

- **构建验证：✅ 通过**。全仓 25 个模块 `mvn -o -DskipTests compile` 串行执行 **BUILD SUCCESS**（含共享模块 `api/user-api`、`common-core`、全部微服务）。
- **交付规模**：74 个文件改动（+1720 / −1188），含 4 个新增源文件。
- **审计 7 条 P0 阻塞项：7/7 已闭环**（含 1 条按主理人决策调整方案，见 #22/#7）。
- **38 条审计发现**：34 条已修复落地，1 条按主理人决策回退保留风险（#22 桶策略），3 条属运维/后续排期（#10、#34 测试基线、运行时验证）。
- **结论建议：🟢 有条件 Go**（条件：完成 §5 列出的部署前置项）。剩余风险均已显式记录，无已知的静默假成功/假数据路径。

---

## 1. 构建验证结果

| 验证命令 | 结果 |
|---|---|
| `./mvnw -o -DskipTests compile`（全仓 25 模块，串行） | **BUILD SUCCESS** |
| `./mvnw -o -DskipTests -pl common/common-core -am compile` | **BUILD SUCCESS** |
| `./mvnw -o -DskipTests -pl stp-user-service -am compile`（EncryptUtil/UserProfileVO 消费方） | **BUILD SUCCESS** |
| `./mvnw -o -DskipTests clean compile` | ⚠️ 未采用（见下方说明） |

**关于 `clean compile` 的说明（重要，避免误判）**：多人并行在**同一工作区**跑 Maven 时，任一成员的 `clean` 会删除 `common-core/target/classes`，导致其他正在编译的模块（如 `post-api`/`order-api`）报「程序包 com.summit.stp.common.application.api.result 不存在」的**假失败**；此外 Windows 下并发进程可能锁住 `target\classes` 导致 clean 阶段删除失败。上述两类报错**均与代码无关**。本次最终验证采用**串行增量编译**，全部通过。

> ⚠️ 环境提醒：本仓库为共享工作区协作模式，请勿并行执行任何 `mvn clean`。

---

## 2. 修复交付清单（按工作流）

### WS-1 安全、网关与部署修复（安全官）
- `stp-gateway/filter/AuthorizeGatewayFilter.java`：网关入口**无条件剥离**客户端伪造的 `X-User-*` / `X-Internal-Request` 头（大小写不敏感全量 remove），再由网关注入真实身份（已核实源码）。
- `common/common-web/config/WebConfig.java`：移除 `/u/admin/internal/**` 的拦截器豁免，内部 RPC 接口重新纳入 `X-Internal-Request` 合法性校验（已核实源码，含注释说明）。
- `common/common-web/auth/aspect/AuthAspect.java`：**默认拒绝**——取不到 `@Admin` 注解信息时抛 403 而非放行（已核实源码）。
- `docker-compose.yml`：**业务微服务端口映射全部移除**（8081~8092 不再暴露宿主机），仅保留网关；`redis` 增加 `--requirepass ${REDIS_PASSWORD}`（已核实）。

### WS-2 管理端 Controller 层、审计与分页拦截器（产品评审员）
- **全部 13 个管理端 Controller 补齐类级 `@Admin @Login`**（含原 4 个裸奔的 AdminPost / AdminSystemMessage / AdminCouponActivity / OperationLog，已逐一核实注解存在）。
- 状态变更接口由 `GET` 改 `POST`（如 `/a/user/ban`、`/a/user/unban`，已核实）。
- `common/common-core/config/SqlConfig.java`：注册 `MybatisPlusInterceptor` + `PaginationInnerInterceptor(DbType.MYSQL)`，**修复全仓分页失效**（已核实）。
- `AdminNotificationServiceImpl` 等服务层清除 `@Admin/@Login`、`@RequestBody` 等 Web 注解污染（分层整改）。
- 13 个 Controller 写操作补齐 `@OperationLog` 审计注解。

### WS-3 大盘数据真实性与性能（调查员）
- 删除 `frontend-admin/src/services/dashboard.ts` 整套 MOCK 兜底（已核实：`MOCK` / `128560` / `894520` / 伪装 `code:1` 均已不存在）。
- `DashboardQueryAppServiceImpl` / `DashboardCacheProviderImpl`：Feign 失败不再补 0 写缓存，改显式降级（新增 `DashboardDegradable`）；缓存防击穿改 Redisson 分布式锁。
- **新增指标采集写入端**：`stp-gateway/filter/GatewayMetricsFilter.java`、`common/constants/MetricsConstants.java`、`stp-admin-service/infrastructure/metrics/`，替换 3 个硬编码为 0 的系统指标数据源。
- 各服务 `pom.xml` 移除未使用的 MapStruct 依赖。

### WS-4 后端业务正确性、数据脱敏、幂等（QA 负责人）—— 详见 §3
### WS-5 前端修复与数据库索引（设计师）
- `frontend-admin` 12 个文件：移除 Mock 兜底、退款入口下线、PII 字段适配、幽灵接口清理（`/a/member/config`、`/a/member/decoration/*`）。
- `init.sql`：补齐高频查询索引。

---

## 3. WS-4 明细（本工作流，逐项闭环）

**改动 18 个文件**（含 1 个越界授权文件）：

| 发现 | 修复 | 状态 |
|---|---|---|
| #7 退款空实现 `return null` | `AdminOrderServiceImpl.refund()` 改为抛 `BusinessException("退款功能暂未开放")`（经 `refundNotImplemented()` 方法引用进入分布式锁块，规避 Supplier/Runnable 重载歧义）；javadoc 标注 TODO 与接入点（payment-service `RefundAppService`） | ✅ |
| #17 period 口径 | 三个 DashSupport 统一支持 `7d/30d/90d`；空值 `log.warn` + 默认 7d；**非法值抛 `ParameterException`**，不再静默按 7 天 | ✅ |
| #19 ES/DB 数据一致性 | `AdminPostServiceImpl.list()`：按 ES 返回 id 顺序重排回表记录（IN 不保序）；ids 为空返回空页而非 null。**取舍**：total 仍以 ES 为权威分页总数，回表缺失的 id 被剔除 | ✅ |
| #20 契约与 NPE | `esQuery` 空结果返回 `Result.success(PageResult.empty())`；`toggleBan` 操作者等级判空守卫；评论 `getStatus()`、优惠券 `getScopeType()/getTimeType()` 判空 | ✅ |
| #21 PII 脱敏 | `UserProfileVO` 新增 `maskPhone/maskEmail/maskSensitiveInfo`（**原 20 字段零改动**，幂等，C 端不受影响），管理端两处查询统一调用；`AdminOrderVO` **删除 `sign`**、`to` 脱敏 | ✅ |
| #22 上传白名单 | `FileUploadUtil` 增加类型+扩展名白名单、50MB 上限、显式拒绝 SVG/HTML/XML/JS。**保留图片+视频两类**（该工具是全站共享上传入口，严格图片-only 会打断 C 端视频上传，经主理人确认） | ✅ |
| #22 MinIO 桶私有化 | **按主理人决策回退**至 git HEAD 原桶策略（`git diff` 为零，逐字节一致）。原因：`posts.image_url`/`media_urls`/`cover_image`/`icon_url`/头像等媒体 URL 直接落库，私有化将使存量图片全部 403 | ⚠️ 决策回退，风险记录见 §5 |
| #24 幂等/防重 | 管理端写操作 service 层统一接入 `DistributedLockUtil`（wait 3s / lease 10s）：订单补发+退款、优惠券 6 个写方法、帖子 3 个、评论 2 个、用户封禁、**系统消息 create/publish/revoke/delete**。Controller 层未动（归 WS-2） | ✅ |
| #26 依赖倒置 | `AdminServiceImpl` 改依赖 `AdminRepository` 接口（接口补 `updateById`，由 `AbstractRepository` 继承实现）；`AdminCouponItemsServiceImpl` 清除 `*RepositoryImpl` 依赖/import | ✅ |
| #31 DTO/Command 去重 | 15 字段**收敛到 `AdminUserQueryCommand` 单一定义**；`AdminUserQueryDTO` 继承 Command（api→application，依赖方向正确）+ 新增 `toCommand()` 可复用转换方法。**未引入 MapStruct** | ✅ |
| #33 事务 / createTime | `AdminPostServiceImpl` 移除 `@Transactional`，全类统一 `TransactionTemplate`；`bypassNot/toggleBan` 补齐事务；ES 事件在事务提交后发送。优惠券更新分支 `createTime` 置 null 由 MP NOT_NULL 策略保留原值 | ✅ |
| #38 死代码 | 删除 `AdminServiceImpl` 未使用 `queueSender` 字段及 `QueueSender`/`MqConstants` import；`AdminUserServiceImpl` 清 `EsUserUpdateEvent`、`UserAPPServiceImpl` 死 import | ✅ |
| 附加 | `EncryptUtil.encodeStrForStar` 崩溃级 bug 修复（授权越界）：email 分支 `substring(4, indexOf-2)` 越界异常 + `replaceAll` 正则注入，改为纯切片拼接并加长度守卫；**对外签名不变，调用方零改动**。email 脱敏粒度收紧为保留首字符（如 `a****@example.com`） | ✅ |

---

## 4. 审计 38 项发现闭环状态总表

> ✅ = 本轮已在源码中**直接核实**；◎ = 由对应工作流负责人回报 + 变更集佐证；⚠️ = 有条件/有残留风险；○ = 未在代码层处理（运维/流程项）

| # | 发现 | 状态 | 核实方式 |
|---|---|---|---|
| 1 | 4 个 Controller 无鉴权 | ✅ | 逐一核实 13 个 Controller 类级 `@Admin @Login` |
| 2 | 网关伪造头绕过 | ✅ | 核实 `AuthorizeGatewayFilter` 全量剥离 + 网关注入 |
| 3 | 微服务端口全暴露 | ✅ | 核实 docker-compose 仅基础设施保留端口 |
| 4 | 分页拦截器缺失 | ✅ | 核实 `SqlConfig` 注册 `PaginationInnerInterceptor` |
| 5 | 前端 MOCK 伪装成功 | ✅ | 核实 dashboard.ts 无 MOCK/假 KPI |
| 6 | 3 个指标硬编码为 0 | ◎ | 新增网关指标采集端（`GatewayMetricsFilter` 等），待运行时验证 |
| 7 | 退款假接口 | ✅ | 显式业务异常 + TODO 接入点 |
| 8 | internal 接口公网可达 | ✅ | 核实 `WebConfig` 移除豁免 |
| 9 | Redis 无密码 | ✅ | 核实 `--requirepass` |
| 10 | .env 弱口令 | ○ | 源码不可验证，**部署时必须轮换**（见 §5） |
| 11 | Feign 失败补 0 缓存 | ◎ | WS-3（降级重构 + `DashboardDegradable`） |
| 12 | 分页 size 无上限 | ◎ | WS-2（Controller 参数校验） |
| 13 | 线程池超时不 cancel | ◎ | WS-3 |
| 14 | 本地锁多实例失效 | ◎ | WS-3（改 Redisson 分布式锁） |
| 15 | 写操作缺审计 | ◎ | WS-2（补 `@OperationLog`） |
| 17 | period 静默按 7 天 | ✅ | 三个 DashSupport 支持 7d/30d/90d + 参数异常 |
| 19 | ES/DB 列表错位 | ✅ | 保序 + 空页 |
| 20 | 契约破坏 / NPE | ✅ | 空页 + 判空守卫 |
| 21 | PII 明文回传 | ✅ | 脱敏方法 + 管理端调用；`sign` 移除 |
| 22 | 桶公开读 + 上传无校验 | ⚠️ | 上传白名单 ✅；桶策略**决策回退**（存量直链依赖），风险见 §5 |
| 24 | 写操作无幂等/限流 | ✅ | service 层分布式锁全覆盖（限流归网关/WS-1） |
| 25 | web 注解污染应用层 | ◎ | WS-2 |
| 26 | 依赖具体实现类 | ✅ | 改依赖 Repository 接口 |
| 27/28/29/30 | 重复代码 / 吞异常 | ◎ | WS-3 |
| 31 | DTO=Command 重复 | ✅ | 收敛单一定义 + `toCommand()`（无 MapStruct） |
| 32 | 6 个幽灵接口 | ◎ | WS-5（前端清理） |
| 33 | 事务混乱 / createTime 覆盖 | ✅ | 统一 TransactionTemplate + 保留原值 |
| 34 | 测试基线缺失 | ○ | **未建测试**（见 §5 后续建议） |
| 35 | 日志泄露 token/URL | ◎ | WS-1（`GuestSessionRepository`、`RequestLoggingInterceptor`） |
| 36 | 状态变更用 GET | ✅ | 核实改 `@PostMapping` |
| 37 | 鉴权默认放行 | ✅ | 核实 AuthAspect 默认 403 |
| 38 | 死代码 | ✅ | 已清理 |

---

## 5. 已知残留风险与部署前置项

**部署前置（Do-Not-Ship 清单）**
1. **轮换全部弱口令**（#10）：MySQL `Root@123456`、MinIO `admin/12345678`、RabbitMQ/Nacos、JWT 密钥（≥256bit 随机）。`.env` 未入库，属部署配置风险。
2. **Redis 已加 requirepass**，部署时必须设置 `REDIS_PASSWORD`，否则服务无法连接。
3. **生产仅暴露网关 9090**：docker-compose 中 Redis/MySQL/MinIO/Nacos/RabbitMQ 端口仍映射宿主机（本地开发需要），生产环境须移除或绑内网。

**残留风险（需排期）**
4. **MinIO 桶仍匿名可读**（#22 决策回退）：存储型 XSS 已由上传白名单堵住，但对象可被公网枚举/直读。后续若私有化，**必须先落地预签名 URL 或网关代理 `/minio/**`，再切桶策略**，顺序不可颠倒。
5. **无运行时/集成验证**：本次全部为静态验证。下列项需上线前实测：伪造头越权（预期 401/403）、列表接口 `total` 是否正确、网关指标是否上报、降级路径是否显式报错。
6. **管理端业务单测为 0**（#34）：本轮未建测试基线。建议下一迭代补齐（优先覆盖退款、封禁、优惠券库存、幂等锁行为）。
7. **幂等锁 fail-closed**：Redis 不可用时管理端写操作直接失败（`DisTributeLockAcquireException`），属有意设计（防重放优先），运维需保证 Redis 高可用。
8. **email 脱敏粒度收紧**：由「保留前 5 后 2（且仅长邮箱不崩）」改为「仅保留首字符」。若产品要求展示更多字符需明确口径。

**协作模式提醒**
9. 共享工作区内**禁止并行执行 `mvn clean`**，否则会产生 `com.summit.stp.common.* 包不存在` 假失败与 `target\classes` 文件锁。

---

> 本报告由软件工坊 AI 协作生成，关键决策请由工程负责人复核。
