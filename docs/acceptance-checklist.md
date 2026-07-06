# 上线验收清单

## 端到端流程

- 上传 TXT 简历后能看到解析文本。
- 上传 DOCX 简历后能看到解析文本。
- 上传 PDF 简历后能看到解析文本。
- 模型页能填写 API Key、Base URL、模型名。
- 上传完成后能填写目标岗位和 JD 并生成报告。
- 报告展示总分、星级、维度评分、整体印象、优化建议、改写样例和风险提示。
- 报告支持复制建议、下载 JSON、复制分享摘要。
- 历史页能查看上传解析记录详情。

## 异常流程

- 空文件返回明确错误。
- 不支持的文件格式返回明确错误。
- PDF 页数超过限制返回明确错误。
- 简历文本为空或乱码返回明确错误。
- 未填写 API Key 时分析接口返回明确错误。
- Base URL 协议非法时分析接口返回明确错误。
- 模型返回非 JSON 时返回 `ERROR` 报告，不出现空白页。

## 上线前命令

```powershell
mvn test
```

```powershell
cd frontend
npm run build
```

```powershell
mvn clean package
```

## 数据与安全

- 确认 `uploads/`、`data/` 不提交到 Git。
- 确认用量日志不包含 API Key。
- 确认用户 API Key 不写入 localStorage。
- 确认生产代理限制上传大小。

