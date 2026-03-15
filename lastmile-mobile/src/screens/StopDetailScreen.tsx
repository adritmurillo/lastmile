import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Modal, Platform, SafeAreaView,
  ScrollView, StatusBar, StyleSheet, Text, TextInput,
  TouchableOpacity, View
} from 'react-native'
import { FAILURE_REASONS, useStopDetail } from '../hooks/useStopDetail'
import type { Stop } from '../types'

interface Props {
  stop: Stop
  routeId: string
  onBack: () => void
  onComplete: () => void
}

export default function StopDetailScreen({ stop, routeId, onBack, onComplete }: Props) {
  const {
    loading, failModalOpen, selectedReason, setSelectedReason,
    failureNotes, setFailureNotes,
    handleDeliver, handleFail, handleCall, handleWhatsApp, handleMaps,
    openFailModal, closeFailModal,
  } = useStopDetail(stop, onComplete)

  const canConfirm = selectedReason !== null &&
    (selectedReason !== 'OTHER' || failureNotes.trim().length > 0)

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" />

      <View style={styles.header}>
        <TouchableOpacity onPress={onBack} style={styles.backBtn}>
          <Text style={styles.backText}>← Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Parada {stop.stopOrder}</Text>
        <View style={{ width: 80 }} />
      </View>

      <ScrollView contentContainerStyle={styles.content}>

        {/* Destinatario */}
        <View style={styles.card}>
          <Text style={styles.cardLabel}>DESTINATARIO</Text>
          <Text style={styles.recipientName}>{stop.order.recipientName}</Text>
          <Text style={styles.tracking}>{stop.order.trackingCode}</Text>
        </View>

        {/* Dirección */}
        <View style={styles.card}>
          <Text style={styles.cardLabel}>DIRECCIÓN</Text>
          <Text style={styles.address}>{stop.order.addressText}</Text>
          <TouchableOpacity style={styles.mapsBtn} onPress={handleMaps}>
            <Text style={styles.mapsBtnText}>📍 Navegar</Text>
          </TouchableOpacity>
        </View>

        {/* Info */}
        <View style={styles.card}>
          <View style={styles.infoRow}>
            <View style={styles.infoItem}>
              <Text style={styles.infoLabel}>Prioridad</Text>
              <Text style={[styles.infoValue, { color: stop.order.priority === 'EXPRESS' ? '#ff3b30' : '#007aff' }]}>
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

        {/* Contacto */}
        <View style={styles.card}>
          <Text style={styles.cardLabel}>CONTACTAR</Text>
          <Text style={styles.phoneText}>{stop.order.recipientPhone}</Text>
          <View style={styles.contactRow}>
            <TouchableOpacity style={styles.contactBtn} onPress={handleCall} activeOpacity={0.7}>
              <Text style={styles.contactBtnEmoji}>📞</Text>
              <Text style={styles.contactBtnText}>Llamar</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.contactBtn, styles.whatsappBtn]} onPress={handleWhatsApp} activeOpacity={0.7}>
              <Text style={styles.contactBtnEmoji}>💬</Text>
              <Text style={[styles.contactBtnText, styles.whatsappText]}>WhatsApp</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Acciones */}
        <View style={styles.actions}>
          <TouchableOpacity style={styles.deliverBtn} onPress={handleDeliver} disabled={loading} activeOpacity={0.85}>
            {loading
              ? <ActivityIndicator color="#fff" />
              : <Text style={styles.deliverBtnText}>✓ Entrega exitosa</Text>
            }
          </TouchableOpacity>
          <TouchableOpacity style={styles.failBtn} onPress={openFailModal} disabled={loading} activeOpacity={0.85}>
            <Text style={styles.failBtnText}>✕ Registrar fallo</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>

      <Modal visible={failModalOpen} transparent animationType="slide">
        <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={{ flex: 1 }}>
          <View style={styles.modalOverlay}>
            <View style={styles.modalCard}>
              <Text style={styles.modalTitle}>¿Por qué falló la entrega?</Text>
              <Text style={styles.modalSubtitle}>Selecciona el motivo</Text>

              <ScrollView showsVerticalScrollIndicator={false} keyboardShouldPersistTaps="handled">
                <View style={styles.reasonsContainer}>
                  {FAILURE_REASONS.map(({ key, label, emoji }) => (
                    <TouchableOpacity
                      key={key}
                      style={[styles.reasonBtn, selectedReason === key && styles.reasonBtnSelected]}
                      onPress={() => setSelectedReason(key)}
                      activeOpacity={0.7}
                    >
                      <Text style={styles.reasonEmoji}>{emoji}</Text>
                      <Text style={[styles.reasonLabel, selectedReason === key && styles.reasonLabelSelected]}>
                        {label}
                      </Text>
                      {selectedReason === key && <Text style={styles.checkmark}>✓</Text>}
                    </TouchableOpacity>
                  ))}
                </View>

                {selectedReason === 'OTHER' && (
                  <TextInput
                    style={styles.notesInput}
                    placeholder="Describe el motivo..."
                    placeholderTextColor="#c7c7cc"
                    value={failureNotes}
                    onChangeText={setFailureNotes}
                    multiline
                    numberOfLines={3}
                    textAlignVertical="top"
                  />
                )}
              </ScrollView>

              <View style={styles.modalActions}>
                <TouchableOpacity style={styles.modalCancelBtn} onPress={closeFailModal}>
                  <Text style={styles.modalCancelText}>Cancelar</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.modalConfirmBtn, !canConfirm && styles.modalConfirmBtnDisabled]}
                  onPress={handleFail}
                  disabled={loading || !canConfirm}
                >
                  {loading
                    ? <ActivityIndicator color="#fff" size="small" />
                    : <Text style={styles.modalConfirmText}>Confirmar</Text>
                  }
                </TouchableOpacity>
              </View>
            </View>
          </View>
        </KeyboardAvoidingView>
      </Modal>
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f2f2f7' },
  header: {
    flexDirection: 'row', alignItems: 'center',
    justifyContent: 'space-between', paddingHorizontal: 16, paddingVertical: 12,
  },
  backBtn: { width: 80 },
  backText: { fontSize: 16, color: '#007aff', fontWeight: '500' },
  headerTitle: { fontSize: 17, fontWeight: '600', color: '#1c1c1e' },
  content: { padding: 16, gap: 12 },
  card: {
    backgroundColor: '#fff', borderRadius: 16, padding: 16,
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05, shadowRadius: 6, elevation: 1,
  },
  cardLabel: { fontSize: 11, fontWeight: '600', color: '#8e8e93', letterSpacing: 0.5, marginBottom: 6 },
  recipientName: { fontSize: 22, fontWeight: '700', color: '#1c1c1e', letterSpacing: -0.3 },
  tracking: { fontSize: 13, color: '#8e8e93', marginTop: 4, fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace' },
  address: { fontSize: 16, color: '#1c1c1e', lineHeight: 22, marginBottom: 12 },
  mapsBtn: { backgroundColor: '#f2f2f7', borderRadius: 10, padding: 10, alignItems: 'center' },
  mapsBtnText: { fontSize: 14, color: '#007aff', fontWeight: '500' },
  infoRow: { flexDirection: 'row', justifyContent: 'space-around' },
  infoItem: { alignItems: 'center', flex: 1 },
  infoLabel: { fontSize: 11, color: '#8e8e93', marginBottom: 4, fontWeight: '500' },
  infoValue: { fontSize: 15, fontWeight: '600', color: '#1c1c1e' },
  infoDivider: { width: StyleSheet.hairlineWidth, backgroundColor: '#e5e5ea' },
  phoneText: { fontSize: 17, fontWeight: '600', color: '#1c1c1e', marginBottom: 12 },
  contactRow: { flexDirection: 'row', gap: 10 },
  contactBtn: {
    flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
    gap: 6, backgroundColor: '#f2f2f7', borderRadius: 12, paddingVertical: 12,
  },
  whatsappBtn: { backgroundColor: '#e8f8ed' },
  contactBtnEmoji: { fontSize: 18 },
  contactBtnText: { fontSize: 15, fontWeight: '600', color: '#007aff' },
  whatsappText: { color: '#34c759' },
  actions: { gap: 10, marginTop: 8 },
  deliverBtn: {
    backgroundColor: '#34c759', borderRadius: 14, paddingVertical: 16, alignItems: 'center',
    shadowColor: '#34c759', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 8, elevation: 4,
  },
  deliverBtnText: { color: '#fff', fontSize: 17, fontWeight: '600' },
  failBtn: {
    backgroundColor: '#fff', borderRadius: 14, paddingVertical: 16, alignItems: 'center',
    borderWidth: StyleSheet.hairlineWidth, borderColor: '#ff3b30',
  },
  failBtnText: { color: '#ff3b30', fontSize: 17, fontWeight: '600' },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'flex-end' },
  modalCard: { backgroundColor: '#fff', borderTopLeftRadius: 24, borderTopRightRadius: 24, padding: 24, paddingBottom: 40 },
  modalTitle: { fontSize: 20, fontWeight: '700', color: '#1c1c1e', marginBottom: 4 },
  modalSubtitle: { fontSize: 14, color: '#8e8e93', marginBottom: 16 },
  reasonsContainer: { gap: 8, marginBottom: 12 },
  reasonBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 12,
    backgroundColor: '#f2f2f7', borderRadius: 12, padding: 14,
    borderWidth: 2, borderColor: 'transparent',
  },
  reasonBtnSelected: { backgroundColor: '#fff1f0', borderColor: '#ff3b30' },
  reasonEmoji: { fontSize: 20 },
  reasonLabel: { flex: 1, fontSize: 15, fontWeight: '500', color: '#1c1c1e' },
  reasonLabelSelected: { color: '#ff3b30', fontWeight: '600' },
  checkmark: { fontSize: 16, color: '#ff3b30', fontWeight: '700' },
  notesInput: {
    backgroundColor: '#f2f2f7', borderRadius: 12, padding: 14,
    fontSize: 15, color: '#1c1c1e', minHeight: 80,
    marginBottom: 12, borderWidth: 2, borderColor: '#ff3b30',
  },
  modalActions: { flexDirection: 'row', gap: 10 },
  modalCancelBtn: { flex: 1, backgroundColor: '#f2f2f7', borderRadius: 12, paddingVertical: 14, alignItems: 'center' },
  modalCancelText: { fontSize: 16, fontWeight: '600', color: '#8e8e93' },
  modalConfirmBtn: { flex: 1, backgroundColor: '#ff3b30', borderRadius: 12, paddingVertical: 14, alignItems: 'center' },
  modalConfirmBtnDisabled: { backgroundColor: '#ffb3b0' },
  modalConfirmText: { fontSize: 16, fontWeight: '600', color: '#fff' },
})