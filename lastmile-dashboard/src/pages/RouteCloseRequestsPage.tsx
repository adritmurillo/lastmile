import { useEffect, useState, useCallback } from 'react'
import { Card, Table, Tag, Button, Modal, message, Image, Descriptions, Empty } from 'antd'
import { ReloadOutlined, CheckOutlined, CloseOutlined, ExclamationCircleOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import dayjs from 'dayjs'
import { routeCloseRequestsApi } from '../api/routeCloseRequestsApi'
import type { RouteCloseRequest } from '../types'
import { ROUTE_CLOSE_REASON_LABELS } from '../types'
import { useAuthStore } from '../store/authStore'
import { useNotificationStore } from '../store/notificationStore'

const statusColors: Record<string, string> = {
  PENDING: 'gold',
  APPROVED: 'green',
  REJECTED: 'red',
}

const statusLabels: Record<string, string> = {
  PENDING: 'Pendiente',
  APPROVED: 'Aprobado',
  REJECTED: 'Rechazado',
}

export default function RouteCloseRequestsPage() {
  const [requests, setRequests] = useState<RouteCloseRequest[]>([])
  const [loading, setLoading] = useState(false)
  const [selectedRequest, setSelectedRequest] = useState<RouteCloseRequest | null>(null)
  const [detailModalOpen, setDetailModalOpen] = useState(false)
  const [processing, setProcessing] = useState(false)
  const [messageApi, contextHolder] = message.useMessage()
  const { user } = useAuthStore()
  
  // Listen to WebSocket notifications for auto-refresh
  const closeRequestTrigger = useNotificationStore((state) => state.closeRequestTrigger)

  const fetchRequests = useCallback(async () => {
    setLoading(true)
    try {
      const res = await routeCloseRequestsApi.getPendingRequests()
      setRequests(res.data)
    } catch {
      messageApi.error('Error al cargar solicitudes')
    } finally {
      setLoading(false)
    }
  }, [messageApi])

  // Initial fetch and refresh when WebSocket notification arrives
  useEffect(() => {
    fetchRequests()
  }, [fetchRequests, closeRequestTrigger])

  const handleApprove = async (request: RouteCloseRequest) => {
    Modal.confirm({
      title: 'Aprobar solicitud de cierre',
      icon: <ExclamationCircleOutlined />,
      content: (
        <div>
          <p>¿Estás seguro de aprobar esta solicitud?</p>
          <p style={{ color: '#666', fontSize: 13, marginTop: 8 }}>
            <strong>Courier:</strong> {request.courierName}<br />
            <strong>Motivo:</strong> {ROUTE_CLOSE_REASON_LABELS[request.reason]}<br />
            <strong>Paquetes pendientes:</strong> {request.pendingStopsCount}
          </p>
          <p style={{ color: '#fa8c16', fontSize: 13, marginTop: 12 }}>
            Los paquetes pendientes serán marcados como DEVUELTOS AL ALMACÉN y 
            deberán ser re-escaneados antes de volver a asignarlos.
          </p>
        </div>
      ),
      okText: 'Aprobar',
      okType: 'primary',
      cancelText: 'Cancelar',
      onOk: async () => {
        setProcessing(true)
        try {
          await routeCloseRequestsApi.approve(request.id, user!.id)
          messageApi.success('Solicitud aprobada. Ruta cerrada.')
          setDetailModalOpen(false)
          fetchRequests()
        } catch {
          messageApi.error('Error al aprobar la solicitud')
        } finally {
          setProcessing(false)
        }
      },
    })
  }

  const handleReject = async (request: RouteCloseRequest) => {
    Modal.confirm({
      title: 'Rechazar solicitud de cierre',
      icon: <ExclamationCircleOutlined />,
      content: (
        <div>
          <p>¿Estás seguro de rechazar esta solicitud?</p>
          <p style={{ color: '#666', fontSize: 13, marginTop: 8 }}>
            El courier <strong>{request.courierName}</strong> deberá continuar con su ruta.
          </p>
        </div>
      ),
      okText: 'Rechazar',
      okType: 'danger',
      cancelText: 'Cancelar',
      onOk: async () => {
        setProcessing(true)
        try {
          await routeCloseRequestsApi.reject(request.id, user!.id)
          messageApi.success('Solicitud rechazada. El courier debe continuar.')
          setDetailModalOpen(false)
          fetchRequests()
        } catch {
          messageApi.error('Error al rechazar la solicitud')
        } finally {
          setProcessing(false)
        }
      },
    })
  }

  const openDetailModal = (request: RouteCloseRequest) => {
    setSelectedRequest(request)
    setDetailModalOpen(true)
  }

  const columns: ColumnsType<RouteCloseRequest> = [
    {
      title: 'Fecha/Hora',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 160,
      render: (date) => dayjs(date).format('DD/MM/YYYY HH:mm'),
    },
    {
      title: 'Courier',
      dataIndex: 'courierName',
      key: 'courierName',
    },
    {
      title: 'Motivo',
      dataIndex: 'reason',
      key: 'reason',
      render: (reason) => ROUTE_CLOSE_REASON_LABELS[reason as keyof typeof ROUTE_CLOSE_REASON_LABELS] || reason,
    },
    {
      title: 'Paquetes Pendientes',
      dataIndex: 'pendingStopsCount',
      key: 'pendingStopsCount',
      width: 140,
      align: 'center',
      render: (count) => <Tag color={count > 5 ? 'red' : 'orange'}>{count}</Tag>,
    },
    {
      title: 'Estado',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status) => <Tag color={statusColors[status]}>{statusLabels[status]}</Tag>,
    },
    {
      title: 'Acciones',
      key: 'actions',
      width: 280,
      render: (_, record) => (
        <div style={{ display: 'flex', gap: 8 }}>
          <Button size="small" onClick={() => openDetailModal(record)}>
            Ver detalle
          </Button>
          {record.status === 'PENDING' && (
            <>
              <Button
                size="small"
                type="primary"
                icon={<CheckOutlined />}
                onClick={() => handleApprove(record)}
                loading={processing}
              >
                Aprobar
              </Button>
              <Button
                size="small"
                danger
                icon={<CloseOutlined />}
                onClick={() => handleReject(record)}
                loading={processing}
              >
                Rechazar
              </Button>
            </>
          )}
        </div>
      ),
    },
  ]

  return (
    <div>
      {contextHolder}
      <Card
        title="Solicitudes de Cierre de Ruta"
        extra={
          <Button icon={<ReloadOutlined />} onClick={fetchRequests} loading={loading}>
            Actualizar
          </Button>
        }
      >
        {requests.length === 0 && !loading ? (
          <Empty description="No hay solicitudes pendientes" />
        ) : (
          <Table
            columns={columns}
            dataSource={requests}
            rowKey="id"
            loading={loading}
            pagination={false}
          />
        )}
      </Card>

      {/* Modal de detalle */}
      <Modal
        title="Detalle de Solicitud de Cierre"
        open={detailModalOpen}
        onCancel={() => setDetailModalOpen(false)}
        footer={
          selectedRequest?.status === 'PENDING' ? (
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
              <Button onClick={() => setDetailModalOpen(false)}>Cerrar</Button>
              <Button
                danger
                icon={<CloseOutlined />}
                onClick={() => handleReject(selectedRequest)}
                loading={processing}
              >
                Rechazar
              </Button>
              <Button
                type="primary"
                icon={<CheckOutlined />}
                onClick={() => handleApprove(selectedRequest)}
                loading={processing}
              >
                Aprobar
              </Button>
            </div>
          ) : null
        }
        width={600}
        centered
      >
        {selectedRequest && (
          <div>
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="Courier">
                {selectedRequest.courierName}
              </Descriptions.Item>
              <Descriptions.Item label="Fecha/Hora">
                {dayjs(selectedRequest.createdAt).format('DD/MM/YYYY HH:mm:ss')}
              </Descriptions.Item>
              <Descriptions.Item label="Motivo">
                <Tag color="orange">
                  {ROUTE_CLOSE_REASON_LABELS[selectedRequest.reason]}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Mensaje del Courier">
                <div style={{ whiteSpace: 'pre-wrap', color: '#333' }}>
                  {selectedRequest.message}
                </div>
              </Descriptions.Item>
              <Descriptions.Item label="Paquetes Pendientes">
                <Tag color={selectedRequest.pendingStopsCount > 5 ? 'red' : 'orange'}>
                  {selectedRequest.pendingStopsCount} paquetes
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Estado">
                <Tag color={statusColors[selectedRequest.status]}>
                  {statusLabels[selectedRequest.status]}
                </Tag>
              </Descriptions.Item>
            </Descriptions>

            {selectedRequest.photoUrl && (
              <div style={{ marginTop: 16 }}>
                <p style={{ fontWeight: 600, marginBottom: 8 }}>Foto de evidencia:</p>
                <Image
                  src={selectedRequest.photoUrl}
                  style={{ maxWidth: '100%', borderRadius: 8 }}
                  preview={{ mask: 'Click para ampliar' }}
                />
              </div>
            )}

            {selectedRequest.status === 'PENDING' && (
              <div style={{ 
                marginTop: 16, 
                padding: 12, 
                backgroundColor: '#fffbe6', 
                border: '1px solid #ffe58f',
                borderRadius: 6 
              }}>
                <p style={{ margin: 0, fontSize: 13, color: '#ad6800' }}>
                  <strong>Nota:</strong> Al aprobar, la ruta será cerrada y los {selectedRequest.pendingStopsCount} paquetes 
                  pendientes serán marcados como "Devueltos al Almacén". El courier que devuelve estos paquetes 
                  no podrá recibirlos de nuevo hoy.
                </p>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}
