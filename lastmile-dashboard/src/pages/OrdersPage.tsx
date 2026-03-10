import { useEffect, useState } from 'react'
import { Table, Tag, Button, Select, DatePicker, Space, Card, Modal, Form, Input, InputNumber, message, Upload } from 'antd'
import { PlusOutlined, ReloadOutlined, UploadOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import dayjs from 'dayjs'
import { ordersApi } from '../api/ordersApi'
import type { Order } from '../types'

const statusColors: Record<string, string> = {
  PENDING: 'gold',
  ASSIGNED: 'blue',
  IN_TRANSIT: 'purple',
  DELIVERED: 'green',
  FAILED: 'red',
  CANCELLED: 'default',
}

const statusLabels: Record<string, string> = {
  PENDING: 'Pendiente',
  ASSIGNED: 'Asignado',
  IN_TRANSIT: 'En tránsito',
  DELIVERED: 'Entregado',
  FAILED: 'Fallido',
  CANCELLED: 'Cancelado',
}

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [statusFilter, setStatusFilter] = useState<string | undefined>()
  const [dateFilter, setDateFilter] = useState<string | undefined>()
  const [form] = Form.useForm()
  const [messageApi, contextHolder] = message.useMessage()

  const fetchOrders = async () => {
    setLoading(true)
    try {
      const res = await ordersApi.getOrders({
        status: statusFilter,
        date: dateFilter,
      })
      setOrders(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchOrders()
  }, [statusFilter, dateFilter])

  const handleCreate = async (values: any) => {
    await ordersApi.createOrder(values)
    setModalOpen(false)
    form.resetFields()
    fetchOrders()
  }

  const columns: ColumnsType<Order> = [
    {
      title: 'Código',
      dataIndex: 'trackingCode',
      key: 'trackingCode',
      render: (code) => <strong>{code}</strong>,
    },
    {
      title: 'Destinatario',
      dataIndex: 'recipientName',
      key: 'recipientName',
    },
    {
      title: 'Dirección',
      dataIndex: 'addressText',
      key: 'addressText',
      ellipsis: true,
    },
    {
      title: 'Prioridad',
      dataIndex: 'priority',
      key: 'priority',
      render: (p) => <Tag color={p === 'EXPRESS' ? 'red' : 'blue'}>{p}</Tag>,
    },
    {
      title: 'Estado',
      dataIndex: 'status',
      key: 'status',
      render: (s) => <Tag color={statusColors[s]}>{statusLabels[s]}</Tag>,
    },
    {
      title: 'Intentos',
      dataIndex: 'deliveryAttempts',
      key: 'deliveryAttempts',
      render: (a) => `${a}/3`,
    },
    {
      title: 'Fecha límite',
      dataIndex: 'deliveryDeadline',
      key: 'deliveryDeadline',
      render: (d) => dayjs(d).format('DD/MM/YYYY'),
    },
  ]

  return (
    <div>
      {contextHolder}
      <Card
        title="Gestión de Órdenes"
        extra={
          <Space>
            <Upload
              accept=".xlsx,.csv"
              showUploadList={false}
              beforeUpload={async (file) => {
                setLoading(true)
                try {
                  await ordersApi.uploadFile(file, 'dispatcher')
                  messageApi.success('Archivo subido correctamente, procesando órdenes...')
                  setTimeout(() => fetchOrders(), 3000)
                } catch {
                  messageApi.error('Error al subir el archivo')
                } finally {
                  setLoading(false)
                }
                return false
              }}
            >
              <Button icon={<UploadOutlined />}>Subir Excel/CSV</Button>
            </Upload>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
              Nueva orden
            </Button>
          </Space>
        }
      >
        <Input.Search
          placeholder="Buscar por código de tracking..."
          style={{ marginBottom: 16, width: 350 }}
          allowClear
          onSearch={async (value) => {
            if (!value) {
              fetchOrders()
              return
            }
            setLoading(true)
            try {
              const res = await ordersApi.getByTracking(value)
              setOrders([res.data])
            } catch {
              messageApi.error('Orden no encontrada')
            } finally {
              setLoading(false)
            }
          }}
        />
        <Space style={{ marginBottom: 16 }}>
          <Select
            placeholder="Filtrar por estado"
            allowClear
            style={{ width: 180 }}
            onChange={setStatusFilter}
            options={Object.entries(statusLabels).map(([value, label]) => ({ value, label }))}
          />
          <DatePicker
            placeholder="Filtrar por fecha"
            onChange={(_, dateStr) => setDateFilter(dateStr as string)}
            format="YYYY-MM-DD"
          />
          <Button icon={<ReloadOutlined />} onClick={fetchOrders}>
            Actualizar
          </Button>
        </Space>

        <Table
          columns={columns}
          dataSource={orders}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>

      <Modal
        title="Nueva Orden"
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        okText="Crear"
        cancelText="Cancelar"
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="externalTrackingCode" label="Código externo" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="recipientName" label="Destinatario" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="recipientPhone" label="Teléfono" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="recipientEmail" label="Email del destinatario">
            <Input type="email" />
          </Form.Item>
          <Form.Item name="addressText" label="Dirección" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="weightKg" label="Peso (kg)" rules={[{ required: true }]}>
            <InputNumber min={0.1} step={0.1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="volumeCm3" label="Volumen (cm³)" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="priority" label="Prioridad" rules={[{ required: true }]}>
            <Select options={[{ value: 'STANDARD', label: 'Standard' }, { value: 'EXPRESS', label: 'Express' }]} />
          </Form.Item>
          <Form.Item name="deliveryDeadline" label="Fecha límite" rules={[{ required: true }]}>
            <Input type="date" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}