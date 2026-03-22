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
  status: 'PENDING' | 'DELIVERED' | 'FAILED'
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
  status: string
  totalStops: number
  deliveredCount: number
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