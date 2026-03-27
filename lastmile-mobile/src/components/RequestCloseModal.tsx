import {
  ActivityIndicator,
  Image,
  KeyboardAvoidingView,
  Modal,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native'
import { useTheme } from '../context/ThemeContext'
import { ROUTE_CLOSE_REASONS, type RouteCloseReason } from '../types'

interface Props {
  visible: boolean
  loading: boolean
  uploadingPhoto: boolean
  selectedReason: RouteCloseReason | null
  onSelectReason: (reason: RouteCloseReason) => void
  message: string
  onChangeMessage: (text: string) => void
  photoUri: string | null
  onTakePhoto: () => void
  onRemovePhoto: () => void
  onSubmit: () => void
  onClose: () => void
}

export default function RequestCloseModal({
  visible,
  loading,
  uploadingPhoto,
  selectedReason,
  onSelectReason,
  message,
  onChangeMessage,
  photoUri,
  onTakePhoto,
  onRemovePhoto,
  onSubmit,
  onClose,
}: Props) {
  const { colors } = useTheme()
  const isSubmitting = loading || uploadingPhoto

  return (
    <Modal visible={visible} transparent animationType="slide">
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={{ flex: 1 }}
      >
        <View style={styles.overlay}>
          <View style={[styles.container, { backgroundColor: colors.card }]}>
            <View style={[styles.header, { borderBottomColor: colors.separator }]}>
              <Text style={[styles.title, { color: colors.text }]}>Solicitar cierre de ruta</Text>
              <TouchableOpacity
                onPress={onClose}
                style={[styles.closeBtn, { backgroundColor: colors.backgroundSecondary }]}
                disabled={isSubmitting}
              >
                <Text style={[styles.closeBtnText, { color: colors.textMuted }]}>X</Text>
              </TouchableOpacity>
            </View>

            <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
              <Text style={[styles.description, { color: colors.textMuted }]}>
                Si no puedes continuar con las entregas, solicita el cierre de tu ruta.
                El despachador revisara tu solicitud.
              </Text>

              <Text style={[styles.label, { color: colors.text }]}>Motivo del cierre *</Text>
              <View style={styles.reasonsGrid}>
                {ROUTE_CLOSE_REASONS.map((reason) => (
                  <TouchableOpacity
                    key={reason.key}
                    style={[
                      styles.reasonCard,
                      { backgroundColor: colors.backgroundSecondary },
                      selectedReason === reason.key && {
                        backgroundColor: colors.primaryLight,
                        borderColor: colors.primary,
                      },
                    ]}
                    onPress={() => onSelectReason(reason.key)}
                    activeOpacity={0.7}
                    disabled={isSubmitting}
                  >
                    <Text style={styles.reasonEmoji}>{reason.emoji}</Text>
                    <Text
                      style={[
                        styles.reasonLabel,
                        { color: colors.text },
                        selectedReason === reason.key && {
                          color: colors.primary,
                          fontWeight: '600',
                        },
                      ]}
                    >
                      {reason.label}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>

              <Text style={[styles.label, { color: colors.text }]}>Describe el problema *</Text>
              <TextInput
                style={[
                  styles.textArea,
                  {
                    backgroundColor: colors.inputBackground,
                    color: colors.text,
                  }
                ]}
                placeholder="Explica brevemente por que necesitas cerrar la ruta..."
                placeholderTextColor={colors.placeholder}
                multiline
                numberOfLines={4}
                value={message}
                onChangeText={onChangeMessage}
                editable={!isSubmitting}
              />

              <Text style={[styles.label, { color: colors.text }]}>Foto (opcional)</Text>
              {photoUri ? (
                <View style={styles.photoPreview}>
                  <Image
                    source={{ uri: photoUri }}
                    style={[styles.photo, { backgroundColor: colors.backgroundSecondary }]}
                  />
                  <TouchableOpacity
                    style={styles.removePhotoBtn}
                    onPress={onRemovePhoto}
                    disabled={isSubmitting}
                  >
                    <Text style={styles.removePhotoBtnText}>X</Text>
                  </TouchableOpacity>
                </View>
              ) : (
                <TouchableOpacity
                  style={[
                    styles.takePhotoBtn,
                    {
                      backgroundColor: colors.backgroundSecondary,
                      borderColor: colors.border,
                    }
                  ]}
                  onPress={onTakePhoto}
                  disabled={isSubmitting}
                >
                  <Text style={[styles.takePhotoBtnText, { color: colors.primary }]}>Tomar foto</Text>
                </TouchableOpacity>
              )}

              <View style={[styles.warning, { backgroundColor: colors.warningLight }]}>
                <Text style={styles.warningIcon}>!</Text>
                <Text style={[styles.warningText, { color: colors.warning }]}>
                  Si tu solicitud es aprobada, deberas devolver los paquetes pendientes al almacen.
                </Text>
              </View>
            </ScrollView>

            <View style={[styles.footer, { borderTopColor: colors.separator }]}>
              <TouchableOpacity
                style={[styles.cancelBtn, { backgroundColor: colors.backgroundSecondary }]}
                onPress={onClose}
                disabled={isSubmitting}
              >
                <Text style={[styles.cancelBtnText, { color: colors.textMuted }]}>Cancelar</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[
                  styles.submitBtn,
                  { backgroundColor: colors.error },
                  isSubmitting && styles.submitBtnDisabled
                ]}
                onPress={onSubmit}
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <ActivityIndicator color="#fff" size="small" />
                ) : (
                  <Text style={styles.submitBtnText}>Enviar solicitud</Text>
                )}
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </KeyboardAvoidingView>
    </Modal>
  )
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'flex-end',
  },
  container: {
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    maxHeight: '90%',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 20,
    borderBottomWidth: 1,
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
  },
  closeBtn: {
    width: 32,
    height: 32,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
  },
  closeBtnText: {
    fontSize: 16,
    fontWeight: '600',
  },
  content: {
    padding: 20,
  },
  description: {
    fontSize: 14,
    lineHeight: 20,
    marginBottom: 20,
  },
  label: {
    fontSize: 15,
    fontWeight: '600',
    marginBottom: 10,
  },
  reasonsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
    marginBottom: 20,
  },
  reasonCard: {
    width: '48%',
    borderRadius: 12,
    padding: 14,
    alignItems: 'center',
    borderWidth: 2,
    borderColor: 'transparent',
  },
  reasonEmoji: {
    fontSize: 24,
    marginBottom: 6,
  },
  reasonLabel: {
    fontSize: 13,
    fontWeight: '500',
    textAlign: 'center',
  },
  textArea: {
    borderRadius: 12,
    padding: 14,
    fontSize: 15,
    minHeight: 100,
    textAlignVertical: 'top',
    marginBottom: 20,
  },
  takePhotoBtn: {
    borderRadius: 12,
    padding: 14,
    alignItems: 'center',
    borderWidth: 1,
    borderStyle: 'dashed',
    marginBottom: 20,
  },
  takePhotoBtnText: {
    fontSize: 15,
    fontWeight: '500',
  },
  photoPreview: {
    position: 'relative',
    marginBottom: 20,
  },
  photo: {
    width: '100%',
    height: 150,
    borderRadius: 12,
  },
  removePhotoBtn: {
    position: 'absolute',
    top: 8,
    right: 8,
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: 'rgba(0, 0, 0, 0.6)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  removePhotoBtnText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '600',
  },
  warning: {
    flexDirection: 'row',
    borderRadius: 12,
    padding: 14,
    alignItems: 'flex-start',
    marginBottom: 10,
  },
  warningIcon: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#ff9500',
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
    textAlign: 'center',
    lineHeight: 24,
    marginRight: 12,
  },
  warningText: {
    flex: 1,
    fontSize: 13,
    lineHeight: 18,
  },
  footer: {
    flexDirection: 'row',
    gap: 12,
    padding: 20,
    paddingBottom: Platform.OS === 'ios' ? 34 : 20,
    borderTopWidth: 1,
  },
  cancelBtn: {
    flex: 1,
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  cancelBtnText: {
    fontSize: 16,
    fontWeight: '600',
  },
  submitBtn: {
    flex: 1,
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  submitBtnDisabled: {
    opacity: 0.6,
  },
  submitBtnText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#fff',
  },
})
