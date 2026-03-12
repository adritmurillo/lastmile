import { useState } from 'react'
import {
    ActivityIndicator,
    Alert,
    KeyboardAvoidingView,
    Platform,
    StatusBar,
    StyleSheet,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from 'react-native'
import { useAuth } from '../context/AuthContext'

export default function LoginScreen() {
  const { login } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [focusedInput, setFocusedInput] = useState<string | null>(null)

  const handleLogin = async () => {
    if (!username || !password) {
      Alert.alert('Campos requeridos', 'Ingresa tu usuario y contraseña')
      return
    }
    setLoading(true)
    try {
      await login(username, password)
    } catch {
      Alert.alert('Error al ingresar', 'Usuario o contraseña incorrectos')
    } finally {
      setLoading(false)
    }
  }

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <StatusBar barStyle="dark-content" />

      <View style={styles.topSection}>
        <View style={styles.iconContainer}>
          <Text style={styles.icon}>🚚</Text>
        </View>
        <Text style={styles.appName}>LastMile</Text>
        <Text style={styles.tagline}>Gestión de entregas</Text>
      </View>

      <View style={styles.formSection}>
        <Text style={styles.formTitle}>Iniciar sesión</Text>

        <View style={styles.inputGroup}>
          <TextInput
            style={[
              styles.input,
              focusedInput === 'username' && styles.inputFocused,
            ]}
            placeholder="Usuario"
            placeholderTextColor="#c7c7cc"
            value={username}
            onChangeText={setUsername}
            autoCapitalize="none"
            autoCorrect={false}
            onFocus={() => setFocusedInput('username')}
            onBlur={() => setFocusedInput(null)}
          />
          <View style={styles.separator} />
          <TextInput
            style={[
              styles.input,
              styles.inputLast,
              focusedInput === 'password' && styles.inputFocused,
            ]}
            placeholder="Contraseña"
            placeholderTextColor="#c7c7cc"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            onFocus={() => setFocusedInput('password')}
            onBlur={() => setFocusedInput(null)}
          />
        </View>

        <TouchableOpacity
          style={[styles.button, loading && styles.buttonDisabled]}
          onPress={handleLogin}
          disabled={loading}
          activeOpacity={0.85}
        >
          {loading
            ? <ActivityIndicator color="#fff" />
            : <Text style={styles.buttonText}>Continuar</Text>
          }
        </TouchableOpacity>

        <Text style={styles.footer}>
          Solo para couriers autorizados
        </Text>
      </View>
    </KeyboardAvoidingView>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f2f2f7',
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
  topSection: {
    alignItems: 'center',
    marginBottom: 48,
  },
  iconContainer: {
    width: 80,
    height: 80,
    backgroundColor: '#fff',
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.08,
    shadowRadius: 12,
    elevation: 4,
    marginBottom: 16,
  },
  icon: {
    fontSize: 40,
  },
  appName: {
    fontSize: 28,
    fontWeight: '700',
    color: '#1c1c1e',
    letterSpacing: -0.5,
  },
  tagline: {
    fontSize: 15,
    color: '#8e8e93',
    marginTop: 4,
  },
  formSection: {
    gap: 16,
  },
  formTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: '#1c1c1e',
    marginBottom: 4,
  },
  inputGroup: {
    backgroundColor: '#fff',
    borderRadius: 12,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 6,
    elevation: 1,
  },
  input: {
    paddingHorizontal: 16,
    paddingVertical: 15,
    fontSize: 16,
    color: '#1c1c1e',
    backgroundColor: '#fff',
  },
  inputLast: {
    marginBottom: 0,
  },
  inputFocused: {
    backgroundColor: '#f9f9fb',
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#e5e5ea',
    marginLeft: 16,
  },
  button: {
    backgroundColor: '#007aff',
    borderRadius: 12,
    paddingVertical: 16,
    alignItems: 'center',
    shadowColor: '#007aff',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 4,
  },
  buttonDisabled: {
    opacity: 0.7,
  },
  buttonText: {
    color: '#fff',
    fontSize: 17,
    fontWeight: '600',
    letterSpacing: -0.2,
  },
  footer: {
    textAlign: 'center',
    fontSize: 13,
    color: '#8e8e93',
    marginTop: 8,
  },
})