import { useEffect, useState, useRef } from 'react'
import { Card, Table, Tag, Button, Modal, Form, Input, message, Space, Select, InputNumber, Tabs } from 'antd'
import { PlusOutlined, CheckOutlined, StopOutlined, EditOutlined, CarOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { couriersApi } from '../api/couriersApi'
import type { Courier, Vehicle } from '../types'

export default function CouriersPage() {
  const [couriers, setCouriers] = useState<Courier[]>([])
  const [vehicles, setVehicles] = useState<Vehicle[]>([])
  const allVehiclesRef = useRef<Vehicle[]>([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [editModalOpen, setEditModalOpen] = useState(false)
  const [vehicleModalOpen, setVehicleModalOpen] = useState(false)
  const [assignModalOpen, setAssignModalOpen] = useState(false)
  const [selectedCourier, setSelectedCourier] = useState<Courier | null>(null)
  const [messageApi, contextHolder] = message.useMessage()
  const [form] = Form.useForm()
  const [editForm] = Form.useForm()
  const [vehicleForm] = Form.useForm()
  const [search, setSearch] = useState('')

  const fetchCouriers = async () => {
    setLoading(true)
    try {
      const res = await couriersApi.getAll()
      setCouriers(res.data)
    } finally {
      setLoading(false)
    }
  }

  const fetchVehicles = async () => {
    const res = await couriersApi.getVehicles()
    setVehicles(res.data)
    allVehiclesRef.current = res.data
  }

  useEffect(() => {
    fetchCouriers()
    fetchVehicles()
  }, [])

  const filteredCouriers = couriers.filter(c =>
    c.fullName.toLowerCase().includes(search.toLowerCase())
  )

  const handleToggleStatus = async (courier: Courier) => {
    try {
      if (courier.status === 'ACTIVE') {
        await couriersApi.deactivate(courier.id)
        messageApi.success(`${courier.fullName} desactivado`)
      } else {
        await couriersApi.activate(courier.id)
        messageApi.success(`${courier.fullName} activado`)
      }
      fetchCouriers()
    } catch {
      messageApi.error('Error al cambiar estado')
    }
  }

  const handleCreate = async (values: { firstName: string; lastName: string; documentNumber: string; phone: string }) => {
    try {
      await couriersApi.register(values)
      messageApi.success('Courier registrado correctamente')
      setModalOpen(false)
      form.resetFields()
      fetchCouriers()
    } catch {
      messageApi.error('Error al registrar courier')
    }
  }

  const handleEdit = (courier: Courier) => {
    setSelectedCourier(courier)
    editForm.setFieldsValue({
      firstName: courier.fullName.split(' ')[0],
      lastName: courier.fullName.split(' ')[1],
      documentNumber: courier.documentNumber,
      phone: courier.phone,
    })
    setEditModalOpen(true)
  }

  const handleUpdate = async (values: { firstName: string; lastName: string; documentNumber: string; phone: string }) => {
    if (!selectedCourier) return
    try {
      await couriersApi.update(selectedCourier.id, values)
      messageApi.success('Courier actualizado correctamente')
      setEditModalOpen(false)
      editForm.resetFields()
      fetchCouriers()
    } catch {
      messageApi.error('Error al actualizar courier')
    }
  }

  const handleAssignVehicle = (courier: Courier) => {
    setSelectedCourier(courier)
    setAssignModalOpen(true)
  }

  const handleAssignSubmit = async (values: { vehicleId: string }) => {
    if (!selectedCourier) return
    try {
      await couriersApi.assignVehicle(selectedCourier.id, values.vehicleId)
      messageApi.success('Vehículo asignado correctamente')
      setAssignModalOpen(false)
      fetchCouriers()
    } catch {
      messageApi.error('Error al asignar vehículo')
    }
  }

  const handleRegisterVehicle = async (values: { licensePlate: string; type: string; maxWeightKg: number; maxVolumeCm3: number }) => {
    try {
      await couriersApi.registerVehicle(values)
      messageApi.success('Vehículo registrado correctamente')
      setVehicleModalOpen(false)
      vehicleForm.resetFields()
      fetchVehicles()
    } catch {
      messageApi.error('Error al registrar vehículo')
    }
  }

  const courierColumns: ColumnsType<Courier> = [
    { title: 'Nombre', dataIndex: 'fullName', key: 'fullName' },
    { title: 'Documento', dataIndex: 'documentNumber', key: 'documentNumber' },
    { title: 'Teléfono', dataIndex: 'phone', key: 'phone' },
    {
      title: 'Vehículo',
      key: 'vehicle',
      render: (_, record) =>
        record.vehicle?.licensePlate
          ? `${record.vehicle.licensePlate} (${record.vehicle.type})`
          : <Tag color="warning">Sin vehículo</Tag>,
    },
    {
      title: 'Estado',
      dataIndex: 'status',
      key: 'status',
      render: (s) => <Tag color={s === 'ACTIVE' ? 'green' : 'red'}>{s === 'ACTIVE' ? 'Activo' : 'Inactivo'}</Tag>,
    },
    {
      title: 'Acciones',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>Editar</Button>
          <Button size="small" icon={<CarOutlined />} onClick={() => handleAssignVehicle(record)}>Vehículo</Button>
          {record.status === 'ACTIVE'
            ? <Button size="small" danger icon={<StopOutlined />} onClick={() => handleToggleStatus(record)}>Desactivar</Button>
            : <Button size="small" icon={<CheckOutlined />} style={{ color: '#52c41a', borderColor: '#52c41a' }} onClick={() => handleToggleStatus(record)}>Activar</Button>
          }
        </Space>
      ),
    },
  ]



  const vehicleColumns: ColumnsType<Vehicle> = [
    { title: 'Placa', dataIndex: 'licensePlate', key: 'licensePlate' },
    { title: 'Tipo', dataIndex: 'type', key: 'type' },
    { title: 'Peso máx (kg)', dataIndex: 'maxWeightKg', key: 'maxWeightKg' },
    { title: 'Volumen máx (cm³)', dataIndex: 'maxVolumeCm3', key: 'maxVolumeCm3' },
    {
      title: 'Estado',
      dataIndex: 'status',
      key: 'status',
      render: (s) => <Tag color={s === 'AVAILABLE' ? 'green' : 'orange'}>{s}</Tag>,
    },
  ]

  return (
    <div>
      {contextHolder}
      <Tabs
        defaultActiveKey="couriers"
        items={[
          {
            key: 'couriers',
            label: 'Couriers',
            children: (
              <Card
                title="Gestión de Couriers"
                extra={
                  <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
                    Nuevo courier
                  </Button>
                }
              >
                <Input.Search
                  placeholder="Buscar por nombre..."
                  style={{ marginBottom: 16, width: 300 }}
                  onChange={(e) => setSearch(e.target.value)}
                />
                <Table columns={courierColumns} dataSource={filteredCouriers} rowKey="id" loading={loading} pagination={{ pageSize: 10 }} />
              </Card>
            ),
          },
          {
            key: 'vehicles',
            label: 'Vehículos',
            children: (
              <Card
                title="Gestión de Vehículos"
                extra={
                  <Button type="primary" icon={<PlusOutlined />} onClick={() => setVehicleModalOpen(true)}>
                    Nuevo vehículo
                  </Button>
                }
              >
                <Input.Search
                  placeholder="Buscar por placa o tipo..."
                  style={{ marginBottom: 16, width: 300 }}
                  allowClear
                  onChange={(e) => {
                    const value = e.target.value
                    if (!value) {
                      setVehicles(allVehiclesRef.current)
                      return
                    }
                    const filtered = allVehiclesRef.current.filter(v =>
                      v.licensePlate.toLowerCase().includes(value.toLowerCase()) ||
                      v.type.toLowerCase().includes(value.toLowerCase())
                    )
                    setVehicles(filtered)
                  }}
                />
                <Table columns={vehicleColumns} dataSource={vehicles} rowKey="id" loading={loading} pagination={{ pageSize: 10 }} />
              </Card>
            ),
          },
        ]}
      />

      <Modal title="Nuevo Courier" open={modalOpen} onCancel={() => setModalOpen(false)} onOk={() => form.submit()} okText="Registrar" cancelText="Cancelar">
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="firstName" label="Nombre" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="lastName" label="Apellido" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="documentNumber" label="Documento" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="phone" label="Teléfono" rules={[{ required: true }]}><Input /></Form.Item>
        </Form>
      </Modal>

      <Modal title="Editar Courier" open={editModalOpen} onCancel={() => setEditModalOpen(false)} onOk={() => editForm.submit()} okText="Guardar" cancelText="Cancelar">
        <Form form={editForm} layout="vertical" onFinish={handleUpdate}>
          <Form.Item name="firstName" label="Nombre" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="lastName" label="Apellido" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="documentNumber" label="Documento" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="phone" label="Teléfono" rules={[{ required: true }]}><Input /></Form.Item>
        </Form>
      </Modal>

      <Modal title={`Asignar vehículo a ${selectedCourier?.fullName}`} open={assignModalOpen} onCancel={() => setAssignModalOpen(false)} onOk={() => { const form = document.querySelector('form'); form?.dispatchEvent(new Event('submit', { bubbles: true })) }} okText="Asignar" cancelText="Cancelar">
        <Form layout="vertical" onFinish={handleAssignSubmit}>
          <Form.Item name="vehicleId" label="Vehículo" rules={[{ required: true }]}>
            <Select options={vehicles.map(v => ({ value: v.id, label: `${v.licensePlate} (${v.type})` }))} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="Nuevo Vehículo" open={vehicleModalOpen} onCancel={() => setVehicleModalOpen(false)} onOk={() => vehicleForm.submit()} okText="Registrar" cancelText="Cancelar">
        <Form form={vehicleForm} layout="vertical" onFinish={handleRegisterVehicle}>
          <Form.Item name="licensePlate" label="Placa" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="type" label="Tipo" rules={[{ required: true }]}>
            <Select options={[
              { value: 'MOTORCYCLE', label: 'Moto' },
              { value: 'CAR', label: 'Auto' },
              { value: 'VAN', label: 'Van' },
              { value: 'TRUCK', label: 'Camión' },
            ]} />
          </Form.Item>
          <Form.Item name="maxWeightKg" label="Peso máximo (kg)" rules={[{ required: true }]}><InputNumber min={1} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="maxVolumeCm3" label="Volumen máximo (cm³)" rules={[{ required: true }]}><InputNumber min={1} style={{ width: '100%' }} /></Form.Item>
        </Form>
      </Modal>
    </div>
  )
}