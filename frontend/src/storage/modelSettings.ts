import type { PersistedModelSettings } from '../types/modelSettings'

const STORAGE_KEY = 'ai-resume-optimizer-model-settings'

export const defaultModelSettings: PersistedModelSettings = {
  baseUrl: 'https://api.openai.com/v1',
  model: 'gpt-4.1-mini',
  qualityMode: 'balanced',
}

export function loadModelSettings(): PersistedModelSettings {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return defaultModelSettings

  try {
    return { ...defaultModelSettings, ...JSON.parse(raw) }
  } catch {
    return defaultModelSettings
  }
}

export function saveModelSettings(settings: PersistedModelSettings) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(settings))
}

export function clearModelSettings() {
  localStorage.removeItem(STORAGE_KEY)
}

