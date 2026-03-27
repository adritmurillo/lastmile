import { create } from 'zustand'
import type { NotificationMessage } from '../types'

interface NotificationStore {
  // Last received notification for each type
  lastCloseRequest: NotificationMessage | null
  lastOrderUpdate: NotificationMessage | null
  
  // Trigger counters to force re-renders
  closeRequestTrigger: number
  orderUpdateTrigger: number
  
  // Actions
  notifyCloseRequest: (message: NotificationMessage) => void
  notifyOrderUpdate: (message: NotificationMessage) => void
  clearCloseRequest: () => void
  clearOrderUpdate: () => void
}

export const useNotificationStore = create<NotificationStore>((set) => ({
  lastCloseRequest: null,
  lastOrderUpdate: null,
  closeRequestTrigger: 0,
  orderUpdateTrigger: 0,

  notifyCloseRequest: (message) =>
    set((state) => ({
      lastCloseRequest: message,
      closeRequestTrigger: state.closeRequestTrigger + 1,
    })),

  notifyOrderUpdate: (message) =>
    set((state) => ({
      lastOrderUpdate: message,
      orderUpdateTrigger: state.orderUpdateTrigger + 1,
    })),

  clearCloseRequest: () =>
    set({ lastCloseRequest: null }),

  clearOrderUpdate: () =>
    set({ lastOrderUpdate: null }),
}))
