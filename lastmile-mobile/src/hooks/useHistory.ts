import { useCallback, useEffect, useState } from 'react'
import { Alert } from 'react-native'
import { routesApi } from '../api/routesApi'
import { useAuth } from '../context/AuthContext'
import type { HistoryStats, Route } from '../types'

export function useHistory(onBack: () => void) {
  const { user } = useAuth()
  const [routes, setRoutes] = useState<Route[]>([])
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  const fetchHistory = useCallback(async () => {
    if (!user?.courierId) return
    try {
      const data = await routesApi.getHistory(user.courierId)
      setRoutes(data ?? [])
    } catch {
      Alert.alert('Error', 'No se pudo cargar el historial')
      setRoutes([])
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }, [user?.courierId])

  useEffect(() => {
    fetchHistory()
  }, [fetchHistory])

  const onRefresh = () => {
    setRefreshing(true)
    fetchHistory()
  }

  const stats: HistoryStats = routes.reduce(
    (acc, route) => ({
      totalRoutes: acc.totalRoutes + 1,
      totalDelivered: acc.totalDelivered + route.deliveredCount,
      totalFailed: acc.totalFailed + (route.failedCount ?? 0),
      successRate: 0,
    }),
    { totalRoutes: 0, totalDelivered: 0, totalFailed: 0, successRate: 0 }
  )

  const total = stats.totalDelivered + stats.totalFailed
  stats.successRate = total > 0 ? Math.round((stats.totalDelivered / total) * 100) : 0

  return {
    routes,
    loading,
    refreshing,
    onRefresh,
    onBack,
    stats,
  }
}
