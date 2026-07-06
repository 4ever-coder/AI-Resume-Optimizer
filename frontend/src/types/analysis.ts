import type { ModelSettings } from './modelSettings'
import type { AnalysisReport } from './report'

export interface AnalysisRequest {
  resumeId: string
  position: string
  jobDescription?: string
  modelSettings: Pick<ModelSettings, 'apiKey' | 'baseUrl' | 'model'>
}

export interface AnalysisResponse {
  resumeId: string
  position: string
  model: string
  report: AnalysisReport
  createdAt: string
}

