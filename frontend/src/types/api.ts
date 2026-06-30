/**
 * Wrapper response chung mọi endpoint backend trả về.
 * Match với class ApiResponse<T> ở Spring Boot.
 */
export interface ApiResponse<T> {
  status: number
  message: string
  data: T
  timestamp: string
}

/**
 * Wrapper cho endpoint có pagination.
 * Match với PageResponse<T> ở Spring Boot.
 */
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}
