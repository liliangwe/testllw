import ReactECharts from 'echarts-for-react'
import { Empty } from 'antd'

// 汇总所有时间桶（有序去重），作为折线/柱状图 X 轴
function collectBuckets(items) {
  const set = new Set()
  items.forEach((it) => it.series.forEach((s) => set.add(s.bucket)))
  return Array.from(set).sort()
}

// key -> { bucket: count }
function seriesMap(items) {
  const map = {}
  items.forEach((it) => {
    const m = {}
    it.series.forEach((s) => { m[s.bucket] = s.count })
    map[it.key] = m
  })
  return map
}

// 折线图：各 key 的 series（count 随 bucket 时间趋势）
export function LineChart({ items }) {
  if (!items || items.length === 0) return <Empty description="暂无数据" />
  const buckets = collectBuckets(items)
  const map = seriesMap(items)
  const series = items.map((it) => ({
    name: it.key,
    type: 'line',
    data: buckets.map((b) => (map[it.key][b] != null ? map[it.key][b] : 0))
  }))
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { type: 'scroll' },
    xAxis: { type: 'category', data: buckets },
    yAxis: { type: 'value', minInterval: 1 },
    series
  }
  return <ReactECharts option={option} style={{ height: 320 }} />
}

// 饼图：按所选维度各 key 的 count 合计占比
export function PieChart({ items }) {
  if (!items || items.length === 0) return <Empty description="暂无数据" />
  const data = items.map((it) => ({
    name: it.key,
    value: it.series.reduce((sum, s) => sum + (s.count || 0), 0)
  }))
  const option = {
    tooltip: { trigger: 'item' },
    legend: { type: 'scroll' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        data
      }
    ]
  }
  return <ReactECharts option={option} style={{ height: 320 }} />
}

// 柱状图：同一时间桶内各 key 分组对比
export function BarChart({ items }) {
  if (!items || items.length === 0) return <Empty description="暂无数据" />
  const buckets = collectBuckets(items)
  const map = seriesMap(items)
  const series = items.map((it) => ({
    name: it.key,
    type: 'bar',
    data: buckets.map((b) => (map[it.key][b] != null ? map[it.key][b] : 0))
  }))
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { type: 'scroll' },
    xAxis: { type: 'category', data: buckets },
    yAxis: { type: 'value', minInterval: 1 },
    series
  }
  return <ReactECharts option={option} style={{ height: 320 }} />
}
