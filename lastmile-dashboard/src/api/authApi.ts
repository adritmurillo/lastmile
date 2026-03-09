import api from './axiosConfig'
import type { ApiResponse } from '../types'

export const authApi = {
  login: async (username: string, password: string) => {
    const response = await api.post<ApiResponse<{ token: string }>>('/auth/login', {
      username,
      password,
    })
    return response.data
  },
}