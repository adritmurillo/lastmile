import { registerRootComponent } from 'expo'
import { useState } from 'react'
import { ActivityIndicator, StyleSheet, View } from 'react-native'
import { AuthProvider, useAuth } from './src/context/AuthContext'
import LoginScreen from './src/screens/LoginScreen'
import RouteScreen from './src/screens/RouteScreen'
import StopDetailScreen from './src/screens/StopDetailScreen'
import type { Stop } from './src/types'

type Screen = 'route' | 'stopDetail'

const styles = StyleSheet.create({
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
})

function AppNavigator() {
  const { user, loading } = useAuth()
  const [screen, setScreen] = useState<Screen>('route')
  const [selectedStop, setSelectedStop] = useState<Stop | null>(null)
  const [selectedRouteId, setSelectedRouteId] = useState<string>('')

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#007aff" />
      </View>
    )
  }

  if (!user) {
    return <LoginScreen />
  }

  if (screen === 'stopDetail' && selectedStop) {
    return (
      <StopDetailScreen
        stop={selectedStop}
        routeId={selectedRouteId}
        onBack={() => setScreen('route')}
        onComplete={() => setScreen('route')}
      />
    )
  }

  return (
    <RouteScreen
      onSelectStop={(stop, routeId) => {
        setSelectedStop(stop)
        setSelectedRouteId(routeId)
        setScreen('stopDetail')
      }}
    />
  )
}

function App() {
  return (
    <AuthProvider>
      <AppNavigator />
    </AuthProvider>
  )
}

export default registerRootComponent(App)