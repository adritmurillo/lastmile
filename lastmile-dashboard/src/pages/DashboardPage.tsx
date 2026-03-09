import { useEffect, useState } from 'react'
import { Row, Col, Card, Statistic, Progress, Alert } from 'antd'
import {
  ShoppingOutlined,
  CarOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  TeamOutlined,
} from '@ant-design/icons'
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts'
import { statsApi } from '../api/statsApi'
import type { Stats } from '../types'

const COLORS = ['#1677ff', '#52c41a', '#ff4d4f', '#faad14', '#722ed1']

export default function DashboardPage() {
  const [stats, setStats] = useState<Stats | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    statsApi.getTodayStats()
      .then(res => setStats(res.data))
      .finally(() => setLoading(false))
  }, [])

  const pieData = stats ? [
    { name: 'Pendientes', value: stats.pendingOrders },
    { name: 'Entregados', value: stats.deliveredOrders },
    { name: 'Fallidos', value: stats.failedOrders },
    { name: 'En tránsito', value: stats.inTransitOrders },
    { name: 'Asignados', value: stats.assignedOrders },
  ] : []

  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>Dashboard — Hoy</h2>

      <Row gutter={[16, 16]}>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic
              title="Total órdenes"
              value={stats?.totalOrders}
              prefix={<ShoppingOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic
              title="Pendientes"
              value={stats?.pendingOrders}
              prefix={<ClockCircleOutlined />}
              styles={{ content :{color : '#faad14' }}}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic
              title="En tránsito"
              value={stats?.inTransitOrders}
              prefix={<CarOutlined />}
              styles={{content : { color: '#1677ff' }}}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic
              title="Entregados"
              value={stats?.deliveredOrders}
              prefix={<CheckCircleOutlined />}
              styles={{ content: { color: '#52c41a' } }}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic
              title="Fallidos"
              value={stats?.failedOrders}
              prefix={<CloseCircleOutlined />}
              styles={{ content: { color: '#ff4d4f' } }}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic
              title="Couriers activos"
              value={stats?.activeCouriers}
              prefix={<TeamOutlined />}
              styles={{ content: { color: '#722ed1' } }}
            />
          </Card>
        </Col>
      </Row>

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
            <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={80} dataKey="value">
                  {pieData.map((_, index) => (
                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card title="Rutas del día" loading={loading}>
            <Row gutter={16}>
              <Col span={8}>
                <Statistic title="Total rutas" value={stats?.totalRoutes} />
              </Col>
              <Col span={8}>
                <Statistic
                  title="En progreso"
                  value={stats?.inProgressRoutes}
                  styles={{ content: { color: '#1677ff' } }}
                />
              </Col>
              <Col span={8}>
                <Statistic
                  title="Completadas"
                  value={stats?.completedRoutes}
                  styles={{ content: { color: '#52c41a' } }}
                />
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