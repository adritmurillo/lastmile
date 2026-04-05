import { CameraView, useCameraPermissions } from 'expo-camera'
import { useState } from 'react'
import {
  ActivityIndicator,
  FlatList,
  Platform,
  SafeAreaView,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native'
import { useTheme } from '../context/ThemeContext'
import { usePickup } from '../hooks/usePickup'
import type { Stop } from '../types'

interface Props {
  onBack: () => void
  onStartRoute: () => void
}

export default function PickupScreen({ onBack, onStartRoute }: Props) {
  const { colors } = useTheme()
  const {
    route,
    pickupStatus,
    loading,
    scanning,
    handleScan,
    handleStartRoute,
    getStopStatus,
  } = usePickup(onBack, onStartRoute)

  const [permission, requestPermission] = useCameraPermissions()
  const [scannerActive, setScannerActive] = useState(false)
  const [manualCode, setManualCode] = useState('')

  const handleBarCodeScanned = ({ data }: { data: string }) => {
    if (!scanning) {
      handleScan(data)
    }
  }

  const handleManualSubmit = () => {
    const code = manualCode.trim().toUpperCase()
    if (code && !scanning) {
      handleScan(code)
      setManualCode('')
    }
  }

  const renderStop = ({ item }: { item: Stop }) => {
    const status = getStopStatus(item)
    const isScanned = status === 'scanned'

    return (
      <View style={[
        styles.stopCard,
        { backgroundColor: isScanned ? colors.successLight : colors.card }
      ]}>
        <View style={[
          styles.stopDot,
          { backgroundColor: isScanned ? colors.success : colors.warning }
        ]} />
        <View style={styles.stopContent}>
          <Text style={[
            styles.stopName,
            { color: isScanned ? colors.success : colors.text }
          ]}>
            {item.order.recipientName}
          </Text>
          <Text style={[styles.stopTracking, { color: colors.textMuted }]}>
            {item.order.trackingCode}
          </Text>
        </View>
        {isScanned ? (
          <Text style={[styles.checkMark, { color: colors.success }]}>✓</Text>
        ) : (
          <Text style={[styles.pendingMark, { color: colors.border }]}>○</Text>
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

  if (!route) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]}>
        <StatusBar barStyle={colors.statusBar} />
        <View style={styles.header}>
          <TouchableOpacity onPress={onBack} style={styles.backBtn}>
            <Text style={[styles.backBtnText, { color: colors.primary }]}>← Volver</Text>
          </TouchableOpacity>
        </View>
        <View style={styles.center}>
          <Text style={styles.emptyIcon}>📦</Text>
          <Text style={[styles.emptyTitle, { color: colors.text }]}>Sin ruta asignada</Text>
          <Text style={[styles.emptySubtitle, { color: colors.textMuted }]}>No tienes ruta para hoy</Text>
        </View>
      </SafeAreaView>
    )
  }

  // Scanner activo
  if (scannerActive) {
    if (!permission?.granted) {
      return (
        <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]}>
          <StatusBar barStyle={colors.statusBar} />
          <View style={styles.center}>
            <Text style={[styles.permissionText, { color: colors.text }]}>
              Necesitamos acceso a la cámara para escanear códigos QR
            </Text>
            <TouchableOpacity
              style={[styles.permissionBtn, { backgroundColor: colors.primary }]}
              onPress={requestPermission}
            >
              <Text style={styles.permissionBtnText}>Permitir cámara</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.cancelBtn} onPress={() => setScannerActive(false)}>
              <Text style={[styles.cancelBtnText, { color: colors.textMuted }]}>Cancelar</Text>
            </TouchableOpacity>
          </View>
        </SafeAreaView>
      )
    }

    return (
      <View style={styles.scannerContainer}>
        <CameraView
          style={styles.camera}
          barcodeScannerSettings={{ barcodeTypes: ['qr', 'code128', 'code39', 'ean13'] }}
          onBarcodeScanned={handleBarCodeScanned}
        />
        <View style={styles.scannerOverlay}>
          <View style={styles.scannerHeader}>
            <TouchableOpacity onPress={() => setScannerActive(false)} style={styles.closeBtn}>
              <Text style={styles.closeBtnText}>✕</Text>
            </TouchableOpacity>
            <Text style={styles.scannerTitle}>Escanea el código QR</Text>
            <View style={{ width: 44 }} />
          </View>

          <View style={styles.scannerFrame}>
            <View style={styles.cornerTL} />
            <View style={styles.cornerTR} />
            <View style={styles.cornerBL} />
            <View style={styles.cornerBR} />
          </View>

          {scanning && (
            <View style={styles.scanningIndicator}>
              <ActivityIndicator color="#fff" />
              <Text style={styles.scanningText}>Procesando...</Text>
            </View>
          )}

          <View style={styles.scannerFooter}>
            <Text style={styles.scannerHint}>
              {pickupStatus?.scannedPackages ?? 0} de {pickupStatus?.totalPackages ?? 0} paquetes escaneados
            </Text>
          </View>
        </View>
      </View>
    )
  }

  // Vista principal de pickup
  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]}>
      <StatusBar barStyle={colors.statusBar} />

      <View style={styles.header}>
        <TouchableOpacity onPress={onBack} style={styles.backBtn}>
          <Text style={[styles.backBtnText, { color: colors.primary }]}>← Volver</Text>
        </TouchableOpacity>
        <Text style={[styles.title, { color: colors.text }]}>Recoger paquetes</Text>
        <View style={{ width: 70 }} />
      </View>

      <FlatList
        data={route.stops}
        keyExtractor={(item) => item.id}
        renderItem={renderStop}
        contentContainerStyle={styles.listContent}
        ListHeaderComponent={
          <>
            {/* Progress card */}
            <View style={[styles.progressCard, { backgroundColor: colors.card }]}>
              <View style={styles.progressRow}>
                <Text style={[styles.progressTitle, { color: colors.text }]}>Paquetes escaneados</Text>
                <Text style={[styles.progressCount, { color: colors.primary }]}>
                  {pickupStatus?.scannedPackages ?? 0}/{pickupStatus?.totalPackages ?? 0}
                </Text>
              </View>
              <View style={[styles.progressBar, { backgroundColor: colors.backgroundSecondary }]}>
                <View
                  style={[
                    styles.progressFill,
                    {
                      backgroundColor: colors.success,
                      width: `${
                        pickupStatus
                          ? (pickupStatus.scannedPackages / pickupStatus.totalPackages) * 100
                          : 0
                      }%`,
                    },
                  ]}
                />
              </View>

              <TouchableOpacity
                style={[styles.scanBtn, { backgroundColor: colors.primary }]}
                onPress={() => setScannerActive(true)}
                activeOpacity={0.85}
              >
                <Text style={styles.scanBtnText}>📷 Escanear paquete</Text>
              </TouchableOpacity>

              {/* Entrada manual de código */}
              <View style={styles.manualInputRow}>
                <TextInput
                  style={[
                    styles.manualInput,
                    {
                      backgroundColor: colors.inputBackground,
                      borderColor: colors.inputBorder,
                      color: colors.text,
                    }
                  ]}
                  placeholder="Código de tracking..."
                  placeholderTextColor={colors.placeholder}
                  value={manualCode}
                  onChangeText={setManualCode}
                  autoCapitalize="characters"
                  autoCorrect={false}
                  returnKeyType="done"
                  onSubmitEditing={handleManualSubmit}
                />
                <TouchableOpacity
                  style={[
                    styles.manualSubmitBtn,
                    { backgroundColor: manualCode.trim() ? colors.success : colors.border }
                  ]}
                  onPress={handleManualSubmit}
                  disabled={!manualCode.trim() || scanning}
                  activeOpacity={0.85}
                >
                  {scanning ? (
                    <ActivityIndicator color="#fff" size="small" />
                  ) : (
                    <Text style={styles.manualSubmitBtnText}>✓</Text>
                  )}
                </TouchableOpacity>
              </View>

              {pickupStatus?.readyToStart && (
                <TouchableOpacity
                  style={[styles.startBtn, { backgroundColor: colors.success }]}
                  onPress={handleStartRoute}
                  activeOpacity={0.85}
                >
                  <Text style={styles.startBtnText}>🚀 Iniciar ruta</Text>
                </TouchableOpacity>
              )}
            </View>

            <Text style={[styles.sectionTitle, { color: colors.text }]}>Lista de paquetes</Text>
          </>
        }
      />
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 32 },
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

  // Empty state
  emptyIcon: { fontSize: 52, marginBottom: 16 },
  emptyTitle: { fontSize: 20, fontWeight: '700' },
  emptySubtitle: { fontSize: 15, marginTop: 6 },

  // Progress card
  progressCard: {
    borderRadius: 16,
    padding: 20,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 10,
    elevation: 2,
  },
  progressRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 },
  progressTitle: { fontSize: 15, fontWeight: '600' },
  progressCount: { fontSize: 15, fontWeight: '700' },
  progressBar: { height: 6, borderRadius: 3, marginBottom: 16, overflow: 'hidden' },
  progressFill: { height: '100%', borderRadius: 3 },
  scanBtn: {
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
    marginBottom: 8,
  },
  scanBtnText: { color: '#fff', fontSize: 16, fontWeight: '600' },
  manualInputRow: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 8,
  },
  manualInput: {
    flex: 1,
    borderRadius: 10,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 15,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
    borderWidth: 1,
  },
  manualSubmitBtn: {
    borderRadius: 10,
    width: 48,
    justifyContent: 'center',
    alignItems: 'center',
  },
  manualSubmitBtnText: {
    color: '#fff',
    fontSize: 20,
    fontWeight: '700',
  },
  startBtn: {
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  startBtnText: { color: '#fff', fontSize: 16, fontWeight: '600' },

  sectionTitle: { fontSize: 17, fontWeight: '700', marginBottom: 12 },

  // Stop card
  stopCard: {
    borderRadius: 12,
    padding: 14,
    marginBottom: 8,
    flexDirection: 'row',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 4,
    elevation: 1,
  },
  stopDot: { width: 12, height: 12, borderRadius: 6, marginRight: 12 },
  stopContent: { flex: 1 },
  stopName: { fontSize: 15, fontWeight: '600' },
  stopTracking: {
    fontSize: 12,
    marginTop: 2,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
  },
  checkMark: { fontSize: 20, fontWeight: '700' },
  pendingMark: { fontSize: 20 },

  // Scanner
  scannerContainer: { flex: 1, backgroundColor: '#000' },
  camera: { flex: 1 },
  scannerOverlay: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'space-between',
  },
  scannerHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 60,
    paddingHorizontal: 16,
  },
  closeBtn: {
    width: 44,
    height: 44,
    backgroundColor: 'rgba(0,0,0,0.5)',
    borderRadius: 22,
    justifyContent: 'center',
    alignItems: 'center',
  },
  closeBtnText: { color: '#fff', fontSize: 20 },
  scannerTitle: { color: '#fff', fontSize: 18, fontWeight: '600' },
  scannerFrame: {
    width: 250,
    height: 250,
    alignSelf: 'center',
    position: 'relative',
  },
  cornerTL: { position: 'absolute', top: 0, left: 0, width: 40, height: 40, borderTopWidth: 4, borderLeftWidth: 4, borderColor: '#fff', borderTopLeftRadius: 12 },
  cornerTR: { position: 'absolute', top: 0, right: 0, width: 40, height: 40, borderTopWidth: 4, borderRightWidth: 4, borderColor: '#fff', borderTopRightRadius: 12 },
  cornerBL: { position: 'absolute', bottom: 0, left: 0, width: 40, height: 40, borderBottomWidth: 4, borderLeftWidth: 4, borderColor: '#fff', borderBottomLeftRadius: 12 },
  cornerBR: { position: 'absolute', bottom: 0, right: 0, width: 40, height: 40, borderBottomWidth: 4, borderRightWidth: 4, borderColor: '#fff', borderBottomRightRadius: 12 },
  scanningIndicator: {
    position: 'absolute',
    top: '50%',
    alignSelf: 'center',
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(0,0,0,0.7)',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 25,
  },
  scanningText: { color: '#fff', marginLeft: 10, fontSize: 14 },
  scannerFooter: {
    paddingBottom: 60,
    paddingHorizontal: 16,
    alignItems: 'center',
  },
  scannerHint: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
    backgroundColor: 'rgba(0,0,0,0.5)',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 20,
  },

  // Permission
  permissionText: { fontSize: 16, textAlign: 'center', marginBottom: 20 },
  permissionBtn: {
    borderRadius: 12,
    paddingVertical: 14,
    paddingHorizontal: 24,
    marginBottom: 12,
  },
  permissionBtnText: { color: '#fff', fontSize: 16, fontWeight: '600' },
  cancelBtn: { paddingVertical: 14 },
  cancelBtnText: { fontSize: 16 },
})
