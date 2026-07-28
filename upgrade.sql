alter table `stp-project`.payment_order add column `timeout_time` datetime not null  default  NOW() comment '超时时间'  ;

ALTER TABLE `stp-project`.payment_order ADD INDEX `idx_payment_order_status_timeout_time_id` (`status`, `timeout_time`, `id`);
