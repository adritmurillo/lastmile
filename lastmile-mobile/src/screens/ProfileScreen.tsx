import {
    ActivityIndicator, KeyboardAvoidingView, Platform, SafeAreaView, ScrollView, StatusBar,
    StyleSheet, Text, TextInput, TouchableOpacity, View,
} from 'react-native'
import { useProfile } from '../hooks/useProfile'

interface Props {
  onBack: () => void
}

export default function ProfileScreen({ onBack }: Props) {
  const {
    courier, loading, saving, hasChanges,
    phone, initials,
    handleChange, handleSave, handleBack, logout,
  } = useProfile()

  if (loading) {
    return (
      <SafeAreaView style={styles.container}>
        <ActivityIndicator size="large" color="#007aff" style={{ flex: 1 }} />
      </SafeAreaView>
    )
  }

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" />

      <View style={styles.header}>
        <TouchableOpacity onPress={() => handleBack(onBack)} style={styles.backBtn}>
          <Text style={styles.backText}>← Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Mi perfil</Text>
        <TouchableOpacity
          onPress={handleSave}
          disabled={saving || !hasChanges}
          style={styles.saveBtn}
        >
          {saving
            ? <ActivityIndicator size="small" color="#007aff" />
            : <Text style={[styles.saveText, !hasChanges && styles.saveTextDisabled]}>
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
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>{initials}</Text>
          </View>
          <Text style={styles.fullName}>{courier?.fullName}</Text>
          <Text style={styles.role}>Courier</Text>
        </View>

        {/* Vehículo */}
        {courier?.vehicle && (
          <View style={styles.card}>
            <Text style={styles.cardLabel}>VEHÍCULO ASIGNADO</Text>
            <View style={styles.vehicleRow}>
              <Text style={styles.vehicleEmoji}>
                {courier.vehicle.type === 'MOTORCYCLE' ? '🏍' :
                 courier.vehicle.type === 'CAR' ? '🚗' : '🚐'}
              </Text>
              <View>
                <Text style={styles.vehiclePlate}>{courier.vehicle.licensePlate}</Text>
                <Text style={styles.vehicleType}>{courier.vehicle.type}</Text>
              </View>
            </View>
          </View>
        )}

        {/* Solo teléfono editable */}
        <View style={styles.card}>
          <Text style={styles.cardLabel}>INFORMACIÓN PERSONAL</Text>

          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Nombre completo</Text>
            <Text style={styles.infoValue}>{courier?.fullName}</Text>
          </View>
          <View style={styles.separator} />

          <Text style={[styles.inputLabel, { marginTop: 12 }]}>Teléfono</Text>
          <TextInput
            style={[styles.input, { marginBottom: 0 }]}
            value={phone}
            onChangeText={handleChange}
            placeholder="Teléfono"
            returnKeyType="done"
            placeholderTextColor="#c7c7cc"
            keyboardType="phone-pad"
          />
        </View>

        {/* Datos no editables */}
        <View style={styles.card}>
          <Text style={styles.cardLabel}>DATOS DEL SISTEMA</Text>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>DNI</Text>
            <Text style={styles.infoValue}>{courier?.documentNumber}</Text>
          </View>
          <View style={styles.separator} />
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Estado</Text>
            <Text style={[styles.infoValue, { color: '#34c759' }]}>{courier?.status}</Text>
          </View>
        </View>

        {/* Logout */}
        <TouchableOpacity style={styles.logoutBtn} onPress={logout}>
          <Text style={styles.logoutText}>Cerrar sesión</Text>
        </TouchableOpacity>

        </ScrollView>
      </KeyboardAvoidingView>
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
  saveBtn: { width: 80, alignItems: 'flex-end' },
  saveText: { fontSize: 16, color: '#007aff', fontWeight: '600' },
  saveTextDisabled: { color: '#c7c7cc' },
  content: { padding: 16, gap: 12 },
  avatarSection: { alignItems: 'center', paddingVertical: 24 },
  avatar: {
    width: 80, height: 80, borderRadius: 40,
    backgroundColor: '#007aff', justifyContent: 'center', alignItems: 'center',
    marginBottom: 12,
    shadowColor: '#007aff', shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3, shadowRadius: 8, elevation: 4,
  },
  avatarText: { fontSize: 28, fontWeight: '700', color: '#fff' },
  fullName: { fontSize: 22, fontWeight: '700', color: '#1c1c1e', letterSpacing: -0.3 },
  role: { fontSize: 14, color: '#8e8e93', marginTop: 4 },
  card: {
    backgroundColor: '#fff', borderRadius: 16, padding: 16,
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05, shadowRadius: 6, elevation: 1,
  },
  cardLabel: { fontSize: 11, fontWeight: '600', color: '#8e8e93', letterSpacing: 0.5, marginBottom: 12 },
  vehicleRow: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  vehicleEmoji: { fontSize: 32 },
  vehiclePlate: { fontSize: 18, fontWeight: '700', color: '#1c1c1e' },
  vehicleType: { fontSize: 13, color: '#8e8e93', marginTop: 2 },
  inputLabel: { fontSize: 12, color: '#8e8e93', marginBottom: 6, fontWeight: '500' },
  input: {
    backgroundColor: '#f2f2f7', borderRadius: 10, padding: 12,
    fontSize: 15, color: '#1c1c1e', marginBottom: 12,
  },
  infoRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 8 },
  infoLabel: { fontSize: 15, color: '#8e8e93' },
  infoValue: { fontSize: 15, fontWeight: '500', color: '#1c1c1e' },
  separator: { height: StyleSheet.hairlineWidth, backgroundColor: '#e5e5ea' },
  logoutBtn: {
    backgroundColor: '#fff', borderRadius: 14, paddingVertical: 16, alignItems: 'center',
    borderWidth: StyleSheet.hairlineWidth, borderColor: '#ff3b30', marginTop: 8,
  },
  logoutText: { color: '#ff3b30', fontSize: 17, fontWeight: '600' },
})