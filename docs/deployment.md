# 部署文档

## 环境要求

- Java 17。
- Maven 3.9+。
- Node.js 20+。
- npm 10+。

## 后端部署

```powershell
mvn clean package
java -jar target/ai-resume-optimizer-0.1.0-SNAPSHOT.jar
```

默认端口：`8080`。

可通过环境变量或外部配置覆盖：

- `server.port`：后端端口。
- `app.storage.upload-dir`：上传文件和解析元信息目录。
- `app.storage.history-file`：后续报告历史文件。
- `app.analysis.usage-log-file`：用量日志位置。
- `app.resume.max-file-size-bytes`：上传大小限制。
- `app.resume.max-pdf-pages`：PDF 页数限制。

## 前端部署

```powershell
cd frontend
npm install
npm run build
```

构建产物在 `frontend/dist`。生产环境需要让前端的 `/api` 请求转发到后端 `http://localhost:8080` 或实际后端地址。

## 用户 API Key

本项目不托管平台级 API Key。用户在前端“模型”页自行填写 API Key，后端只在单次分析请求中使用，不写入日志或持久化文件。

## 健康检查

```powershell
curl http://localhost:8080/api/health
```

返回 `status: UP` 表示后端已启动。

