import type { AnalysisRequest, AnalysisResponse } from '../types/analysis'

export async function createAnalysis(payload: AnalysisRequest): Promise<AnalysisResponse> {
  const response = await fetch('/api/analyses', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '分析失败，请稍后重试。' }))
    throw new Error(error.message || '分析失败，请稍后重试。')
  }

  return response.json()
}

