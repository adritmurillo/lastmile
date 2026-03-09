import api from './axiosConfig'
import type { ApiResponse, Courier, Vehicle} from '../types'

export const couriersApi = {
  getAvailable: async () => {
    const response = await api.get<ApiResponse<Courier[]>>('/couriers/available')
    return response.data
  },
  activate: async (id: string) => {
    const response = await api.patch<ApiResponse<Courier>>(`/couriers/${id}/activate`)
    return response.data
  },

  deactivate: async (id: string) => {
    const response = await api.patch<ApiResponse<Courier>>(`/couriers/${id}/deactivate`)
    return response.data
  },

  register: async (data: { firstName: string; lastName: string; documentNumber: string; phone: string }) => {
    const response = await api.post<ApiResponse<Courier>>('/couriers', data)
    return response.data
  },

  getAll: async () => {
    const response = await api.get<ApiResponse<Courier[]>>('/couriers')
    return response.data
  },

  update: async (id: string, data: { firstName: string; lastName: string; documentNumber: string; phone: string }) => {
    const response = await api.put<ApiResponse<Courier>>(`/couriers/${id}`, data)
    return response.data
  },
  assignVehicle: async (courierId: string, vehicleId: string) => {
    const response = await api.patch<ApiResponse<Courier>>(`/couriers/${courierId}/vehicle`, { vehicleId })
    return response.data
  },

  getVehicles: async () => {
    const response = await api.get<ApiResponse<Vehicle[]>>('/couriers/vehicles')
    return response.data
  },

  registerVehicle: async (data: { licensePlate: string; type: string; maxWeightKg: number; maxVolumeCm3: number }) => {
    const response = await api.post<ApiResponse<Vehicle>>('/couriers/vehicles', data)
    return response.data
  },
}