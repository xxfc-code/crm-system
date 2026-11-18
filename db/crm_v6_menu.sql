-- ============================================================
-- v6：按新菜单结构重建 sys_menu / sys_role_menu
-- ============================================================
USE crm;

DELETE FROM sys_role_menu;
DELETE FROM sys_menu;

-- 一级目录
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort) VALUES
(1, 0, '工作台',     'M', '/dashboard', 'Odometer', 1),
(2, 0, '客户管理',   'M', '/customer',  'User',     2),
(3, 0, '产品管理',   'M', '/product',   'Goods',    3),
(4, 0, '销售管理',   'M', '/sales',     'Sell',     4),
(5, 0, '交付与回款', 'M', '/delivery',  'Van',      5),
(6, 0, '售后服务',   'M', '/service',   'Service',  6),
(7, 0, '数据统计',   'M', '/stat',      'DataLine', 7),
(8, 0, '系统设置',   'M', '/system',    'Setting',  8);

-- 二级菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, sort) VALUES
(11, 1, '经营概览', 'C', 'overview',   'dashboard/overview',   'crm:dashboard:view', 1),
(12, 1, '我的待办', 'C', 'todo',       'dashboard/todo',       'crm:todo:view',      2),

(21, 2, '销售线索', 'C', 'lead',       'customer/lead',        'crm:lead:list',      1),
(22, 2, '客户公海', 'C', 'pool',       'customer/pool',        'crm:lead:pool',      2),
(23, 2, '客户档案', 'C', 'archive',    'customer/archive',     'crm:customer:list',  3),
(24, 2, '联系人',   'C', 'contact',    'customer/contact',     'crm:contact:list',   4),
(25, 2, '跟进记录', 'C', 'follow',     'customer/follow',      'crm:follow:list',    5),

(31, 3, '产品分类', 'C', 'category',   'product/category',     'crm:productCategory:list', 1),
(32, 3, '产品档案', 'C', 'list',       'product/list',         'crm:product:list',   2),
(33, 3, '客户价格', 'C', 'price',      'product/price',        'crm:price:list',     3),

(41, 4, '销售商机', 'C', 'opportunity','sales/opportunity',    'crm:opportunity:list', 1),
(42, 4, '报价单',   'C', 'quotation',  'sales/quotation',      'crm:quotation:list', 2),
(43, 4, '销售订单', 'C', 'order',      'sales/order',          'crm:order:list',     3),
(44, 4, '退换货单', 'C', 'return',     'sales/return',         'crm:return:list',    4),

(51, 5, '发货记录', 'C', 'delivery',   'delivery/delivery',    'crm:delivery:list',  1),
(52, 5, '收款记录', 'C', 'payment',    'delivery/payment',     'crm:payment:list',   2),
(53, 5, '应收账款', 'C', 'receivable', 'delivery/receivable',  'crm:receivable:list',3),

(61, 6, '售后工单', 'C', 'ticket',     'service/ticket',       'crm:ticket:list',    1),
(62, 6, '客户回访', 'C', 'visit',      'service/visit',        'crm:visit:list',     2),

(71, 7, '销售统计',     'C', 'sales',    'stat/sales',    'crm:stat:sales',    1),
(72, 7, '客户采购分析', 'C', 'customer', 'stat/customer', 'crm:stat:customer', 2),
(73, 7, '产品销售分析', 'C', 'product',  'stat/product',  'crm:stat:product',  3),

(81, 8, '员工管理', 'C', 'employee', 'system/employee', 'crm:employee:list', 1),
(82, 8, '部门管理', 'C', 'dept',     'system/dept',     'crm:dept:list',     2),
(83, 8, '角色管理', 'C', 'role',     'system/role',     'crm:role:list',     3),
(84, 8, '权限管理', 'C', 'menu',     'system/menu',     'crm:menu:list',     4);

-- 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort) VALUES
(200, 21, '线索新增', 'F', 'crm:lead:add',          1),
(201, 21, '线索编辑', 'F', 'crm:lead:edit',         2),
(202, 21, '线索删除', 'F', 'crm:lead:remove',       3),
(203, 21, '线索转化', 'F', 'crm:lead:convert',      4),
(204, 22, '领取线索', 'F', 'crm:lead:claim',        1),
(205, 23, '客户新增', 'F', 'crm:customer:add',      1),
(206, 23, '客户编辑', 'F', 'crm:customer:edit',     2),
(207, 23, '客户删除', 'F', 'crm:customer:remove',   3),
(208, 24, '联系人新增', 'F', 'crm:contact:add',     1),
(209, 24, '联系人编辑', 'F', 'crm:contact:edit',    2),
(210, 24, '联系人删除', 'F', 'crm:contact:remove',  3),
(211, 25, '跟进新增', 'F', 'crm:follow:add',        1),
(212, 25, '跟进删除', 'F', 'crm:follow:remove',     2),

(220, 31, '分类新增', 'F', 'crm:productCategory:add',    1),
(221, 31, '分类编辑', 'F', 'crm:productCategory:edit',   2),
(222, 31, '分类删除', 'F', 'crm:productCategory:remove', 3),
(223, 32, '产品新增', 'F', 'crm:product:add',            1),
(224, 32, '产品编辑', 'F', 'crm:product:edit',           2),
(225, 32, '产品删除', 'F', 'crm:product:remove',         3),
(226, 33, '价格新增', 'F', 'crm:price:add',              1),
(227, 33, '价格编辑', 'F', 'crm:price:edit',             2),
(228, 33, '价格删除', 'F', 'crm:price:remove',           3),

(230, 41, '商机新增', 'F', 'crm:opportunity:add',    1),
(231, 41, '商机编辑', 'F', 'crm:opportunity:edit',   2),
(232, 41, '商机删除', 'F', 'crm:opportunity:remove', 3),
(233, 41, '阶段推进', 'F', 'crm:opportunity:stage',  4),
(234, 42, '报价新增', 'F', 'crm:quotation:add',      1),
(235, 42, '报价编辑', 'F', 'crm:quotation:edit',     2),
(236, 42, '报价删除', 'F', 'crm:quotation:remove',   3),
(237, 43, '订单新增', 'F', 'crm:order:add',          1),
(238, 43, '订单编辑', 'F', 'crm:order:edit',         2),
(239, 43, '订单删除', 'F', 'crm:order:remove',       3),
(240, 43, '状态变更', 'F', 'crm:order:status',       4),
(241, 44, '退换新增', 'F', 'crm:return:add',         1),
(242, 44, '退换编辑', 'F', 'crm:return:edit',        2),
(243, 44, '退换删除', 'F', 'crm:return:remove',      3),

(250, 51, '发货新增', 'F', 'crm:delivery:add',   1),
(251, 51, '发货编辑', 'F', 'crm:delivery:edit',  2),
(252, 51, '发货删除', 'F', 'crm:delivery:remove',3),
(253, 52, '收款新增', 'F', 'crm:payment:add',    1),
(254, 52, '收款编辑', 'F', 'crm:payment:edit',   2),
(255, 52, '收款删除', 'F', 'crm:payment:remove', 3),

(260, 61, '工单新增', 'F', 'crm:ticket:add',   1),
(261, 61, '工单编辑', 'F', 'crm:ticket:edit',  2),
(262, 61, '工单删除', 'F', 'crm:ticket:remove',3),
(263, 62, '回访新增', 'F', 'crm:visit:add',    1),
(264, 62, '回访编辑', 'F', 'crm:visit:edit',   2),
(265, 62, '回访删除', 'F', 'crm:visit:remove', 3),

(270, 81, '员工新增', 'F', 'crm:employee:add',      1),
(271, 81, '员工编辑', 'F', 'crm:employee:edit',     2),
(272, 81, '员工删除', 'F', 'crm:employee:remove',   3),
(273, 81, '重置密码', 'F', 'crm:employee:resetPwd', 4),
(274, 82, '部门新增', 'F', 'crm:dept:add',          1),
(275, 82, '部门编辑', 'F', 'crm:dept:edit',         2),
(276, 82, '部门删除', 'F', 'crm:dept:remove',       3),
(277, 83, '角色新增', 'F', 'crm:role:add',          1),
(278, 83, '角色编辑', 'F', 'crm:role:edit',         2),
(279, 83, '角色删除', 'F', 'crm:role:remove',       3),
(280, 83, '分配权限', 'F', 'crm:role:auth',         4),
(281, 84, '菜单新增', 'F', 'crm:menu:add',          1),
(282, 84, '菜单编辑', 'F', 'crm:menu:edit',         2),
(283, 84, '菜单删除', 'F', 'crm:menu:remove',       3);

-- 角色授权
-- 超管：全部
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu;

-- 销售经理：工作台 + 客户 + 销售 + 交付 + 统计
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, m.id FROM sys_menu m
WHERE m.id IN (1, 2, 4, 5, 7)
   OR m.parent_id IN (1, 2, 4, 5, 7)
   OR m.parent_id IN (SELECT id FROM (SELECT id FROM sys_menu WHERE parent_id IN (1, 2, 4, 5, 7)) t);

-- 销售专员：工作台 + 客户管理（线索/公海/档案/联系人/跟进）+ 售后工单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 3, m.id FROM sys_menu m
WHERE m.id IN (1, 2, 6, 11, 12, 21, 22, 23, 24, 25, 61)
   OR m.parent_id IN (21, 22, 23, 24, 25, 61);

-- 人事专员：工作台 + 系统设置
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 4, m.id FROM sys_menu m
WHERE m.id IN (1, 8, 11, 12)
   OR m.parent_id IN (1, 8)
   OR m.parent_id IN (SELECT id FROM (SELECT id FROM sys_menu WHERE parent_id = 8) t);
