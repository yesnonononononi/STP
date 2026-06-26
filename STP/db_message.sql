SET NAMES utf8mb4;

-- 创建消息会话表
DROP TABLE IF EXISTS `message_session`;
CREATE TABLE `message_session` (
    `id` BIGINT NOT NULL COMMENT '会话id',
    `user_id` BIGINT NOT NULL COMMENT '当前用户id',
    `target_id` BIGINT NOT NULL COMMENT '目标用户id',
    `type` INT DEFAULT 1 COMMENT '会话类型',
    `last_message_id` BIGINT DEFAULT NULL COMMENT '最后一条消息id',
    `last_message_content` VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息内容',
    `last_sender_id` BIGINT DEFAULT NULL COMMENT '最后发送者id',
    `last_time` DATETIME DEFAULT NULL COMMENT '最后发送时间',
    `unread_count_for_user` INT DEFAULT 0 COMMENT '用户的未读消息数量',
    `unread_count_for_target` INT DEFAULT 0 COMMENT '目标用户的未读消息数量',
    `is_top` INT DEFAULT 0 COMMENT '是否置顶',
    `is_mute` INT DEFAULT 0 COMMENT '是否免打扰',
    `draft` VARCHAR(500) DEFAULT NULL COMMENT '草稿',
    `is_hidden` INT DEFAULT 0 COMMENT '是否隐藏',
    `target_nick_name` VARCHAR(100) DEFAULT NULL COMMENT '目标用户昵称',
    `target_avatar` VARCHAR(255) DEFAULT NULL COMMENT '目标用户头像',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_target` (`user_id`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 创建私信消息表
DROP TABLE IF EXISTS `private_message`;
CREATE TABLE `private_message` (
    `id` BIGINT NOT NULL COMMENT '消息id',
    `user_id` BIGINT NOT NULL COMMENT '发送者id',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者id',
    `send_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `content` VARCHAR(1000) DEFAULT NULL COMMENT '消息内容',
    `image` VARCHAR(255) DEFAULT NULL COMMENT '图片',
    `audio` VARCHAR(255) DEFAULT NULL COMMENT '音频',
    `video` VARCHAR(255) DEFAULT NULL COMMENT '视频',
    `type` INT NOT NULL COMMENT '消息类型',
    `status` INT NOT NULL COMMENT '消息状态',
    `session_id` BIGINT NOT NULL COMMENT '会话id',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_user_receiver` (`user_id`, `receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信消息表';

-- 创建系统消息表
DROP TABLE IF EXISTS `system_message`;
CREATE TABLE `system_message` (
    `id` BIGINT NOT NULL COMMENT '消息id',
    `from_user_id` BIGINT NOT NULL COMMENT '发送者id',
    `content` VARCHAR(2000) DEFAULT NULL COMMENT '消息内容',
    `status` INT DEFAULT 1 COMMENT '状态',
    `public_time` DATETIME DEFAULT NULL COMMENT '公开时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统消息表';

-- 创建系统消息图片关联表
DROP TABLE IF EXISTS `system_message_image`;
CREATE TABLE `system_message_image` (
    `id` BIGINT NOT NULL COMMENT 'id',
    `message_id` BIGINT NOT NULL COMMENT '消息id',
    `image` VARCHAR(255) DEFAULT NULL COMMENT '图片地址',
    `status` VARCHAR(50) DEFAULT NULL COMMENT '状态',
    `create_time` VARCHAR(100) DEFAULT NULL COMMENT '创建时间',
    `update_time` VARCHAR(100) DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统消息图片关联表';
