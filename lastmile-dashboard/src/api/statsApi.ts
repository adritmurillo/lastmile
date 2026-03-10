import api from './axiosConfig'
import type { ApiResponse, Stats } from '../types'

export const statsApi = {
  getTodayStats: async () => {
    const response = await api.get<ApiResponse<Stats>>('/stats/today')
    return response.data
  },
  getStatsByPeriod: async (startDate: string, endDate: string) => {
    const response = await api.get<ApiResponse<Stats>>('/stats/period', {
        params: { startDate, endDate }
    })
    return response.data
},
}