import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import type { IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { notification } from 'antd'
import { BellOutlined } from '@ant-design/icons'
import type { NotificationMessage } from '../types'
import { useAuthStore } from '../store/authStore'
import { useNotificationStore } from '../store/notificationStore'

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws'

interface UseWebSocketOptions {
  onRouteCloseRequested?: (data: NotificationMessage) => void
  onNewOrderCreated?: (data: NotificationMessage) => void
  onOrderDelivered?: (data: NotificationMessage) => void
  onOrderFailed?: (data: NotificationMessage) => void
}

export function useWebSocket(options: UseWebSocketOptions = {}) {
  const clientRef = useRef<Client | null>(null)
  const [connected, setConnected] = useState(false)
  const token = useAuthStore((state) => state.token)
  const user = useAuthStore((state) => state.user)
  const { notifyCloseRequest, notifyOrderUpdate } = useNotificationStore()

  // Store options in ref to avoid re-triggering useEffect
  const optionsRef = useRef(options)
  optionsRef.current = options

  // Store notification functions in refs
  const notifyCloseRequestRef = useRef(notifyCloseRequest)
  notifyCloseRequestRef.current = notifyCloseRequest
  const notifyOrderUpdateRef = useRef(notifyOrderUpdate)
  notifyOrderUpdateRef.current = notifyOrderUpdate

  useEffect(() => {
    // Only connect if user is a dispatcher or admin
    if (!token || !user || (user.role !== 'ADMIN' && user.role !== 'DISPATCHER')) {
      return
    }

    // Prevent duplicate connections
    if (clientRef.current?.active) {
      return
    }

    const playNotificationSound = () => {
      try {
        const audioContext = new (window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext)()
        const oscillator = audioContext.createOscillator()
        const gainNode = audioContext.createGain()
        
        oscillator.connect(gainNode)
        gainNode.connect(audioContext.destination)
        
        oscillator.frequency.value = 800
        oscillator.type = 'sine'
        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.3)
        
        oscillator.start(audioContext.currentTime)
        oscillator.stop(audioContext.currentTime + 0.3)
      } catch {
        // Fallback: do nothing if audio fails
      }
    }

    const showNotification = (msg: NotificationMessage) => {
      playNotificationSound()
      
      notification.open({
        message: msg.title,
        description: msg.body,
        icon: <BellOutlined style={{ color: '#1890ff' }} />,
        placement: 'topRight',
        duration: 5,
      })
    }

    const handleMessage = (message: IMessage, topic: string) => {
      try {
        const msg: NotificationMessage = JSON.parse(message.body)
        console.log(`[WebSocket] Received on ${topic}:`, msg.type)

        // Show toast notification
        showNotification(msg)

        // Update global notification store
        if (msg.type === 'ROUTE_CLOSE_REQUESTED') {
          notifyCloseRequestRef.current(msg)
        } else if (['NEW_ORDER_CREATED', 'ORDER_DELIVERED', 'ORDER_FAILED'].includes(msg.type)) {
          notifyOrderUpdateRef.current(msg)
        }

        // Call specific handlers from options ref
        const opts = optionsRef.current
        switch (msg.type) {
          case 'ROUTE_CLOSE_REQUESTED':
            opts.onRouteCloseRequested?.(msg)
            break
          case 'NEW_ORDER_CREATED':
            opts.onNewOrderCreated?.(msg)
            break
          case 'ORDER_DELIVERED':
            opts.onOrderDelivered?.(msg)
            break
          case 'ORDER_FAILED':
            opts.onOrderFailed?.(msg)
            break
        }
      } catch (error) {
        console.error('[WebSocket] Error parsing message:', error)
      }
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str) => {
        // Only log important events in development
        if (import.meta.env.DEV) {
          if (str.includes('Connected') || str.includes('error') || str.includes('DISCONNECT')) {
            console.log('[STOMP]', str)
          }
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    })

    client.onConnect = () => {
      console.log('[WebSocket] Connected')
      setConnected(true)

      // Subscribe to dispatcher topics
      client.subscribe('/topic/dispatcher/close-requests', (message) => {
        handleMessage(message, '/topic/dispatcher/close-requests')
      })

      client.subscribe('/topic/dispatcher/orders', (message) => {
        handleMessage(message, '/topic/dispatcher/orders')
      })
    }

    client.onDisconnect = () => {
      console.log('[WebSocket] Disconnected')
      setConnected(false)
    }

    client.onStompError = (frame) => {
      console.error('[WebSocket] STOMP error:', frame.headers['message'])
    }

    client.activate()
    clientRef.current = client

    return () => {
      if (clientRef.current?.active) {
        clientRef.current.deactivate()
        clientRef.current = null
      }
    }
  }, [token, user?.id, user?.role]) // Only depend on stable values

  return {
    connected,
    client: clientRef.current,
  }
}
