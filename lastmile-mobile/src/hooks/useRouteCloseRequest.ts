import * as ImagePicker from 'expo-image-picker'
import { useState } from 'react'
import { Alert } from 'react-native'
import { routesApi } from '../api/routesApi'
import { useAuth } from '../context/AuthContext'
import type { RouteCloseReason, RouteCloseRequest } from '../types'

export function useRouteCloseRequest(routeId: string | undefined, onSuccess: () => void) {
  const { user } = useAuth()
  const [modalVisible, setModalVisible] = useState(false)
  const [loading, setLoading] = useState(false)
  const [selectedReason, setSelectedReason] = useState<RouteCloseReason | null>(null)
  const [message, setMessage] = useState('')
  const [photoUri, setPhotoUri] = useState<string | null>(null)
  const [uploadingPhoto, setUploadingPhoto] = useState(false)
  const [pendingRequest, setPendingRequest] = useState<RouteCloseRequest | null>(null)
  const [checkingPending, setCheckingPending] = useState(false)

  const checkPendingRequest = async () => {
    if (!routeId) return
    setCheckingPending(true)
    try {
      const request = await routesApi.getPendingCloseRequest(routeId)
      setPendingRequest(request)
    } catch {
      setPendingRequest(null)
    } finally {
      setCheckingPending(false)
    }
  }

  const openModal = () => {
    setSelectedReason(null)
    setMessage('')
    setPhotoUri(null)
    setModalVisible(true)
  }

  const closeModal = () => {
    setSelectedReason(null)
    setMessage('')
    setPhotoUri(null)
    setModalVisible(false)
  }

  const handleTakePhoto = async () => {
    const permission = await ImagePicker.requestCameraPermissionsAsync()
    if (!permission.granted) {
      Alert.alert('Permiso requerido', 'Necesitamos acceso a tu camara para tomar la foto')
      return
    }

    const result = await ImagePicker.launchCameraAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.7,
      allowsEditing: false,
    })

    if (!result.canceled && result.assets[0]) {
      setPhotoUri(result.assets[0].uri)
    }
  }

  const handleRemovePhoto = () => {
    setPhotoUri(null)
  }

  const handleSubmit = async () => {
    if (!selectedReason) {
      Alert.alert('Requerido', 'Selecciona el motivo del cierre')
      return
    }
    if (!message.trim()) {
      Alert.alert('Requerido', 'Describe el motivo del cierre')
      return
    }
    if (!routeId || !user?.courierId) {
      Alert.alert('Error', 'No se pudo identificar la ruta')
      return
    }

    Alert.alert(
      'Solicitar cierre',
      'Se notificara al despachador para que apruebe tu solicitud. Los paquetes pendientes deberan ser devueltos al almacen.',
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Enviar solicitud',
          onPress: async () => {
            setLoading(true)
            try {
              let uploadedPhotoUrl: string | undefined

              if (photoUri) {
                setUploadingPhoto(true)
                uploadedPhotoUrl = await routesApi.uploadDeliveryPhoto(photoUri)
                setUploadingPhoto(false)
              }

              await routesApi.createCloseRequest({
                routeId,
                courierId: user.courierId!,
                reason: selectedReason,
                message: message.trim(),
                photoUrl: uploadedPhotoUrl,
              })

              closeModal()
              Alert.alert(
                'Solicitud enviada',
                'El despachador revisara tu solicitud. Te notificaremos cuando sea procesada.',
                [{ text: 'Entendido', onPress: onSuccess }]
              )
            } catch (error: any) {
              const errorMsg = error?.response?.data?.error || 'No se pudo enviar la solicitud'
              Alert.alert('Error', errorMsg)
            } finally {
              setLoading(false)
              setUploadingPhoto(false)
            }
          }
        }
      ]
    )
  }

  return {
    modalVisible,
    loading,
    uploadingPhoto,
    selectedReason,
    setSelectedReason,
    message,
    setMessage,
    photoUri,
    pendingRequest,
    checkingPending,
    openModal,
    closeModal,
    handleTakePhoto,
    handleRemovePhoto,
    handleSubmit,
    checkPendingRequest,
  }
}
