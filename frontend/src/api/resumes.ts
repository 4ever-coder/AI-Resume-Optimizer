import type { ResumeDocument } from '../types/resume'

export async function uploadResume(file: File): Promise<ResumeDocument> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch('/api/resumes', {
    method: 'POST',
    body: formData,
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '上传失败，请稍后重试。' }))
    throw new Error(error.message || '上传失败，请稍后重试。')
  }

  return response.json()
}

