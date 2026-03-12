import AsyncStorage from '@react-native-async-storage/async-storage'
import { createContext, useContext, useEffect, useState } from 'react'
import { authApi } from '../api/authApi'
import type { AuthResponse } from '../types'

interface AuthContextType {
    user: AuthResponse | null
    loading: boolean
    login: (username: string, password: string) => Promise<void>
    logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType>({} as AuthContextType)

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<AuthResponse | null>(null)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        loadUser()
    }, [])

    const loadUser = async () => {
        try {
            const token = await AsyncStorage.getItem('token')
            const username = await AsyncStorage.getItem('username')
            const role = await AsyncStorage.getItem('role')
            const courierId = await AsyncStorage.getItem('courierId')
            if (token && username && role) {
                setUser({ token, username, role, courierId: courierId ?? undefined })
            }
        } finally {
            setLoading(false)
        }
    }

    const login = async (username: string, password: string) => {
        const data = await authApi.login({ username, password })
        await AsyncStorage.setItem('token', data.token)
        await AsyncStorage.setItem('username', data.username)
        await AsyncStorage.setItem('role', data.role)
        if (data.courierId) {
            await AsyncStorage.setItem('courierId', data.courierId)
        }
        setUser(data)
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

export const useAuth = () => useContext(AuthContext)