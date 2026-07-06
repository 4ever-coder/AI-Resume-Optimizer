export interface ModelSettings {
  apiKey: string
  baseUrl: string
  model: string
  qualityMode: 'balanced' | 'quality' | 'compatible'
}

export interface PersistedModelSettings {
  baseUrl: string
  model: string
  qualityMode: ModelSettings['qualityMode']
}

