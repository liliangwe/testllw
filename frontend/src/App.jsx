import { useEffect } from 'react'
import DemoPage from './DemoPage.jsx'

// 单页应用：仅 /demo 路由，其它路径统一归一化到 /demo
export default function App() {
  useEffect(() => {
    if (window.location.pathname !== '/demo') {
      window.history.replaceState(null, '', '/demo')
    }
  }, [])

  return <DemoPage />
}
