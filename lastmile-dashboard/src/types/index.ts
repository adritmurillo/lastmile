export interface User {
  id: string
  username: string
  email: string
  role: 'ADMIN' | 'DISPATCHER' | 'COURIER'
}

export interface AuthState {
  token: string | null
  user: User | null
}

export interface ApiResponse<T> {
  success: boolean
  data: T
  error?: string
  timestamp: string
}

export interface Order {
  id: string
  trackingCode: string
  externalTrackingCode: string
  recipientName: string
  recipientPhone: string
  recipientEmail?: string
  addressText: string
  weightKg: number
  volumeCm3: number
  priority: 'STANDARD' | 'EXPRESS'
  status: 'PENDING' | 'ASSIGNED' | 'IN_TRANSIT' | 'DELIVERED' | 'FAILED' | 'CANCELLED'
  deliveryAttempts: number
  deliveryDeadline: string
  createdAt: string
  notes?: string
}

export interface Stats {
  totalOrders: number
  pendingOrders: number
  assignedOrders: number
  inTransitOrders: number
  deliveredOrders: number
  failedOrders: number
  successRate: number
  totalRoutes: number
  inProgressRoutes: number
  completedRoutes: number
  totalCouriers: number
  activeCouriers: number
}

export interface Courier {
  id: string
  fullName: string
  documentNumber: string
  phone: string
  status: 'ACTIVE' | 'INACTIVE'
  vehicle?: Vehicle
}

export interface Vehicle {
  id: string
  licensePlate: string
  type: string
  maxWeightKg: number
  maxVolumeCm3: number
  status: string
}

export interface Route {
  id: string
  courier: Courier
  date: string
  status: 'PENDING' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED'
  stops: Stop[]
  totalStops: number
  deliveredCount: number
  failedCount: number
  pendingCount: number
  completionPercentage: number
}

export interface Stop {
  id: string
  order: Order
  stopOrder: number
  status: 'PENDING' | 'DELIVERED' | 'FAILED'
}