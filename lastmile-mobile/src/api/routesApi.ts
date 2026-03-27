import type { CreateRouteCloseRequest, Order, PickupStatus, Route, RouteCloseRequest, Stop } from '../types'
import apiClient from './apiClient'

export const routesApi = {
  getHistory: async (courierId: string): Promise<Route[]> => {
    const response = await apiClient.get<{ data: Route[] }>('/routes/my-history', {
      params: { courierId }
    })
    return response.data.data
  },

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

  scanPickup: async (routeId: string, trackingCode: string): Promise<Order> => {
    const response = await apiClient.post<{ data: Order }>(
      `/routes/${routeId}/pickup-scan`,
      { trackingCode }
    )
    return response.data.data
  },

  getPickupStatus: async (routeId: string): Promise<PickupStatus> => {
    const response = await apiClient.get<{ data: PickupStatus }>(
      `/routes/${routeId}/pickup-status`
    )
    return response.data.data
  },

  startRoute: async (routeId: string) => {
    const response = await apiClient.post<{ data: Route }>(`/routes/${routeId}/start`)
    return response.data.data
  },

  deliverStop: async (stopId: string, photoUrls?: string[]) => {
    const response = await apiClient.post<{ data: any }>(`/routes/stops/${stopId}/deliver`, {
      photoUrls: photoUrls ?? []
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

  // Route Close Request methods
  createCloseRequest: async (request: CreateRouteCloseRequest): Promise<RouteCloseRequest> => {
    const response = await apiClient.post<{ data: RouteCloseRequest }>(
      '/route-close-requests',
      request
    )
    return response.data.data
  },

  getPendingCloseRequest: async (routeId: string): Promise<RouteCloseRequest | null> => {
    try {
      const response = await apiClient.get<{ data: RouteCloseRequest }>(
        `/route-close-requests/route/${routeId}/pending`
      )
      return response.data.data
    } catch {
      return null
    }
  },

}
