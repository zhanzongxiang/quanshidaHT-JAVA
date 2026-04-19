import { http } from './http'
import type {
  DictionaryGroup,
  DictionaryItem,
  DictionaryItemSavePayload,
  DictionaryOption,
} from '../types/dictionary'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export async function fetchDictionaryGroups(): Promise<DictionaryGroup[]> {
  const { data } = await http.get<ApiResponse<DictionaryGroup[]>>('/dictionaries')
  return data.data
}

export async function fetchDictionaryOptions(
  types: string[],
  enabledOnly = true,
): Promise<Record<string, DictionaryOption[]>> {
  if (types.length === 0) {
    return {}
  }

  const { data } = await http.get<ApiResponse<Record<string, DictionaryOption[]>>>('/dictionaries/options', {
    params: {
      types,
      enabledOnly,
    },
    paramsSerializer: {
      indexes: null,
    },
  })
  return data.data
}

export async function createDictionaryItem(payload: DictionaryItemSavePayload): Promise<DictionaryItem> {
  const { data } = await http.post<ApiResponse<DictionaryItem>>('/dictionaries', payload)
  return data.data
}

export async function updateDictionaryItem(id: number, payload: DictionaryItemSavePayload): Promise<DictionaryItem> {
  const { data } = await http.put<ApiResponse<DictionaryItem>>(`/dictionaries/${id}`, payload)
  return data.data
}

export async function deleteDictionaryItem(id: number): Promise<void> {
  await http.delete(`/dictionaries/${id}`)
}
