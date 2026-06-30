import axios from 'axios'

/**
 * Axios instance dùng chung cho mọi API call.
 *
 * baseURL = '/api' nhờ Vite proxy forward sang Spring Boot ở port 8080.
 * Production build sẽ cần override qua biến môi trường VITE_API_BASE_URL.
 */
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 30_000, // Gemini có thể chậm ~5-10s, để timeout rộng
  headers: {
    'Content-Type': 'application/json',
  },
})

// Interceptor log lỗi - giúp dev nhanh chóng thấy được error response từ backend
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (import.meta.env.DEV) {
      console.error('[API Error]', error.response?.data ?? error.message)
    }
    return Promise.reject(error)
  },
)
