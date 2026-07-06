<template>
  <main class="shell">
    <section class="workspace">
      <header class="intro">
        <p class="eyebrow">AI Resume Optimizer</p>
        <h1>简历优化器</h1>
        <p>上传简历，填写目标岗位，自助连接模型后生成评分、风险提示和可复制的优化建议。</p>
      </header>

      <section class="panel upload-panel">
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
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { uploadResume } from './api/resumes'
import type { ResumeDocument } from './types/resume'

const selectedFile = ref<File | null>(null)
const resume = ref<ResumeDocument | null>(null)
const uploading = ref(false)
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
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '上传失败，请稍后重试。'
  } finally {
    uploading.value = false
  }
}
</script>
