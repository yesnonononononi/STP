SET NAMES utf8mb4;

-- 创建对话表
DROP TABLE IF EXISTS `message_session`;
DROP TABLE IF EXISTS `user_session`;
DROP TABLE IF EXISTS `session`;
CREATE TABLE `session` (
    `id` BIGINT NOT NULL COMMENT '对话id，由 hash(max(user_id, target_id), min(user_id, target_id)) 生成',
    `type` INT DEFAULT 1 COMMENT '会话类型',
    `last_message_id` BIGINT DEFAULT NULL COMMENT '最后一条消息id',
    `last_message_content` VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息内容',
    `last_sender_id` BIGINT DEFAULT NULL COMMENT '最后发送者id',
    `last_time` DATETIME DEFAULT NULL COMMENT '最后发送时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话表';

-- 创建用户会话设置表
CREATE TABLE `user_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `user_id` BIGINT NOT NULL COMMENT '当前用户id',
    `session_id` BIGINT NOT NULL COMMENT '对话id，关联 session.id',
    `is_top` INT DEFAULT 0 COMMENT '是否置顶（仅对该用户）',
    `is_mute` INT DEFAULT 0 COMMENT '是否免打扰（仅对该用户）',
    `draft` VARCHAR(500) DEFAULT NULL COMMENT '草稿（仅该用户可见）',
    `unread_count` INT DEFAULT 0 COMMENT '该用户在此对话的未读消息数',
    `is_hidden` INT DEFAULT 0 COMMENT '是否隐藏（仅对该用户）',
    `target_nick_name` VARCHAR(100) DEFAULT NULL COMMENT '对方昵称（冗余，方便列表展示）',
    `target_avatar` VARCHAR(255) DEFAULT NULL COMMENT '对方头像（冗余）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_session` (`user_id`, `session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话设置表（每人一份）';

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
    `associate_user` BIGINT DEFAULT NULL COMMENT '关联的用户id',
    `type` INT DEFAULT 1 COMMENT '消息类型：1广播，2单独用户',
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
