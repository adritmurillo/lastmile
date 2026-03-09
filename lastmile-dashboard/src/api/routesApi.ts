import api from './axiosConfig'
import type { ApiResponse, Route } from '../types'

export const routesApi = {
  getRoutesByDate: async (date: string) => {
    const response = await api.get<ApiResponse<Route[]>>('/routes', { params: { date } })
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
}