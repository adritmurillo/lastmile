import {
  ActivityIndicator,
  Image,
  KeyboardAvoidingView,
  Modal, Platform, SafeAreaView,
  ScrollView, StatusBar, StyleSheet, Text, TextInput,
  TouchableOpacity, View
} from 'react-native'
import { useTheme } from '../context/ThemeContext'
import { FAILURE_REASONS, useStopDetail } from '../hooks/useStopDetail'
import type { Stop } from '../types'

interface Props {
  stop: Stop
  routeId: string
  onBack: () => void
  onComplete: () => void
}

export default function StopDetailScreen({ stop, routeId, onBack, onComplete }: Props) {
  const { colors } = useTheme()
  const {
    loading, uploadingPhoto, photoUris, failModalOpen, selectedReason, setSelectedReason,
    failureNotes, setFailureNotes,
    handleDeliver, handleFail, handleCall, handleWhatsApp, handleMaps, handleTakePhoto,
    handleRemovePhoto,
    openFailModal, closeFailModal,
  } = useStopDetail(stop, onComplete)

  const canConfirm = selectedReason !== null &&
    (selectedReason !== 'OTHER' || failureNotes.trim().length > 0)

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]}>
      <StatusBar barStyle={colors.statusBar} />

      <View style={styles.header}>
        <TouchableOpacity onPress={onBack} style={styles.backBtn}>
          <Text style={[styles.backText, { color: colors.primary }]}>← Volver</Text>
        </TouchableOpacity>
        <Text style={[styles.headerTitle, { color: colors.text }]}>Parada {stop.stopOrder}</Text>
        <View style={{ width: 80 }} />
      </View>

      <ScrollView contentContainerStyle={styles.content}>

        {/* Destinatario */}
        <View style={[styles.card, { backgroundColor: colors.card }]}>
          <Text style={[styles.cardLabel, { color: colors.textSecondary }]}>DESTINATARIO</Text>
          <Text style={[styles.recipientName, { color: colors.text }]}>{stop.order.recipientName}</Text>
          <Text style={[styles.tracking, { color: colors.textSecondary }]}>{stop.order.trackingCode}</Text>
        </View>

        {/* Dirección */}
        <View style={[styles.card, { backgroundColor: colors.card }]}>
          <Text style={[styles.cardLabel, { color: colors.textSecondary }]}>DIRECCIÓN</Text>
          <Text style={[styles.address, { color: colors.text }]}>{stop.order.addressText}</Text>
          <TouchableOpacity style={[styles.mapsBtn, { backgroundColor: colors.cardSecondary }]} onPress={handleMaps}>
            <Text style={[styles.mapsBtnText, { color: colors.primary }]}>📍 Navegar</Text>
          </TouchableOpacity>
        </View>

        {/* Info */}
        <View style={[styles.card, { backgroundColor: colors.card }]}>
          <View style={styles.infoRow}>
            <View style={styles.infoItem}>
              <Text style={[styles.infoLabel, { color: colors.textSecondary }]}>Prioridad</Text>
              <Text style={[styles.infoValue, { color: stop.order.priority === 'EXPRESS' ? colors.error : colors.primary }]}>
                {stop.order.priority}
              </Text>
            </View>
            <View style={[styles.infoDivider, { backgroundColor: colors.separator }]} />
            <View style={styles.infoItem}>
              <Text style={[styles.infoLabel, { color: colors.textSecondary }]}>Peso</Text>
              <Text style={[styles.infoValue, { color: colors.text }]}>{stop.order.weightKg} kg</Text>
            </View>
          </View>
        </View>

        {/* Contacto */}
        <View style={[styles.card, { backgroundColor: colors.card }]}>
          <Text style={[styles.cardLabel, { color: colors.textSecondary }]}>CONTACTAR</Text>
          <Text style={[styles.phoneText, { color: colors.text }]}>{stop.order.recipientPhone}</Text>
          <View style={styles.contactRow}>
            <TouchableOpacity style={[styles.contactBtn, { backgroundColor: colors.cardSecondary }]} onPress={handleCall} activeOpacity={0.7}>
              <Text style={styles.contactBtnEmoji}>📞</Text>
              <Text style={[styles.contactBtnText, { color: colors.primary }]}>Llamar</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.contactBtn, { backgroundColor: colors.successLight }]} onPress={handleWhatsApp} activeOpacity={0.7}>
              <Text style={styles.contactBtnEmoji}>💬</Text>
              <Text style={[styles.contactBtnText, { color: colors.success }]}>WhatsApp</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Foto de entrega */}
        <View style={[styles.card, { backgroundColor: colors.card }]}>
          <Text style={[styles.cardLabel, { color: colors.textSecondary }]}>FOTOS DE ENTREGA</Text>

          {photoUris.length > 0 && (
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ marginBottom: 10 }}>
              <View style={{ flexDirection: 'row', gap: 8 }}>
                {photoUris.map((uri, index) => (
                  <View key={index} style={styles.photoThumbContainer}>
                    <Image source={{ uri }} style={styles.photoThumb} resizeMode="cover" />
                    <TouchableOpacity
                      style={styles.photoRemoveBtn}
                      onPress={() => handleRemovePhoto(index)}
                    >
                      <Text style={styles.photoRemoveText}>✕</Text>
                    </TouchableOpacity>
                  </View>
                ))}
              </View>
            </ScrollView>
          )}

          <TouchableOpacity style={[styles.photoBtn, { backgroundColor: colors.cardSecondary }]} onPress={handleTakePhoto} activeOpacity={0.7}>
            <Text style={styles.photoBtnEmoji}>📷</Text>
            <Text style={[styles.photoBtnText, { color: colors.text }]}>
              {photoUris.length === 0 ? 'Tomar foto de entrega' : '+ Agregar otra foto'}
            </Text>
            {photoUris.length === 0 && <Text style={[styles.photoBtnSub, { color: colors.textSecondary }]}>Opcional pero recomendado</Text>}
          </TouchableOpacity>
        </View>

        {/* Acciones */}
        <View style={styles.actions}>
          <TouchableOpacity style={[styles.deliverBtn, { backgroundColor: colors.success }]} onPress={handleDeliver} disabled={loading} activeOpacity={0.85}>
            {loading
              ? <ActivityIndicator color="#fff" />
              : <Text style={styles.deliverBtnText}>
                {uploadingPhoto ? '⬆️ Subiendo foto...' : '✓ Entrega exitosa'}
              </Text>
            }
          </TouchableOpacity>
          <TouchableOpacity style={[styles.failBtn, { backgroundColor: colors.card, borderColor: colors.error }]} onPress={openFailModal} disabled={loading} activeOpacity={0.85}>
            <Text style={[styles.failBtnText, { color: colors.error }]}>✕ Registrar fallo</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>

      <Modal visible={failModalOpen} transparent animationType="slide">
        <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={{ flex: 1 }}>
          <View style={styles.modalOverlay}>
            <View style={[styles.modalCard, { backgroundColor: colors.card }]}>
              <Text style={[styles.modalTitle, { color: colors.text }]}>¿Por qué falló la entrega?</Text>
              <Text style={[styles.modalSubtitle, { color: colors.textSecondary }]}>Selecciona el motivo</Text>

              <ScrollView showsVerticalScrollIndicator={false} keyboardShouldPersistTaps="handled">
                <View style={styles.reasonsContainer}>
                  {FAILURE_REASONS.map(({ key, label, emoji }) => (
                    <TouchableOpacity
                      key={key}
                      style={[
                        styles.reasonBtn,
                        { backgroundColor: colors.cardSecondary },
                        selectedReason === key && { backgroundColor: colors.errorLight, borderColor: colors.error, borderWidth: 2 }
                      ]}
                      onPress={() => setSelectedReason(key)}
                      activeOpacity={0.7}
                    >
                      <Text style={styles.reasonEmoji}>{emoji}</Text>
                      <Text style={[
                        styles.reasonLabel,
                        { color: colors.text },
                        selectedReason === key && { color: colors.error, fontWeight: '600' }
                      ]}>
                        {label}
                      </Text>
                      {selectedReason === key && <Text style={[styles.checkmark, { color: colors.error }]}>✓</Text>}
                    </TouchableOpacity>
                  ))}
                </View>

                {selectedReason === 'OTHER' && (
                  <TextInput
                    style={[styles.notesInput, { backgroundColor: colors.cardSecondary, color: colors.text, borderColor: colors.error }]}
                    placeholder="Describe el motivo..."
                    placeholderTextColor={colors.placeholder}
                    value={failureNotes}
                    onChangeText={setFailureNotes}
                    multiline
                    numberOfLines={3}
                    textAlignVertical="top"
                  />
                )}
              </ScrollView>

              <View style={styles.modalActions}>
                <TouchableOpacity style={[styles.modalCancelBtn, { backgroundColor: colors.cardSecondary }]} onPress={closeFailModal}>
                  <Text style={[styles.modalCancelText, { color: colors.textSecondary }]}>Cancelar</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.modalConfirmBtn, { backgroundColor: colors.error }, !canConfirm && styles.modalConfirmBtnDisabled]}
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
  container: { flex: 1 },
  header: {
    flexDirection: 'row', alignItems: 'center',
    justifyContent: 'space-between', paddingHorizontal: 16, paddingVertical: 12,
  },
  backBtn: { width: 80 },
  backText: { fontSize: 16, fontWeight: '500' },
  headerTitle: { fontSize: 17, fontWeight: '600' },
  content: { padding: 16, gap: 12 },
  card: {
    borderRadius: 16, padding: 16,
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05, shadowRadius: 6, elevation: 1,
  },
  cardLabel: { fontSize: 11, fontWeight: '600', letterSpacing: 0.5, marginBottom: 6 },
  recipientName: { fontSize: 22, fontWeight: '700', letterSpacing: -0.3 },
  tracking: { fontSize: 13, marginTop: 4, fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace' },
  address: { fontSize: 16, lineHeight: 22, marginBottom: 12 },
  mapsBtn: { borderRadius: 10, padding: 10, alignItems: 'center' },
  mapsBtnText: { fontSize: 14, fontWeight: '500' },
  photoPreview: {
    width: '100%', height: 200, borderRadius: 12, marginBottom: 10,
  },
  retakeBtn: {
    borderRadius: 10, padding: 10, alignItems: 'center',
  },
  retakeBtnText: { fontSize: 14, fontWeight: '500' },
  photoBtn: {
    borderRadius: 12, padding: 20,
    alignItems: 'center', gap: 4,
  },
  photoBtnEmoji: { fontSize: 32, marginBottom: 4 },
  photoBtnText: { fontSize: 15, fontWeight: '600' },
  photoBtnSub: { fontSize: 13 },
  infoRow: { flexDirection: 'row', justifyContent: 'space-around' },
  infoItem: { alignItems: 'center', flex: 1 },
  infoLabel: { fontSize: 11, marginBottom: 4, fontWeight: '500' },
  infoValue: { fontSize: 15, fontWeight: '600' },
  infoDivider: { width: StyleSheet.hairlineWidth },
  phoneText: { fontSize: 17, fontWeight: '600', marginBottom: 12 },
  contactRow: { flexDirection: 'row', gap: 10 },
  contactBtn: {
    flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
    gap: 6, borderRadius: 12, paddingVertical: 12,
  },
  contactBtnEmoji: { fontSize: 18 },
  contactBtnText: { fontSize: 15, fontWeight: '600' },
  actions: { gap: 10, marginTop: 8 },
  deliverBtn: {
    borderRadius: 14, paddingVertical: 16, alignItems: 'center',
    shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 8, elevation: 4,
  },
  deliverBtnText: { color: '#fff', fontSize: 17, fontWeight: '600' },
  failBtn: {
    borderRadius: 14, paddingVertical: 16, alignItems: 'center',
    borderWidth: StyleSheet.hairlineWidth,
  },
  failBtnText: { fontSize: 17, fontWeight: '600' },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'flex-end' },
  modalCard: { borderTopLeftRadius: 24, borderTopRightRadius: 24, padding: 24, paddingBottom: 40 },
  modalTitle: { fontSize: 20, fontWeight: '700', marginBottom: 4 },
  modalSubtitle: { fontSize: 14, marginBottom: 16 },
  reasonsContainer: { gap: 8, marginBottom: 12 },
  reasonBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 12,
    borderRadius: 12, padding: 14,
    borderWidth: 2, borderColor: 'transparent',
  },
  reasonEmoji: { fontSize: 20 },
  reasonLabel: { flex: 1, fontSize: 15, fontWeight: '500' },
  checkmark: { fontSize: 16, fontWeight: '700' },
  notesInput: {
    borderRadius: 12, padding: 14,
    fontSize: 15, minHeight: 80,
    marginBottom: 12, borderWidth: 2,
  },
  modalActions: { flexDirection: 'row', gap: 10 },
  modalCancelBtn: { flex: 1, borderRadius: 12, paddingVertical: 14, alignItems: 'center' },
  modalCancelText: { fontSize: 16, fontWeight: '600' },
  modalConfirmBtn: { flex: 1, borderRadius: 12, paddingVertical: 14, alignItems: 'center' },
  modalConfirmBtnDisabled: { opacity: 0.5 },
  modalConfirmText: { fontSize: 16, fontWeight: '600', color: '#fff' },
  photoThumbContainer: {
    position: 'relative', width: 100, height: 100,
  },
  photoThumb: {
    width: 100, height: 100, borderRadius: 10,
  },
  photoRemoveBtn: {
    position: 'absolute', top: 4, right: 4,
    backgroundColor: 'rgba(0,0,0,0.6)', borderRadius: 10,
    width: 20, height: 20, alignItems: 'center', justifyContent: 'center',
  },
  photoRemoveText: {
    color: '#fff', fontSize: 11, fontWeight: '700',
  },
})
