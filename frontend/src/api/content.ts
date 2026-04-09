import { http } from './http'
import type { HomeContentDocument, HomeContentForm } from '../types/content'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface SaveHomeContentPayload {
  form: HomeContentForm
}

export async function fetchHomeContent(): Promise<HomeContentDocument> {
  const { data } = await http.get<ApiResponse<HomeContentDocument>>('/content/home')
  return data.data
}

export async function saveHomeContentDraft(form: HomeContentForm): Promise<HomeContentDocument> {
  const payload: SaveHomeContentPayload = { form }
  const { data } = await http.put<ApiResponse<HomeContentDocument>>('/content/home/draft', payload)
  return data.data
}

export async function publishHomeContent(form: HomeContentForm): Promise<HomeContentDocument> {
  const payload: SaveHomeContentPayload = { form }
  const { data } = await http.put<ApiResponse<HomeContentDocument>>('/content/home/publish', payload)
  return data.data
}
