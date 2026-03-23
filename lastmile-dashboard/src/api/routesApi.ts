import api from './axiosConfig'
import type { ApiResponse, Route } from '../types'

export const routesApi = {
  getRoutesByDate: async (date: string) => {
    const response = await api.get<ApiResponse<Route[]>>('/routes', { params: { date } })
    return response.data
  },

  closeRoute: async (routeId: string, reason: string) => {
    const response = await api.post<ApiResponse<any>>(`/routes/${routeId}/close`, null, {
      params: { reason }
    })
    return response.data
  },

  generateProposal: async (date: string) => {
    const response = await api.post<ApiResponse<Route[]>>(`/dispatch/proposal`, null, {
      params: { date },
    })
    return response.data
  },

  confirmRoutes: async (date: string) => {
    const response = await api.post<ApiResponse<Route[]>>(`/dispatch/routes/confirm`, null, {
      params: { date },
    })
    return response.data
  },
  moveOrderToCourier: async (orderId: string, targetCourierId: string) => {
    const response = await api.patch<ApiResponse<any>>(
      `/dispatch/routes/move-order`,
      { orderId, targetCourierId }
    )
    return response.data
  },
}