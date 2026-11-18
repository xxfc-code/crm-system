-- ============================================================
-- v6：按新菜单结构补齐业务表
-- ============================================================
USE crm;

-- 1. 产品分类
CREATE TABLE crm_product_category (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  category_name VARCHAR(50) NOT NULL,
  parent_id   BIGINT       NOT NULL DEFAULT 0,
  sort        INT          NOT NULL DEFAULT 0,
  status      TINYINT      NOT NULL DEFAULT 1,
  deleted     TINYINT      NOT NULL DEFAULT 0,
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品分类';

-- 2. 产品档案
CREATE TABLE crm_product (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  product_no  VARCHAR(20)   DEFAULT NULL COMMENT '产品编号 P0001',
  product_name VARCHAR(100) NOT NULL,
  category_id BIGINT        DEFAULT NULL,
  spec        VARCHAR(100)  DEFAULT NULL COMMENT '规格型号',
  unit        VARCHAR(20)   DEFAULT NULL COMMENT '单位',
  price       DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '标准售价',
  cost        DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '成本价',
  stock       INT           NOT NULL DEFAULT 0 COMMENT '库存',
  status      TINYINT       NOT NULL DEFAULT 1 COMMENT '1在售 0停售',
  remark      VARCHAR(500)  DEFAULT NULL,
  deleted     TINYINT       NOT NULL DEFAULT 0,
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品档案';

-- 3. 客户协议价
CREATE TABLE crm_customer_price (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  customer_id BIGINT        NOT NULL,
  product_id  BIGINT        NOT NULL,
  price       DECIMAL(14,2) NOT NULL DEFAULT 0,
  remark      VARCHAR(200)  DEFAULT NULL,
  deleted     TINYINT       NOT NULL DEFAULT 0,
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_customer (customer_id),
  KEY idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户协议价';

-- 4. 联系人
CREATE TABLE crm_contact (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  customer_id BIGINT       NOT NULL,
  contact_name VARCHAR(30) NOT NULL,
  position    VARCHAR(50)  DEFAULT NULL COMMENT '职位',
  phone       VARCHAR(20)  DEFAULT NULL,
  email       VARCHAR(50)  DEFAULT NULL,
  wechat      VARCHAR(50)  DEFAULT NULL,
  is_primary  TINYINT      NOT NULL DEFAULT 0 COMMENT '1主联系人',
  remark      VARCHAR(200) DEFAULT NULL,
  deleted     TINYINT      NOT NULL DEFAULT 0,
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联系人';

-- 5. 报价单
CREATE TABLE crm_quotation (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  quote_no      VARCHAR(20)   DEFAULT NULL COMMENT '报价单号 BJ0001',
  customer_id   BIGINT        NOT NULL,
  quote_date    DATE          DEFAULT NULL,
  valid_until   DATE          DEFAULT NULL COMMENT '有效期至',
  total_amount  DECIMAL(14,2) NOT NULL DEFAULT 0,
  status        VARCHAR(20)   NOT NULL DEFAULT '草稿' COMMENT '草稿/已发送/已确认/已作废',
  owner_id      BIGINT        DEFAULT NULL,
  dept_id       BIGINT        DEFAULT NULL,
  remark        VARCHAR(500)  DEFAULT NULL,
  deleted       TINYINT       NOT NULL DEFAULT 0,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报价单';

-- 6. 报价明细
CREATE TABLE crm_quotation_item (
  id           BIGINT        NOT NULL AUTO_INCREMENT,
  quotation_id BIGINT        NOT NULL,
  product_id   BIGINT        DEFAULT NULL,
  product_name VARCHAR(100)  NOT NULL,
  qty          INT           NOT NULL DEFAULT 1,
  price        DECIMAL(14,2) NOT NULL DEFAULT 0,
  amount       DECIMAL(14,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_quotation (quotation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报价明细';

-- 7. 退换货单
CREATE TABLE crm_return_order (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  return_no   VARCHAR(20)   DEFAULT NULL COMMENT '单号 TH0001',
  order_id    BIGINT        DEFAULT NULL,
  customer_id BIGINT        NOT NULL,
  return_type VARCHAR(10)   NOT NULL DEFAULT '退货' COMMENT '退货/换货',
  reason      VARCHAR(200)  DEFAULT NULL,
  amount      DECIMAL(14,2) NOT NULL DEFAULT 0,
  status      VARCHAR(20)   NOT NULL DEFAULT '待处理' COMMENT '待处理/已处理/已拒绝',
  handle_date DATE          DEFAULT NULL,
  owner_id    BIGINT        DEFAULT NULL,
  dept_id     BIGINT        DEFAULT NULL,
  remark      VARCHAR(500)  DEFAULT NULL,
  deleted     TINYINT       NOT NULL DEFAULT 0,
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退换货单';

-- 8. 发货记录
CREATE TABLE crm_delivery (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  delivery_no   VARCHAR(20)  DEFAULT NULL COMMENT '发货单号 FH0001',
  order_id      BIGINT       DEFAULT NULL,
  customer_id   BIGINT       NOT NULL,
  delivery_date DATE         DEFAULT NULL,
  receiver      VARCHAR(30)  DEFAULT NULL,
  phone         VARCHAR(20)  DEFAULT NULL,
  address       VARCHAR(200) DEFAULT NULL,
  status        VARCHAR(20)  NOT NULL DEFAULT '待发货' COMMENT '待发货/已发货/已签收',
  owner_id      BIGINT       DEFAULT NULL,
  dept_id       BIGINT       DEFAULT NULL,
  remark        VARCHAR(500) DEFAULT NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发货记录';

-- 9. 收款记录
CREATE TABLE crm_payment (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  payment_no  VARCHAR(20)   DEFAULT NULL COMMENT '收款单号 SK0001',
  order_id    BIGINT        DEFAULT NULL,
  customer_id BIGINT        NOT NULL,
  amount      DECIMAL(14,2) NOT NULL DEFAULT 0,
  pay_date    DATE          DEFAULT NULL,
  pay_type    VARCHAR(20)   DEFAULT NULL COMMENT '银行转账/现金/支票/承兑',
  owner_id    BIGINT        DEFAULT NULL,
  dept_id     BIGINT        DEFAULT NULL,
  remark      VARCHAR(500)  DEFAULT NULL,
  deleted     TINYINT       NOT NULL DEFAULT 0,
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_order (order_id),
  KEY idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款记录';

-- 10. 售后工单
CREATE TABLE crm_ticket (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  ticket_no   VARCHAR(20)  DEFAULT NULL COMMENT '工单号 GD0001',
  customer_id BIGINT       NOT NULL,
  title       VARCHAR(100) NOT NULL,
  ticket_type VARCHAR(20)  NOT NULL DEFAULT '咨询' COMMENT '咨询/维修/投诉/建议',
  priority    VARCHAR(10)  NOT NULL DEFAULT '中' COMMENT '高/中/低',
  status      VARCHAR(20)  NOT NULL DEFAULT '待受理' COMMENT '待受理/处理中/已解决/已关闭',
  content     VARCHAR(1000) DEFAULT NULL,
  solution    VARCHAR(1000) DEFAULT NULL,
  owner_id    BIGINT       DEFAULT NULL,
  dept_id     BIGINT       DEFAULT NULL,
  close_time  DATETIME     DEFAULT NULL,
  deleted     TINYINT      NOT NULL DEFAULT 0,
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_customer (customer_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后工单';

-- 11. 客户回访
CREATE TABLE crm_visit (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  customer_id BIGINT       NOT NULL,
  visit_date  DATE         DEFAULT NULL,
  visitor_id  BIGINT       DEFAULT NULL COMMENT '回访人',
  way         VARCHAR(20)  DEFAULT '电话' COMMENT '电话/上门/微信',
  content     VARCHAR(500) DEFAULT NULL,
  satisfaction TINYINT     DEFAULT 5 COMMENT '满意度 1-5',
  next_time   DATE         DEFAULT NULL,
  remark      VARCHAR(200) DEFAULT NULL,
  deleted     TINYINT      NOT NULL DEFAULT 0,
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户回访';
