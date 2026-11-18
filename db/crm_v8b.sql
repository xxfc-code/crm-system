-- ============================================================
-- v8 批B：客户公海 + 退换货回冲
-- 1. 客户表补公海相关字段（释放、领取、追溯）
-- 2. 收款表加 return_id，用于退换货冲销的幂等控制
-- ============================================================
USE crm;

-- 客户公海：客户也能像线索一样被"释放"和"领取"
ALTER TABLE crm_customer
  ADD COLUMN last_follow_time DATETIME DEFAULT NULL COMMENT '最近跟进时间，公海回收判断依据' AFTER remark,
  ADD COLUMN pool_time        DATETIME DEFAULT NULL COMMENT '进入公海的时间' AFTER last_follow_time,
  ADD COLUMN pre_owner_id     BIGINT   DEFAULT NULL COMMENT '进公海前的负责人' AFTER pool_time,
  ADD COLUMN pool_reason      VARCHAR(50) DEFAULT NULL COMMENT '进公海原因' AFTER pre_owner_id;

-- 收款表加退换货来源，用于避免重复冲销
ALTER TABLE crm_payment
  ADD COLUMN return_id BIGINT DEFAULT NULL COMMENT '来源退换货单ID' AFTER order_id;

UPDATE crm_customer SET last_follow_time = create_time WHERE last_follow_time IS NULL;
