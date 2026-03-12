import dayjs from 'dayjs'
import 'dayjs/locale/es'
import { useEffect, useState } from 'react'
import { routesApi } from '../api/routesApi'
import { useAuth } from '../context/AuthContext'
import type { Route, Stop } from '../types'

dayjs.locale('es')

export function useRoute(onSelectStop: (stop: Stop, routeId: string) => void) {
  const { user, logout } = useAuth()
  const [route, setRoute] = useState<Route | null>(null)
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  const fetchRoute = async () => {
    try {
      const data = await routesApi.getMiRuta(user!.courierId!)
      setRoute(data)
    } catch {
      setRoute(null)
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }

  useEffect(() => {
    fetchRoute()
  }, [])

  const onRefresh = () => {
    setRefreshing(true)
    fetchRoute()
  }

  const handleSelectStop = (stop: Stop) => {
    if (stop.status === 'PENDING') {
      onSelectStop(stop, route!.id)
    }
  }

  const statusConfig: Record<string, { color: string; label: string; bg: string }> = {
    PENDING:   { color: '#ff9500', label: 'Pendiente', bg: '#fff3e0' },
    DELIVERED: { color: '#34c759', label: 'Entregado', bg: '#e8f8ed' },
    FAILED:    { color: '#ff3b30', label: 'Fallido',   bg: '#ffeeed' },
  }

  const pendingCount   = route?.stops.filter(s => s.status === 'PENDING').length ?? 0
  const deliveredCount = route?.deliveredCount ?? 0
  const progress       = route ? Math.round(route.completionPercentage) : 0
  const greeting       = user?.username ?? ''
  const dateLabel      = dayjs().format('dddd D [de] MMMM')

  return {
    route,
    loading,
    refreshing,
    onRefresh,
    handleSelectStop,
    statusConfig,
    pendingCount,
    deliveredCount,
    progress,
    greeting,
    dateLabel,
    logout,
  }
}