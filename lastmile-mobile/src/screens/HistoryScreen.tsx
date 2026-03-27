import dayjs from 'dayjs'
import 'dayjs/locale/es'
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
import { useTheme } from '../context/ThemeContext'
import { useHistory } from '../hooks/useHistory'
import type { Route, Stop } from '../types'

dayjs.locale('es')

interface Props {
  onBack: () => void
}

export default function HistoryScreen({ onBack }: Props) {
  const { colors } = useTheme()
  const { routes, loading, refreshing, onRefresh, stats } = useHistory(onBack)

  const statusConfig: Record<string, { color: string; label: string; bg: string }> = {
    COMPLETED: { color: colors.success, label: 'Completada', bg: colors.successLight },
    IN_PROGRESS: { color: colors.primary, label: 'En progreso', bg: colors.primaryLight },
    PENDING: { color: colors.warning, label: 'Pendiente', bg: colors.warningLight },
    CONFIRMED: { color: colors.purple, label: 'Confirmada', bg: colors.purpleLight },
    CLOSED: { color: colors.textSecondary, label: 'Cerrada', bg: colors.border },
  }

  const renderRoute = ({ item }: { item: Route }) => {
    const config = statusConfig[item.status] ?? statusConfig.COMPLETED
    const dateFormatted = dayjs(item.date).format('dddd D MMM')
    const delivered = item.deliveredCount
    const failed = item.failedCount ?? 0
    const total = item.totalStops

    return (
      <View style={[styles.routeCard, { backgroundColor: colors.card }]}>
        <View style={styles.routeHeader}>
          <View>
            <Text style={[styles.routeDate, { color: colors.text }]}>{dateFormatted}</Text>
            <Text style={[styles.routeStops, { color: colors.textSecondary }]}>{total} paradas</Text>
          </View>
          <View style={[styles.badge, { backgroundColor: config.bg }]}>
            <Text style={[styles.badgeText, { color: config.color }]}>{config.label}</Text>
          </View>
        </View>

        <View style={[styles.routeStats, { backgroundColor: colors.cardSecondary }]}>
          <View style={styles.routeStat}>
            <Text style={[styles.routeStatValue, { color: colors.success }]}>{delivered}</Text>
            <Text style={[styles.routeStatLabel, { color: colors.textSecondary }]}>Entregados</Text>
          </View>
          <View style={[styles.routeStatDivider, { backgroundColor: colors.separator }]} />
          <View style={styles.routeStat}>
            <Text style={[styles.routeStatValue, { color: colors.error }]}>{failed}</Text>
            <Text style={[styles.routeStatLabel, { color: colors.textSecondary }]}>Fallidos</Text>
          </View>
          <View style={[styles.routeStatDivider, { backgroundColor: colors.separator }]} />
          <View style={styles.routeStat}>
            <Text style={[styles.routeStatValue, { color: colors.primary }]}>
              {Math.round(item.completionPercentage)}%
            </Text>
            <Text style={[styles.routeStatLabel, { color: colors.textSecondary }]}>Completado</Text>
          </View>
        </View>

        {/* Mini lista de stops */}
        {item.stops.slice(0, 3).map((stop: Stop) => (
          <View key={stop.id} style={[styles.stopMini, { borderTopColor: colors.separator }]}>
            <View
              style={[
                styles.stopMiniDot,
                {
                  backgroundColor:
                    stop.status === 'DELIVERED'
                      ? colors.success
                      : stop.status === 'FAILED'
                      ? colors.error
                      : colors.warning,
                },
              ]}
            />
            <Text style={[styles.stopMiniText, { color: colors.text }]} numberOfLines={1}>
              {stop.order.recipientName}
            </Text>
            <Text style={[styles.stopMiniTracking, { color: colors.textMuted }]}>{stop.order.trackingCode}</Text>
          </View>
        ))}
        {item.stops.length > 3 && (
          <Text style={[styles.moreStops, { color: colors.textSecondary }]}>+{item.stops.length - 3} más</Text>
        )}
      </View>
    )
  }

  if (loading) {
    return (
      <View style={[styles.center, { backgroundColor: colors.background }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    )
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]}>
      <StatusBar barStyle={colors.statusBar} />

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={onBack} style={styles.backBtn}>
          <Text style={[styles.backBtnText, { color: colors.primary }]}>← Volver</Text>
        </TouchableOpacity>
        <Text style={[styles.title, { color: colors.text }]}>Historial</Text>
        <View style={{ width: 70 }} />
      </View>

      <FlatList
        data={routes}
        keyExtractor={(item) => item.id}
        renderItem={renderRoute}
        contentContainerStyle={styles.listContent}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.primary} />
        }
        ListHeaderComponent={
          <View style={[styles.statsCard, { backgroundColor: colors.card }]}>
            <Text style={[styles.statsTitle, { color: colors.text }]}>Resumen</Text>
            <View style={styles.statsRow}>
              <View style={styles.statItem}>
                <Text style={[styles.statValue, { color: colors.text }]}>{stats.totalRoutes}</Text>
                <Text style={[styles.statLabel, { color: colors.textSecondary }]}>Rutas</Text>
              </View>
              <View style={[styles.statDivider, { backgroundColor: colors.separator }]} />
              <View style={styles.statItem}>
                <Text style={[styles.statValue, { color: colors.success }]}>{stats.totalDelivered}</Text>
                <Text style={[styles.statLabel, { color: colors.textSecondary }]}>Entregados</Text>
              </View>
              <View style={[styles.statDivider, { backgroundColor: colors.separator }]} />
              <View style={styles.statItem}>
                <Text style={[styles.statValue, { color: colors.error }]}>{stats.totalFailed}</Text>
                <Text style={[styles.statLabel, { color: colors.textSecondary }]}>Fallidos</Text>
              </View>
              <View style={[styles.statDivider, { backgroundColor: colors.separator }]} />
              <View style={styles.statItem}>
                <Text style={[styles.statValue, { color: colors.primary }]}>{stats.successRate}%</Text>
                <Text style={[styles.statLabel, { color: colors.textSecondary }]}>Éxito</Text>
              </View>
            </View>
          </View>
        }
        ListEmptyComponent={
          <View style={styles.empty}>
            <Text style={styles.emptyIcon}>📭</Text>
            <Text style={[styles.emptyTitle, { color: colors.text }]}>Sin historial</Text>
            <Text style={[styles.emptySubtitle, { color: colors.textSecondary }]}>
              Aquí aparecerán tus rutas completadas
            </Text>
          </View>
        }
      />
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingTop: 8,
    paddingBottom: 16,
  },
  backBtn: { paddingVertical: 8 },
  backBtnText: { fontSize: 16, fontWeight: '500' },
  title: { fontSize: 18, fontWeight: '700' },
  listContent: { paddingHorizontal: 16, paddingBottom: 32 },

  // Stats card
  statsCard: {
    borderRadius: 16,
    padding: 20,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 10,
    elevation: 2,
  },
  statsTitle: { fontSize: 15, fontWeight: '600', marginBottom: 16 },
  statsRow: { flexDirection: 'row', justifyContent: 'space-around' },
  statItem: { alignItems: 'center', flex: 1 },
  statValue: { fontSize: 22, fontWeight: '700' },
  statLabel: { fontSize: 12, marginTop: 2 },
  statDivider: { width: StyleSheet.hairlineWidth },

  // Route card
  routeCard: {
    borderRadius: 14,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 6,
    elevation: 1,
  },
  routeHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 12,
  },
  routeDate: {
    fontSize: 16,
    fontWeight: '600',
    textTransform: 'capitalize',
  },
  routeStops: { fontSize: 13, marginTop: 2 },
  badge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 20 },
  badgeText: { fontSize: 12, fontWeight: '600' },

  routeStats: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    borderRadius: 10,
    paddingVertical: 12,
    marginBottom: 12,
  },
  routeStat: { alignItems: 'center', flex: 1 },
  routeStatValue: { fontSize: 18, fontWeight: '700' },
  routeStatLabel: { fontSize: 11, marginTop: 2 },
  routeStatDivider: { width: StyleSheet.hairlineWidth },

  // Mini stops
  stopMini: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 6,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
  stopMiniDot: { width: 8, height: 8, borderRadius: 4, marginRight: 10 },
  stopMiniText: { flex: 1, fontSize: 14 },
  stopMiniTracking: {
    fontSize: 11,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
  },
  moreStops: { fontSize: 12, marginTop: 8, textAlign: 'center' },

  // Empty
  empty: { alignItems: 'center', paddingVertical: 60 },
  emptyIcon: { fontSize: 52, marginBottom: 16 },
  emptyTitle: { fontSize: 20, fontWeight: '700' },
  emptySubtitle: { fontSize: 15, marginTop: 6, textAlign: 'center' },
})
