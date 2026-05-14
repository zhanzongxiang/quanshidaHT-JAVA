import { ElMessage, ElMessageBox } from 'element-plus'

export function normalizeErrorMessage(error: unknown, fallback = '操作失败，请稍后重试') {
  if (typeof error === 'string') {
    return error.trim() || fallback
  }

  if (error instanceof Error) {
    return error.message.trim() || fallback
  }

  return fallback
}

export function showErrorMessage(error: unknown, fallback?: string) {
  ElMessage.error(normalizeErrorMessage(error, fallback))
}

export function showSuccessMessage(message: string) {
  ElMessage.success(message)
}

export function showWarningMessage(message: string) {
  ElMessage.warning(message)
}

export async function confirmAction(message: string, title: string, confirmButtonText = '确认') {
  try {
    await ElMessageBox.confirm(message, title, {
      type: 'warning',
      confirmButtonText,
      cancelButtonText: '取消',
    })
    return true
  } catch {
    return false
  }
}
