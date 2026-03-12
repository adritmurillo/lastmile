import type { AuthResponse, LoginRequest } from '../types'
import apiClient from './apiClient'

export const authApi = {
  login: async (data: LoginRequest) => {
    const response = await apiClient.post<{ data: AuthResponse }>('/auth/login', data)
    return response.data.data
  }
}