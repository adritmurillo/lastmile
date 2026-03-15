import dayjs from 'dayjs'
import 'dayjs/locale/es'
import { useEffect, useState } from 'react'
import { Alert, Linking } from 'react-native'
import { routesApi } from '../api/routesApi'
import { useAuth } from '../context/AuthContext'
import type { Route, Stop } from '../types'

dayjs.locale('es')

export function useRoute(onSelectStop: (stop: Stop, routeId: string) => void) {
  const { user, logout } = useAuth()
  const [route, setRoute] = useState<Route | null>(null)
  const [pendingStops, setPendingStops] = useState<Stop[]>([])
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [startingRoute, setStartingRoute] = useState(false)

  const fetchRoute = async () => {
    try {
      const data = await routesApi.getMiRuta(user!.courierId!)
      setRoute(data)
    } catch {
      setRoute(null)
    }
  }

  const fetchPendingStops = async () => {
    try {
      const data = await routesApi.getPendingStops(user!.courierId!)
      setPendingStops(data ?? [])
    } catch {
      setPendingStops([])
    }
  }

  const fetchAll = async () => {
    await Promise.all([fetchRoute(), fetchPendingStops()])
    setLoading(false)
    setRefreshing(false)
  }

  useEffect(() => {
    fetchAll()
  }, [])

  const onRefresh = () => {
    setRefreshing(true)
    fetchAll()
  }

  const handleSelectStop = (stop: Stop) => {
    if (stop.status === 'PENDING') {
      onSelectStop(stop, route!.id)
    }
  }

  const openMapsWithAllStops = (stops: Stop[]) => {
    const pendingStops = stops.filter(s => s.status === 'PENDING')
    if (pendingStops.length === 0) return

    const stopsWithCoords = pendingStops.filter(s => s.order.latitude && s.order.longitude)
    const stopsWithAddress = pendingStops.filter(s => !s.order.latitude || !s.order.longitude)

    let mapsUrl: string

    if (stopsWithCoords.length === pendingStops.length) {
      // Todas tienen coordenadas — usar coords
      const last = pendingStops[pendingStops.length - 1]
      const destination = `${last.order.latitude},${last.order.longitude}`
      const waypoints = pendingStops
        .slice(0, -1)
        .map(s => `${s.order.latitude},${s.order.longitude}`)
        .join('|')

      mapsUrl = waypoints
        ? `https://www.google.com/maps/dir/?api=1&destination=${destination}&waypoints=${waypoints}&travelmode=driving`
        : `https://www.google.com/maps/dir/?api=1&destination=${destination}&travelmode=driving`
    } else {
      // Usar texto de dirección sin encodear waypoints con pipe
      const last = pendingStops[pendingStops.length - 1]
      const destination = encodeURIComponent(last.order.addressText + ', Lima, Peru')
      const waypoints = pendingStops
        .slice(0, -1)
        .map(s => encodeURIComponent(s.order.addressText + ', Lima, Peru'))
        .join('|')

      mapsUrl = waypoints
        ? `https://www.google.com/maps/dir/?api=1&destination=${destination}&waypoints=${waypoints}&travelmode=driving`
        : `https://www.google.com/maps/dir/?api=1&destination=${destination}&travelmode=driving`
    }

    Alert.alert(
      'Abrir ruta con...',
      'Selecciona tu app de navegación',
      [
        {
          text: '📍 Google Maps',
          onPress: () => Linking.openURL(mapsUrl)
        },
        {
          text: '🚗 Waze',
          onPress: () => {
            const last = pendingStops[pendingStops.length - 1]
            const wazeUrl = last.order.latitude && last.order.longitude
              ? `waze://?ll=${last.order.latitude},${last.order.longitude}&navigate=yes`
              : `waze://?q=${encodeURIComponent(last.order.addressText + ', Lima, Peru')}&navigate=yes`
            Linking.canOpenURL(wazeUrl).then(can => {
              Linking.openURL(can ? wazeUrl : mapsUrl)
            })
          }
        },
        { text: 'Cancelar', style: 'cancel' }
      ]
    )
  }

  const handleStartRoute = () => {
    if (!route) return

    Alert.alert(
      'Iniciar ruta',
      '¿Estás listo para salir? Se notificará a los destinatarios que su pedido está en camino.',
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Iniciar',
          onPress: async () => {
            setStartingRoute(true)
            try {
              await routesApi.startRoute(route.id)
              await fetchRoute()
              Alert.alert(
                '¡Ruta iniciada!',
                '¿Deseas abrir el mapa con todas las paradas?',
                [
                  { text: 'Ahora no', style: 'cancel' },
                  {
                    text: 'Abrir mapa',
                    onPress: () => openMapsWithAllStops(route.stops)
                  }
                ]
              )
            } catch {
              Alert.alert('Error', 'No se pudo iniciar la ruta')
            } finally {
              setStartingRoute(false)
            }
          }
        }
      ]
    )
  }

  const statusConfig: Record<string, { color: string; label: string; bg: string }> = {
    PENDING: { color: '#ff9500', label: 'Pendiente', bg: '#fff3e0' },
    DELIVERED: { color: '#34c759', label: 'Entregado', bg: '#e8f8ed' },
    FAILED: { color: '#ff3b30', label: 'Fallido', bg: '#ffeeed' },
  }

  const pendingCount = route?.stops.filter(s => s.status === 'PENDING').length ?? 0
  const deliveredCount = route?.deliveredCount ?? 0
  const progress = route ? Math.round(route.completionPercentage) : 0
  const greeting = user?.username ?? ''
  const dateLabel = dayjs().format('dddd D [de] MMMM')
  const routeStarted = route?.status === 'IN_PROGRESS'
  const routeCompleted = route?.status === 'COMPLETED'


  return {
    route,
    pendingStops,
    loading,
    refreshing,
    startingRoute,
    routeStarted,
    routeCompleted,
    onRefresh,
    handleSelectStop,
    handleStartRoute,
    openMapsWithAllStops,
    statusConfig,
    pendingCount,
    deliveredCount,
    progress,
    greeting,
    dateLabel,
    logout,
    
  }
}