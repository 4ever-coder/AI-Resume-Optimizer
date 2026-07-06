export type ReportStatus = 'OK' | 'ERROR'

export type SuggestionType = 'TRIM' | 'EXPAND' | 'SUPPLEMENT' | 'REWRITE' | 'AI_FUSION'

export type RiskCategory = 'AUTHENTICITY' | 'GAP' | 'MISSING_DATA' | 'POSITION_MISMATCH'

export type RiskSeverity = 'LOW' | 'MEDIUM' | 'HIGH'

export interface DimensionScore {
  key: string
  label: string
  score: number
  comment: string
}

export interface Impression {
  label: string
  description: string
}

export interface Suggestion {
  type: SuggestionType
  title: string
  reason: string
  originalText?: string | null
  improvedText: string
  actionHint: string
}

export interface RewriteSample {
  beforeText: string
  afterText: string
  note: string
}

export interface RiskWarning {
  category: RiskCategory
  severity: RiskSeverity
  message: string
  action: string
}

export interface AnalysisReport {
  summaryScore: number
  starLevel: number
  position: string
  overallComment: string
  dimensions: DimensionScore[]
  impressions: Impression[]
  suggestions: Suggestion[]
  rewriteSamples: RewriteSample[]
  riskWarnings: RiskWarning[]
  status: ReportStatus
  errorMessage?: string | null
}

