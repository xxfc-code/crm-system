-- ============================================================
-- v3：数据看板 / 数据权限 / 线索公海
-- ============================================================
USE crm;

-- 1. 角色数据范围：1全部 2本部门 3本部门及以下 4仅本人
ALTER TABLE sys_role
  ADD COLUMN data_scope TINYINT NOT NULL DEFAULT 3 COMMENT '1全部 2本部门 3本部门及以下 4仅本人' AFTER status;

UPDATE sys_role SET data_scope = 1 WHERE id = 1;
UPDATE sys_role SET data_scope = 3 WHERE id = 2;
UPDATE sys_role SET data_scope = 4 WHERE id = 3;
UPDATE sys_role SET data_scope = 1 WHERE id = 4;

-- 2. 线索公海相关字段
ALTER TABLE crm_lead
  ADD COLUMN last_follow_time DATETIME DEFAULT NULL COMMENT '最近一次跟进时间，公海回收的判断依据' AFTER next_time,
  ADD COLUMN pool_time        DATETIME DEFAULT NULL COMMENT '进入公海的时间' AFTER last_follow_time,
  ADD COLUMN pre_owner_id     BIGINT   DEFAULT NULL COMMENT '进公海前的负责人，用于追溯' AFTER pool_time;

UPDATE crm_lead SET last_follow_time = create_time WHERE last_follow_time IS NULL;

-- 3. 工作台菜单（一级，排最前）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort) VALUES
(35, 0, '工作台', 'C', '/dashboard', 'dashboard/index', 'crm:dashboard:view', 'Odometer', 0);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1,35),(2,35),(3,35),(4,35);
