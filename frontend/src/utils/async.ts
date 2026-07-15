import type { Ref } from 'vue'
import { showErrorMessage } from './message'

export async function withLoading<T>(loading: Ref<boolean>, task: () => Promise<T>): Promise<T> {
  loading.value = true
  try {
    return await task()
  } finally {
    loading.value = false
  }
}

export async function runWithLoading<T>(
  loading: Ref<boolean>,
  task: () => Promise<T>,
  fallback: string,
): Promise<T | null> {
  try {
    return await withLoading(loading, task)
  } catch (error) {
    showErrorMessage(error, fallback)
    return null
  }
}

export async function runSafely<T>(task: () => Promise<T>, fallback: string): Promise<T | null> {
  try {
    return await task()
  } catch (error) {
    showErrorMessage(error, fallback)
    return null
  }
}
