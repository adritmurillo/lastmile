import { Form, Input, Button, Card, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { useAuthStore } from '../store/authStore'

export default function LoginPage() {
  const navigate = useNavigate()
  const { setAuth } = useAuthStore()
  const [messageApi, contextHolder] = message.useMessage()

  const onFinish = async (values: { username: string; password: string }) => {
    try {
      const response = await authApi.login(values.username, values.password)
      const token = response.data.token

      const payload = JSON.parse(atob(token.split('.')[1]))
      setAuth(token, {
        id: payload.userId,
        username: payload.sub,
        email: '',
        role: payload.role,
      })

      navigate('/dashboard')
    } catch {
      messageApi.error('Usuario o contraseña incorrectos')
    }
  }

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)'
    }}>
      {contextHolder}
      <Card style={{ width: 400, borderRadius: 12, boxShadow: '0 20px 60px rgba(0,0,0,0.3)' }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{ fontSize: 48 }}>🚚</div>
          <h1 style={{ margin: '8px 0 4px', fontSize: 24, fontWeight: 'bold' }}>LastMile</h1>
          <p style={{ color: '#888', margin: 0 }}>Sistema de gestión de entregas</p>
        </div>
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item name="username" rules={[{ required: true, message: 'Ingresa tu usuario' }]}>
            <Input prefix={<UserOutlined />} placeholder="Usuario" size="large" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: 'Ingresa tu contraseña' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="Contraseña" size="large" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block size="large">
              Iniciar sesión
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}