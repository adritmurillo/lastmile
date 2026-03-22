import { useEffect, useState } from 'react'
import { Alert } from 'react-native'
import { couriersApi } from '../api/couriersApi'
import { useAuth } from '../context/AuthContext'
import type { Courier } from '../types'

export function useProfile() {
  const { user, logout } = useAuth()
  const [courier, setCourier] = useState<Courier | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [hasChanges, setHasChanges] = useState(false)

  const [phone, setPhone] = useState('')

  const fetchCourier = async () => {
    if (!user?.courierId) return
    try {
      const res = await couriersApi.getById(user.courierId)
      setCourier(res.data)
      setPhone(res.data.phone ?? '')
    } catch {
      Alert.alert('Error', 'No se pudo cargar el perfil')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchCourier()
  }, [])

  const handleChange = (value: string) => {
    setHasChanges(true)
    setPhone(value)
  }

  const handleSave = async () => {
    if (!courier) return
    setSaving(true)
    try {
      await couriersApi.updatePhone(courier.id, phone)
      setHasChanges(false)
      Alert.alert('✅ Guardado', 'Teléfono actualizado correctamente')
      fetchCourier()
    } catch {
      Alert.alert('Error', 'No se pudo guardar el perfil')
    } finally {
      setSaving(false)
    }
  }

  const handleBack = (onBack: () => void) => {
    if (hasChanges) {
      Alert.alert(
        '¿Salir sin guardar?',
        'Tienes cambios sin guardar. ¿Estás seguro que quieres salir?',
        [
          { text: 'Cancelar', style: 'cancel' },
          { text: 'Salir', style: 'destructive', onPress: onBack },
        ]
      )
    } else {
      onBack()
    }
  }

  const initials = courier?.fullName
    ? courier.fullName.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2)
    : '??'

  return {
    courier,
    loading,
    saving,
    hasChanges,
    phone,
    initials,
    handleChange,
    handleSave,
    handleBack,
    logout,
  }
}