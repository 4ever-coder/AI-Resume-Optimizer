export interface ResumeDocument {
  id: string
  originalFilename: string
  storedFilename: string
  contentType: string | null
  extension: string
  sizeBytes: number
  pageCount: number
  textLength: number
  parsedText: string
  uploadedAt: string
}

