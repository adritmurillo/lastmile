import AsyncStorage from '@react-native-async-storage/async-storage'
import * as Device from 'expo-device'
import * as Notifications from 'expo-notifications'
import { createContext, useContext, useEffect, useState } from 'react'
import apiClient from '../api/apiClient'
import { authApi } from '../api/authApi'
import type { AuthResponse } from '../types'

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
    shouldShowBanner: true,
    shouldShowList: true,
  }),
})

interface AuthContextType {
  user: AuthResponse | null
  loading: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthResponse | null>(null)
  const [loading, setLoading] = useState(true)

  const registerForPushNotifications = async () => {
    if (!Device.isDevice) return null

    const { status: existingStatus } = await Notifications.getPermissionsAsync()
    let finalStatus = existingStatus

    if (existingStatus !== 'granted') {
      const { status } = await Notifications.requestPermissionsAsync()
      finalStatus = status
    }

    if (finalStatus !== 'granted') return null

    try {
      const token = await Notifications.getExpoPushTokenAsync({
        projectId: 'ee907bd3-8137-4080-a027-de34d1023b52'
      })
      return token.data
    } catch (e) {
      console.log('Could not get push token:', e)
      return null
    }
  }

  const loadUser = async () => {
  try {
    const token = await AsyncStorage.getItem('token')
    const username = await AsyncStorage.getItem('username')
    const role = await AsyncStorage.getItem('role')
    const courierId = await AsyncStorage.getItem('courierId')
    
    if (token && username && role && courierId) {
      setUser({ token, username, role, courierId })
    } else if (token && username && role && !courierId) {
      // Sesión antigua sin courierId — forzar logout
      await AsyncStorage.multiRemove(['token', 'username', 'role', 'courierId'])
    }
  } finally {
    setLoading(false)
  }
}

  useEffect(() => {
    loadUser()
  }, [])

  const login = async (username: string, password: string) => {
    const data = await authApi.login({ username, password })
    await AsyncStorage.setItem('token', data.token)
    await AsyncStorage.setItem('username', data.username)
    await AsyncStorage.setItem('role', data.role)
    if (data.courierId) {
      await AsyncStorage.setItem('courierId', data.courierId)
    }
    setUser(data)

    const pushToken = await registerForPushNotifications()
    if (pushToken && data.courierId) {
      try {
        await apiClient.post(`/couriers/${data.courierId}/fcm-token`, { token: pushToken })
        console.log('Push token registered:', pushToken)
      } catch {
        console.log('Could not register push token')
      }
    }
  }

  const logout = async () => {
    await AsyncStorage.multiRemove(['token', 'username', 'role', 'courierId'])
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}