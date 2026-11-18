/**
 * 看板图表的 ECharts 配置
 * 单独抽出来，页面只管传数据，样式细节集中在这里改
 */

/** 白粉主题下的图表配色 */
export const CHART_COLOR = {
  brand: '#F04E8B',
  brandSoft: '#F583AC',
  brandPale: '#FDC9DB',
  purple: '#C05FCC',
  orange: '#F0A04E',
  green: '#2FB98A',
  red: '#E0635B',
  gray: '#9B949E',
  text: '#1f2329',
  text2: '#646a73',
  text3: '#8f959e',
  line: '#F2EAEE',
  split: '#F7F2F5'
}

/** 来源占比这类分类配色，粉→紫→橙渐次拉开 */
export const CATEGORY_COLOR = [
  '#F04E8B',
  '#C05FCC',
  '#F0A04E',
  '#2FB98A',
  '#E0635B',
  '#8A8494',
  '#F583AC',
  '#7C9CE0'
]

const BASE_TEXT = {
  color: CHART_COLOR.text2,
  fontSize: 12,
  fontFamily: 'inherit'
}

/** 折线图上平滑的粉色渐变填充 */
function areaGradient(color) {
  return {
    type: 'linear',
    x: 0,
    y: 0,
    x2: 0,
    y2: 1,
    colorStops: [
      { offset: 0, color: color + '38' },
      { offset: 1, color: color + '00' }
    ]
  }
}

/**
 * 商机阶段分布：上柱状图（金额）+ 下折线（商机数量），共享 x 轴
 * 折线不放「转化率」——不同阶段的商机金额本来就不可比（初步接洽的单子可能就大），
 * 拿金额算转化率会出现 500% 这种荒谬值。
 * 也不做成双轴叠在一张图里：两个量纲共用一个坐标系，折线必然横穿柱体。
 */
export function funnelOption(stages, amounts, counts) {
  return {
    grid: [
      { left: 8, right: 12, top: 28, height: '48%', containLabel: true },
      { left: 8, right: 12, top: '74%', bottom: 4, containLabel: true }
    ],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(31,35,41,0.92)',
      borderWidth: 0,
      padding: [9, 12],
      textStyle: { color: '#fff', fontSize: 12 },
      formatter: (params) => {
        const bar = params.find((x) => x.seriesType === 'bar')
        const line = params.find((x) => x.seriesType === 'line')
        let html = `<b>${bar.axisValue}</b><br/>${bar.marker}金额 ${Number(bar.value).toLocaleString('zh-CN')}`
        if (line) html += `<br/>${line.marker}${line.value} 个商机`
        return html
      }
    },
    legend: {
      data: ['金额', '商机数'],
      right: 0,
      top: 0,
      itemWidth: 10,
      itemHeight: 10,
      icon: 'roundRect',
      textStyle: BASE_TEXT
    },
    axisPointer: { link: [{ xAxisIndex: 'all' }] },
    xAxis: [
      {
        gridIndex: 0,
        type: 'category',
        data: stages,
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { show: false }
      },
      {
        gridIndex: 1,
        type: 'category',
        data: stages,
        axisLine: { lineStyle: { color: CHART_COLOR.line } },
        axisTick: { show: false },
        axisLabel: { ...BASE_TEXT, color: CHART_COLOR.text2 }
      }
    ],
    yAxis: [
      {
        gridIndex: 0,
        type: 'value',
        name: '金额',
        nameTextStyle: { ...BASE_TEXT, padding: [0, 0, 0, 26] },
        splitLine: { lineStyle: { color: CHART_COLOR.split } },
        axisLabel: { ...BASE_TEXT, formatter: (v) => (v >= 10000 ? v / 10000 + '万' : v) }
      },
      {
        gridIndex: 1,
        type: 'value',
        name: '商机数',
        minInterval: 1,
        nameTextStyle: { ...BASE_TEXT, padding: [0, 0, 0, 26] },
        splitLine: { lineStyle: { color: CHART_COLOR.split } },
        axisLabel: { ...BASE_TEXT }
      }
    ],
    series: [
      {
        name: '金额',
        type: 'bar',
        xAxisIndex: 0,
        yAxisIndex: 0,
        data: amounts,
        barMaxWidth: 32,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: CHART_COLOR.brand,
          opacity: 0.92
        },
        label: {
          show: true,
          position: 'top',
          color: CHART_COLOR.text3,
          fontSize: 11,
          formatter: (p) => (p.value >= 10000 ? (p.value / 10000).toFixed(1) + '万' : p.value)
        }
      },
      {
        name: '商机数',
        type: 'line',
        xAxisIndex: 1,
        yAxisIndex: 1,
        data: counts,
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        lineStyle: { width: 2, color: CHART_COLOR.purple },
        itemStyle: { color: CHART_COLOR.purple, borderColor: '#fff', borderWidth: 2 },
        label: {
          show: true,
          position: 'top',
          color: CHART_COLOR.purple,
          fontSize: 11,
          formatter: '{c}'
        }
      }
    ]
  }
}

/** 线索来源：环形图，中间显示总数 */
export function sourceOption(rows) {
  const total = rows.reduce((s, r) => s + r.value, 0)
  return {
    // title 组件定位比 graphic 稳，中间的大数字用它
    title: {
      text: String(total),
      subtext: '条线索',
      left: '34%',
      top: '40%',
      textStyle: {
        color: CHART_COLOR.text,
        fontSize: 24,
        fontWeight: 500,
        align: 'center'
      },
      subtextStyle: {
        color: CHART_COLOR.text3,
        fontSize: 11,
        align: 'center'
      }
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(31,35,41,0.92)',
      borderWidth: 0,
      padding: [9, 12],
      textStyle: { color: '#fff', fontSize: 12 },
      formatter: (p) => `${p.name}<br/>${p.marker} ${p.value} 条 · ${p.percent}%`
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 4,
      top: 'middle',
      itemWidth: 8,
      itemHeight: 8,
      icon: 'circle',
      itemGap: 12,
      textStyle: { ...BASE_TEXT, color: CHART_COLOR.text2 },
      formatter: (name) => {
        const hit = rows.find((r) => r.name === name)
        return hit ? `${name}  ${hit.value}` : name
      }
    },
    series: [
      {
        type: 'pie',
        radius: ['54%', '78%'],
        center: ['34%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 2, borderRadius: 4 },
        label: { show: false },
        emphasis: { scale: true, scaleSize: 6 },
        data: rows
      }
    ]
  }
}

/**
 * 近 12 个月：订单金额与回款金额双折线
 * 回款用绿色——「钱到手了」是达成，绿色比第二个粉色更好区分
 */
export function trendOption(months, orderAmounts, payAmounts) {
  return {
    grid: { left: 8, right: 14, top: 34, bottom: 4, containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(31,35,41,0.92)',
      borderWidth: 0,
      padding: [9, 12],
      textStyle: { color: '#fff', fontSize: 12 },
      valueFormatter: (v) => Number(v).toLocaleString('zh-CN')
    },
    legend: {
      data: ['订单金额', '回款金额'],
      right: 0,
      top: 0,
      itemWidth: 10,
      itemHeight: 10,
      icon: 'roundRect',
      textStyle: BASE_TEXT
    },
    xAxis: {
      type: 'category',
      data: months,
      boundaryGap: false,
      axisLine: { lineStyle: { color: CHART_COLOR.line } },
      axisTick: { show: false },
      axisLabel: { ...BASE_TEXT, color: CHART_COLOR.text2 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: CHART_COLOR.split } },
      axisLabel: { ...BASE_TEXT, formatter: (v) => (v >= 10000 ? v / 10000 + '万' : v) }
    },
    series: [
      {
        name: '订单金额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: orderAmounts,
        lineStyle: { width: 2.4, color: CHART_COLOR.brand },
        itemStyle: { color: CHART_COLOR.brand, borderColor: '#fff', borderWidth: 2 },
        areaStyle: { color: areaGradient(CHART_COLOR.brand) }
      },
      {
        name: '回款金额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: payAmounts,
        lineStyle: { width: 2.4, color: CHART_COLOR.green },
        itemStyle: { color: CHART_COLOR.green, borderColor: '#fff', borderWidth: 2 },
        areaStyle: { color: areaGradient(CHART_COLOR.green) }
      }
    ]
  }
}

/**
 * 排行条形图（横向）
 * 排行类数据的标准画法：名字长、横着排才读得全，比竖柱挤成一团强
 * data: [{ name, value, extra }]
 */
export function rankOption(data, extraLabel) {
  const values = data.map((d) => d.value)
  const max = Math.max(1, ...values)
  return {
    grid: { left: 8, right: 76, top: 12, bottom: 8, containLabel: true },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(31,35,41,0.92)',
      borderWidth: 0,
      padding: [9, 12],
      textStyle: { color: '#fff', fontSize: 12 },
      formatter: (p) => {
        const row = data[p.dataIndex]
        let html = `<b>${row.name}</b><br/>金额 ${Number(row.value).toLocaleString('zh-CN')}`
        if (row.extra) html += `<br/>${row.extra}`
        return html
      }
    },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: CHART_COLOR.split } },
      axisLabel: { show: false }
    },
    yAxis: {
      type: 'category',
      // 横向条形图默认第一项在底部，反转一下让第一名在最上面
      data: data.map((d) => d.name).reverse(),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { ...BASE_TEXT, color: CHART_COLOR.text2, width: 150, overflow: 'truncate' }
    },
    series: [
      {
        type: 'bar',
        data: values.slice().reverse(),
        barMaxWidth: 14,
        itemStyle: {
          borderRadius: [0, 7, 7, 0],
          color: (p) => {
            const idx = values.length - 1 - p.dataIndex
            return idx < 3 ? CHART_COLOR.brand : CHART_COLOR.brandPale
          }
        },
        label: {
          show: true,
          position: 'right',
          color: CHART_COLOR.text2,
          fontSize: 11,
          formatter: (p) => {
            const row = data[values.length - 1 - p.dataIndex]
            if (!row.value) return row.extra || '0'
            const money = row.value >= 10000 ? (row.value / 10000).toFixed(1) + '万' : row.value
            return row.extra ? `${money}  ${row.extra}` : money
          }
        }
      }
    ]
  }
}
