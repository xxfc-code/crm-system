/** 业务字典，前后端对同一批字符串保持一致 */

export const LEAD_STATUS = {
  0: { label: '未跟进', color: '#9B949E' },
  1: { label: '跟进中', color: '#F04E8B' },
  2: { label: '已转化', color: '#2FB98A' },
  3: { label: '已废弃', color: '#E0635B' }
}

export const SOURCES = ['官网咨询', '电话营销', '展会', '转介绍', '老客户转介绍', '广告投放']

export const CUSTOMER_LEVEL = {
  A: { label: '重点', color: '#E0635B' },
  B: { label: '普通', color: '#F04E8B' },
  C: { label: '一般', color: '#9B949E' }
}

export const OPP_STAGES = ['初步接洽', '需求确认', '方案报价', '商务谈判', '赢单', '输单']

export const STAGE_COLOR = {
  初步接洽: '#8A8494',
  需求确认: '#F04E8B',
  方案报价: '#C05FCC',
  商务谈判: '#F0A04E',
  赢单: '#2FB98A',
  输单: '#E0635B'
}

/** 阶段推进时自动带出的赢率 */
export const STAGE_RATE = {
  初步接洽: 10,
  需求确认: 30,
  方案报价: 50,
  商务谈判: 75,
  赢单: 100,
  输单: 0
}

export const ORDER_STATUS = {
  待付款: { label: '待付款', color: '#F0A04E' },
  部分回款: { label: '部分回款', color: '#C05FCC' },
  已付款: { label: '已付款', color: '#F04E8B' },
  已发货: { label: '已发货', color: '#8A8494' },
  已完成: { label: '已完成', color: '#2FB98A' },
  已取消: { label: '已取消', color: '#9B949E' }
}

export const GENDERS = { 0: '未知', 1: '男', 2: '女' }
