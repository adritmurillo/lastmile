import { createBrowserRouter, Navigate } from 'react-router-dom'
import LoginPage from '../pages/LoginPage'
import DashboardPage from '../pages/DashboardPage'
import OrdersPage from '../pages/OrdersPage'
import DispatchPage from '../pages/DispatchPage'
import CouriersPage from '../pages/CouriersPage'
import RoutesPage from '../pages/RoutesPage'
import MainLayout from '../components/MainLayout'
import ProtectedRoute from './ProtectedRoute'

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <MainLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'dashboard', element: <DashboardPage /> },
      { path: 'orders', element: <OrdersPage /> },
      { path: 'dispatch', element: <DispatchPage /> },
      { path: 'couriers', element: <CouriersPage /> },
      { path: 'routes', element: <RoutesPage /> },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/login" replace />,
  },
])