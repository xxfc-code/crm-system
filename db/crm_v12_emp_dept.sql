-- ============================================================
-- 员工与部门扩充（配合 10 倍演示数据）
--
-- 背景：
--   原数据只有 4 个员工（1 个销售经理 + 1 个销售专员），数据权限里
--   scope=3「本部门及以下」和 scope=4「仅本人」根本看不出差别——
--   因为绝大部分数据都挂在同两个人名下，过滤前后几乎一样。
--   数据量上到 10 倍后，这个问题更明显：页面上「能看到多少条」
--   变成了检验 RBAC 是否真的生效的唯一手段。
--
-- 本脚本做什么：
--   1. 补齐销售组织：华北销售组、西南销售组（挂在销售部下）
--   2. 补 6 个销售专员，分布到各销售组，role=3（scope=4 仅本人）
--   3. 新增的部门/员工都做成幂等：重复执行不会产生重复数据
--
-- 密码统一 123456（BCrypt hash 复用现有员工的值）
-- 工号规则：EMP + %04d（与 EmployeeService 回填逻辑一致）
-- ============================================================
USE crm;

-- ---------- 1. 部门：补两个销售组 ----------
INSERT INTO sys_dept (id, parent_id, dept_name, leader, phone, sort, status)
VALUES
  (8, 2, '华北销售组', '王海涛', '0371-88000008', 5, 1),
  (9, 2, '西南销售组', '刘思远', '028-88000009', 6, 1)
ON DUPLICATE KEY UPDATE
  dept_name = VALUES(dept_name),
  leader    = VALUES(leader),
  phone     = VALUES(phone),
  sort      = VALUES(sort),
  status    = VALUES(status);

-- ---------- 2. 员工：补销售专员（role_id=3 → scope=4 仅本人）----------
-- password 直接复用 id=1 的 BCrypt hash（明文 123456）
INSERT INTO sys_employee (id, emp_no, emp_name, password, dept_id, role_id, phone, email, gender, status)
VALUES
  (5, 'EMP0005', '王海涛', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 5, 3, '13800000005', 'wanghaitao@tea-demo.com', 1, 1),
  (6, 'EMP0006', '李思琪', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 5, 3, '13800000006', 'lisiqi@tea-demo.com',   2, 1),
  (7, 'EMP0007', '刘思远', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 9, 3, '13800000007', 'liusiyuan@tea-demo.com', 1, 1),
  (8, 'EMP0008', '陈静怡', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 6, 3, '13800000008', 'chenjingyi@tea-demo.com', 2, 1),
  (9, 'EMP0009', '赵一鸣', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 8, 3, '13800000009', 'zhaoyiming@tea-demo.com', 1, 1),
  (10, 'EMP0010', '孙悦然', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 8, 3, '13800000010', 'sunyueran@tea-demo.com', 2, 1)
ON DUPLICATE KEY UPDATE
  emp_name = VALUES(emp_name),
  dept_id  = VALUES(dept_id),
  role_id  = VALUES(role_id),
  phone    = VALUES(phone),
  email    = VALUES(email),
  status   = VALUES(status);

-- ---------- 3. 自增计数器 ----------
ALTER TABLE sys_dept AUTO_INCREMENT = 10;
ALTER TABLE sys_employee AUTO_INCREMENT = 11;

-- ---------- 4. 员工管理页数量也上到 10 倍量级 ----------
-- 补几个非销售岗（技术/客服/运营），让员工列表不是只有 6 个人
INSERT INTO sys_employee (id, emp_no, emp_name, password, dept_id, role_id, phone, email, gender, status)
VALUES
  (11, 'EMP0011', '周文轩', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 7, 4, '13800000011', 'zhouwenxuan@tea-demo.com', 1, 1),
  (12, 'EMP0012', '吴佳宁', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 3, 4, '13800000012', 'wujiani@tea-demo.com',   2, 1),
  (13, 'EMP0013', '郑浩然', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 3, 4, '13800000013', 'zhenghaoran@tea-demo.com', 1, 1),
  (14, 'EMP0014', '冯晓琳', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 4, 4, '13800000014', 'fengxiaolin@tea-demo.com', 2, 1),
  (15, 'EMP0015', '何俊豪', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 7, 4, '13800000015', 'hejunhao@tea-demo.com',  1, 1),
  (16, 'EMP0016', '马晓东', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 1, 4, '13800000016', 'maxiaodong@tea-demo.com', 1, 1),
  (17, 'EMP0017', '许美玲', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 4, 4, '13800000017', 'xumeiling@tea-demo.com', 2, 1),
  (18, 'EMP0018', '曹峻熙', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 3, 4, '13800000018', 'caojunxi@tea-demo.com',  1, 1),
  (19, 'EMP0019', '范子墨', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 7, 4, '13800000019', 'fanzimo@tea-demo.com',    1, 1),
  (20, 'EMP0020', '唐若曦', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 4, 4, '13800000020', 'tangruoxi@tea-demo.com',  2, 1)
ON DUPLICATE KEY UPDATE
  emp_name = VALUES(emp_name),
  dept_id  = VALUES(dept_id),
  role_id  = VALUES(role_id),
  phone    = VALUES(phone),
  email    = VALUES(email),
  status   = VALUES(status);

ALTER TABLE sys_employee AUTO_INCREMENT = 21;

-- ---------- 5. 把演示数据的 owner 分布到新增销售专员上 ----------
-- 目的：让 scope=4（仅本人）过滤后，不同人看到的条数明显不同；
--      scope=3（销售部及以下）则应覆盖绝大部分销售数据。
-- 销售部(2) 及其下级：华东(5)、华南(6)、华北(8)、西南(9)
UPDATE crm_customer SET owner_id = 5, dept_id = 5 WHERE id % 7 = 0;
UPDATE crm_customer SET owner_id = 6, dept_id = 5 WHERE id % 7 = 1;
UPDATE crm_customer SET owner_id = 8, dept_id = 6 WHERE id % 7 = 2;
UPDATE crm_customer SET owner_id = 9, dept_id = 8 WHERE id % 7 = 3;
UPDATE crm_customer SET owner_id = 7, dept_id = 9 WHERE id % 7 = 4;

UPDATE crm_lead SET owner_id = 5, dept_id = 5 WHERE id % 7 = 0;
UPDATE crm_lead SET owner_id = 6, dept_id = 5 WHERE id % 7 = 1;
UPDATE crm_lead SET owner_id = 8, dept_id = 6 WHERE id % 7 = 2;
UPDATE crm_lead SET owner_id = 9, dept_id = 8 WHERE id % 7 = 3;
UPDATE crm_lead SET owner_id = 7, dept_id = 9 WHERE id % 7 = 4;

-- 商机/订单/报价等：跟随客户走，保持同一条业务链的负责人一致
UPDATE crm_opportunity o
  JOIN crm_customer c ON c.id = o.customer_id
  SET o.owner_id = c.owner_id, o.dept_id = c.dept_id;

UPDATE crm_quotation q
  JOIN crm_customer c ON c.id = q.customer_id
  SET q.owner_id = c.owner_id, q.dept_id = c.dept_id;

UPDATE crm_order o2
  JOIN crm_customer c ON c.id = o2.customer_id
  SET o2.owner_id = c.owner_id, o2.dept_id = c.dept_id;

UPDATE crm_delivery d
  JOIN crm_customer c ON c.id = d.customer_id
  SET d.owner_id = c.owner_id, d.dept_id = c.dept_id;

UPDATE crm_payment p
  JOIN crm_customer c ON c.id = p.customer_id
  SET p.owner_id = c.owner_id, p.dept_id = c.dept_id;

UPDATE crm_ticket t
  JOIN crm_customer c ON c.id = t.customer_id
  SET t.owner_id = c.owner_id, t.dept_id = c.dept_id;

UPDATE crm_return_order r
  JOIN crm_customer c ON c.id = r.customer_id
  SET r.owner_id = c.owner_id, r.dept_id = c.dept_id;

-- 跟进记录的记录人跟随它跟进的业务对象
UPDATE crm_follow f
  JOIN crm_lead l ON f.biz_type = 'lead' AND l.id = f.biz_id
  SET f.create_by = l.owner_id
  WHERE f.biz_type = 'lead' AND l.owner_id IS NOT NULL;

UPDATE crm_follow f
  JOIN crm_customer c ON f.biz_type = 'customer' AND c.id = f.biz_id
  SET f.create_by = c.owner_id
  WHERE f.biz_type = 'customer' AND c.owner_id IS NOT NULL;

UPDATE crm_follow f
  JOIN crm_opportunity op ON f.biz_type = 'opportunity' AND op.id = f.biz_id
  SET f.create_by = op.owner_id
  WHERE f.biz_type = 'opportunity' AND op.owner_id IS NOT NULL;

-- ---------- 6. 客户公海：造一批长期未跟进的客户 ----------
-- 规则（CustomerService.recycle）：level != 'A' 且 last_follow_time 超 60 天 → 回公海
-- 这里手工置成公海态：owner_id 置空 + pool_time + pool_reason + 保留 pre_owner_id
UPDATE crm_customer
   SET owner_id = NULL,
       pool_time = DATE_SUB(NOW(), INTERVAL FLOOR(1 + id % 40) DAY),
       pool_reason = '长期未跟进',
       pre_owner_id = CASE id % 7
                        WHEN 0 THEN 5 WHEN 1 THEN 6 WHEN 2 THEN 8
                        WHEN 3 THEN 9 WHEN 4 THEN 7 ELSE 3 END
 WHERE level != 'A' AND id % 9 = 0;

-- 线索公海：15 天规则比客户更激进，同样手工造一批公海线索
-- 线索回收会重置 status=0，所以公海线索的 status 统一为 0
UPDATE crm_lead
   SET owner_id = NULL,
       status = 0,
       pool_time = DATE_SUB(NOW(), INTERVAL FLOOR(1 + id % 30) DAY),
       pre_owner_id = CASE id % 7
                        WHEN 0 THEN 5 WHEN 1 THEN 6 WHEN 2 THEN 8
                        WHEN 3 THEN 9 WHEN 4 THEN 7 ELSE 3 END,
       last_follow_time = DATE_SUB(NOW(), INTERVAL FLOOR(20 + id % 40) DAY)
 WHERE id % 8 = 0 AND status != 2 AND customer_id IS NULL;

-- 线索稳定化：仍有主的线索，last_follow_time 必须在 15 天内，
-- 否则凌晨 2 点的线索回收任务会把它们全部收进公海
UPDATE crm_lead
   SET last_follow_time = DATE_SUB(NOW(), INTERVAL FLOOR(1 + id % 13) DAY)
 WHERE owner_id IS NOT NULL
   AND status != 2
   AND (last_follow_time IS NULL OR last_follow_time < DATE_SUB(NOW(), INTERVAL 13 DAY));

-- A 类客户不回收（客户公海规则排除 level='A'），保持有主
UPDATE crm_customer
   SET last_follow_time = DATE_SUB(NOW(), INTERVAL FLOOR(20 + id % 100) DAY)
 WHERE level = 'A' AND last_follow_time IS NOT NULL;

-- 稳定化：仍在库且非 A 类的客户，把 last_follow_time 拉回 45 天内。
-- 否则凌晨 3 点的客户公海回收任务会把 last_follow_time 超过 60 天的
-- 客户自动 owner_id 置空，演示数据会在一夜之间悄悄变样。
UPDATE crm_customer
   SET last_follow_time = DATE_SUB(NOW(), INTERVAL FLOOR(1 + id % 44) DAY)
 WHERE owner_id IS NOT NULL
   AND level != 'A'
   AND (last_follow_time IS NULL OR last_follow_time < DATE_SUB(NOW(), INTERVAL 45 DAY));

-- 公海客户：last_follow_time 应该是很久以前的，符合「长期未跟进」的语义
UPDATE crm_customer
   SET last_follow_time = DATE_SUB(NOW(), INTERVAL FLOOR(70 + id % 60) DAY)
 WHERE owner_id IS NULL;

-- ---------- 7. 校验 ----------
SELECT '员工' AS 项目, COUNT(*) AS 数量 FROM sys_employee
UNION ALL SELECT '部门', COUNT(*) FROM sys_dept
UNION ALL SELECT '客户', COUNT(*) FROM crm_customer
UNION ALL SELECT '线索', COUNT(*) FROM crm_lead
UNION ALL SELECT '客户公海', COUNT(*) FROM crm_customer WHERE owner_id IS NULL;
