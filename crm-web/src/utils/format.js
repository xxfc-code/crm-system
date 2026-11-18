/** 金额：12.5 万这种读起来比 125000.00 快 */
export function money(value) {
  const n = Number(value || 0)
  if (n >= 10000) return (n / 10000).toFixed(n % 10000 === 0 ? 0 : 1) + ' 万'
  return n.toLocaleString('zh-CN')
}

/** 精确金额，详情页用 */
export function moneyExact(value) {
  return '¥ ' + Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

/**
 * 表格金额列专用：统一千分位，不做大数转「万」
 * 同一列里混着 "3.2 万" 和 "8,000" 最难看，长度也参差
 */
export function amount(value) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 2026-11-13T10:20:00 -> 11-13 10:20 */
export function shortTime(str) {
  if (!str) return '-'
  const s = String(str)
  return s.length >= 16 ? `${s.slice(5, 10)} ${s.slice(11, 16)}` : s
}

export function day(str) {
  return str ? String(str).slice(0, 10) : '-'
}

export function initial(name) {
  return (name || '?').slice(0, 1)
}
