-- 34 张表全局自增与 public_id 升级脚本

ALTER TABLE comment_image ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE comment_image SET public_id = id;
ALTER TABLE comment_image MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE comment_image ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE comment_like ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE comment_like SET public_id = id;
ALTER TABLE comment_like MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE comment_like ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE comments ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE comments SET public_id = id;
ALTER TABLE comments MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE comments ADD UNIQUE KEY uk_public_id (public_id);
ALTER TABLE comments MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '自增主键';

ALTER TABLE coupon ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE coupon SET public_id = id;
ALTER TABLE coupon MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE coupon ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE coupon_activity ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE coupon_activity SET public_id = id;
ALTER TABLE coupon_activity MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE coupon_activity ADD UNIQUE KEY uk_public_id (public_id);
ALTER TABLE coupon_activity MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '自增主键';

ALTER TABLE coupon_use_scope ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE coupon_use_scope SET public_id = id;
ALTER TABLE coupon_use_scope MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE coupon_use_scope ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE creator_rank ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE creator_rank SET public_id = id;
ALTER TABLE creator_rank MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE creator_rank ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE emoji ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE emoji SET public_id = id;
ALTER TABLE emoji MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE emoji ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE emoji_package ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE emoji_package SET public_id = id;
ALTER TABLE emoji_package MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE emoji_package ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE member_package ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE member_package SET public_id = id;
ALTER TABLE member_package MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE member_package ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE payment_order ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE payment_order SET public_id = id;
ALTER TABLE payment_order MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE payment_order ADD UNIQUE KEY uk_public_id (public_id);
ALTER TABLE payment_order MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '自增主键';

ALTER TABLE post_collect ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE post_collect SET public_id = id;
ALTER TABLE post_collect MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE post_collect ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE post_image ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE post_image SET public_id = id;
ALTER TABLE post_image MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE post_image ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE post_like ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE post_like SET public_id = id;
ALTER TABLE post_like MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE post_like ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE post_rank ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE post_rank SET public_id = id;
ALTER TABLE post_rank MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE post_rank ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE post_tag_rel ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE post_tag_rel SET public_id = id;
ALTER TABLE post_tag_rel MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE post_tag_rel ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE posts ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE posts SET public_id = id;
ALTER TABLE posts MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE posts ADD UNIQUE KEY uk_public_id (public_id);
ALTER TABLE posts MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '自增主键';

ALTER TABLE private_message ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE private_message SET public_id = id;
ALTER TABLE private_message MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE private_message ADD UNIQUE KEY uk_public_id (public_id);
ALTER TABLE private_message MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '自增主键';

ALTER TABLE session ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE session SET public_id = id;
ALTER TABLE session MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE session ADD UNIQUE KEY uk_public_id (public_id);
ALTER TABLE session MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '自增主键';

ALTER TABLE system_message ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE system_message SET public_id = id;
ALTER TABLE system_message MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE system_message ADD UNIQUE KEY uk_public_id (public_id);
ALTER TABLE system_message MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '自增主键';

ALTER TABLE system_message_image ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE system_message_image SET public_id = id;
ALTER TABLE system_message_image MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE system_message_image ADD UNIQUE KEY uk_public_id (public_id);
ALTER TABLE system_message_image MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '自增主键';

ALTER TABLE tag ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE tag SET public_id = id;
ALTER TABLE tag MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE tag ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE topic_rank ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE topic_rank SET public_id = id;
ALTER TABLE topic_rank MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE topic_rank ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE user ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE user SET public_id = id;
ALTER TABLE user MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE user ADD UNIQUE KEY uk_public_id (public_id);
ALTER TABLE user MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '自增主键';

ALTER TABLE user_coupon ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE user_coupon SET public_id = id;
ALTER TABLE user_coupon MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE user_coupon ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE user_follow ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE user_follow SET public_id = id;
ALTER TABLE user_follow MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE user_follow ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE user_session ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE user_session SET public_id = id;
ALTER TABLE user_session MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE user_session ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE user_setting ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE user_setting SET public_id = id;
ALTER TABLE user_setting MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE user_setting ADD UNIQUE KEY uk_public_id (public_id);
ALTER TABLE user_setting MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '自增主键';

ALTER TABLE user_sign_log ADD COLUMN public_id BIGINT DEFAULT NULL COMMENT '业务唯一ID' AFTER id;
UPDATE user_sign_log SET public_id = id;
ALTER TABLE user_sign_log MODIFY COLUMN public_id BIGINT NOT NULL;
ALTER TABLE user_sign_log ADD UNIQUE KEY uk_public_id (public_id);

ALTER TABLE interaction_message CHANGE COLUMN uuid public_id BIGINT NOT NULL COMMENT '业务唯一ID';
ALTER TABLE interaction_message MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '自增主键';
ALTER TABLE interaction_message DROP KEY idx_receiver_isdel_uuid;
ALTER TABLE interaction_message ADD KEY idx_receiver_isdel_public_id (receiver_id, is_del, public_id);
