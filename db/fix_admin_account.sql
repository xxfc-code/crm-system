-- ============================================================
-- 给 CRM 补一个能登录的超级管理员账号
--
-- 适用场景：部署到服务器后 sys_employee 里没有可用账号，
--           或者有账号但密码不知道（系统没有注册入口）
--
-- 最终结果：工号 EMP0001 / 密码 123456 / 超级管理员 / 在职
-- 脚本可重复执行：已存在就重置密码并修复状态，不存在就新建
-- 数据库密码：$2b$10$D3P05... 是 BCrypt 加密的 123456，
--            与 EmployeeService 新增员工时用的是同一个初始密码规则
-- ============================================================

USE crm;

-- ---------- 0. 执行前体检（只看不改）----------
SELECT 'sys_employee' AS table_name, COUNT(*) AS rows_cnt FROM sys_employee
UNION ALL SELECT 'sys_role',   COUNT(*) FROM sys_role
UNION ALL SELECT 'sys_menu',   COUNT(*) FROM sys_menu
UNION ALL SELECT 'sys_dept',   COUNT(*) FROM sys_dept;

-- ---------- 1. 兜底：确保 admin 角色存在 ----------
-- 登录后能不能看到全部菜单，取决于 role_key 是否等于 'admin'
INSERT INTO sys_role (role_name, role_key, sort, status, remark)
SELECT '超级管理员', 'admin', 1, 1, '拥有全部权限'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'admin' AND deleted = 0);

-- ---------- 2. 工号 EMP0001 已存在：重置密码 + 恢复在职 ----------
UPDATE sys_employee
   SET password = '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6',
       emp_name = '超级管理员',
       role_id  = (SELECT id FROM sys_role WHERE role_key = 'admin' AND deleted = 0 ORDER BY id LIMIT 1),
       status   = 1,
       deleted  = 0
 WHERE emp_no = 'EMP0001';

-- ---------- 3. 工号 EMP0001 不存在：直接新建 ----------
INSERT INTO sys_employee (emp_no, emp_name, password, dept_id, role_id, phone, email, gender, status, deleted)
SELECT 'EMP0001', '超级管理员',
       '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6',
       (SELECT id FROM sys_dept ORDER BY id LIMIT 1),
       (SELECT id FROM sys_role WHERE role_key = 'admin' AND deleted = 0 ORDER BY id LIMIT 1),
       '13800000001', 'admin@crm.com', 1, 1, 0
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM sys_employee WHERE emp_no = 'EMP0001');

-- ---------- 4. 结果确认 ----------
SELECT id, emp_no, emp_name, role_id, status, deleted,
       LEFT(password, 7) AS hash_prefix
  FROM sys_employee
 WHERE emp_no = 'EMP0001';
