import type { Route, Stop } from '../types'
import apiClient from './apiClient'

export const routesApi = {
  getMiRuta: async (courierId: string) => {
    const response = await apiClient.get<{ data: Route }>('/routes/my-route', {
      params: { courierId }
    })
    return response.data.data
  },

  getPendingStops: async (courierId: string) => {
    const response = await apiClient.get<{ data: Stop[] }>('/routes/my-pending', {
      params: { courierId }
    })
    return response.data.data
  },

  startRoute: async (routeId: string) => {
    const response = await apiClient.post<{ data: Route }>(`/routes/${routeId}/start`)
    return response.data.data
  },

  deliverStop: async (stopId: string, proofPhotoUrl?: string | null) => {
    const response = await apiClient.post<{ data: any }>(`/routes/stops/${stopId}/deliver`, {
      proofPhotoUrl: proofPhotoUrl ?? null
    })
    return response.data.data
  },

  failStop: async (stopId: string, reason: string, failureNotes?: string) => {
    const response = await apiClient.post<{ data: any }>(`/routes/stops/${stopId}/fail`, {
      reason,
      failureNotes
    })
    return response.data.data
  },

  uploadDeliveryPhoto: async (imageUri: string): Promise<string> => {
  const formData = new FormData()
  formData.append('file', {
    uri: imageUri,
    type: 'image/jpeg',
    name: 'delivery.jpg',
  } as any)
  formData.append('upload_preset', 'lastmile_delivery')
  formData.append('cloud_name', 'dxdj5zgse')

  const response = await fetch(
    'https://api.cloudinary.com/v1_1/dxdj5zgse/image/upload',
    { method: 'POST', body: formData }
  )
  const data = await response.json()
  return data.secure_url
},
}