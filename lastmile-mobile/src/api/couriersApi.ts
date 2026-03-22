import type { Courier } from '../types'
import apiClient from './apiClient'

export const couriersApi = {
  getById: async (id: string) => {
    const response = await apiClient.get<{ data: Courier }>(`/couriers/${id}`)
    return response.data
  },

  update: async (id: string, data: {
    firstName: string
    lastName: string
    documentNumber: string
    phone: string
  }) => {
    const response = await apiClient.put<{ data: Courier }>(`/couriers/${id}`, data)
    return response.data
  },

  updatePhone: async (id: string, phone: string) => {
  const response = await apiClient.patch<{ data: string }>(`/couriers/${id}/phone`, { phone })
  return response.data
},
}