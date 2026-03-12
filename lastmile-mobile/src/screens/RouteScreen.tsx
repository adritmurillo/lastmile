import dayjs from 'dayjs'
import 'dayjs/locale/es'
import { useEffect, useState } from 'react'
import {
    ActivityIndicator,
    FlatList,
    Platform,
    RefreshControl,
    SafeAreaView,
    StatusBar,
    StyleSheet,
    Text,
    TouchableOpacity,
    View,
} from 'react-native'
import { routesApi } from '../api/routesApi'
import { useAuth } from '../context/AuthContext'
import type { Route, Stop } from '../types'

dayjs.locale('es')

interface Props {
  onSelectStop: (stop: Stop, routeId: string) => void
}

export default function RouteScreen({ onSelectStop }: Props) {
  const { user, logout } = useAuth()
  const [route, setRoute] = useState<Route | null>(null)
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  const today = dayjs().format('YYYY-MM-DD')

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

  const statusConfig: Record<string, { color: string; label: string; bg: string }> = {
    PENDING:   { color: '#ff9500', label: 'Pendiente',  bg: '#fff3e0' },
    DELIVERED: { color: '#34c759', label: 'Entregado',  bg: '#e8f8ed' },
    FAILED:    { color: '#ff3b30', label: 'Fallido',    bg: '#ffeeed' },
  }

  const pendingCount = route?.stops.filter(s => s.status === 'PENDING').length ?? 0
  const deliveredCount = route?.deliveredCount ?? 0
  const progress = route ? Math.round(route.completionPercentage) : 0

  const renderStop = ({ item, index }: { item: Stop; index: number }) => {
    const config = statusConfig[item.status]
    const isPending = item.status === 'PENDING'

    return (
      <TouchableOpacity
        style={[styles.stopCard, !isPending && styles.stopCardDone]}
        onPress={() => isPending && onSelectStop(item, route!.id)}
        activeOpacity={isPending ? 0.7 : 1}
      >
        <View style={[styles.stopIndex, { backgroundColor: config.bg }]}>
          <Text style={[styles.stopIndexText, { color: config.color }]}>
            {item.stopOrder}
          </Text>
        </View>

        <View style={styles.stopContent}>
          <Text style={[styles.stopName, !isPending && styles.textMuted]}>
            {item.order.recipientName}
          </Text>
          <Text style={styles.stopAddress} numberOfLines={1}>
            {item.order.addressText}
          </Text>
          <Text style={styles.stopTracking}>{item.order.trackingCode}</Text>
        </View>

        <View style={[styles.badge, { backgroundColor: config.bg }]}>
          <Text style={[styles.badgeText, { color: config.color }]}>
            {config.label}
          </Text>
        </View>
      </TouchableOpacity>
    )
  }

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#007aff" />
      </View>
    )
  }

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" />

      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>Hola, {user?.username} 👋</Text>
          <Text style={styles.date}>{dayjs().format('dddd D [de] MMMM')}</Text>
        </View>
        <TouchableOpacity onPress={logout} style={styles.logoutBtn}>
          <Text style={styles.logoutText}>Salir</Text>
        </TouchableOpacity>
      </View>

      {route ? (
        <FlatList
          data={route.stops}
          keyExtractor={(item) => item.id}
          renderItem={renderStop}
          contentContainerStyle={styles.listContent}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={() => { setRefreshing(true); fetchRoute() }}
              tintColor="#007aff"
            />
          }
          ListHeaderComponent={
            <>
              {/* Progress Card */}
              <View style={styles.progressCard}>
                <View style={styles.progressRow}>
                  <Text style={styles.progressTitle}>Progreso del día</Text>
                  <Text style={styles.progressPercent}>{progress}%</Text>
                </View>
                <View style={styles.progressBar}>
                  <View style={[styles.progressFill, { width: `${progress}%` }]} />
                </View>
                <View style={styles.statsRow}>
                  <View style={styles.statItem}>
                    <Text style={styles.statValue}>{route.totalStops}</Text>
                    <Text style={styles.statLabel}>Total</Text>
                  </View>
                  <View style={styles.statDivider} />
                  <View style={styles.statItem}>
                    <Text style={[styles.statValue, { color: '#34c759' }]}>{deliveredCount}</Text>
                    <Text style={styles.statLabel}>Entregados</Text>
                  </View>
                  <View style={styles.statDivider} />
                  <View style={styles.statItem}>
                    <Text style={[styles.statValue, { color: '#ff9500' }]}>{pendingCount}</Text>
                    <Text style={styles.statLabel}>Pendientes</Text>
                  </View>
                </View>
              </View>

              <Text style={styles.sectionTitle}>Paradas</Text>
            </>
          }
        />
      ) : (
        <View style={styles.center}>
          <Text style={styles.emptyIcon}>📦</Text>
          <Text style={styles.emptyTitle}>Sin ruta para hoy</Text>
          <Text style={styles.emptySubtitle}>No tienes entregas asignadas</Text>
        </View>
      )}
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f2f2f7',
  },
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f2f2f7',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 8,
    paddingBottom: 16,
  },
  greeting: {
    fontSize: 22,
    fontWeight: '700',
    color: '#1c1c1e',
    letterSpacing: -0.5,
  },
  date: {
    fontSize: 14,
    color: '#8e8e93',
    marginTop: 2,
    textTransform: 'capitalize',
  },
  logoutBtn: {
    paddingHorizontal: 14,
    paddingVertical: 7,
    backgroundColor: '#ffeeed',
    borderRadius: 20,
  },
  logoutText: {
    color: '#ff3b30',
    fontWeight: '600',
    fontSize: 14,
  },
  listContent: {
    paddingHorizontal: 16,
    paddingBottom: 32,
  },
  progressCard: {
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 20,
    marginBottom: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 10,
    elevation: 2,
  },
  progressRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  progressTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: '#1c1c1e',
  },
  progressPercent: {
    fontSize: 15,
    fontWeight: '700',
    color: '#007aff',
  },
  progressBar: {
    height: 6,
    backgroundColor: '#e5e5ea',
    borderRadius: 3,
    marginBottom: 16,
    overflow: 'hidden',
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#007aff',
    borderRadius: 3,
  },
  statsRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  statItem: {
    alignItems: 'center',
    flex: 1,
  },
  statValue: {
    fontSize: 20,
    fontWeight: '700',
    color: '#1c1c1e',
  },
  statLabel: {
    fontSize: 12,
    color: '#8e8e93',
    marginTop: 2,
  },
  statDivider: {
    width: StyleSheet.hairlineWidth,
    backgroundColor: '#e5e5ea',
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#1c1c1e',
    marginBottom: 12,
    letterSpacing: -0.3,
  },
  stopCard: {
    backgroundColor: '#fff',
    borderRadius: 14,
    padding: 14,
    marginBottom: 8,
    flexDirection: 'row',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 6,
    elevation: 1,
  },
  stopCardDone: {
    opacity: 0.6,
  },
  stopIndex: {
    width: 36,
    height: 36,
    borderRadius: 18,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  stopIndexText: {
    fontWeight: '700',
    fontSize: 15,
  },
  stopContent: {
    flex: 1,
  },
  stopName: {
    fontSize: 15,
    fontWeight: '600',
    color: '#1c1c1e',
  },
  textMuted: {
    color: '#8e8e93',
  },
  stopAddress: {
    fontSize: 13,
    color: '#8e8e93',
    marginTop: 2,
  },
  stopTracking: {
    fontSize: 11,
    color: '#c7c7cc',
    marginTop: 2,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
  },
  badge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 20,
    marginLeft: 8,
  },
  badgeText: {
    fontSize: 12,
    fontWeight: '600',
  },
  emptyIcon: {
    fontSize: 52,
    marginBottom: 16,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#1c1c1e',
  },
  emptySubtitle: {
    fontSize: 15,
    color: '#8e8e93',
    marginTop: 6,
  },
})