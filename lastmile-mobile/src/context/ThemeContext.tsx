import AsyncStorage from '@react-native-async-storage/async-storage'
import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { useColorScheme } from 'react-native'

// Color palettes
export const lightColors = {
  // Backgrounds
  background: '#f2f2f7',
  backgroundSecondary: '#fff',
  card: '#fff',
  cardSecondary: '#f8f8f8',
  
  // Text
  text: '#1c1c1e',
  textSecondary: '#8e8e93',
  textMuted: '#c7c7cc',
  
  // Primary
  primary: '#007aff',
  primaryLight: '#e8f4ff',
  
  // Status colors
  success: '#34c759',
  successLight: '#e8f8ed',
  warning: '#ff9500',
  warningLight: '#fff3e0',
  error: '#ff3b30',
  errorLight: '#ffeeed',
  purple: '#5856d6',
  purpleLight: '#f0efff',
  
  // Borders and separators
  border: '#e5e5ea',
  separator: '#e5e5ea',
  
  // Input
  inputBackground: '#f2f2f7',
  inputBorder: '#e5e5ea',
  placeholder: '#c7c7cc',
  
  // StatusBar
  statusBar: 'dark-content' as const,
}

export const darkColors = {
  // Backgrounds
  background: '#000000',
  backgroundSecondary: '#1c1c1e',
  card: '#1c1c1e',
  cardSecondary: '#2c2c2e',
  
  // Text
  text: '#ffffff',
  textSecondary: '#8e8e93',
  textMuted: '#636366',
  
  // Primary
  primary: '#0a84ff',
  primaryLight: '#1a3a5c',
  
  // Status colors
  success: '#30d158',
  successLight: '#1a3d2a',
  warning: '#ff9f0a',
  warningLight: '#3d3020',
  error: '#ff453a',
  errorLight: '#3d1f1d',
  purple: '#bf5af2',
  purpleLight: '#2d1f3d',
  
  // Borders and separators
  border: '#38383a',
  separator: '#38383a',
  
  // Input
  inputBackground: '#2c2c2e',
  inputBorder: '#38383a',
  placeholder: '#636366',
  
  // StatusBar
  statusBar: 'light-content' as const,
}

export type ThemeColors = {
  background: string
  backgroundSecondary: string
  card: string
  cardSecondary: string
  text: string
  textSecondary: string
  textMuted: string
  primary: string
  primaryLight: string
  success: string
  successLight: string
  warning: string
  warningLight: string
  error: string
  errorLight: string
  purple: string
  purpleLight: string
  border: string
  separator: string
  inputBackground: string
  inputBorder: string
  placeholder: string
  statusBar: 'dark-content' | 'light-content'
}

export type ThemeMode = 'light' | 'dark' | 'system'

interface ThemeContextType {
  colors: ThemeColors
  isDark: boolean
  themeMode: ThemeMode
  setThemeMode: (mode: ThemeMode) => void
}

const ThemeContext = createContext<ThemeContextType | null>(null)

const THEME_STORAGE_KEY = '@lastmile_theme_mode'

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const systemColorScheme = useColorScheme()
  const [themeMode, setThemeModeState] = useState<ThemeMode>('system')
  const [isLoaded, setIsLoaded] = useState(false)

  // Load saved theme preference
  useEffect(() => {
    AsyncStorage.getItem(THEME_STORAGE_KEY).then((savedMode) => {
      if (savedMode === 'light' || savedMode === 'dark' || savedMode === 'system') {
        setThemeModeState(savedMode)
      }
      setIsLoaded(true)
    })
  }, [])

  const setThemeMode = useCallback((mode: ThemeMode) => {
    setThemeModeState(mode)
    AsyncStorage.setItem(THEME_STORAGE_KEY, mode)
  }, [])

  // Determine if dark mode is active
  const isDark = themeMode === 'system' 
    ? systemColorScheme === 'dark'
    : themeMode === 'dark'

  const colors = isDark ? darkColors : lightColors

  // Don't render until we've loaded the theme preference
  if (!isLoaded) {
    return null
  }

  return (
    <ThemeContext.Provider value={{ colors, isDark, themeMode, setThemeMode }}>
      {children}
    </ThemeContext.Provider>
  )
}

export function useTheme() {
  const context = useContext(ThemeContext)
  if (!context) throw new Error('useTheme must be used within ThemeProvider')
  return context
}
