<template>
  <main class="shell">
    <section class="workspace">
      <header class="intro">
        <p class="eyebrow">AI Resume Optimizer</p>
        <h1>简历优化器</h1>
        <p>上传简历，填写目标岗位，自助连接模型后生成评分、风险提示和可复制的优化建议。</p>
      </header>

      <nav class="tabs" aria-label="功能视图">
        <button :class="{ active: view === 'upload' }" @click="view = 'upload'">上传</button>
        <button :class="{ active: view === 'settings' }" @click="view = 'settings'">模型</button>
        <button :class="{ active: view === 'history' }" @click="openHistory">历史</button>
      </nav>

      <section v-if="view === 'upload'" class="panel upload-panel">
        <div>
          <h2>上传简历</h2>
          <p>支持 PDF、DOCX、TXT，文件会使用 UUID 存储名，原文件名只保存在元信息中。</p>
        </div>
        <label class="upload-box">
          <input type="file" accept=".pdf,.docx,.txt" @change="handleFileChange" />
          <span>{{ selectedFileName }}</span>
        </label>
        <button :disabled="!selectedFile || uploading" @click="submitResume">
          {{ uploading ? '解析中...' : '上传并解析' }}
        </button>
        <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
      </section>

      <section v-if="resume" class="panel result-panel">
        <div>
          <p class="eyebrow">解析完成</p>
          <h2>{{ resume.originalFilename }}</h2>
          <p>{{ resume.extension.toUpperCase() }} · {{ resume.pageCount }} 页 · {{ resume.textLength }} 字</p>
        </div>
        <pre>{{ resume.parsedText }}</pre>
      </section>

      <section v-if="resume" class="panel analysis-panel">
        <div class="analysis-form">
          <h2>生成报告</h2>
          <label>
            <span>目标岗位</span>
            <input v-model="position" type="text" placeholder="例如：Java 后端工程师" />
          </label>
          <label>
            <span>岗位 JD</span>
            <textarea v-model="jobDescription" placeholder="粘贴目标岗位 JD，可选"></textarea>
          </label>
          <button :disabled="analyzing" @click="submitAnalysis">{{ analyzing ? '分析中...' : '开始分析' }}</button>
        </div>

        <article v-if="analysis" class="report">
          <div class="scoreboard">
            <div>
              <p class="eyebrow">报告</p>
              <h2>{{ analysis.report.position }}</h2>
              <p>{{ analysis.report.overallComment }}</p>
            </div>
            <strong>{{ analysis.report.summaryScore }}</strong>
            <span>{{ '★'.repeat(analysis.report.starLevel) }}{{ '☆'.repeat(5 - analysis.report.starLevel) }}</span>
          </div>

          <div class="dimension-grid">
            <div v-for="dimension in analysis.report.dimensions" :key="dimension.key" class="dimension">
              <div>
                <strong>{{ dimension.label }}</strong>
                <span>{{ dimension.score }}</span>
              </div>
              <p>{{ dimension.comment }}</p>
            </div>
          </div>

          <div class="impressions">
            <span v-for="impression in analysis.report.impressions" :key="impression.label">
              {{ impression.label }}：{{ impression.description }}
            </span>
          </div>

          <section class="suggestion-section">
            <h3>优化建议</h3>
            <div class="suggestion-grid">
              <article v-for="suggestion in analysis.report.suggestions" :key="suggestion.title" class="suggestion-card">
                <span>{{ suggestionTypeLabel[suggestion.type] }}</span>
                <h4>{{ suggestion.title }}</h4>
                <p>{{ suggestion.reason }}</p>
                <blockquote v-if="suggestion.originalText">{{ suggestion.originalText }}</blockquote>
                <strong>{{ suggestion.improvedText }}</strong>
                <small>{{ suggestion.actionHint }}</small>
              </article>
            </div>
          </section>

          <section v-if="analysis.report.rewriteSamples.length" class="rewrite-section">
            <h3>改写样例</h3>
            <article v-for="sample in analysis.report.rewriteSamples" :key="sample.afterText" class="rewrite-card">
              <p>{{ sample.beforeText }}</p>
              <strong>{{ sample.afterText }}</strong>
              <small>{{ sample.note }}</small>
            </article>
          </section>

          <section v-if="analysis.report.riskWarnings.length" class="risk-section">
            <h3>风险提示</h3>
            <article v-for="risk in analysis.report.riskWarnings" :key="risk.message" class="risk-card">
              <span>{{ risk.severity }}</span>
              <strong>{{ risk.message }}</strong>
              <p>{{ risk.action }}</p>
            </article>
          </section>
        </article>
      </section>

      <section v-if="view === 'history'" class="panel history-panel">
        <div class="history-list">
          <div class="history-head">
            <h2>历史记录</h2>
            <button @click="loadHistory">刷新</button>
          </div>
          <p v-if="historyLoading">加载中...</p>
          <p v-else-if="history.length === 0">暂无历史记录。</p>
          <button
            v-for="item in history"
            v-else
            :key="item.id"
            class="history-item"
            :class="{ active: selectedHistory?.id === item.id }"
            @click="selectHistory(item.id)"
          >
            <strong>{{ item.originalFilename }}</strong>
            <span>{{ item.extension.toUpperCase() }} · {{ item.textLength }} 字 · {{ formatTime(item.uploadedAt) }}</span>
          </button>
        </div>
        <div class="history-detail">
          <template v-if="selectedHistory">
            <p class="eyebrow">详情</p>
            <h2>{{ selectedHistory.originalFilename }}</h2>
            <p>{{ selectedHistory.storedFilename }} · {{ selectedHistory.pageCount }} 页</p>
            <pre>{{ selectedHistory.parsedText }}</pre>
          </template>
          <p v-else>选择一条记录查看解析文本。</p>
        </div>
      </section>

      <section v-if="view === 'settings'" class="panel settings-panel">
        <div class="settings-copy">
          <p class="eyebrow">模型连接</p>
          <h2>自助填写 API Key</h2>
          <p>API Key 只保存在当前页面内存中，刷新页面后会清空；Base URL 和模型名会保存到浏览器，便于下次使用。</p>
        </div>

        <label>
          <span>API Key</span>
          <input v-model="modelSettings.apiKey" type="password" autocomplete="off" placeholder="sk-..." />
        </label>

        <label>
          <span>Base URL</span>
          <input v-model="modelSettings.baseUrl" type="url" placeholder="https://api.openai.com/v1" />
        </label>

        <label>
          <span>模型名</span>
          <input v-model="modelSettings.model" type="text" placeholder="gpt-4.1-mini" />
        </label>

        <div class="segmented" aria-label="模型模式">
          <button :class="{ active: modelSettings.qualityMode === 'balanced' }" @click="applyMode('balanced')">均衡</button>
          <button :class="{ active: modelSettings.qualityMode === 'quality' }" @click="applyMode('quality')">高质量</button>
          <button :class="{ active: modelSettings.qualityMode === 'compatible' }" @click="applyMode('compatible')">兼容</button>
        </div>

        <div class="settings-actions">
          <button @click="persistModelSettings">保存模型设置</button>
          <button class="secondary" @click="clearSettings">清空设置</button>
        </div>
        <p v-if="settingsMessage" class="success">{{ settingsMessage }}</p>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { createAnalysis } from './api/analyses'
import { getResume, listResumes, uploadResume } from './api/resumes'
import { clearModelSettings, loadModelSettings, saveModelSettings } from './storage/modelSettings'
import type { ModelSettings } from './types/modelSettings'
import type { ResumeDocument } from './types/resume'
import type { AnalysisResponse } from './types/analysis'

const persistedSettings = loadModelSettings()
const view = ref<'upload' | 'settings' | 'history'>('upload')
const selectedFile = ref<File | null>(null)
const resume = ref<ResumeDocument | null>(null)
const analysis = ref<AnalysisResponse | null>(null)
const history = ref<ResumeDocument[]>([])
const selectedHistory = ref<ResumeDocument | null>(null)
const uploading = ref(false)
const analyzing = ref(false)
const historyLoading = ref(false)
const errorMessage = ref('')
const settingsMessage = ref('')
const position = ref('')
const jobDescription = ref('')
const modelSettings = ref<ModelSettings>({
  ...persistedSettings,
  apiKey: '',
})
const suggestionTypeLabel = {
  TRIM: '精简',
  EXPAND: '扩展',
  SUPPLEMENT: '补充',
  REWRITE: '重写',
  AI_FUSION: 'AI 融合',
}

const selectedFileName = computed(() => selectedFile.value?.name ?? '选择 PDF、DOCX 或 TXT 文件')

function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  selectedFile.value = input.files?.[0] ?? null
  errorMessage.value = ''
}

async function submitResume() {
  if (!selectedFile.value) return
  uploading.value = true
  errorMessage.value = ''
  try {
    resume.value = await uploadResume(selectedFile.value)
    analysis.value = null
    history.value = [resume.value, ...history.value.filter((item) => item.id !== resume.value?.id)]
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '上传失败，请稍后重试。'
  } finally {
    uploading.value = false
  }
}

async function submitAnalysis() {
  if (!resume.value) return
  if (!position.value.trim()) {
    errorMessage.value = '请先填写目标岗位。'
    return
  }
  analyzing.value = true
  errorMessage.value = ''
  try {
    analysis.value = await createAnalysis({
      resumeId: resume.value.id,
      position: position.value,
      jobDescription: jobDescription.value,
      modelSettings: {
        apiKey: modelSettings.value.apiKey,
        baseUrl: modelSettings.value.baseUrl,
        model: modelSettings.value.model,
      },
    })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '分析失败，请稍后重试。'
  } finally {
    analyzing.value = false
  }
}

async function openHistory() {
  view.value = 'history'
  await loadHistory()
}

async function loadHistory() {
  historyLoading.value = true
  errorMessage.value = ''
  try {
    history.value = await listResumes()
    selectedHistory.value = history.value[0] ?? null
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '历史记录加载失败，请稍后重试。'
  } finally {
    historyLoading.value = false
  }
}

async function selectHistory(id: string) {
  selectedHistory.value = await getResume(id)
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

function applyMode(mode: ModelSettings['qualityMode']) {
  modelSettings.value.qualityMode = mode
  if (mode === 'balanced') modelSettings.value.model = 'gpt-4.1-mini'
  if (mode === 'quality') modelSettings.value.model = 'gpt-4.1'
}

function persistModelSettings() {
  saveModelSettings({
    baseUrl: modelSettings.value.baseUrl,
    model: modelSettings.value.model,
    qualityMode: modelSettings.value.qualityMode,
  })
  settingsMessage.value = '模型设置已保存，API Key 会在刷新页面后清空。'
}

function clearSettings() {
  clearModelSettings()
  modelSettings.value = { ...loadModelSettings(), apiKey: '' }
  settingsMessage.value = '模型设置已清空。'
}
</script>
