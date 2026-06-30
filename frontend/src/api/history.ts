import { apiClient } from './client'
import type { ApiResponse, PageResponse } from '@/types/api'
import type { UserHistory } from '@/types/history'

export interface HistoryQueryParams {
  page?: number
  size?: number
  sort?: string // ví dụ: 'searchTime,desc'
}

/** GET /api/v1/history?page=...&size=...&sort=... */
export async function getHistory(
  params: HistoryQueryParams = {},
): Promise<PageResponse<UserHistory>> {
  const { data } = await apiClient.get<ApiResponse<PageResponse<UserHistory>>>(
    '/v1/history',
    { params },
  )
  return data.data
}

/** GET /api/v1/history/{id} */
export async function getHistoryById(id: string): Promise<UserHistory> {
  const { data } = await apiClient.get<ApiResponse<UserHistory>>(`/v1/history/${id}`)
  return data.data
}

/** DELETE /api/v1/history/{id} - 204 No Content khi thành công */
export async function deleteHistory(id: string): Promise<void> {
  await apiClient.delete(`/v1/history/${id}`)
}
