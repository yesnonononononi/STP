-- 数据库初始化脚本 (包含唯一索引与 public_id 隔离优化)

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`    BIGINT       NOT NULL COMMENT '业务唯一ID',
    `uname`        VARCHAR(100) NOT NULL COMMENT '用户名',
    `password`     VARCHAR(255) NOT NULL COMMENT '密码',
    `phone`        VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `avatar`       VARCHAR(200) NOT NULL DEFAULT 'https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c' COMMENT '头像',
    `email`        VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `ip`           VARCHAR(50)           DEFAULT NULL COMMENT 'IP地址',
    `introduction` VARCHAR(300)          DEFAULT NULL COMMENT '个人简介',
    `nick`         VARCHAR(100) NOT NULL DEFAULT (`uname`) COMMENT '昵称',
    `gender`       CHAR(1)      NOT NULL DEFAULT '男' COMMENT '性别',
    `age`          INT                   DEFAULT NULL COMMENT '年龄',
    `status_code`  INT                   DEFAULT 1 COMMENT '用户状态: 1激活, 0封禁',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    UNIQUE KEY `uname` (`uname`),
    KEY `idx_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- 2. 商品表
CREATE TABLE IF NOT EXISTS `commodity`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`   BIGINT         NOT NULL COMMENT '业务唯一ID',
    `name`        VARCHAR(128)   NOT NULL COMMENT '商品名称',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '商品价格',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品表';

-- 3. 优惠券表
CREATE TABLE IF NOT EXISTS `coupon`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`   BIGINT       NOT NULL COMMENT '业务唯一ID',
    `name`        VARCHAR(128) NOT NULL COMMENT '优惠券名称',
    `discount`    DECIMAL(3, 2)         DEFAULT NULL COMMENT '折扣比例 (如 0.85)',
    `amount`      DECIMAL(10, 2)        DEFAULT NULL COMMENT '优惠金额',
    `type`        INT                   DEFAULT 0 COMMENT '优惠券类型: 0折扣, 1金额',
    `status`      INT                   DEFAULT 1 COMMENT '状态: 1可用, 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='优惠券表';

-- 4. 支付订单表
CREATE TABLE IF NOT EXISTS `payment_order`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`   BIGINT         NOT NULL COMMENT '业务唯一ID',
    `creator_id`  BIGINT         NOT NULL COMMENT '创建者用户ID',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    `pay_type`    INT                     DEFAULT NULL COMMENT '支付类型: 1支付宝, 2微信',
    `to_name`     VARCHAR(128)            DEFAULT NULL COMMENT '收款方名称',
    `sign`        VARCHAR(512)            DEFAULT NULL COMMENT '支付签名',
    `status`      INT                     DEFAULT 0 COMMENT '订单状态: 0待支付, 1已支付, 2已取消',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='支付订单表';

-- 5. 购物车表
CREATE TABLE IF NOT EXISTS `shopping_cart`
(
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`    BIGINT      NOT NULL COMMENT '业务唯一ID',
    `uname`        VARCHAR(64) NOT NULL COMMENT '关联用户名',
    `commodity_id` BIGINT      NOT NULL COMMENT '商品ID',
    `quantity`     INT         NOT NULL DEFAULT 1 COMMENT '购买数量',
    `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    UNIQUE KEY `uk_user_commodity` (`uname`, `commodity_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='购物车表';

-- 6. 帖子表
CREATE TABLE IF NOT EXISTS `posts`
(
    `id`            BIGINT                                   NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`     BIGINT                                   NOT NULL COMMENT '业务唯一ID',
    `creator_id`    BIGINT                                   NOT NULL COMMENT '发布者用户ID',
    `title`         VARCHAR(255)                             NOT NULL COMMENT '帖子标题',
    `type`          ENUM ('image', 'video', 'audio', 'text') NOT NULL DEFAULT 'text' COMMENT '帖子类型',
    `content`       TEXT                                              DEFAULT NULL COMMENT '文本内容',
    `media_urls`    TEXT                                              DEFAULT NULL COMMENT '媒体资源URL列表',
    `like_count`    INT UNSIGNED                             NOT NULL DEFAULT 0 COMMENT '获赞数',
    `reply_count`   INT UNSIGNED                             NOT NULL DEFAULT 0 COMMENT '回复数',
    `collect_count` INT UNSIGNED                             NOT NULL DEFAULT 0 COMMENT '收藏数',
    `status`        TINYINT                                  NOT NULL DEFAULT 1 COMMENT '状态: 1正常, 2删除, 3封禁, 4举报, 5未知, 6草稿',
    `create_time`   TIMESTAMP                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   TIMESTAMP                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_type` (`type`),
    KEY `idx_created_at` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='帖子表';

-- 7. 评论表
CREATE TABLE IF NOT EXISTS `comments`
(
    `id`          BIGINT    NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`   BIGINT    NOT NULL COMMENT '业务唯一ID',
    `post_id`     BIGINT    NOT NULL COMMENT '所属帖子ID',
    `user_id`     BIGINT    NOT NULL COMMENT '评论用户ID',
    `parent_id`   BIGINT             DEFAULT NULL COMMENT '父评论ID，NULL表示一级评论',
    `root_id`     BIGINT             DEFAULT NULL COMMENT '根评论ID',
    `content`     TEXT      NOT NULL COMMENT '评论内容',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_root_id` (`root_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='评论表（支持回复）';

-- 8. 标签表
CREATE TABLE IF NOT EXISTS `tag`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`   BIGINT      NOT NULL COMMENT '业务唯一ID',
    `tag_name`    VARCHAR(32) NOT NULL COMMENT '标签名',
    `tag_color`   VARCHAR(20)          DEFAULT '' COMMENT '标签颜色',
    `sort`        INT                  DEFAULT 0 COMMENT '权重排序',
    `use_count`   INT                  DEFAULT 0 COMMENT '使用次数',
    `status`      TINYINT              DEFAULT 1 COMMENT '状态: 0禁用, 1启用',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    UNIQUE KEY `uk_name` (`tag_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='标签表';

-- 9. 帖子标签关联表
CREATE TABLE IF NOT EXISTS `post_tag_rel`
(
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`   BIGINT   NOT NULL COMMENT '业务唯一ID',
    `post_id`     BIGINT   NOT NULL COMMENT '帖子ID',
    `tag_id`      BIGINT   NOT NULL COMMENT '标签ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
    KEY `idx_tag` (`tag_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='帖子标签关联表';


CREATE TABLE `user_follow`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`   bigint(20) NOT NULL COMMENT '业务唯一ID',
    `follower_id` bigint(20) NOT NULL COMMENT '关注者用户ID',
    `followee_id` bigint(20) NOT NULL COMMENT '被关注者用户ID',
    `status`      tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1-正常关注，2-已取消，3-互相关注（可选）',
    `source`      varchar(16)         DEFAULT NULL COMMENT '关注来源',
    `create_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注创建时间',
    `update_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    UNIQUE KEY `uk_follower_followee` (`follower_id`, `followee_id`),
    KEY `idx_followee_id` (`followee_id`),
    KEY `idx_follower_status` (`follower_id`, `status`),
    KEY `idx_followee_status` (`followee_id`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户关注关系表';

-- 帖子图片表
CREATE TABLE `post_image`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `public_id`   bigint       NOT NULL COMMENT '业务唯一ID',
    `post_id`     bigint       NOT NULL COMMENT '所属帖子ID',
    `image_url`   varchar(512) NOT NULL,
    `width`       smallint              DEFAULT NULL,
    `height`      smallint              DEFAULT NULL,
    `size`        int                   DEFAULT NULL,
    `sort_order`  tinyint      NOT NULL DEFAULT 0,
    `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '1-正常，2-违规',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    KEY `idx_post_id` (`post_id`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='帖子图片关联表（逻辑外键）';

-- 评论图片表
CREATE TABLE `comment_image`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `public_id`   bigint       NOT NULL COMMENT '业务唯一ID',
    `comment_id`  bigint       NOT NULL COMMENT '所属评论ID',
    `image_url`   varchar(512) NOT NULL,
    `width`       smallint              DEFAULT NULL,
    `height`      smallint              DEFAULT NULL,
    `size`        int                   DEFAULT NULL,
    `sort_order`  tinyint      NOT NULL DEFAULT 0,
    `status`      tinyint      NOT NULL DEFAULT 1,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    KEY `idx_comment_id` (`comment_id`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='评论图片关联表（逻辑外键）';

-- 用户统计表
CREATE TABLE IF NOT EXISTS `user_stat`
(
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `fans`    BIGINT NOT NULL DEFAULT 0 COMMENT '粉丝数',
    `topic`   BIGINT NOT NULL DEFAULT 0 COMMENT '话题/帖子数',
    `liked`   BIGINT NOT NULL DEFAULT 0 COMMENT '获赞/喜欢的作品数',
    PRIMARY KEY (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户统计表';


create table emoji
(
    id          bigint primary key auto_increment,
    public_id   bigint not null comment '业务唯一ID',
    name        varchar(128) not null,
    package_id  bigint,
    url         varchar(512) not null,
    tiny        varchar(512) not null,
    create_time timestamp default current_timestamp,
    update_time timestamp on update current_timestamp,
    unique key uk_public_id (public_id)
);

create table emoji_package
(
    id          bigint auto_increment primary key,
    public_id   bigint not null comment '业务唯一ID',
    name        varchar(128) not null comment '表情包名称',
    description varchar(1024) comment '表情包描述',
    cover_image varchar(512) comment '表情包封面图片',
    status      tinyint   default 1 comment '1-正常，2-禁用',
    create_time timestamp default current_timestamp comment '创建时间',
    update_time timestamp default current_timestamp on update current_timestamp comment '更新时间',
    unique key uk_public_id (public_id)
) comment ='表情包表';

create table rank_definition
(
    id          bigint auto_increment primary key,
    public_id   bigint not null comment '业务唯一ID',
    name        varchar(128) not null comment '排行榜名称',
    description varchar(1024) comment '排行榜描述',
    type        tinyint   default 1 comment '1-日榜，2-周榜，3-月榜，4-总榜',
    status      tinyint   default 1 comment '1-正常，2-禁用',
    create_time timestamp default current_timestamp,
    update_time timestamp default current_timestamp on update current_timestamp,
    unique key uk_public_id (public_id),
    unique key uk_name (name)
) comment = '排行榜定义表';

create table rank_entry
(
    id          bigint auto_increment primary key,
    public_id   bigint not null comment '业务唯一ID',
    rank_def_id bigint         not null comment '关联排行榜定义ID',
    entity_id   bigint         not null comment '实体ID',
    entity_name varchar(128)   not null comment '实体名称',
    score       decimal(10, 2) not null comment '分数',
    `rank`      int            not null comment '排名',
    period_date date           not null comment '所属周期日期',
    create_time timestamp default current_timestamp,
    update_time timestamp default current_timestamp on update current_timestamp,
    unique key uk_public_id (public_id),
    index idx_rank_def_period (rank_def_id, period_date),
    index idx_entity (entity_id)
) comment = '排行榜条目表';

-- 12. 创作者排行榜数据表
CREATE TABLE IF NOT EXISTS `creator_rank`
(
    `id`          BIGINT AUTO_INCREMENT COMMENT '自增主键',
    `public_id`   BIGINT         NOT NULL COMMENT '业务唯一ID',
    `user_id`     BIGINT         NOT NULL COMMENT '用户ID',
    `score`       DECIMAL(10, 2) NOT NULL COMMENT '热度分数',
    `rank`        INT            NOT NULL COMMENT '排名',
    `period_date` DATE           NOT NULL COMMENT '所属统计周期日期',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    UNIQUE KEY `uk_period_user` (`period_date`, `user_id`),
    INDEX `idx_period_rank` (`period_date`, `rank`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='创作者排行榜数据表';

-- 13. 帖子排行榜数据表
CREATE TABLE IF NOT EXISTS `post_rank`
(
    `id`          BIGINT AUTO_INCREMENT COMMENT '自增主键',
    `public_id`   BIGINT         NOT NULL COMMENT '业务唯一ID',
    `post_id`     BIGINT         NOT NULL COMMENT '帖子ID',
    `score`       DECIMAL(10, 2) NOT NULL COMMENT '热度分数',
    `rank`        INT            NOT NULL COMMENT '排名',
    `period_date` DATE           NOT NULL COMMENT '所属统计周期日期',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    UNIQUE KEY `uk_period_post` (`period_date`, `post_id`),
    INDEX `idx_period_rank` (`period_date`, `rank`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='帖子排行榜数据表';

-- 14. 话题/标签排行榜数据表
CREATE TABLE IF NOT EXISTS `topic_rank`
(
    `id`          BIGINT AUTO_INCREMENT COMMENT '自增主键',
    `public_id`   BIGINT         NOT NULL COMMENT '业务唯一ID',
    `tag_id`      BIGINT         NOT NULL COMMENT '标签/话题ID',
    `score`       DECIMAL(10, 2) NOT NULL COMMENT '热度分数',
    `rank`        INT            NOT NULL COMMENT '排名',
    `period_date` DATE           NOT NULL COMMENT '所属统计周期日期',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    UNIQUE KEY `uk_period_tag` (`period_date`, `tag_id`),
    INDEX `idx_period_rank` (`period_date`, `rank`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='话题/标签排行榜数据表';

CREATE TABLE `coupon_use_scope`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`   bigint(20) NOT NULL COMMENT '业务唯一ID',
    `coupon_id`   bigint(20) NOT NULL COMMENT '优惠券模板ID',
    `relation_id` bigint(20) NOT NULL COMMENT '关联的实体ID',
    `create_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    KEY `idx_coupon_id` (`coupon_id`),
    KEY `idx_relation_id` (`relation_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='优惠券可用范围关联表';


create table coupon_activity
(
    id                  bigint auto_increment primary key,
    public_id           bigint       not null comment '业务唯一ID',
    coupon_id           bigint       not null comment '关联优惠券ID',
    name                varchar(128) not null comment '活动名称',
    stock               int          not null default 0 comment '活动剩余库存',
    activity_start_time datetime     not null comment '活动开始时间',
    activity_end_time   datetime     not null comment '活动结束时间',
    status              tinyint      not null default 1 comment '活动状态',
    create_time         datetime     not null default current_timestamp comment '创建时间',
    update_time         datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    unique key uk_public_id (public_id)
) comment = '优惠券投放活动表';

create table user_setting
(
    id                      bigint auto_increment primary key comment '自增主键',
    public_id               bigint   not null comment '业务唯一ID',
    user_id                 bigint   not null comment '用户ID',
    show_delPost            tinyint  not null default 1 comment '是否显示删除的帖子',
    Customization_recommend tinyint  not null default 1 comment '是否开启个性化推荐',
    create_time             datetime not null default current_timestamp comment '创建时间',
    update_time             datetime not null default current_timestamp on update current_timestamp comment '更新时间',
    unique key uk_public_id (public_id),
    unique index `uk_user_id` (`user_id`)
) comment '用户配置表';

-- 15. 用户签到流水表
CREATE TABLE IF NOT EXISTS `user_sign_log`
(
    `id`                       bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`                bigint(20) NOT NULL COMMENT '业务唯一ID',
    `user_id`                  bigint(20) NOT NULL COMMENT '用户ID',
    `sign_date`                date       NOT NULL COMMENT '签到日期',
    `sign_time`                datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
    `sign_source`              tinyint(4)          DEFAULT '1' COMMENT '签到来源',
    `reward_points`            int(11)             DEFAULT '0' COMMENT '本次签到获得的积分',
    `continuous_days_snapshot` int(11)             DEFAULT '0' COMMENT '连续天数快照',
    `extra`                    json                DEFAULT NULL COMMENT '扩展字段',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `sign_date`),
    KEY `idx_sign_date` (`sign_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户签到流水表';

-- 16. 用户签到统计表
CREATE TABLE IF NOT EXISTS `user_sign_stats`
(
    `user_id`                 bigint(20) NOT NULL COMMENT '用户ID',
    `total_days`              int(11)    NOT NULL DEFAULT '0' COMMENT '历史累计签到总天数',
    `current_continuous_days` int(11)    NOT NULL DEFAULT '0' COMMENT '当前连续签到天数',
    `max_continuous_days`     int(11)    NOT NULL DEFAULT '0' COMMENT '历史最高连续签到天数',
    `last_sign_date`          date                DEFAULT NULL COMMENT '最后一次签到日期',
    `update_time`             datetime            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户签到统计表';

-- 17. 互动消息表
CREATE TABLE `interaction_message`
(
    `id`                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `public_id`         BIGINT     NOT NULL COMMENT '业务唯一ID',
    `sender_id`         BIGINT(20) NOT NULL COMMENT '发送者ID',
    `sender_avatar`     VARCHAR(255)        DEFAULT NULL COMMENT '发送者头像',
    `sender_name`       VARCHAR(255)        DEFAULT NULL COMMENT '发送者名称',
    `receiver_id`       BIGINT(20) NOT NULL COMMENT '接收者ID',
    `message_type`      TINYINT(4) NOT NULL COMMENT '消息类型',
    `content`           VARCHAR(1024)       DEFAULT NULL COMMENT '消息内容',
    `associate_content` BIGINT              DEFAULT NULL COMMENT '关联内容ID',
    `post_id`           BIGINT              DEFAULT NULL COMMENT '关联帖子ID',
    `associate_title`   VARCHAR(255)        DEFAULT NULL COMMENT '关联内容标题/快照',
    `is_del`            TINYINT(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time`       DATETIME            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_public_id` (`public_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_isdel_public_id` (`receiver_id`, `is_del`, `public_id`)
) COMMENT '互动消息表' ENGINE = InnoDB;