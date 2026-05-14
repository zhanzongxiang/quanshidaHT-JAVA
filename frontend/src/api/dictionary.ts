import { http } from './http'
import { unwrapResponse } from './shared'
import type { ApiResponse } from './shared'
import type {
  DictionaryGroup,
  DictionaryItem,
  DictionaryItemSavePayload,
  DictionaryOption,
} from '../types/dictionary'

export async function fetchDictionaryGroups(): Promise<DictionaryGroup[]> {
  return unwrapResponse(await http.get<ApiResponse<DictionaryGroup[]>>('/dictionaries'))
}

export async function fetchDictionaryOptions(
  types: string[],
  enabledOnly = true,
): Promise<Record<string, DictionaryOption[]>> {
  if (types.length === 0) {
    return {}
  }

  return unwrapResponse(
    await http.get<ApiResponse<Record<string, DictionaryOption[]>>>('/dictionaries/options', {
      params: {
        types,
        enabledOnly,
      },
      paramsSerializer: {
        indexes: null,
      },
    }),
  )
}

export async function createDictionaryItem(payload: DictionaryItemSavePayload): Promise<DictionaryItem> {
  return unwrapResponse(await http.post<ApiResponse<DictionaryItem>>('/dictionaries', payload))
}

export async function updateDictionaryItem(id: number, payload: DictionaryItemSavePayload): Promise<DictionaryItem> {
  return unwrapResponse(await http.put<ApiResponse<DictionaryItem>>(`/dictionaries/${id}`, payload))
}

export async function deleteDictionaryItem(id: number): Promise<void> {
  await http.delete(`/dictionaries/${id}`)
}
