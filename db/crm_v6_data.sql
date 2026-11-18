-- ============================================================
-- v6：新模块种子数据
-- ============================================================
USE crm;

INSERT INTO crm_product_category (id, category_name, parent_id, sort) VALUES
(1, '软件产品', 0, 1),
(2, '硬件设备', 0, 2),
(3, '专业服务', 0, 3);

INSERT INTO crm_product (product_no, product_name, category_id, spec, unit, price, cost, stock, status) VALUES
('P0001', 'CRM 标准版', 1, '20 用户授权', '套', 120000.00, 30000.00, 100, 1),
('P0002', 'CRM 企业版', 1, '不限用户授权', '套', 300000.00, 80000.00, 100, 1),
('P0003', '数据中台模块', 1, '含 BI 报表', '套', 180000.00, 50000.00, 100, 1),
('P0004', '一体机服务器', 2, '2U / 64G / 4T', '台', 68000.00, 42000.00, 12, 1),
('P0005', '实施交付服务', 3, '人天', '人天', 2000.00, 1200.00, 0, 1),
('P0006', '年度运维服务', 3, '标准 SLA', '年', 30000.00, 12000.00, 0, 1);

INSERT INTO crm_customer_price (customer_id, product_id, price, remark) VALUES
(1, 1, 105000.00, '年度框架协议价'),
(1, 2, 260000.00, '年度框架协议价'),
(3, 3, 165000.00, '战略客户价');

INSERT INTO crm_contact (customer_id, contact_name, position, phone, email, wechat, is_primary, remark) VALUES
(1, '王建国', '技术总监', '13901110001', 'wang@yunqi.com', 'wangjg', 1, '决策人'),
(1, '李雪',   '采购经理', '13901110021', 'lixue@yunqi.com', 'lixue88', 0, '负责合同'),
(2, '刘敏',   '总经理',   '13901110002', 'liu@hengxin.com', 'liumin', 1, ''),
(3, '陈志强', '信息部长', '13901110003', 'chen@zhizao.com', 'chenzq', 1, '技术对接人'),
(3, '张伟',   '项目经理', '13901110031', 'zhangwei@zhizao.com', 'zhangw', 0, '项目实施'),
(4, '赵雪',   '市场总监', '13901110004', 'zhao@tianhe.com', 'zhaoxue', 1, '');

INSERT INTO crm_quotation (quote_no, customer_id, quote_date, valid_until, total_amount, status, owner_id, dept_id, remark) VALUES
('BJ0001', 1, '2026-11-08', '2026-12-08', 405000.00, '已发送', 3, 5, '标准版 + 实施服务'),
('BJ0002', 3, '2026-11-10', '2026-12-10', 1285000.00, '已确认', 2, 2, '企业版 + 一体机 + 实施'),
('BJ0003', 4, '2026-11-11', '2026-11-25', 80000.00, '草稿',   2, 2, '官网重建项目');

INSERT INTO crm_quotation_item (quotation_id, product_id, product_name, qty, price, amount) VALUES
(1, 1, 'CRM 标准版', 1, 120000.00, 120000.00),
(1, 5, '实施交付服务', 15, 2000.00, 30000.00),
(1, 6, '年度运维服务', 1, 30000.00, 30000.00),
(2, 2, 'CRM 企业版', 1, 300000.00, 300000.00),
(2, 3, '数据中台模块', 1, 180000.00, 180000.00),
(2, 4, '一体机服务器', 8, 68000.00, 544000.00),
(2, 5, '实施交付服务', 30, 2000.00, 60000.00),
(3, 5, '实施交付服务', 40, 2000.00, 80000.00);

INSERT INTO crm_return_order (return_no, order_id, customer_id, return_type, reason, amount, status, handle_date, owner_id, dept_id) VALUES
('TH0001', 3, 3, '退货', '设备型号不符，客户要求更换', 34000.00, '已处理', '2026-11-02', 2, 2),
('TH0002', 4, 2, '换货', '部分授权模块需要调整', 12000.00, '待处理', NULL, 3, 5);

INSERT INTO crm_delivery (delivery_no, order_id, customer_id, delivery_date, receiver, phone, address, status, owner_id, dept_id) VALUES
('FH0001', 1, 5, '2026-11-11', '周婷', '13901110006', '南京市鼓楼区中山路 12 号', '已签收', 3, 6),
('FH0002', 2, 1, '2026-11-13', '王建国', '13901110001', '杭州市西湖区文三路 100 号', '已发货', 3, 5),
('FH0003', 3, 3, '2026-10-29', '陈志强', '13901110003', '深圳市南山区科技园', '已签收', 2, 2),
('FH0004', 4, 2, '2026-09-16', '刘敏', '13901110002', '上海市浦东新区世纪大道 88 号', '已签收', 3, 5);

INSERT INTO crm_payment (payment_no, order_id, customer_id, amount, pay_date, pay_type, owner_id, dept_id) VALUES
('SK0001', 1, 5, 350000.00, '2026-11-11', '银行转账', 3, 6),
('SK0002', 3, 3, 96000.00, '2026-10-30', '银行转账', 2, 2),
('SK0003', 4, 2, 52000.00, '2026-09-20', '承兑汇票', 3, 5),
('SK0004', 2, 1, 80000.00, '2026-11-12', '银行转账', 3, 5);

INSERT INTO crm_ticket (ticket_no, customer_id, title, ticket_type, priority, status, content, solution, owner_id, dept_id) VALUES
('GD0001', 1, '报表导出乱码', '维修', '高', '已解决', '客户导出 Excel 出现中文乱码', '升级补丁 v1.2.3 已修复', 3, 5),
('GD0002', 3, '系统登录偶发超时', '维修', '中', '处理中', '每日 9-10 点登录偶发超时', NULL, 2, 2),
('GD0003', 2, '希望增加审批流', '建议', '低', '待受理', '客户希望合同审批支持多级会签', NULL, 3, 5),
('GD0004', 4, '售前咨询响应慢', '投诉', '高', '已关闭', '客户反馈需求响应超过 48 小时', '已安排专属客户成功经理', 2, 2);

INSERT INTO crm_visit (customer_id, visit_date, visitor_id, way, content, satisfaction, next_time) VALUES
(1, '2026-11-05', 3, '上门', '季度回访，系统使用良好，客户提出增加两个模块需求', 5, '2026-12-05'),
(2, '2026-11-07', 3, '电话', '确认一期交付进度，客户满意', 4, '2026-12-07'),
(3, '2026-11-09', 2, '上门', '产线改造项目沟通，客户对方案认可', 5, '2026-11-28'),
(4, '2026-10-20', 2, '微信', '官网项目预算调整，暂缓推进', 3, '2026-12-20');
