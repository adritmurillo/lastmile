import { registerRootComponent } from 'expo'
import { useRef, useState } from 'react'
import { ActivityIndicator, Animated, Dimensions, StyleSheet, View } from 'react-native'
import { GestureHandlerRootView, PanGestureHandler, State } from 'react-native-gesture-handler'
import { AuthProvider, useAuth } from './src/context/AuthContext'
import LoginScreen from './src/screens/LoginScreen'
import ProfileScreen from './src/screens/ProfileScreen'
import RouteScreen from './src/screens/RouteScreen'
import StopDetailScreen from './src/screens/StopDetailScreen'
import type { Stop } from './src/types'


const { width } = Dimensions.get('window')

const styles = StyleSheet.create({
  root: { flex: 1 },
  container: { flex: 1, overflow: 'hidden', backgroundColor: '#f2f2f7' },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#f2f2f7' },
  screen: { ...StyleSheet.absoluteFillObject, backgroundColor: '#f2f2f7' },
})

type Screen = 'route' | 'stopDetail' | 'profile'

function AppNavigator() {
  const { user, loading } = useAuth()
  const [screen, setScreen] = useState<Screen>('route')
  const [selectedStop, setSelectedStop] = useState<Stop | null>(null)
  const [selectedRouteId, setSelectedRouteId] = useState<string>('')
  
  // posición del detail screen: 0 = visible, width = fuera a la derecha
  const detailX = useRef(new Animated.Value(width)).current
  // posición del route screen: 0 = normal, -width*0.3 = empujado a la izquierda
  const routeX = useRef(new Animated.Value(0)).current
  // valor del gesto en curso
  const gestureX = useRef(new Animated.Value(0)).current

  const navigateTo = (newScreen: Screen) => {
    setScreen(newScreen)
    gestureX.setValue(0)
    Animated.parallel([
      Animated.spring(detailX, {
        toValue: 0,
        useNativeDriver: true,
        tension: 65,
        friction: 11,
      }),
      Animated.spring(routeX, {
        toValue: -width * 0.3,
        useNativeDriver: true,
        tension: 65,
        friction: 11,
      }),
    ]).start()
  }

  const navigateBack = (velocity = 0) => {
    Animated.parallel([
      Animated.spring(detailX, {
        toValue: width,
        useNativeDriver: true,
        tension: 65,
        friction: 11,
        velocity,
      }),
      Animated.spring(routeX, {
        toValue: 0,
        useNativeDriver: true,
        tension: 65,
        friction: 11,
      }),
    ]).start(() => {
      setScreen('route')
      gestureX.setValue(0)
    })
  }

  const cancelSwipe = () => {
    Animated.parallel([
      Animated.spring(detailX, {
        toValue: 0,
        useNativeDriver: true,
        tension: 65,
        friction: 11,
      }),
      Animated.spring(routeX, {
        toValue: -width * 0.3,
        useNativeDriver: true,
        tension: 65,
        friction: 11,
      }),
    ]).start()
    gestureX.setValue(0)
  }

  const onGestureEvent = Animated.event(
    [{ nativeEvent: { translationX: gestureX } }],
    { useNativeDriver: true }
  )

  const onHandlerStateChange = (event: any) => {
    if (event.nativeEvent.state === State.END) {
      const { translationX, velocityX } = event.nativeEvent
      if (translationX > width * 0.3 || velocityX > 800) {
        navigateBack(velocityX)
      } else {
        cancelSwipe()
      }
    }
  }

  // Durante el gesto el detail se mueve con el dedo (solo cuando > 0)
  const detailTranslate = Animated.add(
    detailX,
    gestureX.interpolate({
      inputRange: [-width, 0, width],
      outputRange: [0, 0, width],
      extrapolate: 'clamp',
    })
  )

  // El route se mueve inversamente durante el gesto
  const routeTranslate = Animated.add(
    routeX,
    gestureX.interpolate({
      inputRange: [0, width],
      outputRange: [0, width * 0.3],
      extrapolate: 'clamp',
    })
  )

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#007aff" />
      </View>
    )
  }

  if (!user) return <LoginScreen />

  return (
    <View style={[styles.container, { backgroundColor: '#f2f2f7' }]}>
      <Animated.View
        style={[
          styles.screen,
          {
            transform: [{ translateX: routeTranslate }],
            backgroundColor: '#f2f2f7',
          },
        ]}
      >
        <RouteScreen
          onSelectStop={(stop, routeId) => {
            setSelectedStop(stop)
            setSelectedRouteId(routeId)
            navigateTo('stopDetail')
          }}
          onProfile={()=> navigateTo('profile')}
        />
      </Animated.View>

      {screen === 'stopDetail' && selectedStop && (
        <PanGestureHandler
          onGestureEvent={onGestureEvent}
          onHandlerStateChange={onHandlerStateChange}
          activeOffsetX={10}
          failOffsetY={[-20, 20]}
          enabled={screen === 'stopDetail'}
        >
          <Animated.View
            style={[styles.screen, { transform: [{ translateX: detailTranslate }] }]}
          >
            <StopDetailScreen
              stop={selectedStop}
              routeId={selectedRouteId}
              onBack={() => navigateBack()}
              onComplete={() => navigateBack()}
            />
          </Animated.View>
        </PanGestureHandler>
      )}

      {screen === 'profile' && (
        <PanGestureHandler
          onGestureEvent={onGestureEvent}
          onHandlerStateChange={onHandlerStateChange}
          activeOffsetX={10}
          failOffsetY={[-20, 20]}
          enabled={screen === 'profile'}
        >
          <Animated.View
            style={[styles.screen, { transform: [{ translateX: detailTranslate }] }]}
          >
            <ProfileScreen onBack={() => navigateBack()} />
          </Animated.View>
        </PanGestureHandler>
      )}
    </View>
  )
}

function App() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <AuthProvider>
        <AppNavigator />
      </AuthProvider>
    </GestureHandlerRootView>
  )
}

export default registerRootComponent(App)