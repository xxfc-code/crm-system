-- ============================================================
-- v8 批C：协议价 / 金额联动 / 毛利 / 阶段留痕
-- 1. 商机阶段流转日志
-- 2. 报价明细加成本快照，用于算毛利
-- ============================================================
USE crm;

CREATE TABLE crm_opportunity_stage_log (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  opportunity_id BIGINT      NOT NULL,
  from_stage    VARCHAR(20)  DEFAULT NULL COMMENT '原阶段',
  to_stage      VARCHAR(20)  NOT NULL COMMENT '新阶段',
  win_rate      INT          DEFAULT NULL COMMENT '变更后的赢率',
  operator_id   BIGINT       DEFAULT NULL COMMENT '操作人',
  remark        VARCHAR(200) DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_opp (opportunity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机阶段流转记录';

ALTER TABLE crm_quotation_item
  ADD COLUMN cost DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '成本快照，算毛利用' AFTER price;
