import type { Ref } from 'vue'
import { showError } from '@/utils/toast'

export async function withPageLoading<T>(loading: Ref<boolean>, task: () => Promise<T>) {
  loading.value = true
  try {
    return await task()
  } finally {
    loading.value = false
  }
}

export async function runPageTask<T>(task: () => Promise<T>, fallback: string) {
  try {
    return await task()
  } catch (error) {
    showError(error, fallback)
    return null
  }
}

export async function runPageTaskWithLoading<T>(
  loading: Ref<boolean>,
  task: () => Promise<T>,
  fallback: string,
) {
  return runPageTask(() => withPageLoading(loading, task), fallback)
}
