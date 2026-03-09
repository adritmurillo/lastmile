import { useEffect, useState } from 'react'
import { Card, Table, Tag, DatePicker, Button } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import dayjs from 'dayjs'
import { routesApi } from '../api/routesApi'
import type { Route, Stop } from '../types'

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

const stopStatusColors: Record<string, string> = {
  PENDING: 'gold',
  DELIVERED: 'green',
  FAILED: 'red',
}

export default function RoutesPage() {
  const [routes, setRoutes] = useState<Route[]>([])
  const [loading, setLoading] = useState(false)
  const [selectedDate, setSelectedDate] = useState(dayjs().format('YYYY-MM-DD'))

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

  const expandedRowRender = (route: Route) => {
    const stopColumns: ColumnsType<Stop> = [
      { title: '#', dataIndex: 'stopOrder', key: 'stopOrder', width: 50 },
      { title: 'Destinatario', dataIndex: ['order', 'recipientName'], key: 'recipient' },
      { title: 'Dirección', dataIndex: ['order', 'addressText'], key: 'address', ellipsis: true },
      { title: 'Tracking', dataIndex: ['order', 'trackingCode'], key: 'tracking' },
      {
        title: 'Estado',
        dataIndex: 'status',
        key: 'status',
        render: (s) => <Tag color={stopStatusColors[s]}>{s}</Tag>,
      },
    ]
    return (
      <Table
        columns={stopColumns}
        dataSource={route.stops}
        rowKey="id"
        pagination={false}
        size="small"
      />
    )
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
  ]

  return (
    <div>
      <Card
        title="Rutas"
        extra={
          <Button icon={<ReloadOutlined />} onClick={fetchRoutes}>
            Actualizar
          </Button>
        }
      >
        <DatePicker
          value={dayjs(selectedDate)}
          onChange={(_, dateStr) => setSelectedDate(dateStr as string)}
          format="YYYY-MM-DD"
          style={{ marginBottom: 16 }}
        />
        <Table
          columns={columns}
          dataSource={routes}
          rowKey="id"
          loading={loading}
          expandable={{ expandedRowRender }}
          pagination={false}
        />
      </Card>
    </div>
  )
}