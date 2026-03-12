import type { Route } from '../types'
import apiClient from './apiClient'

export const routesApi = {
  getMiRuta: async (courierId: string) => {
    const response = await apiClient.get<{ data: Route }>('/routes/my-route', {
      params: { courierId }
    })
    return response.data.data
  },

  deliverStop: async (stopId: string) => {
    const response = await apiClient.post<{ data: any }>(`/routes/stops/${stopId}/deliver`, {
      proofPhotoUrl: null
    })
    return response.data.data
  },

  failStop: async (stopId: string, reason: string, failureNotes?: string) => {
    const response = await apiClient.post<{ data: any }>(`/routes/stops/${stopId}/fail`, {
      reason,
      failureNotes
    })
    return response.data.data
  }
}