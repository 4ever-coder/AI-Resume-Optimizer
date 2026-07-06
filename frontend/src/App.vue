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
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { getResume, listResumes, uploadResume } from './api/resumes'
import type { ResumeDocument } from './types/resume'

const view = ref<'upload' | 'history'>('upload')
const selectedFile = ref<File | null>(null)
const resume = ref<ResumeDocument | null>(null)
const history = ref<ResumeDocument[]>([])
const selectedHistory = ref<ResumeDocument | null>(null)
const uploading = ref(false)
const historyLoading = ref(false)
const errorMessage = ref('')

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
    history.value = [resume.value, ...history.value.filter((item) => item.id !== resume.value?.id)]
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '上传失败，请稍后重试。'
  } finally {
    uploading.value = false
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
</script>
