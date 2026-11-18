# CRM 管理系统

一套面向中小型销售型企业的 CRM 系统，覆盖 **线索 → 客户 → 商机 → 报价 → 订单 → 发货 → 回款 → 售后** 的完整业务闭环，内置数据权限、按钮级权限、公海自动回收与经营分析看板。

前后端分离：后端 Spring Boot 3 + MyBatis，前端 Vue 3 + Element Plus。共 23 个 REST 控制器、30 个页面、24 张数据表。

## 功能模块

| 模块 | 页面 | 说明 |
| --- | --- | --- |
| 工作台 | 经营概览、我的待办 | 核心指标看板、待处理事项聚合 |
| 客户管理 | 销售线索、客户公海、客户档案、联系人、跟进记录 | 线索池、客户流转与归属追溯 |
| 产品管理 | 产品分类、产品档案、客户价格 | 支持按客户维护协议价 |
| 销售管理 | 销售商机、报价单、销售订单、退换货单 | 商机阶段流转，报价确认后转订单 |
| 交付回款 | 发货记录、收款记录、应收账款 | 回款由收款流水推导，应收实时联动 |
| 售后服务 | 售后工单、客户回访 | 工单可关联订单与产品 |
| 统计分析 | 销售统计、客户采购分析、产品销售分析 | 基于 ECharts 的多维分析 |
| 系统管理 | 员工、部门、角色、权限、操作日志 | RBAC + 菜单/按钮权限 + 审计留痕 |

## 技术栈

**后端**

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| Java | 17 | 运行环境 |
| Spring Boot | 3.2.5 | 基础框架 |
| MyBatis Starter | 3.0.3 | 持久层 |
| PageHelper | 2.1.0 | 物理分页 |
| MySQL Connector/J | - | 数据库驱动 |
| JJWT | 0.12.5 | JWT 签发与校验 |
| spring-security-crypto | - | BCrypt 密码加密 |
| Lombok | 1.18.46 | 简化实体代码 |

**前端**

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| Vue | 3.5 | 视图框架（组合式 API） |
| Vite | 5.4 | 构建工具 |
| Element Plus | 2.8 | UI 组件库 |
| ECharts | 6.1 | 图表 |
| Vue Router | 4.4 | 路由与菜单守卫 |
| Axios | 1.7 | HTTP 请求封装 |

## 核心设计

**认证与鉴权**
登录签发 JWT，`AuthInterceptor` 统一校验；接口方法上标注 `@RequiresPerm("crm:lead:add")`，与 `sys_menu.perms` 对应，实现按钮级权限。前端登录后按角色拉取菜单树，路由守卫据此动态放行，未授权路径自动重定向。

**数据权限**
`DataScope` 把可见范围归纳为四级——全部 / 本部门 / 本部门及以下 / 仅本人，查询时动态拼入 SQL 条件；`canAccess(ownerId, deptId)` 用于逐条判断资源是否可访问，`ownerId` 为空视为公海数据，全员可领取。

**公海自动回收**
`LeadPoolJob` 两个定时任务分别在每天 02:00 回收线索、03:00 回收客户，回收天数由配置项控制（线索 15 天、客户 60 天），避免销售长期占用资源却不跟进。

**金额一致性**
订单金额由订单明细推导，回款金额由收款流水推导，避免同一事实存两处导致对不上；退换货通过冲销流水回冲，并用 `return_id` 做幂等控制，重复提交不会重复冲账。

**审计留痕**
`OperLogInterceptor` 拦截写操作请求，记录操作人、时间、接口与参数，落到 `sys_oper_log`；商机阶段流转单独记日志表，可回溯每个阶段的停留时长。

## 目录结构

```
CRM管理系统/
├── crm-server/                     # 后端 Spring Boot 工程
│   ├── src/main/java/com/crm/
│   │   ├── common/                 # JWT、数据权限、拦截器、统一返回、异常
│   │   ├── config/                 # CORS 与拦截器注册
│   │   ├── controller/             # 23 个 REST 控制器
│   │   ├── entity/                 # 实体
│   │   ├── mapper/                 # MyBatis Mapper
│   │   ├── service/                # 业务逻辑
│   │   └── job/                    # 公海回收定时任务
│   ├── src/main/resources/
│   │   └── application.yml         # 数据源、MyBatis、日志、公海天数
│   └── pom.xml
├── crm-web/                        # 前端 Vue 3 工程
│   ├── src/
│   │   ├── layout/                 # 侧边栏 + 顶栏框架
│   │   ├── views/                  # 30 个业务页面
│   │   ├── components/             # CrudPage、Chart 等复用组件
│   │   ├── composables/            # useTable 列表逻辑复用
│   │   ├── router/                 # 路由 + 菜单守卫
│   │   ├── store/                  # 用户状态
│   │   ├── utils/                  # axios 封装、格式化、图表配置
│   │   └── styles/                 # 全局样式
│   ├── vite.config.js              # 开发代理 /api → 8080
│   └── package.json
├── db/                             # 数据库脚本（按版本增量）
└── .gitignore
```

## 快速开始

**环境要求**：JDK 17、Maven 3.6+、MySQL 8、Node.js 18+

### 1. 初始化数据库

```sql
CREATE DATABASE crm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

`db/` 下是开发过程中的增量脚本，**必须按以下顺序执行**才能还原完整结构（24 张表）：

```
crm.sql                  # 系统表：员工 / 部门 / 角色 / 菜单
crm_business.sql         # 业务表：线索 / 客户 / 商机 / 订单 / 跟进
crm_v3.sql               # 数据看板 / 数据权限 / 线索公海
crm_v6_tables.sql        # 产品、报价、发货、售后等业务表
crm_v6_data.sql          # 产品分类等种子数据
crm_v6_menu.sql          # 重建菜单树与角色菜单关联
crm_v8a.sql              # 订单明细、状态推导、报价关联商机
crm_v8b.sql              # 客户公海字段、退换货回冲
crm_v8c.sql              # 协议价、成本快照、商机阶段日志
crm_v8d.sql              # 报价审批、加盟字段、工单关联、操作日志
crm_business_menu.sql    # 业务模块按钮级权限
crm_v12_emp_dept.sql     # 员工与部门扩充（用于演示数据权限）
```

可选：

```
crm_demo_tea_v2.sql      # 演示数据（10 倍扩充版，会清空业务表并重建）
fix_admin_account.sql    # 若没有可登录账号，重置出 EMP0001 / 123456
```

> 已有可用库时，也可用 `mysqldump` 导出全量结构 + 数据，一步还原。

### 2. 启动后端

```bash
cd crm-server
mvn spring-boot:run
```

服务运行在 `http://127.0.0.1:8080`。

数据库连接默认读 `application.yml` 中的配置，密码支持环境变量覆盖：

```bash
# Linux / macOS
export DB_PASSWORD=你的数据库密码
export CRM_JWT_SECRET=至少32字节的随机字符串

# Windows PowerShell
$env:DB_PASSWORD="你的数据库密码"
$env:CRM_JWT_SECRET="至少32字节的随机字符串"
```

两个变量都有默认值，本地开发不设置也能直接启动；生产环境必须覆盖 `CRM_JWT_SECRET`，否则 JWT 签名密钥与源码中的示例值一致，token 可被伪造。

### 3. 启动前端

```bash
cd crm-web
npm install
npm run dev
```

访问 `http://127.0.0.1:5173`。开发代理已把 `/api` 转发到后端 8080，无需额外配置跨域。

### 4. 登录

| 工号 | 密码 | 角色 |
| --- | --- | --- |
| EMP0001 | 123456 | 超级管理员 |

系统无注册入口，账号由管理员在「员工管理」中创建，初始密码规则见 `EmployeeService`。

## 关键配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | 8080 | 后端端口 |
| `spring.datasource.url` | `jdbc:mysql://127.0.0.1:3306/crm` | 数据库地址 |
| `spring.datasource.username` | `${DB_USER:root}` | 优先读环境变量 |
| `spring.datasource.password` | `${DB_PASSWORD:123456}` | 优先读环境变量 |
| `crm.jwt.secret` | `${CRM_JWT_SECRET:...}` | JWT 签名密钥，不足 32 字节启动即报错 |
| `crm.lead.pool-days` | 15 | 线索未跟进多久回收进公海 |
| `crm.customer.pool-days` | 60 | 客户未跟进多久回收进公海 |

## 说明

- `db/crm_demo_tea_v2.sql` 中的公司名、门店名、人名、金额均为虚构测试数据，不使用任何真实品牌商标。
- 仓库不含 `node_modules`、构建产物、IDE 配置与数据库备份文件，克隆后按上述步骤安装依赖即可运行。
