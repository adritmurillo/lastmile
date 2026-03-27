import { useEffect, useState, useRef } from 'react'
import { Table, Tag, Button, Select, DatePicker, Space, Card, Modal, Form, Input, InputNumber, message, Upload } from 'antd'
import { PlusOutlined, ReloadOutlined, UploadOutlined, QrcodeOutlined, PrinterOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import dayjs from 'dayjs'
import { QRCodeSVG } from 'qrcode.react'
import { ordersApi } from '../api/ordersApi'
import type { Order } from '../types'

const statusColors: Record<string, string> = {
  PENDING: 'gold',
  READY_TO_DISPATCH: 'cyan',
  ASSIGNED: 'blue',
  PICKED_UP: 'geekblue',
  IN_TRANSIT: 'purple',
  DELIVERED: 'green',
  FAILED: 'red',
  SKIPPED: 'orange',
  RETURNED_TO_WAREHOUSE: 'volcano',
  RETURNED: 'magenta',
  CANCELLED: 'default',
}

const statusLabels: Record<string, string> = {
  PENDING: 'Pendiente',
  READY_TO_DISPATCH: 'Listo para despachar',
  ASSIGNED: 'Asignado',
  PICKED_UP: 'Recogido',
  IN_TRANSIT: 'En tránsito',
  DELIVERED: 'Entregado',
  FAILED: 'Fallido',
  SKIPPED: 'Omitido (ruta cerrada)',
  RETURNED_TO_WAREHOUSE: 'Devuelto al almacén',
  RETURNED: 'Devuelto (3 intentos)',
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
  const allOrdersRef = useRef<Order[]>([])
  const [detailModalOpen, setDetailModalOpen] = useState(false)
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null)
  const [proofPhotoUrls, setProofPhotoUrls] = useState<string[]>([])
  const [qrModalOpen, setQrModalOpen] = useState(false)
  const [qrOrder, setQrOrder] = useState<Order | null>(null)
  const [selectedOrders, setSelectedOrders] = useState<Order[]>([])

  const handlePrintQr = (order: Order) => {
    setQrOrder(order)
    setQrModalOpen(true)
  }

  const handlePrintSelected = () => {
    if (selectedOrders.length === 0) {
      messageApi.warning('Selecciona al menos una orden para imprimir')
      return
    }
    // Print multiple QR labels
    const printWindow = window.open('', '_blank')
    if (!printWindow) return
    
    const labelsHtml = selectedOrders.map(order => `
      <div style="page-break-after: always; padding: 20px; border: 1px dashed #ccc; width: 300px; margin: 0 auto;">
        <div style="text-align: center; margin-bottom: 10px;">
          <strong style="font-size: 14px;">LASTMILE DELIVERY</strong>
        </div>
        <div style="text-align: center; margin: 15px 0;">
          <svg id="qr-${order.id}"></svg>
        </div>
        <div style="text-align: center; font-family: monospace; font-size: 18px; font-weight: bold; margin: 10px 0;">
          ${order.trackingCode}
        </div>
        <hr style="border: none; border-top: 1px dashed #ccc; margin: 10px 0;" />
        <div style="font-size: 12px;">
          <div><strong>Para:</strong> ${order.recipientName}</div>
          <div><strong>Dir:</strong> ${order.addressText}</div>
          <div><strong>Tel:</strong> ${order.recipientPhone}</div>
          <div style="margin-top: 5px;"><strong>Prioridad:</strong> ${order.priority === 'EXPRESS' ? '⚡ EXPRESS' : 'STANDARD'}</div>
        </div>
      </div>
    `).join('')

    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>Etiquetas QR</title>
        <script src="https://cdn.jsdelivr.net/npm/qrcode@1.5.3/build/qrcode.min.js"></script>
        <style>
          body { font-family: Arial, sans-serif; }
          @media print { 
            body { margin: 0; }
            div { page-break-inside: avoid; }
          }
        </style>
      </head>
      <body>
        ${labelsHtml}
        <script>
          ${selectedOrders.map(order => `
            QRCode.toCanvas(document.createElement('canvas'), '${order.trackingCode}', { width: 150 }, function(err, canvas) {
              const container = document.getElementById('qr-${order.id}');
              if (container) container.replaceWith(canvas);
            });
          `).join('')}
          setTimeout(function() { window.print(); }, 500);
        </script>
      </body>
      </html>
    `)
    printWindow.document.close()
  }

  const printSingleLabel = () => {
    if (!qrOrder) return
    const printWindow = window.open('', '_blank')
    if (!printWindow) return

    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>Etiqueta - ${qrOrder.trackingCode}</title>
        <script src="https://cdn.jsdelivr.net/npm/qrcode@1.5.3/build/qrcode.min.js"></script>
        <style>
          body { font-family: Arial, sans-serif; display: flex; justify-content: center; padding: 20px; }
          .label { border: 1px dashed #ccc; padding: 20px; width: 300px; }
          .header { text-align: center; margin-bottom: 10px; font-weight: bold; }
          .qr-container { text-align: center; margin: 15px 0; }
          .tracking { text-align: center; font-family: monospace; font-size: 18px; font-weight: bold; margin: 10px 0; }
          .divider { border: none; border-top: 1px dashed #ccc; margin: 10px 0; }
          .info { font-size: 12px; }
          .info div { margin: 3px 0; }
        </style>
      </head>
      <body>
        <div class="label">
          <div class="header">LASTMILE DELIVERY</div>
          <div class="qr-container"><canvas id="qrcode"></canvas></div>
          <div class="tracking">${qrOrder.trackingCode}</div>
          <hr class="divider" />
          <div class="info">
            <div><strong>Para:</strong> ${qrOrder.recipientName}</div>
            <div><strong>Dir:</strong> ${qrOrder.addressText}</div>
            <div><strong>Tel:</strong> ${qrOrder.recipientPhone}</div>
            <div style="margin-top: 5px;"><strong>Prioridad:</strong> ${qrOrder.priority === 'EXPRESS' ? '⚡ EXPRESS' : 'STANDARD'}</div>
            <div><strong>Fecha límite:</strong> ${dayjs(qrOrder.deliveryDeadline).format('DD/MM/YYYY')}</div>
          </div>
        </div>
        <script>
          QRCode.toCanvas(document.getElementById('qrcode'), '${qrOrder.trackingCode}', { width: 150 }, function() {
            setTimeout(function() { window.print(); }, 300);
          });
        </script>
      </body>
      </html>
    `)
    printWindow.document.close()
  }

  const fetchOrders = async () => {
    setLoading(true)
    try {
      const res = await ordersApi.getOrders({
        status: statusFilter,
        date: dateFilter,
      })
      allOrdersRef.current = res.data
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

  const handleViewOrder = async (order: Order) => {
    setSelectedOrder(order)
    setDetailModalOpen(true)
    setProofPhotoUrls([])
    if (order.status === 'DELIVERED') {
      try {
        const res = await ordersApi.getProofPhotos(order.id)
        setProofPhotoUrls(res.data ?? [])
      } catch {
        setProofPhotoUrls([])
      }
    }
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
    {
      title: 'Acciones',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => handleViewOrder(record)}>Ver</Button>
          <Button size="small" icon={<QrcodeOutlined />} onClick={() => handlePrintQr(record)}>QR</Button>
          {record.status === 'PENDING' && (
            <Button size="small" type="primary" onClick={async () => {
              try {
                await ordersApi.receiveOrder(record.id)
                messageApi.success('Paquete recibido en almacén')
                fetchOrders()
              } catch {
                messageApi.error('No se pudo actualizar la orden')
              }
            }}>Recibido</Button>
          )}
          {record.status === 'RETURNED_TO_WAREHOUSE' && (
            <Button size="small" type="primary" style={{ backgroundColor: '#fa8c16' }} onClick={async () => {
              try {
                await ordersApi.confirmReturn(record.id)
                messageApi.success('Devolución confirmada, paquete listo para re-despacho')
                fetchOrders()
              } catch {
                messageApi.error('No se pudo confirmar la devolución')
              }
            }}>Confirmar devolución</Button>
          )}
          {record.status !== 'DELIVERED' && record.status !== 'CANCELLED'
            && record.status !== 'RETURNED' && record.status !== 'RETURNED_TO_WAREHOUSE'
            && record.status !== 'SKIPPED' && (
              <Button size="small" danger onClick={async () => {
                try {
                  await ordersApi.cancelOrder(record.id)
                  messageApi.success('Orden cancelada')
                  fetchOrders()
                } catch {
                  messageApi.error('No se puede cancelar esta orden')
                }
              }}>Cancelar</Button>
            )}
        </Space>
      )
    }
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
          onChange={(e) => {
            const value = e.target.value
            if (!value) {
              fetchOrders()
              return
            }
            const filtered = allOrdersRef.current.filter(o =>
              o.trackingCode.toLowerCase().includes(value.toLowerCase())
            )
            setOrders(filtered)
          }}
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
          {selectedOrders.length > 0 && (
            <Button 
              type="primary" 
              icon={<PrinterOutlined />} 
              onClick={handlePrintSelected}
            >
              Imprimir {selectedOrders.length} etiqueta(s)
            </Button>
          )}
        </Space>

        <Table
          columns={columns}
          dataSource={orders}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          rowSelection={{
            selectedRowKeys: selectedOrders.map(o => o.id),
            onChange: (_, rows) => setSelectedOrders(rows),
          }}
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

      <Modal
        title={`Detalle — ${selectedOrder?.trackingCode}`}
        open={detailModalOpen}
        onCancel={() => setDetailModalOpen(false)}
        footer={null}
      >
        {selectedOrder && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            <div><strong>Destinatario:</strong> {selectedOrder.recipientName}</div>
            <div><strong>Teléfono:</strong> {selectedOrder.recipientPhone}</div>
            <div><strong>Email:</strong> {selectedOrder.recipientEmail ?? '—'}</div>
            <div><strong>Dirección:</strong> {selectedOrder.addressText}</div>
            <div><strong>Peso:</strong> {selectedOrder.weightKg} kg</div>
            <div><strong>Volumen:</strong> {selectedOrder.volumeCm3} cm³</div>
            <div><strong>Prioridad:</strong> {selectedOrder.priority}</div>
            <div><strong>Estado:</strong> {statusLabels[selectedOrder.status]}</div>
            <div><strong>Intentos:</strong> {selectedOrder.deliveryAttempts}/3</div>
            <div><strong>Fecha límite:</strong> {dayjs(selectedOrder.deliveryDeadline).format('DD/MM/YYYY')}</div>
            <div><strong>Creado:</strong> {dayjs(selectedOrder.createdAt).format('DD/MM/YYYY HH:mm')}</div>
            {proofPhotoUrls.length > 0 && (
              <div>
                <strong>Fotos de entrega:</strong>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 8 }}>
                  {proofPhotoUrls.map((url, i) => (
                    <img
                      key={i}
                      src={url}
                      alt={`Foto ${i + 1}`}
                      style={{ width: '100%', borderRadius: 8, cursor: 'pointer' }}
                      onClick={() => window.open(url, '_blank')}
                    />
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>

      <Modal
        title="Etiqueta de paquete"
        open={qrModalOpen}
        onCancel={() => setQrModalOpen(false)}
        footer={[
          <Button key="cancel" onClick={() => setQrModalOpen(false)}>Cerrar</Button>,
          <Button key="print" type="primary" icon={<PrinterOutlined />} onClick={printSingleLabel}>
            Imprimir
          </Button>
        ]}
        width={400}
      >
        {qrOrder && (
          <div style={{ textAlign: 'center', padding: '20px 0' }}>
            <div style={{ 
              border: '1px dashed #d9d9d9', 
              borderRadius: 8, 
              padding: 20,
              display: 'inline-block'
            }}>
              <div style={{ fontWeight: 'bold', marginBottom: 12 }}>LASTMILE DELIVERY</div>
              <QRCodeSVG value={qrOrder.trackingCode} size={150} />
              <div style={{ 
                fontFamily: 'monospace', 
                fontSize: 18, 
                fontWeight: 'bold', 
                marginTop: 12 
              }}>
                {qrOrder.trackingCode}
              </div>
              <hr style={{ border: 'none', borderTop: '1px dashed #d9d9d9', margin: '12px 0' }} />
              <div style={{ textAlign: 'left', fontSize: 12 }}>
                <div><strong>Para:</strong> {qrOrder.recipientName}</div>
                <div><strong>Dir:</strong> {qrOrder.addressText}</div>
                <div><strong>Tel:</strong> {qrOrder.recipientPhone}</div>
                <div style={{ marginTop: 8 }}>
                  <Tag color={qrOrder.priority === 'EXPRESS' ? 'red' : 'blue'}>
                    {qrOrder.priority === 'EXPRESS' ? '⚡ EXPRESS' : 'STANDARD'}
                  </Tag>
                </div>
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}