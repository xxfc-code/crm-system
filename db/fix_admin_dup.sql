-- ============================================================
-- 修复 EMP0001 重复 + 重置为可登录状态
--
-- 背景：截图里 update ... where emp_no='EMP0001' 返回 Rows matched: 2，
--       说明 sys_employee 里有两条工号相同的记录。
--       登录 SQL 是 select * from sys_employee where emp_no=? and deleted=0
--       且 mapper 返回类型是单个 Employee —— 查出 2 行会抛
--       TooManyResultsException，接口直接 500，账号密码对也进不去。
--
-- 顺带：截图里用的 hash 不是 123456（实测 123456/admin/123456789 全部 false），
--       这里统一换成已验证等于 123456 的哈希。
--
-- 执行方式：整段一起执行（用到了会话变量 @keep），别一条条挑着跑。
-- ============================================================

USE crm;

-- ---------- 1. 先看清现状：同一个工号有几条、分别是什么 ----------
SELECT id, emp_no, emp_name, role_id, dept_id, status, deleted, create_time
  FROM sys_employee
 WHERE emp_no = 'EMP0001'
 ORDER BY id;

-- 顺便扫一遍其它工号有没有重复（都应该为 0 行）
SELECT emp_no, COUNT(*) AS cnt
  FROM sys_employee
 WHERE deleted = 0
 GROUP BY emp_no
HAVING cnt > 1;

-- ---------- 2. 去重：只留 id 最小的那条 ----------
-- 多余的做软删除（deleted=1），并把工号改成 EMP0001_X<id>，
-- 这样以后任何 where emp_no='EMP0001' 都只命中一条。
SET @keep := (SELECT MIN(id) FROM sys_employee WHERE emp_no = 'EMP0001');

UPDATE sys_employee
   SET deleted = 1,
       status  = 0,
       emp_no  = CONCAT('EMP0001_X', id)
 WHERE emp_no = 'EMP0001'
   AND id <> @keep;

-- ---------- 3. 把留下的那条规格化：管理员 / 123456 / 在职 ----------
UPDATE sys_employee
   SET password = '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6',
       emp_name = '管理员',
       role_id  = 1,
       dept_id  = 1,
       status   = 1,
       deleted  = 0
 WHERE emp_no = 'EMP0001'
   AND id = @keep;

-- ---------- 4. 复核：必须只剩 1 条有效记录 ----------
SELECT COUNT(*) AS 有效条数
  FROM sys_employee
 WHERE emp_no = 'EMP0001' AND deleted = 0;

SELECT id, emp_no, emp_name, role_id, dept_id, status, deleted, LEFT(password, 7) AS hash_prefix
  FROM sys_employee
 WHERE emp_no LIKE 'EMP0001%'
 ORDER BY id;
