import { ElMessage, ElMessageBox } from 'element-plus'

export function normalizeErrorMessage(error: unknown, fallback = 'Operation failed, please retry later') {
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

export async function confirmAction(message: string, title: string, confirmButtonText = 'Confirm') {
  try {
    await ElMessageBox.confirm(message, title, {
      type: 'warning',
      confirmButtonText,
      cancelButtonText: 'Cancel',
    })
    return true
  } catch {
    return false
  }
}
