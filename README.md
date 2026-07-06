# AI Resume Optimizer 简历优化器

面向小白用户的 AI 简历优化器。用户上传简历、填写目标岗位或 JD、在页面自助连接 OpenAI-compatible API Key 后，系统生成评分、岗位匹配分析、风险提示和可复制的优化建议。

## MVP 范围

- 简历上传：支持 PDF、DOCX、TXT，后端提取并清洗文本。
- 用户自助连接模型：页面填写 API Key、Base URL 和模型名，后端只用于本次分析请求，不保存明文密钥。
- AI 分析报告：输出总分、星级、维度评分、整体印象、风险提示、建议卡片和改写样例。
- 历史记录：本地保存报告摘要和详情，方便回看。
- 导出分享：支持复制建议、下载报告、生成基础分享文本。

## 非 MVP 范围

- 多租户账号体系、团队协作、在线支付和企业级权限。
- 长期托管用户 API Key 或平台统一代付模型费用。
- 简历模板编辑器和招聘 ATS 投递闭环。

## 技术方案

- 后端：Spring Boot 3、Java 17、Maven。
- 文件解析：Apache PDFBox、Apache POI、TXT 原文读取。
- AI 接入：OpenAI-compatible Chat Completions，请求由用户在前端提供 API Key/Base URL/模型。
- 前端：Vue 3、Vite、TypeScript。
- 数据存储：MVP 使用本地 JSON 文件存储报告历史，后续可替换为数据库。

## 本地运行

```powershell
mvn spring-boot:run
```

```powershell
cd frontend
npm install
npm run dev
```

后端默认运行在 `http://localhost:8080`，前端默认运行在 `http://localhost:5173`。

## Linear 执行顺序

所有开发严格按 Linear 项目 `AI Resume Optimizer 简历优化器` 的子任务串行推进：`QIA-122` 到 `QIA-133`。每完成一个子任务，使用中文提交消息提交并推送到 GitHub。

