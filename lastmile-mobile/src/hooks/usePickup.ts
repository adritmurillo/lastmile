import { useCallback, useEffect, useRef, useState } from 'react'
import { Alert, Vibration } from 'react-native'
import { routesApi } from '../api/routesApi'
import { useAuth } from '../context/AuthContext'
import type { PickupStatus, Route, Stop } from '../types'

export function usePickup(onBack: () => void, onStartRoute: () => void) {
  const { user } = useAuth()
  const [route, setRoute] = useState<Route | null>(null)
  const [pickupStatus, setPickupStatus] = useState<PickupStatus | null>(null)
  const [loading, setLoading] = useState(true)
  const [scanning, setScanning] = useState(false)
  
  // Use refs for immediate blocking (state updates are async)
  const isProcessingRef = useRef(false)
  const lastScannedRef = useRef<string | null>(null)
  const lastScanTimeRef = useRef<number>(0)

  const fetchData = useCallback(async () => {
    if (!user?.courierId) return
    try {
      const routeData = await routesApi.getMiRuta(user.courierId)
      setRoute(routeData)
      if (routeData) {
        const status = await routesApi.getPickupStatus(routeData.id)
        setPickupStatus(status)
      }
    } catch {
      setRoute(null)
      setPickupStatus(null)
    } finally {
      setLoading(false)
    }
  }, [user?.courierId])

  useEffect(() => {
    fetchData()
  }, [fetchData])

  const handleScan = async (trackingCode: string) => {
    const now = Date.now()
    
    // Immediate blocking using refs (not async state)
    if (!route) return
    if (isProcessingRef.current) return
    if (trackingCode === lastScannedRef.current && now - lastScanTimeRef.current < 3000) return
    
    // Block immediately
    isProcessingRef.current = true
    lastScannedRef.current = trackingCode
    lastScanTimeRef.current = now
    setScanning(true)

    try {
      await routesApi.scanPickup(route.id, trackingCode)
      Vibration.vibrate(100) // Vibración corta de éxito

      // Actualizar estado
      const status = await routesApi.getPickupStatus(route.id)
      setPickupStatus(status)

      // Refrescar ruta para ver el nuevo estado
      const updatedRoute = await routesApi.getMiRuta(user!.courierId!)
      setRoute(updatedRoute)

      if (status.readyToStart) {
        Alert.alert(
          '¡Todos los paquetes escaneados!',
          '¿Deseas iniciar la ruta ahora?',
          [
            { text: 'Ahora no', style: 'cancel' },
            { text: 'Iniciar ruta', onPress: handleStartRoute }
          ]
        )
      }
    } catch (error: any) {
      Vibration.vibrate([0, 100, 50, 100]) // Vibración de error (corta)
      
      const serverMessage = error.response?.data?.error || ''
      
      // Detectar tipo de error y mostrar mensaje amigable
      let title = 'Error'
      let message = 'No se pudo escanear el paquete'
      
      if (serverMessage.includes('no pertenece a esta ruta')) {
        title = 'Paquete incorrecto'
        message = `El código "${trackingCode}" no está en tu ruta de hoy.\n\nVerifica que estés escaneando un paquete asignado a ti.`
      } else if (serverMessage.includes('no está en estado ASSIGNED')) {
        title = 'Ya escaneado'
        message = 'Este paquete ya fue escaneado anteriormente.'
      } else if (serverMessage) {
        message = serverMessage
      }
      
      Alert.alert(title, message)
    } finally {
      setScanning(false)
      // Allow processing again after a short delay
      setTimeout(() => {
        isProcessingRef.current = false
      }, 500)
    }
  }

  const handleStartRoute = async () => {
    if (!route) return
    try {
      await routesApi.startRoute(route.id)
      Alert.alert('¡Ruta iniciada!', 'Los clientes han sido notificados.')
      onStartRoute()
    } catch (error: any) {
      const message = error.response?.data?.error || 'No se pudo iniciar la ruta'
      Alert.alert('Error', message)
    }
  }

  const getStopStatus = (stop: Stop): 'scanned' | 'pending' => {
    return stop.order.status === 'PICKED_UP' || 
           stop.order.status === 'IN_TRANSIT' || 
           stop.order.status === 'DELIVERED'
      ? 'scanned'
      : 'pending'
  }

  return {
    route,
    pickupStatus,
    loading,
    scanning,
    handleScan,
    handleStartRoute,
    getStopStatus,
    onBack,
  }
}
