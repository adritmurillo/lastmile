import { useState } from 'react'
import { Alert, Linking, Platform } from 'react-native'
import { routesApi } from '../api/routesApi'
import type { Stop } from '../types'

export type FailureReason = 'NOBODY_HOME' | 'INCORRECT_ADDRESS' | 'CUSTOMER_REJECTED' | 'INACCESSIBLE_AREA' | 'OTHER'

export const FAILURE_REASONS: { key: FailureReason; label: string; emoji: string }[] = [
  { key: 'NOBODY_HOME', label: 'Nadie en casa', emoji: '🏠' },
  { key: 'INCORRECT_ADDRESS', label: 'Dirección incorrecta', emoji: '📍' },
  { key: 'CUSTOMER_REJECTED', label: 'Cliente rechazó', emoji: '🚫' },
  { key: 'INACCESSIBLE_AREA', label: 'Zona inaccesible', emoji: '🚧' },
  { key: 'OTHER', label: 'Otro motivo', emoji: '📝' },
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

  const handleCall = () => {
    Linking.openURL(`tel:${stop.order.recipientPhone}`)
  }

  const handleWhatsApp = () => {
    const raw = stop.order.recipientPhone.replace(/\D/g, '')
    const phone = raw.startsWith('51') ? raw : `51${raw}`
    const msg = encodeURIComponent(
      `Hola ${stop.order.recipientName}, soy el repartidor de LastMile. Estoy camino a entregar tu pedido ${stop.order.trackingCode}. ¿Estás disponible?`
    )
    const url = `whatsapp://send?phone=${phone}&text=${msg}`
    const fallback = `https://wa.me/${phone}?text=${msg}`
    Linking.canOpenURL(url).then(canOpen => {
      Linking.openURL(canOpen ? url : fallback)
    })
  }

  const handleMaps = () => {
    const address = encodeURIComponent(stop.order.addressText)
    const lat = stop.order.latitude
    const lng = stop.order.longitude
    const hasCoords = lat != null && lng != null

    const apps = [
      {
        label: '🗺 Apple Maps',
        url: hasCoords ? `maps://?daddr=${lat},${lng}` : `maps://?q=${address}`,
        available: Platform.OS === 'ios',
      },
      {
        label: '📍 Google Maps',
        url: hasCoords
          ? `comgooglemaps://?daddr=${lat},${lng}&directionsmode=driving`
          : `comgooglemaps://?q=${address}`,
        fallback: hasCoords
          ? `https://www.google.com/maps/dir/?api=1&destination=${lat},${lng}&travelmode=driving`
          : `https://www.google.com/maps/search/?api=1&query=${address}`,
      },
      {
        label: '🚗 Waze',
        url: hasCoords ? `waze://?ll=${lat},${lng}&navigate=yes` : `waze://?q=${address}&navigate=yes`,
        fallback: `https://waze.com/ul?q=${address}`,
      },
    ]

    const availableApps = apps.filter(app => app.available === undefined ? true : app.available)

    Alert.alert(
      'Abrir con...',
      'Selecciona tu app de navegación',
      [
        ...availableApps.map(app => ({
          text: app.label,
          onPress: async () => {
            const canOpen = await Linking.canOpenURL(app.url)
            if (canOpen) {
              Linking.openURL(app.url)
            } else if (app.fallback) {
              Linking.openURL(app.fallback)
            }
          }
        })),
        { text: 'Cancelar', style: 'cancel' }
      ]
    )
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
    handleWhatsApp,
    handleMaps,
    openFailModal,
    closeFailModal,
  }
}