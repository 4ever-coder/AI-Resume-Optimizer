<template>
  <main class="shell">
    <section class="workspace">
      <div class="intro">
        <p class="eyebrow">AI Resume Optimizer</p>
        <h1>简历优化器</h1>
        <p>上传简历，填写目标岗位，自助连接模型后生成评分、风险提示和可复制的优化建议。</p>
      </div>

      <div class="flow">
        <article v-for="item in steps" :key="item.title" class="step">
          <span>{{ item.index }}</span>
          <h2>{{ item.title }}</h2>
          <p>{{ item.description }}</p>
        </article>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
const steps = [
  { index: '01', title: '上传简历', description: '支持 PDF、DOCX、TXT，后端提取并清洗文本。' },
  { index: '02', title: '连接模型', description: '用户自行填写 API Key、Base URL 和模型名，本项目不托管密钥。' },
  { index: '03', title: '生成报告', description: '输出总分、星级、维度评分、风险提示和优化建议。' },
]
</script>

