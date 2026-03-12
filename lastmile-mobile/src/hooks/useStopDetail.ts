import { useState } from 'react'
import { Alert, Linking, Platform } from 'react-native'
import { routesApi } from '../api/routesApi'
import type { Stop } from '../types'

export type FailureReason = 'NOBODY_HOME' | 'INCORRECT_ADDRESS' | 'CUSTOMER_REJECTED' | 'INACCESSIBLE_AREA' | 'OTHER'

export const FAILURE_REASONS: { key: FailureReason; label: string; emoji: string }[] = [
  { key: 'NOBODY_HOME',        label: 'Nadie en casa',        emoji: '🏠' },
  { key: 'INCORRECT_ADDRESS',  label: 'Dirección incorrecta', emoji: '📍' },
  { key: 'CUSTOMER_REJECTED',  label: 'Cliente rechazó',      emoji: '🚫' },
  { key: 'INACCESSIBLE_AREA',  label: 'Zona inaccesible',     emoji: '🚧' },
  { key: 'OTHER',              label: 'Otro motivo',          emoji: '📝' },
]

export function useStopDetail(stop: Stop, onComplete: () => void) {
  const [loading, setLoading] = useState(false)
  const [failModalOpen, setFailModalOpen] = useState(false)
  const [selectedReason, setSelectedReason] = useState<FailureReason | null>(null)
  const [failureNotes, setFailureNotes] = useState('')

  const handleDeliver = () => {
    Alert.alert(
      'Confirmar entrega',
      `¿Confirmas la entrega a ${stop.order.recipientName}?`,
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Confirmar',
          onPress: async () => {
            setLoading(true)
            try {
              await routesApi.deliverStop(stop.id)
              onComplete()
            } catch {
              Alert.alert('Error', 'No se pudo registrar la entrega')
            } finally {
              setLoading(false)
            }
          }
        }
      ]
    )
  }

  const handleFail = async () => {
    if (!selectedReason) {
      Alert.alert('Requerido', 'Selecciona el motivo del fallo')
      return
    }
    if (selectedReason === 'OTHER' && !failureNotes.trim()) {
      Alert.alert('Requerido', 'Describe el motivo del fallo')
      return
    }
    setLoading(true)
    try {
      await routesApi.failStop(stop.id, selectedReason, failureNotes.trim() || undefined)
      setFailModalOpen(false)
      setSelectedReason(null)
      setFailureNotes('')
      onComplete()
    } catch {
      Alert.alert('Error', 'No se pudo registrar el fallo')
    } finally {
      setLoading(false)
    }
  }

  const handleCall = () => Linking.openURL(`tel:${stop.order.recipientPhone}`)

  const handleMaps = () => {
    const address = encodeURIComponent(stop.order.addressText)
    const url = Platform.OS === 'ios' ? `maps://?q=${address}` : `geo:0,0?q=${address}`
    Linking.openURL(url)
  }

  const openFailModal = () => { setSelectedReason(null); setFailureNotes(''); setFailModalOpen(true) }
  const closeFailModal = () => { setSelectedReason(null); setFailureNotes(''); setFailModalOpen(false) }

  return {
    loading,
    failModalOpen,
    selectedReason,
    setSelectedReason,
    failureNotes,
    setFailureNotes,
    handleDeliver,
    handleFail,
    handleCall,
    handleMaps,
    openFailModal,
    closeFailModal,
  }
}