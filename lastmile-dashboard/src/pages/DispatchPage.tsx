import { useEffect, useState } from 'react'
import { Card, Button, DatePicker, Table, Tag, Space, Steps, message, Row, Col, Statistic } from 'antd'
import { ThunderboltOutlined, CheckOutlined, NodeIndexOutlined, CarOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import dayjs from 'dayjs'
import { routesApi } from '../api/routesApi'
import type { Route } from '../types'

const statusColors: Record<string, string> = {
  PENDING: 'gold',
  CONFIRMED: 'blue',
  IN_PROGRESS: 'purple',
  COMPLETED: 'green',
}

const statusLabels: Record<string, string> = {
  PENDING: 'Pendiente',
  CONFIRMED: 'Confirmado',
  IN_PROGRESS: 'En progreso',
  COMPLETED: 'Completado',
}

export default function DispatchPage() {
  const [routes, setRoutes] = useState<Route[]>([])
  const [loading, setLoading] = useState(false)
  const [actionLoading, setActionLoading] = useState(false)
  const [selectedDate, setSelectedDate] = useState(dayjs().format('YYYY-MM-DD'))
  const [messageApi, contextHolder] = message.useMessage()

  const fetchRoutes = async () => {
    setLoading(true)
    try {
      const res = await routesApi.getRoutesByDate(selectedDate)
      setRoutes(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchRoutes()
  }, [selectedDate])

  const handleGenerateProposal = async () => {
    setActionLoading(true)
    try {
      await routesApi.generateProposal(selectedDate)
      messageApi.success('Propuesta generada correctamente')
      fetchRoutes()
    } catch {
      messageApi.error('Error al generar la propuesta')
    } finally {
      setActionLoading(false)
    }
  }

  const handleConfirmRoutes = async () => {
    setActionLoading(true)
    try {
      await routesApi.confirmRoutes(selectedDate)
      messageApi.success('Rutas confirmadas correctamente')
      fetchRoutes()
    } catch {
      messageApi.error('Error al confirmar las rutas')
    } finally {
      setActionLoading(false)
    }
  }

  const currentStep = () => {
    if (routes.length === 0) return 0
    if (routes.every(r => r.status === 'COMPLETED')) return 3
    if (routes.some(r => r.status === 'IN_PROGRESS')) return 2
    if (routes.some(r => r.status === 'CONFIRMED')) return 1
    return 0
  }

  const columns: ColumnsType<Route> = [
    {
      title: 'Courier',
      dataIndex: ['courier', 'fullName'],
      key: 'courier',
    },
    {
      title: 'Vehículo',
      dataIndex: ['courier', 'vehicle', 'licensePlate'],
      key: 'vehicle',
      render: (plate, record) => `${plate} (${record.courier.vehicle?.type || '-'})`,
    },
    {
      title: 'Estado',
      dataIndex: 'status',
      key: 'status',
      render: (s) => <Tag color={statusColors[s]}>{statusLabels[s]}</Tag>,
    },
    {
      title: 'Paradas',
      dataIndex: 'totalStops',
      key: 'totalStops',
      render: (total, record) => `${record.deliveredCount}/${total}`,
    },
    {
      title: 'Progreso',
      dataIndex: 'completionPercentage',
      key: 'completionPercentage',
      render: (p) => `${Math.round(p)}%`,
    },
    {
      title: 'Fallidos',
      dataIndex: 'failedCount',
      key: 'failedCount',
      render: (f) => f > 0 ? <Tag color="red">{f}</Tag> : <Tag color="green">0</Tag>,
    },
  ]

  return (
    <div>
      {contextHolder}
      <Card title="Centro de Despacho" style={{ marginBottom: 16 }}>
        <Steps
          current={currentStep()}
          items={[
            { title: 'Sin rutas', icon: <NodeIndexOutlined /> },
            { title: 'Propuesta generada', icon: <ThunderboltOutlined /> },
            { title: 'Rutas confirmadas', icon: <CheckOutlined /> },
            { title: 'En ejecución', icon: <CarOutlined /> },
          ]}
          style={{ marginBottom: 24 }}
        />
        <Row gutter={16} align="middle">
          <Col>
            <DatePicker
              value={dayjs(selectedDate, 'YYYY-MM-DD')}
              onChange={(date) => setSelectedDate(date ? date.format('YYYY-MM-DD') : dayjs().format('YYYY-MM-DD'))}
              format="YYYY-MM-DD"
            />
          </Col>
          <Col>
            <Space>
              <Button
                type="primary"
                icon={<ThunderboltOutlined />}
                onClick={handleGenerateProposal}
                loading={actionLoading}
              >
                Generar propuesta
              </Button>
              <Button
                type="primary"
                icon={<CheckOutlined />}
                onClick={handleConfirmRoutes}
                loading={actionLoading}
                disabled={routes.length === 0}
                style={{ background: '#52c41a', borderColor: '#52c41a' }}
              >
                Confirmar rutas
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      <Card title={`Rutas del ${dayjs(selectedDate).format('DD/MM/YYYY')}`}>
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Statistic title="Total rutas" value={routes.length} />
          </Col>
          <Col span={6}>
            <Statistic
              title="En progreso"
              value={routes.filter(r => r.status === 'IN_PROGRESS').length}
              styles={{ content: { color: '#722ed1' } }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="Completadas"
              value={routes.filter(r => r.status === 'COMPLETED').length}
              styles={{ content: { color: '#52c41a' } }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="Total paradas"
              value={routes.reduce((acc, r) => acc + r.totalStops, 0)}
            />
          </Col>
        </Row>
        <Table
          columns={columns}
          dataSource={routes}
          rowKey="id"
          loading={loading}
          pagination={false}
        />
      </Card>
    </div>
  )
}