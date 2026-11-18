#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
CRM 演示数据生成器（平价茶饮连锁场景）

用途
----
原有演示数据只有 10~12 条，页面上的分页、图表、报表全是空的。
本脚本按 10 倍规模生成一整套**自洽**的演示数据。

为什么不用手写 SQL
------------------
后端有 4 条「自动推导」规则，手填的常量必然和它们打架：

  1. 订单状态由「发货 + 收款」推导（OrderService.refreshStatus）
     已签收且收满 → 已完成；有发货 → 已发货；收满 → 已付款；
     有部分回款 → 部分回款；否则 → 待付款。（已取消只能手动置）
  2. 报价金额回写到商机（QuotationService.syncOpportunity）
     报价总额必须等于它挂的那个商机的金额。
  3. 退换货「已处理」会写一条负数收款冲减应收（ReturnOrderService）
     且同一退换货单只能冲销一次（payment.return_id 幂等）。
  4. 报价总额超过 5 万自动转「待审批」（applyApproveRule，阈值 50000）。

所以本脚本的策略是：**先生成「事实」（明细/发货/收款），再按后端同一套
规则反推出派生值（订单状态、报价总额、商机金额）**，让数据天然自洽。

用法
----
    python db/gen_demo_data.py --scale 10 > db/crm_demo_tea_v2.sql
    python db/gen_demo_data.py --scale 10 --out db/crm_demo_tea_v2.sql

导入：
    mysql -uroot -p123456 --default-character-set=utf8mb4 crm < db/crm_demo_tea_v2.sql

说明
----
* 固定随机种子，同样的 scale 产出完全一样的数据（便于复现和比对）。
* 只重建**业务表**，不动 sys_* 系统表（员工/部门/角色/菜单保持不变）。
* 所有公司名、门店名、人名、金额均为虚构，不使用任何真实品牌商标。
"""

import argparse
import random
import sys
from datetime import date, datetime, timedelta
from decimal import Decimal

# ============================================================================
# 配置
# ============================================================================

SEED = 20261113          # 固定种子 → 数据可复现
BASE_DATE = date(2026, 11, 13)   # 数据基准日（现有演示数据的时间锚点）
APPROVE_THRESHOLD = Decimal("50000")  # crm.quotation.approve-threshold
LEAD_POOL_DAYS = 15      # crm.lead.pool-days
CUSTOMER_POOL_DAYS = 60  # crm.customer.pool-days

# 员工（sys_employee）—— 系统表，不重建，只引用
#   id 1 张总   dept1 总公司     role1 admin
#   id 2 李经理 dept2 销售部     role2 sales_manager (data_scope=3 本部门及以下)
#   id 3 孙组长 dept5 华东销售组 role3 sales          (data_scope=4 仅本人)
#   id 4 赵会计 dept4 财务部     role4 hr
EMPLOYEES = [
    # (id, 姓名, dept_id, role_id, data_scope)
    (1, "张总", 1, 1, 1),
    (2, "李经理", 2, 2, 3),
    (3, "孙组长", 5, 3, 4),
    (4, "赵会计", 4, 4, 1),
]

# 销售岗员工池：owner_id 只能取这些，保证数据权限过滤有意义
SALES_IDS = [1, 2, 3]
# 销售组 id 5 华东 / 6 华南
SALES_DEPTS = [2, 5, 6]

# ---- 词库（全部虚构）----
CITY = [
    "郑州", "杭州", "成都", "广州", "武汉", "西安", "南京", "长沙", "合肥", "苏州",
    "重庆", "天津", "青岛", "济南", "宁波", "厦门", "福州", "昆明", "南宁", "沈阳",
    "大连", "哈尔滨", "长春", "石家庄", "太原", "兰州", "无锡", "常州", "嘉兴", "绍兴",
]
DISTRICT = [
    "金水", "花园路", "下沙", "春熙路", "天河", "体育西", "光谷", "钟楼", "仙林",
    "五一广场", "政务区", "观音桥", "观前街", "大学城", "中央路", "解放路", "人民路",
    "高新区", "滨江", "东湖", "南山", "福田", "洛溪", "车陂", "西丽", "钱江",
]
STORE_SUFFIX = ["店", "旗舰店", "形象店", "社区店", "校园店", "景区店", "商圈店", "社区店"]
CUSTOMER_TYPE = ["连锁门店", "校园门店", "旗舰门店", "景区门店", "商圈门店", "社区门店"]
DEALER_TYPE = ["区域经销", "省级代理", "城市总代"]

SURNAME = list("王李张刘陈杨黄赵吴周徐孙马朱胡林郭何高罗郑梁谢宋唐许韩冯邓曹彭曾肖田董袁潘于蒋蔡余杜叶程苏魏吕丁任沈姚卢姜崔钟谭陆汪范金石廖贾夏韦付方白邹孟熊秦邱江尹薛闫段雷侯龙史陶黎贺顾毛郝龚邵万钱严覃武戴莫孔向汤")
GIVEN = [
    "建国", "敏", "志强", "雪", "婷", "昊", "丽", "立", "芳", "磊", "文", "雯",
    "海涛", "静怡", "晓峰", "雨欣", "浩然", "佳琪", "子涵", "一鸣", "思远", "若曦",
    "明辉", "嘉怡", "博文", "雅静", "天宇", "梦琪", "俊杰", "欣怡", "泽宇", "诗涵",
    "志强", "建国", "伟", "芳", "娜", "敏", "静", "磊", "洋", "勇", "艳", "杰",
]
POSITION = ["店长", "老板", "总经理", "采购", "区域经理", "后厨主管", "仓储主管", "合伙人"]

LEAD_SURNAME_TITLE = ["女士", "先生", "老板", "店长", "经理"]
LEAD_COMPANY = [
    "加盟意向", "校园店选址咨询", "本地连锁餐饮", "商圈物料采购", "企业团购合作",
    "县域加盟咨询", "景区门店意向", "原料长期供货询价", "区域代理洽谈", "新品试销咨询",
]

SOURCE = ["官网咨询", "电话营销", "展会", "转介绍", "老客户转介绍", "广告投放"]

OPP_NAME = [
    "开业物料包", "设备采购", "年度签约", "形象升级", "开学季备货",
    "原料铺货", "冷饮设备", "包材年框", "区域代理年度合作", "门店扩展计划",
]

STAGES = ["初步接洽", "需求确认", "方案报价", "商务谈判", "赢单", "输单"]
# 与前端 dict.js 的 STAGE_RATE 保持一致
STAGE_RATE = {
    "初步接洽": 10, "需求确认": 30, "方案报价": 50, "商务谈判": 75, "赢单": 100, "输单": 0,
}

QUOTE_STATUS = ["草稿", "已发送", "已确认", "已作废"]
TICKET_TYPE = ["咨询", "维修", "投诉", "建议"]
PRIORITY = ["高", "中", "低"]
TICKET_STATUS = ["待受理", "处理中", "已解决", "已关闭"]
VISIT_WAY = ["电话", "上门", "微信"]
PAY_TYPE = ["银行转账", "现金", "支票", "承兑汇票", "微信支付"]
RETURN_TYPE = ["退货", "换货"]

# 产品分两类：物料（可批量下单）与成品（零售，不适合进订单明细）
# products: (id, name, category_id, spec, unit, price, cost, stock, is_packaging)
# 这里从数据库读实际产品，生成器只负责业务数据 → 见 load_products()

# ============================================================================
# 工具
# ============================================================================

def money(v) -> str:
    """金额统一两位小数，避免出现 7200.0 这种脏格式"""
    return f"{Decimal(v).quantize(Decimal('0.01'))}"


def dt(d) -> str:
    """日期 → 'YYYY-MM-DD'（接受 date 或已是字符串的值）"""
    if d is None:
        return None
    if isinstance(d, str):
        return d[:10]
    return d.strftime("%Y-%m-%d")


def dts(d) -> str:
    """时间戳 → 'YYYY-MM-DD HH:MM:SS'（接受 date/datetime 或已是字符串的值）"""
    if d is None:
        return None
    if isinstance(d, str):
        return d
    return d.strftime("%Y-%m-%d %H:%M:%S")


def esc(v) -> str:
    """SQL 字符串转义"""
    if v is None:
        return "NULL"
    if isinstance(v, (int, Decimal)):
        return str(v)
    return "'" + str(v).replace("\\", "\\\\").replace("'", "''") + "'"


class Gen:
    def __init__(self, scale: int):
        self.scale = scale
        self.rng = random.Random(SEED)
        self.out = []
        # 各表的自增 id 计数器
        self.ids = {}
        # 数据集合
        self.customers = []
        self.leads = []
        self.contacts = []
        self.opps = []
        self.quotes = []
        self.quote_items = []
        self.orders = []
        self.order_items = []
        self.payments = []
        self.deliveries = []
        self.returns = []
        self.tickets = []
        self.visits = []
        self.follows = []
        self.cust_prices = []
        self.stage_logs = []
        self.products = []

    def nid(self, table: str) -> int:
        self.ids[table] = self.ids.get(table, 0) + 1
        return self.ids[table]

    def pick(self, seq):
        return self.rng.choice(seq)

    def sample(self, seq, k):
        k = min(k, len(seq))
        return self.rng.sample(seq, k)

    def chance(self, p: float) -> bool:
        return self.rng.random() < p

    def rand_int(self, a: int, b: int) -> int:
        return self.rng.randint(a, b)

    # ------------------------------------------------------------------
    # 产品：物料类（category 7 原料物料 / 8 包材设备）才适合进订单明细
    # ------------------------------------------------------------------
    def load_products(self, rows):
        """rows: (id, name, category_id, spec, unit, price, cost, stock)"""
        self.products = []
        for (pid, name, cid, spec, unit, price, cost, stock) in rows:
            self.products.append({
                "id": pid, "name": name, "category_id": cid, "spec": spec,
                "unit": unit, "price": Decimal(str(price)), "cost": Decimal(str(cost)),
                "stock": stock, "material": cid in (7, 8),
            })
        self.material = [p for p in self.products if p["material"]]
        self.goods = [p for p in self.products if not p["material"]]


def load_products_from_db():
    """从库里读产品。生成器不重建产品表，只引用真实产品。"""
    import subprocess
    mysql = r"E:\phpstudy_pro\Extensions\MySQL8.0.12\bin\mysql.exe"
    sql = ("select id, product_name, category_id, ifnull(spec,''), unit, price, cost, stock "
           "from crm.crm_product where deleted = 0 order by id;")
    out = subprocess.run(
        [mysql, "-uroot", "-p123456", "-h127.0.0.1", "-N", "--default-character-set=utf8mb4", "-e", sql],
        capture_output=True, text=True, encoding="utf-8",
    )
    if out.returncode != 0:
        print("读取产品失败：", out.stderr, file=sys.stderr)
        sys.exit(1)
    rows = []
    for line in out.stdout.strip().splitlines():
        if not line.strip():
            continue
        parts = line.split("\t")
        if len(parts) < 8:
            continue
        rows.append((int(parts[0]), parts[1], int(parts[2]), parts[3],
                     parts[4], parts[5], parts[6], int(parts[7])))
    return rows


# ============================================================================
# 数据生成
# ============================================================================

def build(g: Gen):
    rng = g.rng
    scale = g.scale

    # ---------------- 规模换算（10 倍）----------------
    n_leads = 10 * scale
    n_customers = 12 * scale
    n_contacts = 10 * scale
    n_opps = 8 * scale
    n_quotes = 5 * scale
    n_orders = 9 * scale
    n_tickets = 6 * scale
    n_visits = 6 * scale
    n_follows = 12 * scale
    n_prices = 6 * scale
    n_returns = 3 * scale

    # ---------------- 客户 ----------------
    used_names = set()
    for i in range(n_customers):
        cid = g.nid("crm_customer")
        city = g.pick(CITY)
        dist = g.pick(DISTRICT)
        suffix = g.pick(STORE_SUFFIX)
        is_dealer = g.chance(0.12)
        if is_dealer:
            name = f"{city}{g.pick(['华东','华南','华北','华中','西南','西北'])}区域{g.pick(['物料经销商','总代理','供货商'])}"
            industry = g.pick(DEALER_TYPE)
        else:
            name = f"{city}{dist}{suffix}"
            industry = g.pick(CUSTOMER_TYPE)
        # 去重
        base = name
        k = 2
        while name in used_names:
            name = f"{base}{k}"
            k += 1
        used_names.add(name)

        contact = g.pick(SURNAME) + g.pick(GIVEN)
        phone = f"139{g.rand_int(10000000, 99999999)}"
        email = f"{''.join(['x'] * 1)}{g.rand_int(1000, 9999)}@tea-demo.com"
        level = g.pick(["A", "B", "C", "B", "C"])  # B/C 略多
        owner = g.pick(SALES_IDS)
        emp = next(e for e in EMPLOYEES if e[0] == owner)
        dept_id = emp[2]
        source = g.pick(SOURCE)

        # 加盟字段（v8 批D 新增，原演示数据全空）
        sign_date = BASE_DATE - timedelta(days=g.rand_int(30, 700))
        first_order = sign_date + timedelta(days=g.rand_int(3, 40))
        store_no = f"MD{g.rand_int(10000, 99999)}"
        region = f"{city}·{dist}"
        store_area = Decimal(g.rand_int(35, 320))

        g.customers.append({
            "id": cid, "no": f"KH{cid:04d}", "name": name, "store_no": store_no,
            "sign_date": sign_date, "region": region, "first_order_date": first_order,
            "store_area": store_area, "contact": contact, "phone": phone, "email": email,
            "level": level, "industry": industry,
            "address": f"{city}市{dist}{g.pick(['路','街','大道'])}{g.rand_int(1, 999)} 号",
            "source": source, "owner_id": owner, "dept_id": dept_id,
            "remark": g.pick(["样板店，月采购量大", "开学季备货", "重点形象店", "旅游旺季走量",
                              "单店销量前三", "新签门店，待铺货", "老客户，复购稳定", ""]),
        })

    # ---------------- 线索 ----------------
    for i in range(n_leads):
        lid = g.nid("crm_lead")
        title = g.pick(LEAD_SURNAME_TITLE)
        name = g.pick(SURNAME) + title
        company = f"{g.pick(CITY)}{g.pick(LEAD_COMPANY)}"
        owner = g.pick(SALES_IDS)
        emp = next(e for e in EMPLOYEES if e[0] == owner)
        dept_id = emp[2]
        source = g.pick(SOURCE)

        # 状态分布：0未跟进 25% / 1跟进中 40% / 2已转化 20% / 3已废弃 15%
        r = g.chance(0.25)
        if r:
            status = 0
        elif g.chance(0.571):
            status = 1
        elif g.chance(0.5):
            status = 2
        else:
            status = 3

        last_follow = BASE_DATE - timedelta(days=g.rand_int(0, 12))
        lead = {
            "id": lid, "no": f"LD{lid:04d}", "name": name, "company": company,
            "phone": f"139{g.rand_int(10000000, 99999999)}", "source": source,
            "status": status, "owner_id": owner, "dept_id": dept_id, "customer_id": None,
            "next_time": None, "last_follow_time": None, "pool_time": None,
            "pre_owner_id": None, "remark": g.pick([
                "意向开 2 家社区店，已发加盟手册", "关注大学城铺位，需要设备清单",
                "想增加茶饮产品线", "只问单品价格，暂搁置", "需要 500 份饮品券报价",
                "有仓储和配送能力", "已寄送样品", "关注糖浆和茶底价格",
                "已报价待客户确认", "预算约 20 万，年内落地",
            ]),
        }
        # status=2 已转化 → 必须有 customer_id 且 last_follow_time 合理
        if status == 2:
            cust = g.pick(g.customers)
            lead["customer_id"] = cust["id"]
            lead["last_follow_time"] = dts(last_follow)
            lead["remark"] = "已转为客户"
        elif status == 3:
            # 已废弃：可能被回收，也可能仍归属
            if g.chance(0.35):
                lead["owner_id"] = None
                lead["pool_time"] = dts(last_follow + timedelta(days=LEAD_POOL_DAYS))
                lead["pre_owner_id"] = owner
                lead["last_follow_time"] = dts(last_follow)
                lead["status"] = 0  # 回收时状态会被重置为 0
            else:
                lead["last_follow_time"] = dts(last_follow)
        else:
            lead["last_follow_time"] = dts(last_follow)
            # 下次跟进时间：制造「待跟进」看板数据
            if g.chance(0.7):
                delta = g.rand_int(-5, 20)
                lead["next_time"] = dts(BASE_DATE + timedelta(days=delta) + timedelta(hours=g.rand_int(9, 18)))
        g.leads.append(lead)

    # 把被回收进公海的线索状态改回 0（回收 SQL 会重置 status）
    for lead in g.leads:
        if lead["owner_id"] is None:
            lead["status"] = 0

    # ---------------- 联系人 ----------------
    for i in range(n_contacts):
        cid = g.nid("crm_contact")
        cust = g.pick(g.customers)
        is_primary = 1 if (not g.contacts or g.chance(0.12)) else 0
        name = cust["contact"] if is_primary else g.pick(SURNAME) + g.pick(GIVEN)
        g.contacts.append({
            "id": cid, "customer_id": cust["id"], "name": name,
            "position": cust["contact"] if is_primary else g.pick(POSITION),
            "phone": cust["phone"] if is_primary else f"139{g.rand_int(10000000, 99999999)}",
            "email": cust["email"] if is_primary else f"u{g.rand_int(1000, 9999)}@tea-demo.com",
            "wechat": f"wx{g.rand_int(10000, 99999)}",
            "is_primary": is_primary,
            "remark": g.pick(["决策人", "每周二下单", "物料验收", "收货确认", "负责三家门店", ""]),
        })

    # ---------------- 客户协议价 ----------------
    for i in range(n_prices):
        pid = g.nid("crm_customer_price")
        cust = g.pick(g.customers)
        prod = g.pick(g.material)
        # 协议价 = 标准价打折（85%~95%）
        price = (prod["price"] * Decimal(str(round(g.rng.uniform(0.85, 0.95), 2)))).quantize(Decimal("0.01"))
        g.cust_prices.append({
            "id": pid, "customer_id": cust["id"], "product_id": prod["id"],
            "price": price, "remark": g.pick(["样板店年度协议价", "旗舰店供货价", "连锁多店价", "区域经销价"]),
        })

    # ---------------- 商机 ----------------
    for i in range(n_opps):
        oid = g.nid("crm_opportunity")
        cust = g.pick(g.customers)
        owner = g.pick(SALES_IDS)
        emp = next(e for e in EMPLOYEES if e[0] == owner)
        stage = g.pick(STAGES)
        # 赢单/输单偏少，其余阶段铺开
        if stage in ("赢单", "输单"):
            stage = g.pick(["初步接洽", "需求确认", "方案报价", "商务谈判"])
        amount = Decimal(g.rand_int(8000, 900000))
        expect = BASE_DATE + timedelta(days=g.rand_int(-60, 180))
        g.opps.append({
            "id": oid, "no": f"SJ{oid:04d}",
            "name": f"{cust['name'][:6]}{g.pick(OPP_NAME)}",
            "customer_id": cust["id"], "amount": amount, "stage": stage,
            "win_rate": STAGE_RATE[stage], "expect_date": expect,
            "owner_id": owner, "dept_id": emp[2],
            "remark": g.pick(["含设备与首批原料", "制冰机 + 封口机", "覆盖 30 家门店",
                              "含新品铺货", "已签合同", "客户选择二手设备", "需分期付款"]),
        })

    # ---------------- 报价单 + 明细 ----------------
    for i in range(n_quotes):
        qid = g.nid("crm_quotation")
        cust = g.pick(g.customers)
        owner = g.pick(SALES_IDS)
        emp = next(e for e in EMPLOYEES if e[0] == owner)
        # 60% 挂商机（挂了就必须回写商机金额），40% 独立报价
        linked_opp = None
        if g.chance(0.6):
            cands = [o for o in g.opps if o["customer_id"] == cust["id"]]
            if cands:
                linked_opp = g.pick(cands)
        # 状态：已确认 / 已发送 / 草稿 / 已作废
        status = g.pick(["已发送", "已发送", "已确认", "已确认", "草稿", "已作废"])
        quote_date = BASE_DATE - timedelta(days=g.rand_int(0, 120))
        valid_until = quote_date + timedelta(days=g.rand_int(7, 45))

        # 明细：2~4 行物料
        n_items = g.rand_int(2, 4)
        prods = g.sample(g.material, n_items)
        items = []
        total = Decimal("0")
        for prod in prods:
            qty = g.rand_int(5, 200)
            # 协议价优先
            agreed = [p for p in g.cust_prices if p["customer_id"] == cust["id"] and p["product_id"] == prod["id"]]
            price = agreed[0]["price"] if agreed else prod["price"]
            amount = (price * qty).quantize(Decimal("0.01"))
            items.append({"product": prod, "qty": qty, "price": price, "cost": prod["cost"], "amount": amount})
            total += amount

        # 审批规则：>= 5 万自动待审批（1）；已确认/已作废说明已走过流程
        approve_status = 1 if total >= APPROVE_THRESHOLD else 0
        approve_user = None
        approve_time = None
        approve_remark = None
        if total >= APPROVE_THRESHOLD:
            if status == "已确认":
                approve_status = 2  # 审批通过
                approve_user = 1
                approve_time = dts(quote_date + timedelta(days=g.rand_int(1, 3)))
                approve_remark = "金额较大，总经理审批通过"
            elif status == "已作废":
                approve_status = 3  # 驳回
                approve_user = 1
                approve_time = dts(quote_date + timedelta(days=g.rand_int(1, 3)))
                approve_remark = "预算超限，本次作废"
        else:
            approve_status = 0  # 免审

        g.quotes.append({
            "id": qid, "no": f"BJ{qid:04d}", "customer_id": cust["id"],
            "opp_id": linked_opp["id"] if linked_opp else None,
            "quote_date": quote_date, "valid_until": valid_until,
            "total": total, "status": status, "approve_status": approve_status,
            "approve_user": approve_user, "approve_time": approve_time,
            "approve_remark": approve_remark,
            "owner_id": owner, "dept_id": emp[2],
            "remark": g.pick(["开业首批物料报价", "区域代理年度物料", "设备报价待确认",
                              "夏季果浆铺货", "开学季备货", "门店补货"]),
            "items": items,
        })

        for it in items:
            giid = g.nid("crm_quotation_item")
            g.quote_items.append({
                "id": giid, "quotation_id": qid, "product_id": it["product"]["id"],
                "product_name": it["product"]["name"], "qty": it["qty"],
                "price": it["price"], "cost": it["cost"], "amount": it["amount"],
            })

        # 规则2：报价金额回写商机
        if linked_opp:
            linked_opp["amount"] = total

    # ---------------- 订单 ----------------
    # 事实先行：明细 → 金额 → 发货 → 收款 → 推导状态
    for i in range(n_orders):
        oid = g.nid("crm_order")
        cust = g.pick(g.customers)
        owner = g.pick(SALES_IDS)
        emp = next(e for e in EMPLOYEES if e[0] == owner)

        # 35% 订单来自「已确认报价」（quote_id 关联）
        src_quote = None
        cands = [q for q in g.quotes if q["status"] == "已确认" and q["customer_id"] == cust["id"]
                 and q["approve_status"] != 1 and not q.get("_used")]
        if cands and g.chance(0.35):
            src_quote = g.pick(cands)
            src_quote["_used"] = True

        opp_id = src_quote["opp_id"] if src_quote else None
        if opp_id is None and g.chance(0.5):
            ocands = [o for o in g.opps if o["customer_id"] == cust["id"]]
            if ocands:
                opp_id = g.pick(ocands)["id"]

        if src_quote:
            items = [{"product": it["product"], "qty": it["qty"], "price": it["price"],
                      "cost": it["cost"], "amount": it["amount"]} for it in src_quote["items"]]
        else:
            n_items = g.rand_int(2, 4)
            prods = g.sample(g.material, n_items)
            items = []
            for prod in prods:
                qty = g.rand_int(5, 200)
                price = prod["price"]
                amount = (price * qty).quantize(Decimal("0.01"))
                items.append({"product": prod, "qty": qty, "price": price,
                              "cost": prod["cost"], "amount": amount})

        total = sum((it["amount"] for it in items), Decimal("0"))
        sign_date = BASE_DATE - timedelta(days=g.rand_int(0, 300))
        cancelled = g.chance(0.06)

        order = {
            "id": oid, "no": f"DD{oid:04d}", "customer_id": cust["id"],
            "opp_id": opp_id, "quote_id": src_quote["id"] if src_quote else None,
            "amount": total, "status": None, "sign_date": sign_date,
            "owner_id": owner, "dept_id": emp[2],
            "remark": "由报价单生成" if src_quote else g.pick(["门店补货", "开业物料", "季度备货", "常规补单"]),
            "items": items, "cancelled": cancelled,
            "deliveries": [], "payments": [],
        }
        g.orders.append(order)

        for it in items:
            giid = g.nid("crm_order_item")
            g.order_items.append({
                "id": giid, "order_id": oid, "product_id": it["product"]["id"],
                "product_name": it["product"]["name"], "spec": it["product"]["spec"],
                "unit": it["product"]["unit"], "qty": it["qty"], "price": it["price"],
                "cost": it["cost"], "amount": it["amount"],
            })

    # ---------------- 发货 + 收款（事实）----------------
    for order in g.orders:
        total = order["amount"]
        sign = order["sign_date"]

        # 发货：60% 有发货记录
        if not order["cancelled"] and g.chance(0.62):
            n_del = 1 if g.chance(0.8) else 2
            for k in range(n_del):
                did = g.nid("crm_delivery")
                ddate = sign + timedelta(days=g.rand_int(1, 12))
                # 越晚的越可能已签收
                if k == 0:
                    dst = g.pick(["已发货", "已发货", "已签收", "待发货"])
                else:
                    dst = g.pick(["已发货", "已签收"])
                cust = next(c for c in g.customers if c["id"] == order["customer_id"])
                g.deliveries.append({
                    "id": did, "no": f"FH{did:04d}", "order_id": order["id"],
                    "customer_id": order["customer_id"], "delivery_date": ddate,
                    "receiver": cust["contact"], "phone": cust["phone"], "address": cust["address"],
                    "status": dst, "owner_id": order["owner_id"], "dept_id": order["dept_id"],
                    "remark": "",
                })
                order["deliveries"].append(dst)

        # 收款：分档制造不同状态
        r = g.chance(0.62)
        if order["cancelled"]:
            pay_plan = []          # 已取消 → 不收款
        elif r:
            pay_plan = [total]     # 全额收完
        elif g.chance(0.55):
            # 部分回款：30%~70%
            part = (total * Decimal(str(round(g.rng.uniform(0.3, 0.7), 2)))).quantize(Decimal("0.01"))
            pay_plan = [part]
        else:
            pay_plan = []          # 待付款

        # 收款可以分多笔（最多 3 笔）
        if pay_plan and total > 0:
            if len(pay_plan) == 1 and g.chance(0.25) and pay_plan[0] > 1000:
                whole = pay_plan[0]
                first = (whole * Decimal("0.5")).quantize(Decimal("0.01"))
                pay_plan = [first, whole - first]
            for amt in pay_plan:
                pid = g.nid("crm_payment")
                pdate = sign + timedelta(days=g.rand_int(0, 25))
                if pdate > BASE_DATE:
                    pdate = BASE_DATE
                g.payments.append({
                    "id": pid, "no": f"SK{pid:04d}", "order_id": order["id"],
                    "return_id": None, "customer_id": order["customer_id"],
                    "amount": amt, "pay_date": pdate,
                    "pay_type": g.pick(PAY_TYPE),
                    "owner_id": order["owner_id"], "dept_id": order["dept_id"],
                    "remark": "",
                })
                order["payments"].append(amt)

    # ---------------- 退换货（规则3：已处理 → 负数收款冲减）----------------
    for i in range(n_returns):
        rid = g.nid("crm_return_order")
        cands = [o for o in g.orders if not o["cancelled"] and o["payments"]]
        if not cands:
            break
        order = g.pick(cands)
        rtype = g.pick(RETURN_TYPE)
        # 退换货金额取订单金额的一部分
        amt = (order["amount"] * Decimal(str(round(g.rng.uniform(0.05, 0.3), 2)))).quantize(Decimal("0.01"))
        rdate = order["sign_date"] + timedelta(days=g.rand_int(5, 40))
        status = g.pick(["已处理", "已处理", "待处理", "已拒绝"])
        handle_date = rdate if status == "已处理" else None

        g.returns.append({
            "id": rid, "no": f"TH{rid:04d}", "order_id": order["id"],
            "customer_id": order["customer_id"], "return_type": rtype,
            "reason": g.pick([
                "封口膜批次偏薄，客户要求退货", "封口机型号发错，需换台式款",
                "吸管规格与杯型不匹配", "果浆颜色与样品不一致", "冰糖浆运输中渗漏",
                "设备配件缺失，需补发", "客户仓库已满，延期调货",
            ]),
            "amount": amt, "status": status, "handle_date": handle_date,
            "owner_id": order["owner_id"], "dept_id": order["dept_id"], "remark": "",
        })

        # 已处理 → 写负数收款冲减应收（return_id 幂等）
        if status == "已处理":
            pid = g.nid("crm_payment")
            g.payments.append({
                "id": pid, "no": f"SK{pid:04d}", "order_id": order["id"],
                "return_id": rid, "customer_id": order["customer_id"],
                "amount": -amt, "pay_date": handle_date,
                "pay_type": "退换货冲销",
                "owner_id": order["owner_id"], "dept_id": order["dept_id"],
                "remark": f"退换货单 TH{rid:04d} 冲销",
            })
            order["payments"].append(-amt)

    # ---------------- 规则1：按后端同一套逻辑反推订单状态 ----------------
    for order in g.orders:
        if order["cancelled"]:
            order["status"] = "已取消"
            continue
        paid = sum(order["payments"], Decimal("0"))
        total = order["amount"]
        shipments = len(order["deliveries"])
        signed = sum(1 for s in order["deliveries"] if s == "已签收")
        if signed > 0 and paid >= total:
            order["status"] = "已完成"
        elif shipments > 0:
            order["status"] = "已发货"
        elif paid >= total:
            order["status"] = "已付款"
        elif paid > 0:
            order["status"] = "部分回款"
        else:
            order["status"] = "待付款"

    # ---------------- 商机阶段流转记录 ----------------
    for opp in g.opps:
        # 赢单/输单之前的阶段轨迹
        path = ["初步接洽"]
        if opp["stage"] in ("需求确认", "方案报价", "商务谈判", "赢单", "输单"):
            path.append("需求确认")
        if opp["stage"] in ("方案报价", "商务谈判", "赢单", "输单"):
            path.append("方案报价")
        if opp["stage"] in ("商务谈判", "赢单", "输单"):
            path.append("商务谈判")
        if opp["stage"] in ("赢单", "输单"):
            path.append(opp["stage"])
        t = BASE_DATE - timedelta(days=g.rand_int(30, 200))
        for i in range(len(path) - 1):
            lid = g.nid("crm_opportunity_stage_log")
            g.stage_logs.append({
                "id": lid, "opportunity_id": opp["id"], "from_stage": path[i],
                "to_stage": path[i + 1], "win_rate": STAGE_RATE[path[i + 1]],
                "operator_id": opp["owner_id"],
                "remark": g.pick(["客户确认需求", "方案已提交", "进入商务谈判", "合同已签署", "本次不再跟进"]),
                "create_time": dts(t),
            })
            t = t + timedelta(days=g.rand_int(5, 30))

    # ---------------- 售后工单 ----------------
    for i in range(n_tickets):
        tid = g.nid("crm_ticket")
        cust = g.pick(g.customers)
        tstatus = g.pick(TICKET_STATUS)
        order = g.pick(g.orders)
        prod = g.pick(g.material)
        created = BASE_DATE - timedelta(days=g.rand_int(0, 120))
        ttype = g.pick(TICKET_TYPE)
        priority = g.pick(PRIORITY)
        content = g.pick([
            "门店制冰机开机后不出冰，影响营业", "每天高峰时段偶发封口偏移",
            "新品上市希望总部提供操作培训", "客户反馈芒果果浆两批颜色不一致",
            "区域仓到货比约定晚两天", "咨询如何对接总部会员系统",
            "封口膜封不牢，漏液较多", "茶底预泡设备参数需要调整",
        ])
        solution = None
        close_time = None
        if tstatus == "已解决":
            solution = g.pick(["更换冷凝器，已恢复正常", "已统一批次并补偿 20 箱",
                               "已发送对接文档", "远程指导调整参数", "已补发配件并验收"])
        if tstatus == "已关闭":
            solution = g.pick(["客户确认问题已闭环", "重复报修，已换新设备", "双方协商补偿后关闭"])
            close_time = dts(created + timedelta(days=g.rand_int(1, 20)))
        g.tickets.append({
            "id": tid, "no": f"GD{tid:04d}", "customer_id": cust["id"],
            "order_id": order["id"], "product_id": prod["id"],
            "title": content[:10] if len(content) > 10 else content,
            "ticket_type": ttype, "priority": priority, "status": tstatus,
            "content": content, "solution": solution,
            "owner_id": g.pick(SALES_IDS), "dept_id": g.pick(SALES_DEPTS),
            "close_time": close_time, "create_time": dts(created),
        })

    # ---------------- 客户回访 ----------------
    for i in range(n_visits):
        vid = g.nid("crm_visit")
        cust = g.pick(g.customers)
        vdate = BASE_DATE - timedelta(days=g.rand_int(0, 180))
        g.visits.append({
            "id": vid, "customer_id": cust["id"], "visit_date": vdate,
            "visitor_id": g.pick(SALES_IDS), "way": g.pick(VISIT_WAY),
            "content": g.pick([
                "季度回访，新品动销良好，柠檬水日销 800 杯", "开学季备货到位，设备运行正常",
                "旗舰店形象升级方案沟通，客户基本认可", "反馈果浆色差问题，已安抚并补货",
                "区域代理年度框架续签沟通", "包材年框确认，等待发货",
                "门店客流回升，建议增加冰沙品类", "对账无误，账期按月结 30 天执行",
            ]),
            "satisfaction": g.pick([5, 5, 4, 4, 4, 3, 5]),
            "next_time": dt(vdate + timedelta(days=g.rand_int(15, 60))),
            "remark": "",
        })

    # ---------------- 跟进记录 ----------------
    for i in range(n_follows):
        fid = g.nid("crm_follow")
        biz_type = g.pick(["lead", "lead", "customer", "customer", "opportunity"])
        if biz_type == "lead":
            biz = g.pick(g.leads)
            content = g.pick([
                "电话沟通，客户关注加盟政策与首批物料投入，已发送加盟手册",
                "微信跟进，客户计划下周来总部考察", "展会现场交流，客户已看中铺位，需要设备清单",
                "企业团购需求，已安排报价", "客户有仓储和配送能力，可做大区代理",
            ])
        elif biz_type == "customer":
            biz = g.pick(g.customers)
            content = g.pick([
                "季度回访，确认下月物料增量", "形象升级方案已同步，等待客户确认",
                "合同细节沟通，账期定为月结 30 天", "色差问题处理跟进，已补发 20 箱",
                "门店动销回访，建议上新夏季果茶", "对账明细已发送客户核对",
            ])
        else:
            biz = g.pick(g.opps)
            content = g.pick([
                "方案与报价已提交，客户内部评审中", "商务谈判第二轮，客户希望账期再放宽",
                "合同已签署，转入订单流程", "客户要求补充设备安装说明",
                "价格已谈妥，等待走审批", "客户预算推迟到下季度，保持跟进",
            ])
        ct = BASE_DATE - timedelta(days=g.rand_int(0, 90), hours=g.rand_int(0, 23))
        next_time = None
        if g.chance(0.75):
            next_time = dts(ct + timedelta(days=g.rand_int(1, 14)))
        g.follows.append({
            "id": fid, "biz_type": biz_type, "biz_id": biz["id"], "content": content,
            "next_time": next_time, "create_by": g.pick(SALES_IDS), "create_time": dts(ct),
        })

    # 跟进记录要刷新 last_follow_time（后端 touchFollow 行为）
    # 让有跟进记录的线索/客户，其 last_follow_time 与最新跟进一致
    latest = {}
    for f in g.follows:
        key = (f["biz_type"], f["biz_id"])
        if key not in latest or f["create_time"] > latest[key]:
            latest[key] = f["create_time"]
    for lead in g.leads:
        t = latest.get(("lead", lead["id"]))
        if t and lead["owner_id"] is not None and lead["status"] != 2:
            if lead["last_follow_time"] is None or t > lead["last_follow_time"]:
                lead["last_follow_time"] = t
    for cust in g.customers:
        t = latest.get(("customer", cust["id"]))
        if t:
            cust["last_follow_time"] = t


# ============================================================================
# SQL 输出
# ============================================================================

def emit(g: Gen):
    o = g.out
    o.append("-- ============================================================")
    o.append("-- CRM 演示数据（平价茶饮连锁场景）· 10 倍扩充版")
    o.append(f"-- 由 db/gen_demo_data.py 生成，scale={g.scale}，随机种子={SEED}")
    o.append("-- 所有公司名、门店名、人名、金额均为虚构测试数据，不使用真实品牌商标")
    o.append("-- 会清空业务表并重建；sys_* 系统表（员工/部门/角色/菜单）保持不变")
    o.append("-- ============================================================")
    o.append("USE crm;")
    o.append("SET NAMES utf8mb4;")
    o.append("SET FOREIGN_KEY_CHECKS = 0;")
    o.append("")

    o.append("-- ---------------- 清空业务表 ----------------")
    for t in ["crm_quotation_item", "crm_quotation", "crm_return_order", "crm_delivery",
              "crm_payment", "crm_ticket", "crm_visit", "crm_customer_price", "crm_contact",
              "crm_follow", "crm_opportunity_stage_log", "crm_opportunity", "crm_order_item",
              "crm_order", "crm_customer", "crm_lead"]:
        o.append(f"DELETE FROM {t};")
    o.append("")

    def rows(table, cols, data, batch=200):
        if not data:
            return
        o.append(f"-- ---------------- {table}（{len(data)} 行）----------------")
        for i in range(0, len(data), batch):
            chunk = data[i:i + batch]
            o.append(f"INSERT INTO {table} ({', '.join(cols)}) VALUES")
            vals = []
            for r in chunk:
                vals.append("(" + ", ".join(esc(r[c]) for c in cols) + ")")
            o.append(",\n".join(vals) + ";")
        o.append("")

    # 产品档案：沿用库中现有产品（含测试用的 test1/test2 已在前置清理中排除）
    # 这里不重建产品，只输出引用。

    rows("crm_customer",
         ["id", "customer_no", "store_no", "sign_date", "region", "first_order_date",
          "store_area", "customer_name", "contact", "phone", "email", "level", "industry",
          "address", "source", "owner_id", "dept_id", "lead_id", "remark",
          "last_follow_time", "pool_time", "pre_owner_id", "pool_reason"],
         [{"id": c["id"], "customer_no": c["no"], "store_no": c["store_no"],
           "sign_date": dt(c["sign_date"]), "region": c["region"],
           "first_order_date": dt(c["first_order_date"]), "store_area": c["store_area"],
           "customer_name": c["name"], "contact": c["contact"], "phone": c["phone"],
           "email": c["email"], "level": c["level"], "industry": c["industry"],
           "address": c["address"], "source": c["source"], "owner_id": c["owner_id"],
           "dept_id": c["dept_id"], "lead_id": None, "remark": c["remark"],
           "last_follow_time": dts(c["last_follow_time"]) if c.get("last_follow_time") else None,
           "pool_time": None, "pre_owner_id": None, "pool_reason": None}
          for c in g.customers])

    rows("crm_lead",
         ["id", "lead_no", "lead_name", "company", "phone", "source", "status",
          "owner_id", "dept_id", "customer_id", "next_time", "last_follow_time",
          "pool_time", "pre_owner_id", "remark", "deleted"],
         [{"id": l["id"], "lead_no": l["no"], "lead_name": l["name"], "company": l["company"],
           "phone": l["phone"], "source": l["source"], "status": l["status"],
           "owner_id": l["owner_id"], "dept_id": l["dept_id"], "customer_id": l["customer_id"],
           "next_time": l["next_time"], "last_follow_time": l["last_follow_time"],
           "pool_time": l["pool_time"], "pre_owner_id": l["pre_owner_id"],
           "remark": l["remark"], "deleted": 0}
          for l in g.leads])

    rows("crm_contact",
         ["id", "customer_id", "contact_name", "position", "phone", "email", "wechat",
          "is_primary", "remark"],
         [{"id": c["id"], "customer_id": c["customer_id"], "contact_name": c["name"],
           "position": c["position"], "phone": c["phone"], "email": c["email"],
           "wechat": c["wechat"], "is_primary": c["is_primary"], "remark": c["remark"]}
          for c in g.contacts])

    rows("crm_customer_price",
         ["id", "customer_id", "product_id", "price", "remark"],
         [{"id": p["id"], "customer_id": p["customer_id"], "product_id": p["product_id"],
           "price": p["price"], "remark": p["remark"]} for p in g.cust_prices])

    rows("crm_opportunity",
         ["id", "opp_no", "opp_name", "customer_id", "amount", "stage", "win_rate",
          "expect_date", "owner_id", "dept_id", "remark"],
         [{"id": x["id"], "opp_no": x["no"], "opp_name": x["name"], "customer_id": x["customer_id"],
           "amount": x["amount"], "stage": x["stage"], "win_rate": x["win_rate"],
           "expect_date": dt(x["expect_date"]), "owner_id": x["owner_id"],
           "dept_id": x["dept_id"], "remark": x["remark"]} for x in g.opps])

    rows("crm_opportunity_stage_log",
         ["id", "opportunity_id", "from_stage", "to_stage", "win_rate", "operator_id",
          "remark", "create_time"],
         [{"id": s["id"], "opportunity_id": s["opportunity_id"], "from_stage": s["from_stage"],
           "to_stage": s["to_stage"], "win_rate": s["win_rate"], "operator_id": s["operator_id"],
           "remark": s["remark"], "create_time": s["create_time"]} for s in g.stage_logs])

    rows("crm_quotation",
         ["id", "quote_no", "customer_id", "opp_id", "quote_date", "valid_until",
          "total_amount", "status", "approve_status", "approve_user_id", "approve_time",
          "approve_remark", "owner_id", "dept_id", "remark"],
         [{"id": q["id"], "quote_no": q["no"], "customer_id": q["customer_id"],
           "opp_id": q["opp_id"], "quote_date": dt(q["quote_date"]),
           "valid_until": dt(q["valid_until"]), "total_amount": q["total"], "status": q["status"],
           "approve_status": q["approve_status"], "approve_user_id": q["approve_user"],
           "approve_time": q["approve_time"], "approve_remark": q["approve_remark"],
           "owner_id": q["owner_id"], "dept_id": q["dept_id"], "remark": q["remark"]}
          for q in g.quotes])

    rows("crm_quotation_item",
         ["id", "quotation_id", "product_id", "product_name", "qty", "price", "cost", "amount"],
         [{"id": i["id"], "quotation_id": i["quotation_id"], "product_id": i["product_id"],
           "product_name": i["product_name"], "qty": i["qty"], "price": i["price"],
           "cost": i["cost"], "amount": i["amount"]} for i in g.quote_items])

    rows("crm_order",
         ["id", "order_no", "customer_id", "opp_id", "quote_id", "amount", "status",
          "sign_date", "owner_id", "dept_id", "remark"],
         [{"id": x["id"], "order_no": x["no"], "customer_id": x["customer_id"],
           "opp_id": x["opp_id"], "quote_id": x["quote_id"], "amount": x["amount"],
           "status": x["status"], "sign_date": dt(x["sign_date"]),
           "owner_id": x["owner_id"], "dept_id": x["dept_id"], "remark": x["remark"]}
          for x in g.orders])

    rows("crm_order_item",
         ["id", "order_id", "product_id", "product_name", "spec", "unit", "qty", "price",
          "cost", "amount"],
         [{"id": i["id"], "order_id": i["order_id"], "product_id": i["product_id"],
           "product_name": i["product_name"], "spec": i["spec"], "unit": i["unit"],
           "qty": i["qty"], "price": i["price"], "cost": i["cost"], "amount": i["amount"]}
          for i in g.order_items])

    rows("crm_delivery",
         ["id", "delivery_no", "order_id", "customer_id", "delivery_date", "receiver",
          "phone", "address", "status", "owner_id", "dept_id", "remark"],
         [{"id": d["id"], "delivery_no": d["no"], "order_id": d["order_id"],
           "customer_id": d["customer_id"], "delivery_date": dt(d["delivery_date"]),
           "receiver": d["receiver"], "phone": d["phone"], "address": d["address"],
           "status": d["status"], "owner_id": d["owner_id"], "dept_id": d["dept_id"],
           "remark": d["remark"]} for d in g.deliveries])

    rows("crm_payment",
         ["id", "payment_no", "order_id", "return_id", "customer_id", "amount", "pay_date",
          "pay_type", "owner_id", "dept_id", "remark"],
         [{"id": p["id"], "payment_no": p["no"], "order_id": p["order_id"],
           "return_id": p["return_id"], "customer_id": p["customer_id"], "amount": p["amount"],
           "pay_date": dt(p["pay_date"]) if p["pay_date"] else None, "pay_type": p["pay_type"],
           "owner_id": p["owner_id"], "dept_id": p["dept_id"], "remark": p["remark"]}
          for p in g.payments])

    rows("crm_return_order",
         ["id", "return_no", "order_id", "customer_id", "return_type", "reason", "amount",
          "status", "handle_date", "owner_id", "dept_id", "remark"],
         [{"id": r["id"], "return_no": r["no"], "order_id": r["order_id"],
           "customer_id": r["customer_id"], "return_type": r["return_type"],
           "reason": r["reason"], "amount": r["amount"], "status": r["status"],
           "handle_date": dt(r["handle_date"]) if r["handle_date"] else None,
           "owner_id": r["owner_id"], "dept_id": r["dept_id"], "remark": r["remark"]}
          for r in g.returns])

    rows("crm_ticket",
         ["id", "ticket_no", "customer_id", "order_id", "product_id", "title", "ticket_type",
          "priority", "status", "content", "solution", "owner_id", "dept_id", "close_time"],
         [{"id": t["id"], "ticket_no": t["no"], "customer_id": t["customer_id"],
           "order_id": t["order_id"], "product_id": t["product_id"], "title": t["title"],
           "ticket_type": t["ticket_type"], "priority": t["priority"], "status": t["status"],
           "content": t["content"], "solution": t["solution"], "owner_id": t["owner_id"],
           "dept_id": t["dept_id"], "close_time": t["close_time"]} for t in g.tickets])

    rows("crm_visit",
         ["id", "customer_id", "visit_date", "visitor_id", "way", "content", "satisfaction",
          "next_time", "remark"],
         [{"id": v["id"], "customer_id": v["customer_id"], "visit_date": dt(v["visit_date"]),
           "visitor_id": v["visitor_id"], "way": v["way"], "content": v["content"],
           "satisfaction": v["satisfaction"], "next_time": v["next_time"],
           "remark": v["remark"]} for v in g.visits])

    rows("crm_follow",
         ["id", "biz_type", "biz_id", "content", "next_time", "create_by", "create_time"],
         [{"id": f["id"], "biz_type": f["biz_type"], "biz_id": f["biz_id"],
           "content": f["content"], "next_time": f["next_time"], "create_by": f["create_by"],
           "create_time": f["create_time"]} for f in g.follows])

    # ---------------- 自增计数器重置 ----------------
    o.append("-- ---------------- 重置自增计数器 ----------------")
    for t, n in [("crm_customer", len(g.customers)), ("crm_lead", len(g.leads)),
                 ("crm_contact", len(g.contacts)), ("crm_customer_price", len(g.cust_prices)),
                 ("crm_opportunity", len(g.opps)), ("crm_opportunity_stage_log", len(g.stage_logs)),
                 ("crm_quotation", len(g.quotes)), ("crm_quotation_item", len(g.quote_items)),
                 ("crm_order", len(g.orders)), ("crm_order_item", len(g.order_items)),
                 ("crm_delivery", len(g.deliveries)), ("crm_payment", len(g.payments)),
                 ("crm_return_order", len(g.returns)), ("crm_ticket", len(g.tickets)),
                 ("crm_visit", len(g.visits)), ("crm_follow", len(g.follows))]:
        o.append(f"ALTER TABLE {t} AUTO_INCREMENT = {n + 1};")
    o.append("")
    o.append("SET FOREIGN_KEY_CHECKS = 1;")
    o.append("")

    # ---------------- 统计 ----------------
    o.append("-- ---------------- 数据量 ----------------")
    stats = ["crm_customer", "crm_lead", "crm_contact", "crm_opportunity", "crm_quotation",
             "crm_quotation_item", "crm_order", "crm_order_item", "crm_payment",
             "crm_delivery", "crm_return_order", "crm_ticket", "crm_visit",
             "crm_follow", "crm_customer_price", "crm_opportunity_stage_log"]
    o.append("SELECT 'crm_customer' t, COUNT(*) n FROM crm_customer")
    for t in stats[1:]:
        o.append(f"UNION ALL SELECT '{t}', COUNT(*) FROM {t};")
    o.append("")
    return "\n".join(o)


def main():
    ap = argparse.ArgumentParser(description="生成 CRM 演示数据 SQL")
    ap.add_argument("--scale", type=int, default=10, help="数据量倍数（默认 10）")
    ap.add_argument("--out", default="-", help="输出文件，- 表示标准输出")
    args = ap.parse_args()

    g = Gen(args.scale)
    products = load_products_from_db()
    g.load_products(products)
    if not g.material:
        print("没有物料类产品（分类 7/8），无法生成订单明细", file=sys.stderr)
        sys.exit(1)
    build(g)
    sql = emit(g)
    if args.out == "-":
        sys.stdout.write(sql)
    else:
        with open(args.out, "w", encoding="utf-8") as f:
            f.write(sql)
        print(f"已生成 {args.out}（scale={args.scale}）", file=sys.stderr)


if __name__ == "__main__":
    main()
