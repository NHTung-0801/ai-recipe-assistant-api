import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  deleteHistory,
  getHistory,
  getHistoryById,
  type HistoryQueryParams,
} from '@/api/history'

// Query keys tập trung ở 1 chỗ - tránh typo khi invalidate cache
const KEYS = {
  all: ['history'] as const,
  list: (params: HistoryQueryParams) => [...KEYS.all, 'list', params] as const,
  detail: (id: string) => [...KEYS.all, 'detail', id] as const,
}

/** Query danh sách history có pagination. */
export function useHistoryList(params: HistoryQueryParams = {}) {
  return useQuery({
    queryKey: KEYS.list(params),
    queryFn: () => getHistory(params),
  })
}

/** Query chi tiết 1 phiên history theo id. */
export function useHistoryDetail(id: string | undefined) {
  return useQuery({
    queryKey: KEYS.detail(id ?? ''),
    queryFn: () => getHistoryById(id!),
    enabled: !!id, // Chỉ fetch khi có id - tránh gọi API với undefined
  })
}

/** Mutation xóa history. Sau khi xóa thành công, invalidate list cache để refetch. */
export function useDeleteHistory() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteHistory(id),
    onSuccess: () => {
      // Invalidate mọi query list - đảm bảo UI sync sau khi xóa
      queryClient.invalidateQueries({ queryKey: KEYS.all })
    },
  })
}
