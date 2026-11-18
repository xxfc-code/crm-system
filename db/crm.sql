-- ============================================================
-- CRM 管理系统 数据库脚本
-- MySQL 8.0  InnoDB / utf8mb4
-- 说明：不建物理外键，关联关系由业务代码维护
-- ============================================================

DROP DATABASE IF EXISTS crm;
CREATE DATABASE crm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE crm;

-- ------------------------------------------------------------
-- 1. 部门表（多级，parent_id 自关联，0 表示顶级）
-- ------------------------------------------------------------
CREATE TABLE sys_dept (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  parent_id   BIGINT       NOT NULL DEFAULT 0      COMMENT '父部门ID，0为顶级',
  dept_name   VARCHAR(50)  NOT NULL                COMMENT '部门名称',
  leader      VARCHAR(30)  DEFAULT NULL            COMMENT '负责人',
  phone       VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
  sort        INT          NOT NULL DEFAULT 0      COMMENT '排序（同级升序）',
  status      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 1正常 0停用',
  deleted     TINYINT      NOT NULL DEFAULT 0      COMMENT '0未删除 1已删除',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ------------------------------------------------------------
-- 2. 员工表（员工即登录账号，emp_no 为登录名）
-- ------------------------------------------------------------
CREATE TABLE sys_employee (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  emp_no      VARCHAR(20)  DEFAULT NULL            COMMENT '工号，登录账号，格式 EMP0001，插入后按ID回填',
  emp_name    VARCHAR(30)  NOT NULL                COMMENT '姓名',
  password    VARCHAR(100) NOT NULL                COMMENT '密码（BCrypt）',
  dept_id     BIGINT       DEFAULT NULL            COMMENT '所属部门ID',
  role_id     BIGINT       DEFAULT NULL            COMMENT '角色ID（单角色）',
  phone       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
  email       VARCHAR(50)  DEFAULT NULL            COMMENT '邮箱',
  gender      TINYINT      NOT NULL DEFAULT 0      COMMENT '性别 0未知 1男 2女',
  status      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 1在职 0离职',
  deleted     TINYINT      NOT NULL DEFAULT 0      COMMENT '0未删除 1已删除',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_emp_no (emp_no),
  KEY idx_dept_id (dept_id),
  KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- ------------------------------------------------------------
-- 3. 角色表
-- ------------------------------------------------------------
CREATE TABLE sys_role (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_name   VARCHAR(30)  NOT NULL                COMMENT '角色名称',
  role_key    VARCHAR(50)  NOT NULL                COMMENT '角色标识，如 admin',
  sort        INT          NOT NULL DEFAULT 0      COMMENT '排序',
  status      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 1正常 0停用',
  remark      VARCHAR(200) DEFAULT NULL            COMMENT '备注',
  deleted     TINYINT      NOT NULL DEFAULT 0      COMMENT '0未删除 1已删除',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ------------------------------------------------------------
-- 4. 菜单/权限表
--    menu_type: M=目录(一级) C=菜单(二级) F=按钮
-- ------------------------------------------------------------
CREATE TABLE sys_menu (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  parent_id   BIGINT       NOT NULL DEFAULT 0      COMMENT '父菜单ID，0为顶级',
  menu_name   VARCHAR(30)  NOT NULL                COMMENT '菜单名称',
  menu_type   CHAR(1)      NOT NULL                COMMENT 'M目录 C菜单 F按钮',
  path        VARCHAR(100) DEFAULT NULL            COMMENT '路由地址',
  component   VARCHAR(100) DEFAULT NULL            COMMENT '前端组件路径',
  perms       VARCHAR(100) DEFAULT NULL            COMMENT '权限标识，如 crm:dept:add',
  icon        VARCHAR(50)  DEFAULT NULL            COMMENT '图标',
  sort        INT          NOT NULL DEFAULT 0      COMMENT '排序',
  status      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 1正常 0停用',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- ------------------------------------------------------------
-- 5. 角色-菜单关联表（角色拥有的权限）
-- ------------------------------------------------------------
CREATE TABLE sys_role_menu (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 部门：三级结构，用于验证多级
INSERT INTO sys_dept (id, parent_id, dept_name, leader, phone, sort) VALUES
(1, 0, '总公司',     '张总',   '13800000001', 1),
(2, 1, '销售部',     '李经理', '13800000002', 1),
(3, 1, '技术部',     '王工',   '13800000003', 2),
(4, 1, '财务部',     '赵会计', '13800000004', 3),
(5, 2, '华东销售组', '孙组长', '13800000005', 1),
(6, 2, '华南销售组', '周组长', '13800000006', 2),
(7, 3, '研发一组',   '吴组长', '13800000007', 1);

-- 角色
INSERT INTO sys_role (id, role_name, role_key, sort, remark) VALUES
(1, '超级管理员', 'admin',        1, '拥有全部权限'),
(2, '销售经理',   'sales_manager', 2, '线索、客户、商机、订单'),
(3, '销售专员',   'sales',         3, '线索、客户'),
(4, '人事专员',   'hr',            4, '员工与部门维护');

-- 员工（密码统一为 123456，BCrypt 加密）
INSERT INTO sys_employee (id, emp_no, emp_name, password, dept_id, role_id, phone, email, gender) VALUES
(1, 'EMP0001', '张总',   '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 1, 1, '13800000001', 'zhang@crm.com', 1),
(2, 'EMP0002', '李经理', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 2, 2, '13800000002', 'li@crm.com',    1),
(3, 'EMP0003', '孙组长', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 5, 3, '13800000005', 'sun@crm.com',   1),
(4, 'EMP0004', '赵会计', '$2b$10$D3P05CDkrkSV8xAZaWRQNO0jbu4wcQ2wGTRQSryx5txPb7ACIxuN6', 4, 4, '13800000004', 'zhao@crm.com',  2);

-- 菜单：5个一级目录
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort) VALUES
(1, 0, '线索', 'M', '/lead',        NULL, NULL, 'User',    1),
(2, 0, '客户', 'M', '/customer',    NULL, NULL, 'OfficeBuilding', 2),
(3, 0, '商机', 'M', '/opportunity', NULL, NULL, 'TrendCharts', 3),
(4, 0, '订单', 'M', '/order',       NULL, NULL, 'Document', 4),
(5, 0, '设置', 'M', '/system',      NULL, NULL, 'Setting', 5);

-- 菜单：设置下的二级菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort) VALUES
(6, 5, '员工管理', 'C', 'employee', 'system/employee/index', 'crm:employee:list', 'UserFilled', 1),
(7, 5, '角色管理', 'C', 'role',     'system/role/index',     'crm:role:list',     'Avatar',     2),
(8, 5, '部门管理', 'C', 'dept',     'system/dept/index',     'crm:dept:list',     'Grid',       3);

-- 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort) VALUES
(9,  6, '员工新增',     'F', 'crm:employee:add',       1),
(10, 6, '员工编辑',     'F', 'crm:employee:edit',      2),
(11, 6, '员工删除',     'F', 'crm:employee:remove',    3),
(12, 6, '重置密码',     'F', 'crm:employee:resetPwd',  4),
(13, 7, '角色新增',     'F', 'crm:role:add',           1),
(14, 7, '角色编辑',     'F', 'crm:role:edit',          2),
(15, 7, '角色删除',     'F', 'crm:role:remove',        3),
(16, 7, '分配权限',     'F', 'crm:role:auth',          4),
(17, 8, '部门新增',     'F', 'crm:dept:add',           1),
(18, 8, '部门编辑',     'F', 'crm:dept:edit',          2),
(19, 8, '部门删除',     'F', 'crm:dept:remove',        3);

-- 角色授权：超级管理员拥有全部
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 销售经理：线索/客户/商机/订单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4);

-- 销售专员：线索/客户
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1), (3, 2);

-- 人事专员：设置下的员工管理、部门管理（含按钮）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 5), (4, 6), (4, 8),
(4, 9), (4, 10), (4, 11), (4, 12),
(4, 17), (4, 18), (4, 19);
