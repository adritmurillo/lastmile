import { useState } from 'react'
import { Alert } from 'react-native'
import { useAuth } from '../context/AuthContext'

export function useLogin() {
  const { login } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [focusedInput, setFocusedInput] = useState<string | null>(null)

  const handleLogin = async () => {
    if (!username || !password) {
      Alert.alert('Campos requeridos', 'Ingresa tu usuario y contraseña')
      return
    }
    setLoading(true)
    try {
      await login(username, password)
    } catch {
      Alert.alert('Error al ingresar', 'Usuario o contraseña incorrectos')
    } finally {
      setLoading(false)
    }
  }

  return {
    username, setUsername,
    password, setPassword,
    loading,
    focusedInput, setFocusedInput,
    handleLogin,
  }
}