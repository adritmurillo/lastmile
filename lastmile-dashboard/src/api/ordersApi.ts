import api from './axiosConfig'
import type { ApiResponse, Order } from '../types';

export const ordersApi = {
  getOrders: async (params?: { status?: string; date?: string; priority?: string }) => {
    const response = await api.get<ApiResponse<Order[]>>('/orders', { params })
    return response.data
  },

  createOrder: async (order: Partial<Order>) => {
    const response = await api.post<ApiResponse<Order>>('/orders', order)
    return response.data
  },

  getOrderByTracking: async (trackingCode: string) => {
    const response = await api.get<ApiResponse<Order>>(`/orders/tracking/${trackingCode}`)
    return response.data
  },
  getByTracking: async (trackingCode: string) => {
    const response = await api.get<ApiResponse<Order>>(`/orders/tracking/${trackingCode}`)
    return response.data
  },
  uploadFile: async (file: File, uploadedBy: string) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('uploadedBy', uploadedBy)
    const response = await api.post<ApiResponse<string>>('/orders/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return response.data
  },
  cancelOrder: async (id: string) => {
    const response = await api.patch<ApiResponse<Order>>(`/orders/${id}/cancel`)
    return response.data
  },

  getById: async (id: string) => {
    const response = await api.get<ApiResponse<Order>>(`/orders/${id}`)
    return response.data
  },

}