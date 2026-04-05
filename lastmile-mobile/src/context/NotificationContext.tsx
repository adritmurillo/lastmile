import AsyncStorage from '@react-native-async-storage/async-storage'
import { Client, IMessage } from '@stomp/stompjs'
import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react'
import { Alert, Vibration } from 'react-native'
import { useAuth } from './AuthContext'
import type { NotificationMessage } from '../types'

// API base URL - extract host for WebSocket
const API_HOST = '192.168.18.6:8080'
// Use /ws-native endpoint (without SockJS) for native WebSocket clients
const WS_URL = `ws://${API_HOST}/ws-native`

interface NotificationContextType {
  connected: boolean
  lastNotification: NotificationMessage | null
  onCloseRequestApproved: (() => void) | null
  setOnCloseRequestApproved: (callback: (() => void) | null) => void
  onCloseRequestRejected: (() => void) | null
  setOnCloseRequestRejected: (callback: (() => void) | null) => void
}

const NotificationContext = createContext<NotificationContextType | null>(null)

export function NotificationProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth()
  const [connected, setConnected] = useState(false)
  const [lastNotification, setLastNotification] = useState<NotificationMessage | null>(null)
  
  // Callbacks for close request events
  const onCloseRequestApprovedRef = useRef<(() => void) | null>(null)
  const onCloseRequestRejectedRef = useRef<(() => void) | null>(null)
  
  const clientRef = useRef<Client | null>(null)
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null)

  const setOnCloseRequestApproved = useCallback((callback: (() => void) | null) => {
    onCloseRequestApprovedRef.current = callback
  }, [])

  const setOnCloseRequestRejected = useCallback((callback: (() => void) | null) => {
    onCloseRequestRejectedRef.current = callback
  }, [])

  const handleNotification = useCallback((notification: NotificationMessage) => {
    setLastNotification(notification)
    
    // Vibrate on notification
    Vibration.vibrate(200)

    switch (notification.type) {
      case 'ROUTE_CLOSE_APPROVED':
        Alert.alert(
          'Solicitud Aprobada',
          notification.body,
          [{ text: 'OK', onPress: () => onCloseRequestApprovedRef.current?.() }]
        )
        break

      case 'ROUTE_CLOSE_REJECTED':
        Alert.alert(
          'Solicitud Rechazada',
          notification.body + (notification.data?.dispatcherNotes 
            ? `\n\nNotas: ${notification.data.dispatcherNotes}` 
            : ''),
          [{ text: 'OK', onPress: () => onCloseRequestRejectedRef.current?.() }]
        )
        break

      default:
        // For other notification types, just show a simple alert
        if (notification.title && notification.body) {
          Alert.alert(notification.title, notification.body)
        }
        break
    }
  }, [])

  const connect = useCallback(async () => {
    if (!user?.courierId || !user?.token) {
      return
    }

    // Clear any existing reconnect timeout
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current)
      reconnectTimeoutRef.current = null
    }

    // Disconnect existing client if any
    if (clientRef.current?.active) {
      clientRef.current.deactivate()
    }

    const token = await AsyncStorage.getItem('token')
    if (!token) return

    const client = new Client({
      brokerURL: WS_URL,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str) => {
        if (__DEV__) {
          console.log('[STOMP]', str)
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      
      onConnect: () => {
        console.log('[WebSocket] Connected')
        setConnected(true)

        // Subscribe to courier-specific topic
        client.subscribe(
          `/topic/courier/${user.courierId}`,
          (message: IMessage) => {
            try {
              const notification: NotificationMessage = JSON.parse(message.body)
              console.log('[WebSocket] Received notification:', notification.type)
              handleNotification(notification)
            } catch (error) {
              console.error('[WebSocket] Error parsing message:', error)
            }
          }
        )
      },

      onDisconnect: () => {
        console.log('[WebSocket] Disconnected')
        setConnected(false)
      },

      onStompError: (frame) => {
        console.error('[WebSocket] STOMP error:', frame.headers['message'])
        setConnected(false)
      },

      onWebSocketClose: () => {
        console.log('[WebSocket] Connection closed')
        setConnected(false)
      },

      onWebSocketError: (event) => {
        console.error('[WebSocket] WebSocket error:', event)
        setConnected(false)
      },
    })

    clientRef.current = client

    try {
      client.activate()
    } catch (error) {
      console.error('[WebSocket] Activation error:', error)
      // Schedule reconnect
      reconnectTimeoutRef.current = setTimeout(() => {
        connect()
      }, 5000)
    }
  }, [user?.courierId, user?.token, handleNotification])

  const disconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current)
      reconnectTimeoutRef.current = null
    }
    
    if (clientRef.current?.active) {
      clientRef.current.deactivate()
      clientRef.current = null
    }
    
    setConnected(false)
  }, [])

  // Connect when user logs in, disconnect when user logs out
  useEffect(() => {
    if (user?.courierId && user?.token) {
      connect()
    } else {
      disconnect()
    }

    return () => {
      disconnect()
    }
  }, [user?.courierId, user?.token, connect, disconnect])

  return (
    <NotificationContext.Provider
      value={{
        connected,
        lastNotification,
        onCloseRequestApproved: onCloseRequestApprovedRef.current,
        setOnCloseRequestApproved,
        onCloseRequestRejected: onCloseRequestRejectedRef.current,
        setOnCloseRequestRejected,
      }}
    >
      {children}
    </NotificationContext.Provider>
  )
}

export function useNotifications() {
  const context = useContext(NotificationContext)
  if (!context) throw new Error('useNotifications must be used within NotificationProvider')
  return context
}
