import { useEffect, useState } from 'react'
import { Row, Col, Card, Statistic, Progress, Alert, DatePicker, Radio, Space } from 'antd'
import {
  ShoppingOutlined,
  CarOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  TeamOutlined,
  StopOutlined,
} from '@ant-design/icons'
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts'
import { statsApi } from '../api/statsApi'
import type { Stats } from '../types'
import dayjs from 'dayjs'

const { RangePicker } = DatePicker

const COLORS = ['#faad14', '#52c41a', '#ff4d4f', '#1677ff', '#722ed1', '#8c8c8c']

type PeriodMode = 'today' | 'week' | 'month' | 'custom'

export default function DashboardPage() {
  const [stats, setStats] = useState<Stats | null>(null)
  const [loading, setLoading] = useState(true)
  const [periodMode, setPeriodMode] = useState<PeriodMode>('today')
  const [customRange, setCustomRange] = useState<[string, string] | null>(null)

  const fetchStats = async (mode: PeriodMode, range?: [string, string]) => {
    setLoading(true)
    try {
      if (mode === 'today') {
        const res = await statsApi.getTodayStats()
        setStats(res.data)
      } else {
        let startDate: string
        let endDate: string = dayjs().format('YYYY-MM-DD')

        if (mode === 'week') {
          startDate = dayjs().subtract(7, 'day').format('YYYY-MM-DD')
        } else if (mode === 'month') {
          startDate = dayjs().subtract(30, 'day').format('YYYY-MM-DD')
        } else {
          if (!range) return
          startDate = range[0]
          endDate = range[1]
        }

        const res = await statsApi.getStatsByPeriod(startDate, endDate)
        setStats(res.data)
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchStats(periodMode, customRange ?? undefined)
  }, [periodMode])

  const pieData = stats ? [
    { name: 'Pendientes', value: stats.pendingOrders },
    { name: 'Entregados', value: stats.deliveredOrders },
    { name: 'Fallidos', value: stats.failedOrders },
    { name: 'En tránsito', value: stats.inTransitOrders },
    { name: 'Asignados', value: stats.assignedOrders },
    { name: 'Cancelados', value: stats.cancelledOrders ?? 0 },
  ].filter(d => d.value > 0) : []

  const periodLabel: Record<PeriodMode, string> = {
    today: 'Hoy',
    week: 'Últimos 7 días',
    month: 'Últimos 30 días',
    custom: 'Personalizado',
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2 style={{ margin: 0 }}>Dashboard — {periodLabel[periodMode]}</h2>
        <Space>
          <Radio.Group
            value={periodMode}
            onChange={(e) => setPeriodMode(e.target.value)}
            optionType="button"
            buttonStyle="solid"
          >
            <Radio.Button value="today">Hoy</Radio.Button>
            <Radio.Button value="week">7 días</Radio.Button>
            <Radio.Button value="month">30 días</Radio.Button>
            <Radio.Button value="custom">Personalizado</Radio.Button>
          </Radio.Group>
          {periodMode === 'custom' && (
            <RangePicker
              format="YYYY-MM-DD"
              onChange={(_, dateStrings) => {
                if (dateStrings[0] && dateStrings[1]) {
                  const range: [string, string] = [dateStrings[0], dateStrings[1]]
                  setCustomRange(range)
                  fetchStats('custom', range)
                }
              }}
            />
          )}
        </Space>
      </div>

      {/* Fila 1: Stats de órdenes */}
      <Row gutter={[16, 16]}>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic title="Total órdenes" value={stats?.totalOrders} prefix={<ShoppingOutlined />} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic title="Pendientes" value={stats?.pendingOrders} prefix={<ClockCircleOutlined />} styles={{ content: { color: '#faad14' } }} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic title="En tránsito" value={stats?.inTransitOrders} prefix={<CarOutlined />} styles={{ content: { color: '#1677ff' } }} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic title="Entregados" value={stats?.deliveredOrders} prefix={<CheckCircleOutlined />} styles={{ content: { color: '#52c41a' } }} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic title="Fallidos" value={stats?.failedOrders} prefix={<CloseCircleOutlined />} styles={{ content: { color: '#ff4d4f' } }} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic title="Cancelados" value={stats?.cancelledOrders} prefix={<StopOutlined />} styles={{ content: { color: '#8c8c8c' } }} />
          </Card>
        </Col>
      </Row>

      {/* Fila 2: Couriers activos */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card loading={loading}>
            <Statistic title="Couriers activos" value={stats?.activeCouriers} prefix={<TeamOutlined />} styles={{ content: { color: '#722ed1' } }} />
          </Card>
        </Col>
      </Row>

      {/* Fila 3: Tasa de éxito y distribución */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={12}>
          <Card title="Tasa de éxito" loading={loading}>
            <Progress
              percent={Math.round(stats?.successRate || 0)}
              strokeColor={{ '0%': '#1677ff', '100%': '#52c41a' }}
              size={['100%', 20]}
            />
            <p style={{ marginTop: 8, color: '#888' }}>
              {stats?.deliveredOrders} de {stats?.totalOrders} órdenes entregadas exitosamente
            </p>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="Distribución de órdenes" loading={loading}>
            {pieData.length === 0 ? (
              <div style={{ height: 200, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#888' }}>
                Sin datos por ahora
              </div>
            ) : (
              <ResponsiveContainer width="100%" height={200}>
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={80} dataKey="value">
                    {pieData.map((_, index) => (
                      <Cell key={index} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </Card>
        </Col>
      </Row>

      {/* Fila 4: Rutas del día */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card title="Rutas del período" loading={loading}>
            <Row gutter={16}>
              <Col span={6}>
                <Statistic title="Total rutas" value={stats?.totalRoutes} />
              </Col>
              <Col span={6}>
                <Statistic title="Pendientes" value={stats?.pendingRoutes} styles={{ content: { color: '#faad14' } }} />
              </Col>
              <Col span={6}>
                <Statistic title="En progreso" value={stats?.inProgressRoutes} styles={{ content: { color: '#1677ff' } }} />
              </Col>
              <Col span={6}>
                <Statistic title="Completadas" value={stats?.completedRoutes} styles={{ content: { color: '#52c41a' } }} />
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>

      {(stats?.failedOrders ?? 0) > 0 && (
        <Alert
          style={{ marginTop: 16 }}
          description={`Hay ${stats!.failedOrders} órdenes fallidas que requieren atención`}
          type="warning"
          showIcon
        />
      )}
    </div>
  )
}