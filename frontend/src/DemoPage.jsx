import { useCallback, useEffect, useState } from 'react'
import {
  Alert, App as AntdApp, Button, Card, Col, DatePicker, Form, Input,
  Row, Select, Space, Spin, Tabs, Typography
} from 'antd'
import { api, DEFAULT_CALLER } from './api.js'
import { LineChart, PieChart, BarChart } from './reportCharts.jsx'

const { RangePicker } = DatePicker

const DIMENSION_OPTIONS = [
  { value: 'person_type', label: '人员类型' },
  { value: 'person_level', label: '人员层级' },
  { value: 'department', label: '人员部门' }
]

const GROUPBY_OPTIONS = [
  { value: 'day', label: '按天' },
  { value: 'hour', label: '按小时' }
]

const API_NAME_OPTIONS = [
  { value: 'helloworld', label: 'helloworld' },
  { value: 'hash', label: 'hash' },
  { value: 'bubbleSort', label: 'bubbleSort' }
]

function CallerSettings({ onApply }) {
  const [form] = Form.useForm()
  return (
    <Form
      form={form}
      layout="inline"
      initialValues={{
        callerId: DEFAULT_CALLER.callerId,
        personType: DEFAULT_CALLER.personType,
        personLevel: DEFAULT_CALLER.personLevel,
        dept: DEFAULT_CALLER.dept
      }}
      onFinish={(v) => onApply({
        callerId: (v.callerId || '').trim() || 'anonymous',
        personType: (v.personType || '').trim() || 'unknown',
        personLevel: (v.personLevel || '').trim() || 'unknown',
        dept: (v.dept || '').trim() || 'unknown'
      })}
    >
      <Form.Item name="callerId" label="X-Caller-Id">
        <Input placeholder="anonymous" style={{ width: 130 }} />
      </Form.Item>
      <Form.Item name="personType" label="X-Person-Type">
        <Input placeholder="unknown" style={{ width: 130 }} />
      </Form.Item>
      <Form.Item name="personLevel" label="X-Person-Level">
        <Input placeholder="unknown" style={{ width: 130 }} />
      </Form.Item>
      <Form.Item name="dept" label="X-Dept">
        <Input placeholder="unknown" style={{ width: 130 }} />
      </Form.Item>
      <Form.Item>
        <Button type="primary" htmlType="submit">应用</Button>
      </Form.Item>
    </Form>
  )
}

function ResultBox({ children }) {
  return (
    <Card size="small" style={{ marginTop: 16 }} styles={{ body: { padding: '12px 16px' } }}>
      {children}
    </Card>
  )
}

function HelloTab({ caller }) {
  const { message: messageApi } = AntdApp.useApp()
  const [name, setName] = useState('')
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const run = async () => {
    setLoading(true)
    setError(null)
    try {
      setResult(await api.helloworld(name, caller))
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  const exportResult = async () => {
    setLoading(true)
    setError(null)
    try {
      await api.exportCsv({ tab: 'helloworld', payload: { name } }, caller)
      messageApi.success('已导出')
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Space wrap>
        <Input
          allowClear
          placeholder="name（可选，默认 World）"
          value={name}
          onChange={(e) => setName(e.target.value)}
          style={{ width: 240 }}
        />
        <Button type="primary" loading={loading} onClick={run}>执行</Button>
        <Button onClick={exportResult}>导出结果</Button>
      </Space>
      {error && <Alert type="error" message={error} showIcon />}
      {result && (
        <ResultBox>
          <Typography.Text strong>message：</Typography.Text>
          <Typography.Text>{result.message}</Typography.Text>
        </ResultBox>
      )}
    </Space>
  )
}

function HashTab({ caller }) {
  const { message: messageApi } = AntdApp.useApp()
  const [text, setText] = useState('')
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const run = async () => {
    setLoading(true)
    setError(null)
    try {
      setResult(await api.hash(text, caller))
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  const exportResult = async () => {
    setLoading(true)
    setError(null)
    try {
      await api.exportCsv({ tab: 'hash', payload: { text } }, caller)
      messageApi.success('已导出')
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Space wrap>
        <Input
          allowClear
          placeholder="text（必填）"
          value={text}
          onChange={(e) => setText(e.target.value)}
          style={{ width: 240 }}
        />
        <Button type="primary" loading={loading} onClick={run}>执行</Button>
        <Button onClick={exportResult}>导出结果</Button>
      </Space>
      {error && <Alert type="error" message={error} showIcon />}
      {result && (
        <ResultBox>
          <Typography.Text strong>algorithm：</Typography.Text>
          <Typography.Text>{result.algorithm}</Typography.Text>
          <br />
          <Typography.Text strong>digest：</Typography.Text>
          <Typography.Text copyable style={{ wordBreak: 'break-all' }}>{result.digest}</Typography.Text>
        </ResultBox>
      )}
    </Space>
  )
}

function SortTab({ caller }) {
  const { message: messageApi } = AntdApp.useApp()
  const [text, setText] = useState('')
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const parseArray = () => {
    const parts = text.split(',').map((s) => s.trim()).filter((s) => s !== '')
    if (parts.length === 0) throw new Error('请输入逗号分隔的数字，如 5,3,8,1')
    return parts.map((p) => {
      const n = Number(p)
      if (Number.isNaN(n)) throw new Error(`「${p}」不是有效数字`)
      return n
    })
  }

  const run = async () => {
    setLoading(true)
    setError(null)
    try {
      const array = parseArray()
      setResult(await api.bubbleSort(array, caller))
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  const exportResult = async () => {
    setLoading(true)
    setError(null)
    try {
      const array = parseArray()
      await api.exportCsv({ tab: 'bubbleSort', payload: { array } }, caller)
      messageApi.success('已导出')
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Space wrap>
        <Input
          allowClear
          placeholder="逗号分隔数字，如 5,3,8,1"
          value={text}
          onChange={(e) => setText(e.target.value)}
          style={{ width: 280 }}
        />
        <Button type="primary" loading={loading} onClick={run}>执行</Button>
        <Button onClick={exportResult}>导出结果</Button>
      </Space>
      {error && <Alert type="error" message={error} showIcon />}
      {result && (
        <ResultBox>
          <Typography.Text strong>sorted：</Typography.Text>
          <Typography.Text>[{result.sorted.join(', ')}]</Typography.Text>
          <br />
          <Typography.Text strong>swaps：</Typography.Text>
          <Typography.Text>{result.swaps}</Typography.Text>
        </ResultBox>
      )}
    </Space>
  )
}

function ReportSection({ caller }) {
  const { message: messageApi } = AntdApp.useApp()
  const [dimension, setDimension] = useState('person_type')
  const [groupBy, setGroupBy] = useState('day')
  const [apiName, setApiName] = useState(undefined)
  const [range, setRange] = useState(null)
  const [valueFilter, setValueFilter] = useState(null)
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(false)
  const [dimValues, setDimValues] = useState({
    personTypes: [],
    personLevels: [],
    departments: []
  })

  const fetchDimensions = useCallback(async () => {
    try {
      const data = await api.dimensions(caller)
      setDimValues({
        personTypes: data.personTypes || [],
        personLevels: data.personLevels || [],
        departments: data.departments || []
      })
    } catch (_) { /* 维度可选值失败不影响主流程 */ }
  }, [caller])

  useEffect(() => {
    fetchDimensions()
  }, [fetchDimensions])

  useEffect(() => {
    setValueFilter(null)
  }, [dimension])

  const buildRange = () => {
    if (!range || range.length !== 2) return {}
    return {
      start: range[0].format('YYYY-MM-DD'),
      end: range[1].format('YYYY-MM-DD')
    }
  }

  const fetchReport = async () => {
    setLoading(true)
    try {
      const params = { dimension, groupBy, ...buildRange() }
      if (apiName) params.apiName = apiName
      const data = await api.report(params, caller)
      setItems(data.items || [])
    } catch (e) {
      messageApi.error(e.message)
    } finally {
      setLoading(false)
    }
  }

  const exportReport = async () => {
    try {
      const body = { tab: 'report', dimension, groupBy, ...buildRange() }
      if (apiName) body.apiName = apiName
      await api.exportCsv(body, caller)
      messageApi.success('报表已导出')
    } catch (e) {
      messageApi.error(e.message)
    }
  }

  const valueOptions = dimension === 'person_type'
    ? dimValues.personTypes
    : dimension === 'person_level'
      ? dimValues.personLevels
      : dimValues.departments

  const filteredItems = valueFilter
    ? items.filter((it) => it.key === valueFilter)
    : items

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Space wrap>
        <Select
          value={dimension}
          onChange={setDimension}
          options={DIMENSION_OPTIONS}
          style={{ width: 140 }}
        />
        <RangePicker value={range} onChange={(dates) => setRange(dates)} />
        <Select
          value={groupBy}
          onChange={setGroupBy}
          options={GROUPBY_OPTIONS}
          style={{ width: 120 }}
        />
        <Select
          allowClear
          placeholder="按接口过滤（可选）"
          value={apiName}
          onChange={setApiName}
          options={API_NAME_OPTIONS}
          style={{ width: 180 }}
        />
        <Select
          allowClear
          placeholder="维度值过滤（可选）"
          value={valueFilter}
          onChange={setValueFilter}
          options={valueOptions.map((v) => ({ value: v, label: v }))}
          style={{ width: 180 }}
        />
        <Button type="primary" loading={loading} onClick={fetchReport}>查询报表</Button>
        <Button onClick={exportReport}>导出报表</Button>
      </Space>

      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          <Col xs={24} md={8}>
            <Card size="small" title="折线图（各 key 随时间趋势）">
              <LineChart items={filteredItems} />
            </Card>
          </Col>
          <Col xs={24} md={8}>
            <Card size="small" title="饼图（各 key 占比）">
              <PieChart items={filteredItems} />
            </Card>
          </Col>
          <Col xs={24} md={8}>
            <Card size="small" title="柱状图（bucket 内各 key 对比）">
              <BarChart items={filteredItems} />
            </Card>
          </Col>
        </Row>
      </Spin>
    </Space>
  )
}

export default function DemoPage() {
  const [caller, setCaller] = useState(DEFAULT_CALLER)

  const tabs = [
    { key: 'helloworld', label: 'HelloWorld', children: <HelloTab caller={caller} /> },
    { key: 'hash', label: '哈希', children: <HashTab caller={caller} /> },
    { key: 'bubbleSort', label: '冒泡排序', children: <SortTab caller={caller} /> }
  ]

  return (
    <div style={{ padding: 24, maxWidth: 1280, margin: '0 auto' }}>
      <Typography.Title level={3}>Demo 控制台</Typography.Title>
      <Card size="small" title="调用人设置" style={{ marginBottom: 16 }}>
        <CallerSettings onApply={setCaller} />
        <Typography.Text type="secondary" style={{ display: 'block', marginTop: 8 }}>
          当前请求头：X-Caller-Id={caller.callerId}，X-Person-Type={caller.personType}，
          X-Person-Level={caller.personLevel}，X-Dept={caller.dept}
        </Typography.Text>
      </Card>
      <Tabs items={tabs} />
      <Card title="调用情况报表" style={{ marginTop: 16 }}>
        <ReportSection caller={caller} />
      </Card>
    </div>
  )
}
