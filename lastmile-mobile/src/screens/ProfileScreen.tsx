import {
    ActivityIndicator, KeyboardAvoidingView, Platform, SafeAreaView, ScrollView, StatusBar,
    StyleSheet, Switch, Text, TextInput, TouchableOpacity, View,
} from 'react-native'
import { ThemeMode, useTheme } from '../context/ThemeContext'
import { useProfile } from '../hooks/useProfile'

interface Props {
  onBack: () => void
}

const THEME_OPTIONS: { mode: ThemeMode; label: string; icon: string }[] = [
  { mode: 'system', label: 'Sistema', icon: '📱' },
  { mode: 'light', label: 'Claro', icon: '☀️' },
  { mode: 'dark', label: 'Oscuro', icon: '🌙' },
]

export default function ProfileScreen({ onBack }: Props) {
  const { colors, themeMode, setThemeMode, isDark } = useTheme()
  const {
    courier, loading, saving, hasChanges,
    phone, initials,
    handleChange, handleSave, handleBack, logout,
  } = useProfile()

  if (loading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]}>
        <ActivityIndicator size="large" color={colors.primary} style={{ flex: 1 }} />
      </SafeAreaView>
    )
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]}>
      <StatusBar barStyle={colors.statusBar} />

      <View style={styles.header}>
        <TouchableOpacity onPress={() => handleBack(onBack)} style={styles.backBtn}>
          <Text style={[styles.backText, { color: colors.primary }]}>← Volver</Text>
        </TouchableOpacity>
        <Text style={[styles.headerTitle, { color: colors.text }]}>Mi perfil</Text>
        <TouchableOpacity
          onPress={handleSave}
          disabled={saving || !hasChanges}
          style={styles.saveBtn}
        >
          {saving
            ? <ActivityIndicator size="small" color={colors.primary} />
            : <Text style={[styles.saveText, { color: colors.primary }, !hasChanges && { color: colors.textMuted }]}>
                Guardar
              </Text>
          }
        </TouchableOpacity>
      </View>

      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      >
        <ScrollView
          contentContainerStyle={styles.content}
          keyboardDismissMode="on-drag"
        >

        {/* Avatar */}
        <View style={styles.avatarSection}>
          <View style={[styles.avatar, { backgroundColor: colors.primary }]}>
            <Text style={styles.avatarText}>{initials}</Text>
          </View>
          <Text style={[styles.fullName, { color: colors.text }]}>{courier?.fullName}</Text>
          <Text style={[styles.role, { color: colors.textSecondary }]}>Courier</Text>
        </View>

        {/* Tema */}
        <View style={[styles.card, { backgroundColor: colors.card }]}>
          <Text style={[styles.cardLabel, { color: colors.textSecondary }]}>APARIENCIA</Text>
          <View style={styles.themeOptions}>
            {THEME_OPTIONS.map(({ mode, label, icon }) => (
              <TouchableOpacity
                key={mode}
                style={[
                  styles.themeOption,
                  { backgroundColor: colors.cardSecondary },
                  themeMode === mode && { backgroundColor: colors.primaryLight, borderColor: colors.primary, borderWidth: 2 }
                ]}
                onPress={() => setThemeMode(mode)}
                activeOpacity={0.7}
              >
                <Text style={styles.themeIcon}>{icon}</Text>
                <Text style={[
                  styles.themeLabel,
                  { color: colors.text },
                  themeMode === mode && { color: colors.primary, fontWeight: '600' }
                ]}>
                  {label}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
          <View style={[styles.separator, { backgroundColor: colors.separator }]} />
          <View style={styles.darkModeRow}>
            <View>
              <Text style={[styles.darkModeLabel, { color: colors.text }]}>Modo oscuro</Text>
              <Text style={[styles.darkModeHint, { color: colors.textSecondary }]}>
                {themeMode === 'system' ? 'Siguiendo sistema' : (isDark ? 'Activado' : 'Desactivado')}
              </Text>
            </View>
            <Switch
              value={isDark}
              onValueChange={(value) => setThemeMode(value ? 'dark' : 'light')}
              trackColor={{ false: colors.border, true: colors.primary }}
              thumbColor={isDark ? '#fff' : '#fff'}
            />
          </View>
        </View>

        {/* Vehículo */}
        {courier?.vehicle && (
          <View style={[styles.card, { backgroundColor: colors.card }]}>
            <Text style={[styles.cardLabel, { color: colors.textSecondary }]}>VEHÍCULO ASIGNADO</Text>
            <View style={styles.vehicleRow}>
              <Text style={styles.vehicleEmoji}>
                {courier.vehicle.type === 'MOTORCYCLE' ? '🏍' :
                 courier.vehicle.type === 'CAR' ? '🚗' : '🚐'}
              </Text>
              <View>
                <Text style={[styles.vehiclePlate, { color: colors.text }]}>{courier.vehicle.licensePlate}</Text>
                <Text style={[styles.vehicleType, { color: colors.textSecondary }]}>{courier.vehicle.type}</Text>
              </View>
            </View>
          </View>
        )}

        {/* Solo teléfono editable */}
        <View style={[styles.card, { backgroundColor: colors.card }]}>
          <Text style={[styles.cardLabel, { color: colors.textSecondary }]}>INFORMACIÓN PERSONAL</Text>

          <View style={styles.infoRow}>
            <Text style={[styles.infoLabel, { color: colors.textSecondary }]}>Nombre completo</Text>
            <Text style={[styles.infoValue, { color: colors.text }]}>{courier?.fullName}</Text>
          </View>
          <View style={[styles.separator, { backgroundColor: colors.separator }]} />

          <Text style={[styles.inputLabel, { color: colors.textSecondary, marginTop: 12 }]}>Teléfono</Text>
          <TextInput
            style={[styles.input, { backgroundColor: colors.cardSecondary, color: colors.text, marginBottom: 0 }]}
            value={phone}
            onChangeText={handleChange}
            placeholder="Teléfono"
            returnKeyType="done"
            placeholderTextColor={colors.placeholder}
            keyboardType="phone-pad"
          />
        </View>

        {/* Datos no editables */}
        <View style={[styles.card, { backgroundColor: colors.card }]}>
          <Text style={[styles.cardLabel, { color: colors.textSecondary }]}>DATOS DEL SISTEMA</Text>
          <View style={styles.infoRow}>
            <Text style={[styles.infoLabel, { color: colors.textSecondary }]}>DNI</Text>
            <Text style={[styles.infoValue, { color: colors.text }]}>{courier?.documentNumber}</Text>
          </View>
          <View style={[styles.separator, { backgroundColor: colors.separator }]} />
          <View style={styles.infoRow}>
            <Text style={[styles.infoLabel, { color: colors.textSecondary }]}>Estado</Text>
            <Text style={[styles.infoValue, { color: colors.success }]}>{courier?.status}</Text>
          </View>
        </View>

        {/* Logout */}
        <TouchableOpacity style={[styles.logoutBtn, { backgroundColor: colors.card, borderColor: colors.error }]} onPress={logout}>
          <Text style={[styles.logoutText, { color: colors.error }]}>Cerrar sesión</Text>
        </TouchableOpacity>

        </ScrollView>
      </KeyboardAvoidingView>
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
  saveBtn: { width: 80, alignItems: 'flex-end' },
  saveText: { fontSize: 16, fontWeight: '600' },
  content: { padding: 16, gap: 12 },
  avatarSection: { alignItems: 'center', paddingVertical: 24 },
  avatar: {
    width: 80, height: 80, borderRadius: 40,
    justifyContent: 'center', alignItems: 'center',
    marginBottom: 12,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3, shadowRadius: 8, elevation: 4,
  },
  avatarText: { fontSize: 28, fontWeight: '700', color: '#fff' },
  fullName: { fontSize: 22, fontWeight: '700', letterSpacing: -0.3 },
  role: { fontSize: 14, marginTop: 4 },
  card: {
    borderRadius: 16, padding: 16,
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05, shadowRadius: 6, elevation: 1,
  },
  cardLabel: { fontSize: 11, fontWeight: '600', letterSpacing: 0.5, marginBottom: 12 },
  themeOptions: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 12,
  },
  themeOption: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: 12,
    borderRadius: 12,
    borderWidth: 2,
    borderColor: 'transparent',
  },
  themeIcon: { fontSize: 24, marginBottom: 4 },
  themeLabel: { fontSize: 13, fontWeight: '500' },
  darkModeRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 12,
  },
  darkModeLabel: { fontSize: 15, fontWeight: '500' },
  darkModeHint: { fontSize: 13, marginTop: 2 },
  vehicleRow: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  vehicleEmoji: { fontSize: 32 },
  vehiclePlate: { fontSize: 18, fontWeight: '700' },
  vehicleType: { fontSize: 13, marginTop: 2 },
  inputLabel: { fontSize: 12, marginBottom: 6, fontWeight: '500' },
  input: {
    borderRadius: 10, padding: 12,
    fontSize: 15, marginBottom: 12,
  },
  infoRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 8 },
  infoLabel: { fontSize: 15 },
  infoValue: { fontSize: 15, fontWeight: '500' },
  separator: { height: StyleSheet.hairlineWidth },
  logoutBtn: {
    borderRadius: 14, paddingVertical: 16, alignItems: 'center',
    borderWidth: StyleSheet.hairlineWidth, marginTop: 8,
  },
  logoutText: { fontSize: 17, fontWeight: '600' },
})
