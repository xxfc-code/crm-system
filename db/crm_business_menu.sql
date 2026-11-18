-- 业务模块的按钮级权限
USE crm;

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort) VALUES
(20, 1, '线索新增', 'F', 'crm:lead:add',     1),
(21, 1, '线索编辑', 'F', 'crm:lead:edit',    2),
(22, 1, '线索删除', 'F', 'crm:lead:remove',  3),
(23, 1, '线索转化', 'F', 'crm:lead:convert', 4),
(24, 2, '客户新增', 'F', 'crm:customer:add',    1),
(25, 2, '客户编辑', 'F', 'crm:customer:edit',   2),
(26, 2, '客户删除', 'F', 'crm:customer:remove', 3),
(27, 3, '商机新增', 'F', 'crm:opportunity:add',    1),
(28, 3, '商机编辑', 'F', 'crm:opportunity:edit',   2),
(29, 3, '商机删除', 'F', 'crm:opportunity:remove', 3),
(30, 3, '阶段推进', 'F', 'crm:opportunity:stage',  4),
(31, 4, '订单新增', 'F', 'crm:order:add',    1),
(32, 4, '订单编辑', 'F', 'crm:order:edit',   2),
(33, 4, '订单删除', 'F', 'crm:order:remove', 3),
(34, 4, '状态变更', 'F', 'crm:order:status', 4);

-- 超级管理员 + 销售经理：全部业务按钮
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1,20),(1,21),(1,22),(1,23),(1,24),(1,25),(1,26),(1,27),(1,28),(1,29),(1,30),(1,31),(1,32),(1,33),(1,34),
(2,20),(2,21),(2,22),(2,23),(2,24),(2,25),(2,26),(2,27),(2,28),(2,29),(2,30),(2,31),(2,32),(2,33),(2,34);

-- 销售专员：只能维护线索和客户
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3,20),(3,21),(3,24),(3,25);
