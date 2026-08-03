# STP

## 项目简述

STP论坛是一个基于__Spring Cloud__搭建的微服务项目,涉及数据库`34`张表,DDD领域架构,包含了用户认证,评论,优惠券,VIP系统,IM系统,订单系统,支付系统,帖子系统,用户系统以及排行榜系统等多个服务模块,提供游客模式和登录态,无需登录即可浏览网站帖子,评论等信息;登录用户可以在平台上自由发帖,评论,回复,私聊,关注,点赞,收藏,分享,个性化主页,进行每日签到以及充值VIP等操作

## 项目技术栈

STP论坛用到了以下框架和组件:

__前端__: __Element-Plus,Vue3,Tailwinds,TypeScript,PiniaStore,Socket.IO Client__

__后端__: __SpringBoot,SpringCloud alibaba,MySQL,Redis,RabbitMQ,Socket.IO Server,Mybatis-Plus,MinIO,Reddison__



## 架构

### 目录

本项目每一个服务模块基本按照DDD架构进行目录划分,例如

```text
com.summit.stp.*
│
├── application.java                     // Spring Boot 启动类
│
├── api                                         // 接口层（Web 层）
│   ├── controller                              // REST API 控制器
│   └── dto                                     // 数据传输对象
│       ├── request                             // 请求参数封装
│       └── response                            // 响应结果封装
│
├── application                                 // 应用层（业务流程编排）
│   ├── command                                 // 命令对象（CQRS 模式）
│   ├── service                                 // 应用服务（用例实现）
│   └── vo                                      // 视图对象（返回给前端）
│
├── domain                                      // 领域层（核心业务逻辑）
│   ├── model                                   // 领域模型（实体、值对象、聚合根）
│   ├── service                                 // 领域服务（核心业务规则）
│   ├── repository                              // 仓储接口（定义数据访问契约）
│   ├── event                                   // 领域事件（业务事件定义）
│   └── exception                               // 领域异常（业务规则异常）
│
├── infrastructure                              // 基础设施层（技术细节实现）
│   ├── config                                  // 配置类（Redis、Security、MQ等）
│   ├── persistence                             // 持久化实现
│   │   ├── po                                  // 持久化对象（数据库表映射）
│   │   └── converter                           // PO 与 Domain 对象转换
│   ├── mapper                                  // MyBatis Mapper 接口
│   └── repository                              // 仓储接口的具体实现（注入Mapper）
│
└── publisher                                   // 消息发布者（事件驱动）
```

### database


| 表名 | 描述 |
| :--- | :--- |
| **用户模块** | |
| `user` | 用户基本信息表 |
| `user_follow` | 用户关注关系表 |
| `user_setting` | 用户个性化设置表 |
| `user_sign_log` | 用户签到日志表 |
| `user_sign_stats` | 用户签到统计数据表 |
| `user_stat` | 用户统计数据表（如粉丝数、获赞数等） |
| **内容模块** | |
| `posts` | 帖子/动态主表 |
| `post_image` | 帖子图片表 |
| `post_like` | 帖子点赞记录表 |
| `post_collect` | 帖子收藏记录表 |
| `post_rank` | 帖子排行/热度表 |
| `post_tag_rel` | 帖子与标签关联表 |
| `tag` | 标签表 |
| `topic_rank` | 话题排行榜表 |
| **评论模块** | |
| `comments` | 评论主表 |
| `comment_image` | 评论图片表 |
| `comment_like` | 评论点赞记录表 |
| **社交/消息模块** | |
| `private_message` | 私信消息表 |
| `session` | 会话会话表 |
| `user_session` | 用户与会话关联表 |
| `emoji_package` | 表情包表 |
| `emoji` | 表情/图标表 |
| `interaction_message` | 互动消息/通知表 |
| `system_message` | 系统消息表 |
| `system_message_image` | 系统消息图片表 |
| **会员模块** | |
| `member_level_config` | 会员等级配置表 |
| `member_package` | 会员套餐表 |
| `user_member` | 用户会员记录表 |
| `creator_rank` | 创作者等级/排行表 |
| **优惠券模块** | |
| `coupon` | 优惠券主表 |
| `coupon_activity` | 优惠券活动表 |
| `coupon_use_scope` | 优惠券使用范围表 |
| `user_coupon` | 用户领取优惠券记录表 |
| **支付模块** | |
| `payment_order` | 支付订单表 |
| `table 34` |  |

### Redis结构

```tex
Redis Cache
├── user:  //用户
│   └── detail:<userId>
├── member: //会员
│   ├── level:config
│   ├── pay:consume:<...>
│   ├── pay:lock:<...>
│   └── state:<...>
├── user:auth: //认证
│   ├── token:access:<...>
│   ├── token:refresh:<...>
│   ├── token:guest:<...>
│   ├── session:access:<...>
│   ├── session:refresh:<...>
│   └── session:guest:<...>
├── comment:  //评论
│   ├── like:<commentId>
│   ├── like:count:<commentId>
│   ├── reply:<commentId>
│   ├── reply:count:<commentId>
│   └── changed:...
├── post:  //帖子
│   ├── detail:<postId>
│   ├── tag:posts:<tagId>
│   ├── like:<postId>
│   ├── collect:<postId>
│   ├── view:limit:<...>
│   ├── query:newest
│   ├── query:hot
│   └── active:keys
├── rank:  //排名
│   ├── topic_use:zset
│   ├── hot:<...>
│   └── creator:weekly:<...>
├── coupon:  //优惠券
│   ├── activity:stock:<...>
│   ├── activity:user:limited:<...>
│   ├── use:lock:<...>
│   └── prewarm:<...>
├── daily_sign_in:  //每日签到
│   └── user:
│       ├── sign_info:<userId>
│       └── <userId>:year:<year>:month:<month>
├── ws:   //im
│   ├── online:<uid>
│   ├── connection:<...>
│   └── token:<...>
└── order:  //订单
    ├── timeout:zset
    ├── timeout:lock
    ├── pay:success:<...>
    └── pay:lock:<...>
```



## 页面截图(部分)

<img src="https://img.remit.ee/api/file/BQACAgUAAyEGAASHRsPbAAEYH51qab3KaTdqQfckVYaavHxwZzre6AACkiUAAvjGUFd32ht2LJqvSz0E.png"/>

<img src="https://img.remit.ee/api/file/BQACAgUAAyEGAASHRsPbAAEYH6Nqab40eYeoYBrxXtKqswaLOWQekAACmCUAAvjGUFdP0f4mo4huqD0E.png" alt="BQACAgUAAyEGAASHRsPbAAEYH6Nqab40eYeoYBrxXtKqswaLOWQekAACmCUAAvjGUFdP0f4mo4huqD0E.png" />



## 尚未完成开发的点

- 由于用户认证模块的验证码服务需要对接第三方如`阿里云api`完成,本项目暂时并未开发用户认证服务中的注册验证码逻辑,需要自行补充(对接SDK并接入),同样的,设置模块中,邮箱绑定,手机号绑定也并未完成开发,__默认验证码是123456__

- 处于H5环境下,IM开发比较受限,对此项目也做了一些防护,比如将`websocket`数据包封装在了浏览器的`shared-worker`线程中,全局通知组件(私聊,系统,互动等通知)并未完成开发,因此后端的字段如勿扰,屏蔽,拉黑也没有进行过滤,以及接口开发,不过如需要完善也比较容易,可自行完善
- 会员,会员等级,签到等用户身份等级权益并未开发,补充,目前只是简单的展示身份TAG标签,后续业务拓展时可以进行补充
- 帖子主页TAB,推荐系统尚未完成,实现较为复杂,需要构建用户画像,协同过滤算法,缓存候选池,行为记录等实现步骤,尚未开发



### 需要补充的点

由于本项目始终为本人+AI开发,对于功能测试较为少,一些`fallback`并未尝试`Test`,大部分功能处于正常可使用






## 运行


要求:

- 运行机器具备docker环境

- JDK 8+ 环境

  - 如果没有`maven`,

    ```bash
    ./mvnw.cmd -v
    ```

- Node.js 

步骤:

- 在项目根目录

```shell
mvn clean package
docker-compose up -d
```

- 确保`naocs`启动成功,成功标准能够打开`8080`端口的控制台,这里有概率会报`sql连接错误`,重新登录一遍`mysql`即可

```shell
docker ps
docker logs stp-nacos
```

- 本项目依赖于__共享配置__,所以,进入`nacos`,初始密码设置为`Nacos@123456`

  - 进入配置中心,导入项目根目录的`naocs-*.zip`压缩文件

- 确保`docker`所有容器运行成功,这一点可以查看`nacos`控制台-服务中心查看,一共__10__个服务,后端启动完成

- 进入项目根目录,启动前端

  ```shell
  cd ./frontend
  npm install
  npm run dev
  ```

  





