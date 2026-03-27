export interface LoginRequest {
  username: string
  password: string
}

export interface AuthResponse {
  token: string
  username: string
  role: string
  courierId?: string
}

export interface Stop {
  id: string
  stopOrder: number
  status: 'PENDING' | 'DELIVERED' | 'FAILED' | 'SKIPPED'
  order: {
    id: string
    trackingCode: string
    recipientName: string
    recipientPhone: string
    addressText: string
    weightKg: number
    priority: string
    deliveryDeadline: string
    latitude?: number
    longitude?: number
    status: 'PENDING' | 'READY_TO_DISPATCH' | 'ASSIGNED' | 'PICKED_UP' | 'IN_TRANSIT' | 'DELIVERED' | 'FAILED' | 'SKIPPED' | 'RETURNED_TO_WAREHOUSE'
  }
}

export interface Courier {
  id: string
  firstName: string
  lastName: string
  fullName: string
  documentNumber: string
  phone: string
  status: 'ACTIVE' | 'INACTIVE' | 'ON_VACATION'
  vehicle?: {
    id: string
    licensePlate: string
    type: string
    maxWeightKg: number
    maxVolumeCm3: number
    status: string
  }
}


export interface Route {
  id: string
  date: string
  status: 'PENDING' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CLOSED'
  totalStops: number
  deliveredCount: number
  failedCount: number
  pendingCount: number
  completionPercentage: number
  stops: Stop[]
  courier: {
    id: string
    fullName: string
    vehicle: {
      licensePlate: string
      type: string
    }
  }
}

export interface HistoryStats {
  totalRoutes: number
  totalDelivered: number
  totalFailed: number
  successRate: number
}

export interface PickupStatus {
  totalPackages: number
  scannedPackages: number
  pendingPackages: number
  readyToStart: boolean
}

export interface Order {
  id: string
  trackingCode: string
  recipientName: string
  recipientPhone: string
  addressText: string
  status: 'PENDING' | 'READY_TO_DISPATCH' | 'ASSIGNED' | 'PICKED_UP' | 'IN_TRANSIT' | 'DELIVERED' | 'FAILED' | 'SKIPPED' | 'RETURNED_TO_WAREHOUSE'
  weightKg: number
  priority: string
  deliveryDeadline: string
}

// Route Close Request types
export type RouteCloseReason = 
  | 'END_OF_SHIFT'
  | 'VEHICLE_BREAKDOWN'
  | 'COURIER_ILLNESS'
  | 'WEATHER_CONDITIONS'
  | 'SECURITY_ISSUE'
  | 'OTHER'

export type CloseRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface RouteCloseRequest {
  id: string
  routeId: string
  courierId: string
  reason: RouteCloseReason
  message: string
  photoUrl?: string
  status: CloseRequestStatus
  dispatcherId?: string
  dispatcherNotes?: string
  createdAt: string
  processedAt?: string
}

export interface CreateRouteCloseRequest {
  routeId: string
  courierId: string
  reason: RouteCloseReason
  message: string
  photoUrl?: string
}

export const ROUTE_CLOSE_REASONS: { key: RouteCloseReason; label: string; emoji: string }[] = [
  { key: 'END_OF_SHIFT', label: 'Fin de turno', emoji: '🕐' },
  { key: 'VEHICLE_BREAKDOWN', label: 'Avería del vehículo', emoji: '🚗' },
  { key: 'COURIER_ILLNESS', label: 'Enfermedad', emoji: '🤒' },
  { key: 'WEATHER_CONDITIONS', label: 'Condiciones climáticas', emoji: '⛈️' },
  { key: 'SECURITY_ISSUE', label: 'Problema de seguridad', emoji: '⚠️' },
  { key: 'OTHER', label: 'Otro motivo', emoji: '📝' },
]

// WebSocket notification types
export type NotificationType = 
  | 'ROUTE_CLOSE_REQUESTED'
  | 'ROUTE_CLOSE_APPROVED'
  | 'ROUTE_CLOSE_REJECTED'
  | 'NEW_ORDER_CREATED'
  | 'ORDER_DELIVERED'
  | 'ORDER_FAILED'
  | 'ROUTE_STARTED'
  | 'ROUTE_COMPLETED'
  | 'COURIER_ARRIVED_AT_STOP'
  | 'SYSTEM_ALERT'

export interface NotificationMessage {
  type: NotificationType
  title: string
  body: string
  data: Record<string, unknown>
  timestamp: string
}