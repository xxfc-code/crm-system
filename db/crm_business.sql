-- ============================================================
-- CRM 业务模块表：线索 / 客户 / 商机 / 订单 / 跟进记录
-- 在 crm 库中执行
-- ============================================================
USE crm;

-- ------------------------------------------------------------
-- 线索：一切业务的源头，可转化为客户
-- ------------------------------------------------------------
CREATE TABLE crm_lead (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  lead_no     VARCHAR(20)  DEFAULT NULL            COMMENT '线索编号 LD0001',
  lead_name   VARCHAR(50)  NOT NULL                COMMENT '线索名称/联系人',
  company     VARCHAR(100) DEFAULT NULL            COMMENT '公司名称',
  phone       VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
  source      VARCHAR(20)  DEFAULT NULL            COMMENT '来源：官网咨询/电话营销/展会/转介绍/广告投放',
  status      TINYINT      NOT NULL DEFAULT 0      COMMENT '0未跟进 1跟进中 2已转化 3已废弃',
  owner_id    BIGINT       DEFAULT NULL            COMMENT '负责人',
  dept_id     BIGINT       DEFAULT NULL            COMMENT '所属部门',
  customer_id BIGINT       DEFAULT NULL            COMMENT '转化后的客户ID',
  next_time   DATETIME     DEFAULT NULL            COMMENT '下次跟进时间',
  remark      VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  deleted     TINYINT      NOT NULL DEFAULT 0,
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_status (status),
  KEY idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索表';

-- ------------------------------------------------------------
-- 客户：由线索转化，或直接录入
-- ------------------------------------------------------------
CREATE TABLE crm_customer (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  customer_no   VARCHAR(20)  DEFAULT NULL          COMMENT '客户编号 KH0001',
  customer_name VARCHAR(100) NOT NULL              COMMENT '客户名称',
  contact       VARCHAR(30)  DEFAULT NULL          COMMENT '联系人',
  phone         VARCHAR(20)  DEFAULT NULL,
  email         VARCHAR(50)  DEFAULT NULL,
  level         CHAR(1)      NOT NULL DEFAULT 'C'  COMMENT 'A重点 B普通 C一般',
  industry      VARCHAR(50)  DEFAULT NULL          COMMENT '所属行业',
  address       VARCHAR(200) DEFAULT NULL,
  source        VARCHAR(20)  DEFAULT NULL          COMMENT '来源',
  owner_id      BIGINT       DEFAULT NULL,
  dept_id       BIGINT       DEFAULT NULL,
  lead_id       BIGINT       DEFAULT NULL          COMMENT '来源线索ID',
  remark        VARCHAR(500) DEFAULT NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- ------------------------------------------------------------
-- 商机：挂在客户下，有金额和阶段
-- ------------------------------------------------------------
CREATE TABLE crm_opportunity (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  opp_no      VARCHAR(20)   DEFAULT NULL           COMMENT '商机编号 SJ0001',
  opp_name    VARCHAR(100)  NOT NULL               COMMENT '商机名称',
  customer_id BIGINT        NOT NULL               COMMENT '所属客户',
  amount      DECIMAL(14,2) NOT NULL DEFAULT 0     COMMENT '预计金额',
  stage       VARCHAR(20)   NOT NULL DEFAULT '初步接洽' COMMENT '初步接洽/需求确认/方案报价/商务谈判/赢单/输单',
  win_rate    INT           NOT NULL DEFAULT 10    COMMENT '赢单概率 %',
  expect_date DATE          DEFAULT NULL           COMMENT '预计成交日期',
  owner_id    BIGINT        DEFAULT NULL,
  dept_id     BIGINT        DEFAULT NULL,
  remark      VARCHAR(500)  DEFAULT NULL,
  deleted     TINYINT       NOT NULL DEFAULT 0,
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_customer (customer_id),
  KEY idx_stage (stage)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机表';

-- ------------------------------------------------------------
-- 订单：商机成单后生成，也可直接录入
-- ------------------------------------------------------------
CREATE TABLE crm_order (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  order_no    VARCHAR(20)   DEFAULT NULL           COMMENT '订单号 DD0001',
  customer_id BIGINT        NOT NULL               COMMENT '客户',
  opp_id      BIGINT        DEFAULT NULL           COMMENT '来源商机',
  amount      DECIMAL(14,2) NOT NULL DEFAULT 0     COMMENT '订单金额',
  status      VARCHAR(20)   NOT NULL DEFAULT '待付款' COMMENT '待付款/已付款/已发货/已完成/已取消',
  sign_date   DATE          DEFAULT NULL           COMMENT '签约日期',
  pay_date    DATE          DEFAULT NULL           COMMENT '回款日期',
  owner_id    BIGINT        DEFAULT NULL,
  dept_id     BIGINT        DEFAULT NULL,
  remark      VARCHAR(500)  DEFAULT NULL,
  deleted     TINYINT       NOT NULL DEFAULT 0,
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_customer (customer_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ------------------------------------------------------------
-- 跟进记录：线索/客户/商机共用一张，用 biz_type 区分
-- ------------------------------------------------------------
CREATE TABLE crm_follow (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  biz_type    VARCHAR(20)  NOT NULL                COMMENT 'lead/customer/opportunity',
  biz_id      BIGINT       NOT NULL                COMMENT '业务ID',
  content     VARCHAR(500) NOT NULL                COMMENT '跟进内容',
  next_time   DATETIME     DEFAULT NULL            COMMENT '下次跟进时间',
  create_by   BIGINT       DEFAULT NULL            COMMENT '记录人',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跟进记录表';

-- ============================================================
-- 种子数据
-- ============================================================

INSERT INTO crm_lead (lead_no, lead_name, company, phone, source, status, owner_id, dept_id, next_time, remark) VALUES
('LD0001', '王建国', '杭州云启网络科技', '13901110001', '官网咨询', 1, 3, 5, '2026-11-15 10:00:00', '对公司产品有兴趣，已加微信'),
('LD0002', '刘敏',   '上海恒信贸易',     '13901110002', '展会',     1, 3, 5, '2026-11-16 14:00:00', '展会现场留资，需要方案'),
('LD0003', '陈志强', '深圳智造实业',     '13901110003', '电话营销', 0, 6, NULL, NULL, '首次电话未接通'),
('LD0004', '赵雪',   '北京天禾文化',     '13901110004', '转介绍',   2, 2, 2, NULL, '已转化为客户 KH0005'),
('LD0005', '孙立',   '广州速达物流',     '13901110005', '广告投放', 3, 6, NULL, NULL, '预算不足，暂时搁置'),
('LD0006', '周婷',   '南京星海教育',     '13901110006', '官网咨询', 1, 3, 6, '2026-11-18 09:30:00', '需要演示'),
('LD0007', '吴昊',   '成都锐驰汽车',     '13901110007', '老客户转介绍', 0, 2, 2, NULL, '待分配跟进'),
('LD0008', '郑丽',   '苏州精工机械',     '13901110008', '展会',     1, 6, NULL, '2026-11-20 15:00:00', '已发送产品手册');

INSERT INTO crm_customer (customer_no, customer_name, contact, phone, email, level, industry, address, source, owner_id, dept_id, lead_id, remark) VALUES
('KH0001', '杭州云启网络科技', '王建国', '13901110001', 'wang@yunqi.com',  'A', '互联网',   '杭州市西湖区文三路 100 号', '官网咨询', 3, 5, NULL, '年度框架客户'),
('KH0002', '上海恒信贸易',     '刘敏',   '13901110002', 'liu@hengxin.com', 'B', '贸易',     '上海市浦东新区世纪大道 88 号', '展会', 3, 5, NULL, ''),
('KH0003', '深圳智造实业',     '陈志强', '13901110003', 'chen@zhizao.com', 'A', '制造业',   '深圳市南山区科技园', '电话营销', 2, 2, NULL, '重点跟进'),
('KH0004', '北京天禾文化',     '赵雪',   '13901110004', 'zhao@tianhe.com', 'C', '文化传媒', '北京市朝阳区建国路 66 号', '转介绍', 2, 2, NULL, ''),
('KH0005', '南京星海教育',     '周婷',   '13901110006', 'zhou@xinghai.com','B', '教育',     '南京市鼓楼区中山路 12 号', '官网咨询', 3, 6, 4, '由线索 LD0004 转化');

INSERT INTO crm_opportunity (opp_no, opp_name, customer_id, amount, stage, win_rate, expect_date, owner_id, dept_id, remark) VALUES
('SJ0001', '云启网络-年度 SaaS 采购', 1, 480000.00, '方案报价', 60, '2026-12-15', 3, 5, '已提交报价方案'),
('SJ0002', '恒信贸易-系统集成项目',   2, 260000.00, '需求确认', 40, '2026-12-30', 3, 5, '待技术对接'),
('SJ0003', '智造实业-产线数字化改造', 3, 1200000.00,'商务谈判', 80, '2026-11-30', 2, 2, '价格谈判中'),
('SJ0004', '天禾文化-官网重建',       4, 80000.00,  '初步接洽', 20, '2027-01-20', 2, 2, ''),
('SJ0005', '星海教育-在线课堂平台',   5, 350000.00, '赢单',     100,'2026-11-10', 3, 6, '已签合同'),
('SJ0006', '云启网络-数据中台二期',   1, 620000.00, '需求确认', 45, '2027-02-28', 3, 5, ''),
('SJ0007', '恒信贸易-报表定制',       2, 45000.00,  '输单',     0,  '2026-11-05', 3, 5, '客户选择竞品');

INSERT INTO crm_order (order_no, customer_id, opp_id, amount, status, sign_date, pay_date, owner_id, dept_id, remark) VALUES
('DD0001', 5, 5, 350000.00, '已付款', '2026-11-10', '2026-11-11', 3, 6, '首期款已到账'),
('DD0002', 1, 1, 160000.00, '待付款', '2026-11-12', NULL, 3, 5, '一期款'),
('DD0003', 3, NULL, 96000.00, '已发货', '2026-10-28', '2026-10-30', 2, 2, '硬件设备'),
('DD0004', 2, 2, 52000.00, '已完成', '2026-09-15', '2026-09-20', 3, 5, '已验收'),
('DD0005', 4, 4, 30000.00, '已取消', '2026-10-08', NULL, 2, 2, '客户预算调整');

INSERT INTO crm_follow (biz_type, biz_id, content, next_time, create_by, create_time) VALUES
('lead', 1, '电话沟通，客户关注数据安全与私有化部署，已发送白皮书', '2026-11-15 10:00:00', 3, '2026-11-11 10:20:00'),
('lead', 1, '微信沟通，约定本周五线上演示', '2026-11-15 10:00:00', 3, '2026-11-12 16:40:00'),
('lead', 2, '展会现场交流，客户有明确采购计划，Q4 预算已批', '2026-11-16 14:00:00', 3, '2026-11-09 15:10:00'),
('lead', 6, '客户主动来电咨询，需要产品演示', '2026-11-18 09:30:00', 3, '2026-11-12 09:05:00'),
('customer', 1, '年度框架合同续签沟通，客户希望增加两个模块', '2026-12-01 10:00:00', 3, '2026-11-08 14:00:00'),
('customer', 3, '产线改造项目进度同步，客户方已完成内部立项', '2026-11-28 10:00:00', 2, '2026-11-10 11:30:00'),
('opportunity', 1, '已提交方案与报价，客户内部评审中', '2026-11-20 10:00:00', 3, '2026-11-11 17:00:00'),
('opportunity', 3, '商务谈判第二轮，客户要求降价 8%', '2026-11-18 14:30:00', 2, '2026-11-12 15:20:00'),
('opportunity', 5, '合同已签署，转入订单流程', NULL, 3, '2026-11-10 09:00:00');
