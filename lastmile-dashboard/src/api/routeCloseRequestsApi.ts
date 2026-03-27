import api from './axiosConfig'
import type { ApiResponse, RouteCloseRequest } from '../types'

export const routeCloseRequestsApi = {
  getPendingRequests: async () => {
    const response = await api.get<ApiResponse<RouteCloseRequest[]>>('/route-close-requests/pending')
    return response.data
  },

  getById: async (requestId: string) => {
    const response = await api.get<ApiResponse<RouteCloseRequest>>(`/route-close-requests/${requestId}`)
    return response.data
  },

  getPendingForRoute: async (routeId: string) => {
    const response = await api.get<ApiResponse<RouteCloseRequest | null>>(`/route-close-requests/route/${routeId}/pending`)
    return response.data
  },

  approve: async (requestId: string, dispatcherId: string) => {
    const response = await api.post<ApiResponse<RouteCloseRequest>>(
      `/route-close-requests/${requestId}/approve`,
      null,
      { params: { dispatcherId } }
    )
    return response.data
  },

  reject: async (requestId: string, dispatcherId: string) => {
    const response = await api.post<ApiResponse<RouteCloseRequest>>(
      `/route-close-requests/${requestId}/reject`,
      null,
      { params: { dispatcherId } }
    )
    return response.data
  },
}
