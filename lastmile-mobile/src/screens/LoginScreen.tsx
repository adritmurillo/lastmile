import {
  ActivityIndicator, KeyboardAvoidingView, Platform,
  StatusBar, StyleSheet, Text, TextInput, TouchableOpacity, View,
} from 'react-native'
import { useTheme } from '../context/ThemeContext'
import { useLogin } from '../hooks/useLogin'

export default function LoginScreen() {
  const { colors } = useTheme()
  const {
    username, setUsername,
    password, setPassword,
    loading, focusedInput, setFocusedInput,
    handleLogin,
  } = useLogin()

  return (
    <KeyboardAvoidingView
      style={[styles.container, { backgroundColor: colors.background }]}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <StatusBar barStyle={colors.statusBar} />

      <View style={styles.topSection}>
        <View style={[styles.iconContainer, { backgroundColor: colors.card }]}>
          <Text style={styles.icon}>🚚</Text>
        </View>
        <Text style={[styles.appName, { color: colors.text }]}>LastMile</Text>
        <Text style={[styles.tagline, { color: colors.textSecondary }]}>Gestión de entregas</Text>
      </View>

      <View style={styles.formSection}>
        <Text style={[styles.formTitle, { color: colors.text }]}>Iniciar sesión</Text>

        <View style={[styles.inputGroup, { backgroundColor: colors.card }]}>
          <TextInput
            style={[
              styles.input,
              { backgroundColor: colors.card, color: colors.text },
              focusedInput === 'username' && { backgroundColor: colors.cardSecondary }
            ]}
            placeholder="Usuario"
            placeholderTextColor={colors.placeholder}
            value={username}
            onChangeText={setUsername}
            autoCapitalize="none"
            autoCorrect={false}
            onFocus={() => setFocusedInput('username')}
            onBlur={() => setFocusedInput(null)}
          />
          <View style={[styles.separator, { backgroundColor: colors.separator }]} />
          <TextInput
            style={[
              styles.input,
              styles.inputLast,
              { backgroundColor: colors.card, color: colors.text },
              focusedInput === 'password' && { backgroundColor: colors.cardSecondary }
            ]}
            placeholder="Contraseña"
            placeholderTextColor={colors.placeholder}
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            onFocus={() => setFocusedInput('password')}
            onBlur={() => setFocusedInput(null)}
          />
        </View>

        <TouchableOpacity
          style={[styles.button, { backgroundColor: colors.primary }, loading && styles.buttonDisabled]}
          onPress={handleLogin}
          disabled={loading}
          activeOpacity={0.85}
        >
          {loading
            ? <ActivityIndicator color="#fff" />
            : <Text style={styles.buttonText}>Continuar</Text>
          }
        </TouchableOpacity>

        <Text style={[styles.footer, { color: colors.textSecondary }]}>Solo para couriers autorizados</Text>
      </View>
    </KeyboardAvoidingView>
  )
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', paddingHorizontal: 24 },
  topSection: { alignItems: 'center', marginBottom: 48 },
  iconContainer: {
    width: 80, height: 80, borderRadius: 20,
    justifyContent: 'center', alignItems: 'center',
    shadowColor: '#000', shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.08, shadowRadius: 12, elevation: 4, marginBottom: 16,
  },
  icon: { fontSize: 40 },
  appName: { fontSize: 28, fontWeight: '700', letterSpacing: -0.5 },
  tagline: { fontSize: 15, marginTop: 4 },
  formSection: { gap: 16 },
  formTitle: { fontSize: 20, fontWeight: '600', marginBottom: 4 },
  inputGroup: {
    borderRadius: 12, overflow: 'hidden',
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05, shadowRadius: 6, elevation: 1,
  },
  input: { paddingHorizontal: 16, paddingVertical: 15, fontSize: 16 },
  inputLast: { marginBottom: 0 },
  separator: { height: StyleSheet.hairlineWidth, marginLeft: 16 },
  button: {
    borderRadius: 12, paddingVertical: 16, alignItems: 'center',
    shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 8, elevation: 4,
  },
  buttonDisabled: { opacity: 0.7 },
  buttonText: { color: '#fff', fontSize: 17, fontWeight: '600', letterSpacing: -0.2 },
  footer: { textAlign: 'center', fontSize: 13, marginTop: 8 },
})
