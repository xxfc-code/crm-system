-- ============================================================
-- v8 批D：管理增强
-- 1. 报价过期标记 + 审批字段
-- 2. 客户加盟字段（门店编号 / 签约日 / 区域 / 首单 / 面积）
-- 3. 工单关联订单与产品
-- 4. 操作日志表
-- ============================================================
USE crm;

ALTER TABLE crm_quotation
  ADD COLUMN expired TINYINT NOT NULL DEFAULT 0 COMMENT '是否已过有效期' AFTER status,
  ADD COLUMN approve_status TINYINT NOT NULL DEFAULT 0 COMMENT '0免审 1待审 2通过 3驳回' AFTER expired,
  ADD COLUMN approve_user_id BIGINT DEFAULT NULL COMMENT '审批人' AFTER approve_status,
  ADD COLUMN approve_time DATETIME DEFAULT NULL COMMENT '审批时间' AFTER approve_user_id,
  ADD COLUMN approve_remark VARCHAR(200) DEFAULT NULL COMMENT '审批意见' AFTER approve_time;

ALTER TABLE crm_customer
  ADD COLUMN store_no VARCHAR(30) DEFAULT NULL COMMENT '门店编号' AFTER customer_no,
  ADD COLUMN sign_date DATE DEFAULT NULL COMMENT '签约日期' AFTER store_no,
  ADD COLUMN region VARCHAR(50) DEFAULT NULL COMMENT '所属区域' AFTER sign_date,
  ADD COLUMN first_order_date DATE DEFAULT NULL COMMENT '首单日期' AFTER region,
  ADD COLUMN store_area DECIMAL(10,2) DEFAULT NULL COMMENT '营业面积(平米)' AFTER first_order_date;

ALTER TABLE crm_ticket
  ADD COLUMN order_id BIGINT DEFAULT NULL COMMENT '关联订单' AFTER customer_id,
  ADD COLUMN product_id BIGINT DEFAULT NULL COMMENT '关联产品' AFTER order_id;

CREATE TABLE sys_oper_log (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  oper_user_id  BIGINT       DEFAULT NULL,
  oper_user_name VARCHAR(30) DEFAULT NULL,
  module        VARCHAR(50)  DEFAULT NULL COMMENT '模块',
  operation     VARCHAR(50)  DEFAULT NULL COMMENT '操作',
  method        VARCHAR(200) DEFAULT NULL,
  params        TEXT         DEFAULT NULL,
  ip            VARCHAR(50)  DEFAULT NULL,
  cost_ms       BIGINT       DEFAULT 0,
  success       TINYINT      NOT NULL DEFAULT 1,
  error_msg     VARCHAR(500) DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_time (create_time),
  KEY idx_user (oper_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';
