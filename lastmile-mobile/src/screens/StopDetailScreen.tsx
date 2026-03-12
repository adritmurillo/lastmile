import { useState } from 'react'
import {
    ActivityIndicator,
    Alert,
    Linking,
    Modal,
    Platform,
    SafeAreaView,
    StatusBar,
    StyleSheet,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from 'react-native'
import { routesApi } from '../api/routesApi'
import type { Stop } from '../types'

interface Props {
  stop: Stop
  routeId: string
  onBack: () => void
  onComplete: () => void
}

export default function StopDetailScreen({ stop, routeId, onBack, onComplete }: Props) {
  const [loading, setLoading] = useState(false)
  const [failModalOpen, setFailModalOpen] = useState(false)
  const [failReason, setFailReason] = useState('')

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
    if (!failReason.trim()) {
      Alert.alert('Requerido', 'Ingresa el motivo del fallo')
      return
    }
    setLoading(true)
    try {
      await routesApi.failStop(stop.id, failReason)
      setFailModalOpen(false)
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

  const handleMaps = () => {
    const address = encodeURIComponent(stop.order.addressText)
    const url = Platform.OS === 'ios'
      ? `maps://?q=${address}`
      : `geo:0,0?q=${address}`
    Linking.openURL(url)
  }

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" />

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={onBack} style={styles.backBtn}>
          <Text style={styles.backText}>← Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Parada {stop.stopOrder}</Text>
        <View style={{ width: 80 }} />
      </View>

      <View style={styles.content}>

        {/* Recipient Card */}
        <View style={styles.card}>
          <Text style={styles.cardLabel}>DESTINATARIO</Text>
          <Text style={styles.recipientName}>{stop.order.recipientName}</Text>
          <Text style={styles.tracking}>{stop.order.trackingCode}</Text>
        </View>

        {/* Address Card */}
        <View style={styles.card}>
          <Text style={styles.cardLabel}>DIRECCIÓN</Text>
          <Text style={styles.address}>{stop.order.addressText}</Text>
          <TouchableOpacity style={styles.mapsBtn} onPress={handleMaps}>
            <Text style={styles.mapsBtnText}>📍 Abrir en Mapas</Text>
          </TouchableOpacity>
        </View>

        {/* Info Card */}
        <View style={styles.card}>
          <View style={styles.infoRow}>
            <View style={styles.infoItem}>
              <Text style={styles.infoLabel}>Teléfono</Text>
              <TouchableOpacity onPress={handleCall}>
                <Text style={styles.infoValueLink}>{stop.order.recipientPhone}</Text>
              </TouchableOpacity>
            </View>
            <View style={styles.infoDivider} />
            <View style={styles.infoItem}>
              <Text style={styles.infoLabel}>Prioridad</Text>
              <Text style={[
                styles.infoValue,
                { color: stop.order.priority === 'EXPRESS' ? '#ff3b30' : '#007aff' }
              ]}>
                {stop.order.priority}
              </Text>
            </View>
            <View style={styles.infoDivider} />
            <View style={styles.infoItem}>
              <Text style={styles.infoLabel}>Peso</Text>
              <Text style={styles.infoValue}>{stop.order.weightKg} kg</Text>
            </View>
          </View>
        </View>

        {/* Actions */}
        <View style={styles.actions}>
          <TouchableOpacity
            style={styles.deliverBtn}
            onPress={handleDeliver}
            disabled={loading}
            activeOpacity={0.85}
          >
            {loading
              ? <ActivityIndicator color="#fff" />
              : <Text style={styles.deliverBtnText}>✓ Entrega exitosa</Text>
            }
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.failBtn}
            onPress={() => setFailModalOpen(true)}
            disabled={loading}
            activeOpacity={0.85}
          >
            <Text style={styles.failBtnText}>✕ Registrar fallo</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Fail Modal */}
      <Modal
        visible={failModalOpen}
        transparent
        animationType="slide"
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>Motivo del fallo</Text>
            <Text style={styles.modalSubtitle}>
              Describe por qué no se pudo completar la entrega
            </Text>
            <TextInput
              style={styles.modalInput}
              placeholder="Ej: No había nadie en casa..."
              placeholderTextColor="#c7c7cc"
              value={failReason}
              onChangeText={setFailReason}
              multiline
              numberOfLines={3}
              autoFocus
            />
            <View style={styles.modalActions}>
              <TouchableOpacity
                style={styles.modalCancelBtn}
                onPress={() => { setFailModalOpen(false); setFailReason('') }}
              >
                <Text style={styles.modalCancelText}>Cancelar</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={styles.modalConfirmBtn}
                onPress={handleFail}
                disabled={loading}
              >
                {loading
                  ? <ActivityIndicator color="#fff" size="small" />
                  : <Text style={styles.modalConfirmText}>Confirmar</Text>
                }
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f2f2f7',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  backBtn: {
    width: 80,
  },
  backText: {
    fontSize: 16,
    color: '#007aff',
    fontWeight: '500',
  },
  headerTitle: {
    fontSize: 17,
    fontWeight: '600',
    color: '#1c1c1e',
  },
  content: {
    padding: 16,
    gap: 12,
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 6,
    elevation: 1,
  },
  cardLabel: {
    fontSize: 11,
    fontWeight: '600',
    color: '#8e8e93',
    letterSpacing: 0.5,
    marginBottom: 6,
  },
  recipientName: {
    fontSize: 22,
    fontWeight: '700',
    color: '#1c1c1e',
    letterSpacing: -0.3,
  },
  tracking: {
    fontSize: 13,
    color: '#8e8e93',
    marginTop: 4,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
  },
  address: {
    fontSize: 16,
    color: '#1c1c1e',
    lineHeight: 22,
    marginBottom: 12,
  },
  mapsBtn: {
    backgroundColor: '#f2f2f7',
    borderRadius: 10,
    padding: 10,
    alignItems: 'center',
  },
  mapsBtnText: {
    fontSize: 14,
    color: '#007aff',
    fontWeight: '500',
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  infoItem: {
    alignItems: 'center',
    flex: 1,
  },
  infoLabel: {
    fontSize: 11,
    color: '#8e8e93',
    marginBottom: 4,
    fontWeight: '500',
  },
  infoValue: {
    fontSize: 15,
    fontWeight: '600',
    color: '#1c1c1e',
  },
  infoValueLink: {
    fontSize: 15,
    fontWeight: '600',
    color: '#007aff',
  },
  infoDivider: {
    width: StyleSheet.hairlineWidth,
    backgroundColor: '#e5e5ea',
  },
  actions: {
    gap: 10,
    marginTop: 8,
  },
  deliverBtn: {
    backgroundColor: '#34c759',
    borderRadius: 14,
    paddingVertical: 16,
    alignItems: 'center',
    shadowColor: '#34c759',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 4,
  },
  deliverBtnText: {
    color: '#fff',
    fontSize: 17,
    fontWeight: '600',
  },
  failBtn: {
    backgroundColor: '#fff',
    borderRadius: 14,
    paddingVertical: 16,
    alignItems: 'center',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: '#ff3b30',
  },
  failBtnText: {
    color: '#ff3b30',
    fontSize: 17,
    fontWeight: '600',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.4)',
    justifyContent: 'flex-end',
  },
  modalCard: {
    backgroundColor: '#fff',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 24,
    paddingBottom: 40,
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#1c1c1e',
    marginBottom: 6,
  },
  modalSubtitle: {
    fontSize: 14,
    color: '#8e8e93',
    marginBottom: 16,
  },
  modalInput: {
    backgroundColor: '#f2f2f7',
    borderRadius: 12,
    padding: 14,
    fontSize: 15,
    color: '#1c1c1e',
    minHeight: 80,
    textAlignVertical: 'top',
    marginBottom: 16,
  },
  modalActions: {
    flexDirection: 'row',
    gap: 10,
  },
  modalCancelBtn: {
    flex: 1,
    backgroundColor: '#f2f2f7',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  modalCancelText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#8e8e93',
  },
  modalConfirmBtn: {
    flex: 1,
    backgroundColor: '#ff3b30',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  modalConfirmText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#fff',
  },
})