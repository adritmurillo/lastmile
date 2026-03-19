import { useEffect, useState, useRef } from 'react'
import { Card, Table, Tag, DatePicker, Button, Input, Image, Modal, message, Radio, Space as AntSpace } from 'antd'
import { ReloadOutlined, CameraOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import dayjs from 'dayjs'
import { routesApi } from '../api/routesApi'
import type { Route, Stop } from '../types'
import { ordersApi } from '../api/ordersApi'

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

const CLOSE_REASONS = [
  'Fin de jornada laboral',
  'Vehículo averiado',
  'Problema de seguridad en zona',
  'Courier enfermo',
  'Otro',
]

export default function RoutesPage() {
  const [routes, setRoutes] = useState<Route[]>([])
  const [loading, setLoading] = useState(false)
  const [selectedDate, setSelectedDate] = useState(dayjs().format('YYYY-MM-DD'))
  const [previewPhotos, setPreviewPhotos] = useState<string[]>([])
  const [closeModalOpen, setCloseModalOpen] = useState(false)
  const [routeToClose, setRouteToClose] = useState<Route | null>(null)
  const [selectedReason, setSelectedReason] = useState<string>('')
  const [customReason, setCustomReason] = useState<string>('')
  const [closing, setClosing] = useState(false)
  const allRoutesRef = useRef<Route[]>([])
  const [messageApi, contextHolder] = message.useMessage()

  const fetchRoutes = async () => {
    setLoading(true)
    try {
      const res = await routesApi.getRoutesByDate(selectedDate)
      allRoutesRef.current = res.data
      setRoutes(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchRoutes()
  }, [selectedDate])

  const handleOpenCloseModal = (route: Route) => {
    setRouteToClose(route)
    setSelectedReason('')
    setCustomReason('')
    setCloseModalOpen(true)
  }

  const handleCloseRoute = async () => {
    const reason = selectedReason === 'Otro' ? customReason.trim() : selectedReason
    if (!reason) {
      messageApi.error('Selecciona o escribe el motivo del cierre')
      return
    }
    setClosing(true)
    try {
      await routesApi.closeRoute(routeToClose!.id, reason)
      messageApi.success('Ruta cerrada correctamente')
      setCloseModalOpen(false)
      fetchRoutes()
    } catch {
      messageApi.error('No se pudo cerrar la ruta')
    } finally {
      setClosing(false)
    }
  }

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
      {
        title: 'Fotos',
        key: 'photo',
        render: (_, stop) => stop.proofPhotoUrl ? (
          <Button
            size="small"
            icon={<CameraOutlined />}
            onClick={async () => {
              try {
                const res = await ordersApi.getProofPhotos(stop.order.id)
                setPreviewPhotos(res.data ?? [])
              } catch {
                setPreviewPhotos([stop.proofPhotoUrl!])
              }
            }}
          >
            Ver fotos
          </Button>
        ) : (
          <span style={{ color: '#bfbfbf', fontSize: 12 }}>Sin foto</span>
        )
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
    { title: 'Courier', dataIndex: ['courier', 'fullName'], key: 'courier' },
    { title: 'Vehículo', dataIndex: ['courier', 'vehicle', 'licensePlate'], key: 'vehicle' },
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
      title: 'Acciones',
      key: 'actions',
      render: (_, record) => (
        record.status !== 'COMPLETED' ? (
          <Button
            size="small"
            danger
            onClick={() => handleOpenCloseModal(record)}
          >
            Cerrar ruta
          </Button>
        ) : null
      ),
    },
  ]

  return (
    <div>
      {contextHolder}
      <Card
        title="Rutas"
        extra={<Button icon={<ReloadOutlined />} onClick={fetchRoutes}>Actualizar</Button>}
      >
        <DatePicker
          value={dayjs(selectedDate)}
          onChange={(_, dateStr) => setSelectedDate(dateStr as string)}
          format="YYYY-MM-DD"
          style={{ marginBottom: 16 }}
        />
        <Input.Search
          placeholder="Buscar por nombre de courier..."
          style={{ marginBottom: 16, marginLeft: 16, width: 300 }}
          allowClear
          onChange={(e) => {
            const value = e.target.value
            if (!value) {
              setRoutes(allRoutesRef.current)
              return
            }
            const filtered = allRoutesRef.current.filter(r =>
              r.courier.fullName.toLowerCase().includes(value.toLowerCase())
            )
            setRoutes(filtered)
          }}
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

      {/* Modal cerrar ruta */}
      <Modal
        title={`Cerrar ruta — ${routeToClose?.courier.fullName}`}
        open={closeModalOpen}
        onCancel={() => setCloseModalOpen(false)}
        onOk={handleCloseRoute}
        okText="Cerrar ruta"
        cancelText="Cancelar"
        okButtonProps={{ danger: true, loading: closing }}
        centered
      >
        <p style={{ color: '#666', marginBottom: 16 }}>
          Los stops pendientes se reagendarán automáticamente mañana.
        </p>
        <p style={{ fontWeight: 600, marginBottom: 8 }}>Motivo del cierre:</p>
        <Radio.Group
          value={selectedReason}
          onChange={(e) => setSelectedReason(e.target.value)}
          style={{ width: '100%' }}
        >
          <AntSpace direction="vertical" style={{ width: '100%' }}>
            {CLOSE_REASONS.map(reason => (
              <Radio key={reason} value={reason}>{reason}</Radio>
            ))}
          </AntSpace>
        </Radio.Group>
        {selectedReason === 'Otro' && (
          <Input.TextArea
            style={{ marginTop: 12 }}
            placeholder="Describe el motivo..."
            rows={3}
            value={customReason}
            onChange={(e) => setCustomReason(e.target.value)}
          />
        )}
      </Modal>

      {/* Modal fotos */}
      <Modal
        open={previewPhotos.length > 0}
        onCancel={() => setPreviewPhotos([])}
        footer={null}
        title={`Fotos de entrega (${previewPhotos.length})`}
        centered
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {previewPhotos.map((url, i) => (
            <Image
              key={i}
              src={url}
              style={{ width: '100%', borderRadius: 8 }}
              preview={false}
              onClick={() => window.open(url, '_blank')}
            />
          ))}
        </div>
      </Modal>
    </div>
  )
}