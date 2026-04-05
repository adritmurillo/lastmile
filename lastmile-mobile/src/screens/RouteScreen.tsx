import { useEffect } from 'react'
import {
  ActivityIndicator, FlatList, Platform, RefreshControl,
  SafeAreaView, StatusBar, StyleSheet, Text, TouchableOpacity, View,
} from 'react-native'
import RequestCloseModal from '../components/RequestCloseModal'
import { useNotifications } from '../context/NotificationContext'
import { useTheme } from '../context/ThemeContext'
import { useRoute } from '../hooks/useRoute'
import { useRouteCloseRequest } from '../hooks/useRouteCloseRequest'
import type { Stop } from '../types'
import { ROUTE_CLOSE_REASONS } from '../types'

interface Props {
  onSelectStop: (stop: Stop, routeId: string) => void
  onProfile: () => void
  onHistory: () => void
  onPickup: () => void
}

export default function RouteScreen({ onSelectStop, onProfile, onHistory, onPickup }: Props) {
  const { colors } = useTheme()
  const {
    route, pendingStops, loading, refreshing, startingRoute, routeStarted,
    routeCompleted, onRefresh, handleSelectStop, handleStartRoute, openMapsWithAllStops,
    statusConfig, pendingCount, deliveredCount, progress,
    greeting, dateLabel, logout,
  } = useRoute(onSelectStop, onProfile)

  const closeRequest = useRouteCloseRequest(route?.id, onRefresh)
  const { connected, setOnCloseRequestApproved, setOnCloseRequestRejected } = useNotifications()

  // Register callbacks for WebSocket notifications
  useEffect(() => {
    setOnCloseRequestApproved(() => {
      onRefresh()
    })

    setOnCloseRequestRejected(() => {
      closeRequest.checkPendingRequest()
    })

    return () => {
      setOnCloseRequestApproved(null)
      setOnCloseRequestRejected(null)
    }
  }, [setOnCloseRequestApproved, setOnCloseRequestRejected, onRefresh, closeRequest.checkPendingRequest])

  // Check for pending close request when route loads
  useEffect(() => {
    if (route?.id && route.status === 'IN_PROGRESS') {
      closeRequest.checkPendingRequest()
    }
  }, [route?.id, route?.status])

  const renderStop = ({ item }: { item: Stop }) => {
    const config = statusConfig[item.status]
    const isPending = item.status === 'PENDING'

    return (
      <TouchableOpacity
        style={[styles.stopCard, { backgroundColor: colors.card }, !isPending && styles.stopCardDone]}
        onPress={() => handleSelectStop(item)}
        activeOpacity={isPending ? 0.7 : 1}
      >
        <View style={[styles.stopIndex, { backgroundColor: config.bg }]}>
          <Text style={[styles.stopIndexText, { color: config.color }]}>{item.stopOrder}</Text>
        </View>
        <View style={styles.stopContent}>
          <Text style={[styles.stopName, { color: colors.text }, !isPending && { color: colors.textSecondary }]}>
            {item.order.recipientName}
          </Text>
          <Text style={[styles.stopAddress, { color: colors.textSecondary }]} numberOfLines={1}>
            {item.order.addressText}
          </Text>
          <Text style={[styles.stopTracking, { color: colors.textMuted }]}>{item.order.trackingCode}</Text>
        </View>
        <View style={[styles.badge, { backgroundColor: config.bg }]}>
          <Text style={[styles.badgeText, { color: config.color }]}>{config.label}</Text>
        </View>
      </TouchableOpacity>
    )
  }

  const renderPendingStop = ({ item }: { item: Stop }) => (
    <View style={[styles.overdueCard, { backgroundColor: colors.warningLight, borderColor: colors.warning }]}>
      <View style={styles.overdueLeft}>
        <View style={styles.overdueIcon}>
          <Text style={styles.overdueIconText}>⚠️</Text>
        </View>
        <View style={styles.stopContent}>
          <Text style={[styles.stopName, { color: colors.text }]}>{item.order.recipientName}</Text>
          <Text style={[styles.stopAddress, { color: colors.textSecondary }]} numberOfLines={1}>
            {item.order.addressText}
          </Text>
          <Text style={[styles.stopTracking, { color: colors.textMuted }]}>{item.order.trackingCode}</Text>
        </View>
      </View>
      <View style={[styles.badge, { backgroundColor: colors.warningLight }]}>
        <Text style={[styles.badgeText, { color: colors.warning }]}>Pendiente</Text>
      </View>
    </View>
  )

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

      <View style={styles.header}>
        <View>
          <View style={styles.greetingRow}>
            <Text style={[styles.greeting, { color: colors.text }]}>Hola, {greeting} 👋</Text>
            <View style={[styles.connectionDot, { backgroundColor: connected ? colors.success : colors.error }]} />
          </View>
          <Text style={[styles.date, { color: colors.textSecondary }]}>{dateLabel}</Text>
        </View>
        <View style={{ flexDirection: 'row', gap: 8 }}>
          <TouchableOpacity onPress={onHistory} style={[styles.historyBtn, { backgroundColor: colors.primaryLight }]}>
            <Text style={styles.historyBtnText}>📋</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={onProfile} style={[styles.profileBtn, { backgroundColor: colors.cardSecondary }]}>
            <Text style={styles.profileBtnText}>👤</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={logout} style={[styles.logoutBtn, { backgroundColor: colors.errorLight }]}>
            <Text style={[styles.logoutText, { color: colors.error }]}>Salir</Text>
          </TouchableOpacity>
        </View>
      </View>

      <FlatList
        data={route?.stops ?? []}
        keyExtractor={(item) => item.id}
        renderItem={renderStop}
        contentContainerStyle={styles.listContent}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.primary} />
        }
        ListHeaderComponent={
          <>
            {/* Pendientes de días anteriores */}
            {pendingStops.length > 0 && (
              <>
                <View style={styles.overdueHeader}>
                  <Text style={[styles.overdueTitle, { color: colors.warning }]}>⚠️ Entregas pendientes</Text>
                  <View style={[styles.overdueBadge, { backgroundColor: colors.warning }]}>
                    <Text style={styles.overdueBadgeText}>{pendingStops.length}</Text>
                  </View>
                </View>
                <Text style={[styles.overdueSubtitle, { color: colors.textSecondary }]}>
                  Tienes entregas sin completar de días anteriores. El despachador las incluirá en tu próxima ruta.
                </Text>
                {pendingStops.map(stop => (
                  <View key={stop.id}>
                    {renderPendingStop({ item: stop })}
                  </View>
                ))}
                <View style={[styles.divider, { backgroundColor: colors.separator }]} />
              </>
            )}

            {/* Ruta del día */}
            {route && (
              <>
                <View style={[styles.progressCard, { backgroundColor: colors.card }]}>
                  <View style={styles.progressRow}>
                    <Text style={[styles.progressTitle, { color: colors.text }]}>Progreso del día</Text>
                    <Text style={[styles.progressPercent, { color: colors.primary }]}>{progress}%</Text>
                  </View>
                  <View style={[styles.progressBar, { backgroundColor: colors.border }]}>
                    <View style={[styles.progressFill, { width: `${progress}%`, backgroundColor: colors.primary }]} />
                  </View>
                  <View style={styles.statsRow}>
                    <View style={styles.statItem}>
                      <Text style={[styles.statValue, { color: colors.text }]}>{route.totalStops}</Text>
                      <Text style={[styles.statLabel, { color: colors.textSecondary }]}>Total</Text>
                    </View>
                    <View style={[styles.statDivider, { backgroundColor: colors.separator }]} />
                    <View style={styles.statItem}>
                      <Text style={[styles.statValue, { color: colors.success }]}>{deliveredCount}</Text>
                      <Text style={[styles.statLabel, { color: colors.textSecondary }]}>Entregados</Text>
                    </View>
                    <View style={[styles.statDivider, { backgroundColor: colors.separator }]} />
                    <View style={styles.statItem}>
                      <Text style={[styles.statValue, { color: colors.warning }]}>{pendingCount}</Text>
                      <Text style={[styles.statLabel, { color: colors.textSecondary }]}>Pendientes</Text>
                    </View>
                  </View>

                  {/* Botones de acción de ruta */}
                  <View style={styles.routeActions}>
                    {routeCompleted ? (
                      <View style={[styles.completedBanner, { backgroundColor: colors.successLight, borderColor: colors.success }]}>
                        <Text style={[styles.completedBannerText, { color: colors.success }]}>
                          Ruta completada! Buen trabajo.
                        </Text>
                        <Text style={[styles.completedBannerSub, { color: colors.textSecondary }]}>
                          Espera a que el despachador genere nuevas rutas.
                        </Text>
                      </View>
                    ) : !routeStarted ? (
                      <>
                        <TouchableOpacity
                          style={[styles.pickupBtn, { backgroundColor: colors.warning }]}
                          onPress={onPickup}
                          activeOpacity={0.85}
                        >
                          <Text style={styles.pickupBtnText}>Recoger paquetes</Text>
                        </TouchableOpacity>
                        <TouchableOpacity
                          style={[styles.startRouteBtn, { backgroundColor: colors.primary }]}
                          onPress={handleStartRoute}
                          disabled={startingRoute}
                          activeOpacity={0.85}
                        >
                          {startingRoute
                            ? <ActivityIndicator color="#fff" size="small" />
                            : <Text style={styles.startRouteBtnText}>Iniciar ruta</Text>
                          }
                        </TouchableOpacity>
                      </>
                    ) : (
                      <>
                        <TouchableOpacity
                          style={[styles.mapsRouteBtn, { backgroundColor: colors.cardSecondary, borderColor: colors.border }]}
                          onPress={() => openMapsWithAllStops(route.stops)}
                          activeOpacity={0.85}
                        >
                          <Text style={[styles.mapsRouteBtnText, { color: colors.text }]}>Ver ruta completa</Text>
                        </TouchableOpacity>

                        {/* Pending close request banner */}
                        {closeRequest.pendingRequest ? (
                          <View style={[styles.pendingRequestBanner, { backgroundColor: colors.warningLight, borderColor: colors.warning }]}>
                            <Text style={[styles.pendingRequestIcon, { backgroundColor: colors.warning }]}>!</Text>
                            <View style={styles.pendingRequestContent}>
                              <Text style={[styles.pendingRequestTitle, { color: colors.warning }]}>Solicitud de cierre pendiente</Text>
                              <Text style={[styles.pendingRequestText, { color: colors.warning }]}>
                                {ROUTE_CLOSE_REASONS.find(r => r.key === closeRequest.pendingRequest?.reason)?.label}
                              </Text>
                            </View>
                          </View>
                        ) : (
                          <TouchableOpacity
                            style={[styles.closeRequestBtn, { backgroundColor: colors.errorLight, borderColor: colors.error }]}
                            onPress={closeRequest.openModal}
                            activeOpacity={0.85}
                          >
                            <Text style={[styles.closeRequestBtnText, { color: colors.error }]}>Solicitar cierre de ruta</Text>
                          </TouchableOpacity>
                        )}
                      </>
                    )}
                  </View>
                </View>

                <Text style={[styles.sectionTitle, { color: colors.text }]}>Paradas</Text>
              </>
            )}

            {!route && pendingStops.length === 0 && (
              <View style={styles.emptyContainer}>
                <Text style={styles.emptyIcon}>📦</Text>
                <Text style={[styles.emptyTitle, { color: colors.text }]}>Sin ruta para hoy</Text>
                <Text style={[styles.emptySubtitle, { color: colors.textSecondary }]}>No tienes entregas asignadas</Text>
              </View>
            )}

            {!route && pendingStops.length > 0 && (
              <View style={[styles.noPendingRoute, { backgroundColor: colors.warningLight, borderColor: colors.warning }]}>
                <Text style={[styles.noPendingRouteText, { color: colors.warning }]}>
                  No tienes ruta asignada para hoy. Las entregas pendientes serán incluidas automáticamente.
                </Text>
              </View>
            )}
          </>
        }
      />

      {/* Request Close Modal */}
      <RequestCloseModal
        visible={closeRequest.modalVisible}
        loading={closeRequest.loading}
        uploadingPhoto={closeRequest.uploadingPhoto}
        selectedReason={closeRequest.selectedReason}
        onSelectReason={closeRequest.setSelectedReason}
        message={closeRequest.message}
        onChangeMessage={closeRequest.setMessage}
        photoUri={closeRequest.photoUri}
        onTakePhoto={closeRequest.handleTakePhoto}
        onRemovePhoto={closeRequest.handleRemovePhoto}
        onSubmit={closeRequest.handleSubmit}
        onClose={closeRequest.closeModal}
      />
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 32 },
  header: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    paddingHorizontal: 20, paddingTop: 8, paddingBottom: 16,
  },
  greetingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  connectionDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  profileBtn: {
    width: 36, height: 36,
    borderRadius: 18, justifyContent: 'center', alignItems: 'center',
  },
  profileBtnText: { fontSize: 18 },
  historyBtn: {
    width: 36, height: 36,
    borderRadius: 18, justifyContent: 'center', alignItems: 'center',
  },
  historyBtnText: { fontSize: 18 },
  greeting: { fontSize: 22, fontWeight: '700', letterSpacing: -0.5 },
  date: { fontSize: 14, marginTop: 2, textTransform: 'capitalize' },
  logoutBtn: { paddingHorizontal: 14, paddingVertical: 7, borderRadius: 20 },
  logoutText: { fontWeight: '600', fontSize: 14 },
  listContent: { paddingHorizontal: 16, paddingBottom: 32 },
  overdueHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 6 },
  overdueTitle: { fontSize: 17, fontWeight: '700', flex: 1 },
  overdueBadge: { borderRadius: 12, paddingHorizontal: 8, paddingVertical: 2 },
  overdueBadgeText: { color: '#fff', fontSize: 13, fontWeight: '700' },
  overdueSubtitle: { fontSize: 13, marginBottom: 12, lineHeight: 18 },
  overdueCard: {
    borderRadius: 14, padding: 14, marginBottom: 8,
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    borderWidth: 1,
  },
  overdueLeft: { flexDirection: 'row', alignItems: 'center', flex: 1 },
  overdueIcon: { width: 36, height: 36, justifyContent: 'center', alignItems: 'center', marginRight: 12 },
  overdueIconText: { fontSize: 20 },
  divider: { height: 1, marginVertical: 20 },
  progressCard: {
    borderRadius: 16, padding: 20, marginBottom: 24,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.06, shadowRadius: 10, elevation: 2,
  },
  progressRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 },
  progressTitle: { fontSize: 15, fontWeight: '600' },
  progressPercent: { fontSize: 15, fontWeight: '700' },
  progressBar: { height: 6, borderRadius: 3, marginBottom: 16, overflow: 'hidden' },
  progressFill: { height: '100%', borderRadius: 3 },
  statsRow: { flexDirection: 'row', justifyContent: 'space-around', marginBottom: 16 },
  statItem: { alignItems: 'center', flex: 1 },
  statValue: { fontSize: 20, fontWeight: '700' },
  statLabel: { fontSize: 12, marginTop: 2 },
  statDivider: { width: StyleSheet.hairlineWidth },
  routeActions: { gap: 8 },
  pickupBtn: {
    borderRadius: 12,
    paddingVertical: 13,
    alignItems: 'center',
    shadowOffset: { width: 0, height: 3 },
    shadowOpacity: 0.25,
    shadowRadius: 6,
    elevation: 3,
  },
  pickupBtnText: { color: '#fff', fontSize: 16, fontWeight: '600' },
  startRouteBtn: {
    borderRadius: 12, paddingVertical: 13, alignItems: 'center',
    shadowOffset: { width: 0, height: 3 }, shadowOpacity: 0.25, shadowRadius: 6, elevation: 3,
  },
  startRouteBtnText: { color: '#fff', fontSize: 16, fontWeight: '600' },
  mapsRouteBtn: {
    borderRadius: 12, paddingVertical: 13, alignItems: 'center',
    borderWidth: 1,
  },
  mapsRouteBtnText: { fontSize: 16, fontWeight: '600' },
  sectionTitle: { fontSize: 20, fontWeight: '700', marginBottom: 12, letterSpacing: -0.3 },
  stopCard: {
    borderRadius: 14, padding: 14, marginBottom: 8,
    flexDirection: 'row', alignItems: 'center',
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.04, shadowRadius: 6, elevation: 1,
  },
  stopCardDone: { opacity: 0.6 },
  stopIndex: { width: 36, height: 36, borderRadius: 18, justifyContent: 'center', alignItems: 'center', marginRight: 12 },
  stopIndexText: { fontWeight: '700', fontSize: 15 },
  stopContent: { flex: 1 },
  stopName: { fontSize: 15, fontWeight: '600' },
  stopAddress: { fontSize: 13, marginTop: 2 },
  stopTracking: { fontSize: 11, marginTop: 2, fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace' },
  badge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 20, marginLeft: 8 },
  badgeText: { fontSize: 12, fontWeight: '600' },
  emptyContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 32 },
  emptyIcon: { fontSize: 52, marginBottom: 16 },
  emptyTitle: { fontSize: 20, fontWeight: '700' },
  emptySubtitle: { fontSize: 15, marginTop: 6 },
  noPendingRoute: {
    borderRadius: 14, padding: 16,
    borderWidth: 1, marginTop: 8,
  },
  noPendingRouteText: { fontSize: 14, lineHeight: 20 },
  completedBanner: {
    borderRadius: 12,
    padding: 14,
    alignItems: 'center',
    borderWidth: 1,
  },
  completedBannerText: {
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 4,
  },
  completedBannerSub: {
    fontSize: 13,
    textAlign: 'center',
  },
  closeRequestBtn: {
    borderRadius: 12,
    paddingVertical: 13,
    alignItems: 'center',
    borderWidth: 1,
  },
  closeRequestBtnText: {
    fontSize: 16,
    fontWeight: '600',
  },
  pendingRequestBanner: {
    flexDirection: 'row',
    borderRadius: 12,
    padding: 14,
    alignItems: 'center',
    borderWidth: 1,
  },
  pendingRequestIcon: {
    width: 28,
    height: 28,
    borderRadius: 14,
    color: '#fff',
    fontSize: 18,
    fontWeight: '700',
    textAlign: 'center',
    lineHeight: 28,
    marginRight: 12,
  },
  pendingRequestContent: {
    flex: 1,
  },
  pendingRequestTitle: {
    fontSize: 14,
    fontWeight: '600',
    marginBottom: 2,
  },
  pendingRequestText: {
    fontSize: 13,
  },
})
