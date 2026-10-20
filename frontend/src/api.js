const API_BASE = '/api/demo'

export const DEFAULT_CALLER = {
  callerId: 'anonymous',
  personType: 'unknown',
  personLevel: 'unknown',
  dept: 'unknown'
}

export class ApiError extends Error {
  constructor(message, code) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

// 埋点请求头，缺省 anonymous / unknown
export function buildHeaders(caller = DEFAULT_CALLER) {
  return {
    'Content-Type': 'application/json',
    'X-Caller-Id': caller.callerId || 'anonymous',
    'X-Person-Type': caller.personType || 'unknown',
    'X-Person-Level': caller.personLevel || 'unknown',
    'X-Dept': caller.dept || 'unknown'
  }
}

// 统一 JSON 请求：响应 {code,msg,data}，成功 code==="0" 返回 data，否则抛 ApiError(msg)
async function requestJson(path, options, caller) {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: buildHeaders(caller)
  })
  if (!res.ok) {
    throw new ApiError(`HTTP ${res.status}`, String(res.status))
  }
  const body = await res.json()
  if (body.code !== '0') {
    throw new ApiError(body.msg || '请求失败', body.code)
  }
  return body.data
}

export const api = {
  helloworld(name, caller) {
    const q = name ? `?name=${encodeURIComponent(name)}` : ''
    return requestJson(`/helloworld${q}`, { method: 'GET' }, caller)
  },

  hash(text, caller) {
    return requestJson('/hash', {
      method: 'POST',
      body: JSON.stringify({ text })
    }, caller)
  },

  bubbleSort(array, caller) {
    return requestJson('/bubble-sort', {
      method: 'POST',
      body: JSON.stringify({ array })
    }, caller)
  },

  report(params, caller) {
    const q = new URLSearchParams()
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') q.append(k, v)
    })
    return requestJson(`/tracking/report?${q.toString()}`, { method: 'GET' }, caller)
  },

  dimensions(caller) {
    return requestJson('/tracking/dimensions', { method: 'GET' }, caller)
  },

  // 导出：POST /api/demo/export，后端返回 CSV 文件流（或 JSON 错误体）
  async exportCsv(body, caller) {
    const res = await fetch(`${API_BASE}/export`, {
      method: 'POST',
      headers: buildHeaders(caller),
      body: JSON.stringify(body)
    })
    const contentType = res.headers.get('Content-Type') || ''

    if (!res.ok) {
      let msg = `HTTP ${res.status}`
      try {
        const e = await res.json()
        if (e && e.msg) msg = e.msg
      } catch (_) { /* ignore */ }
      throw new ApiError(msg, String(res.status))
    }

    // 错误场景后端可能直接返回 JSON 体（如 EXP_001 / EXP_002）
    if (contentType.includes('application/json')) {
      // 错误（如 EXP_001 / EXP_002）后端以统一响应 JSON 体返回
      const j = await res.json()
      throw new ApiError(j.msg || '导出失败', j.code)
    }

    const blob = await res.blob()
    const disposition = res.headers.get('Content-Disposition') || ''
    let filename = 'export.csv'
    const m = disposition.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i)
    if (m && m[1]) filename = decodeURIComponent(m[1])

    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
    return true
  }
}
