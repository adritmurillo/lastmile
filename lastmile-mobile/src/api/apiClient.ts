import AsyncStorage from '@react-native-async-storage/async-storage'
import axios from 'axios'

const apiClient = axios.create({
  baseURL: 'http://192.168.18.6:8080/api/v1',
  timeout: 10000,
})

apiClient.interceptors.request.use(async (config) => {
  const token = await AsyncStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default apiClient