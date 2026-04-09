export type NewsBlockType = 'paragraph' | 'heading' | 'image' | 'image_caption'

export interface NewsBlock {
  id: string
  type: NewsBlockType
  content: string
}

export interface NewsArticle {
  id: number
  title: string
  summary: string
  coverImageUrl: string
  blocks: NewsBlock[]
  author: string
  status: 'draft' | 'published'
  publishedAt: string | null
  updatedAt: string
}

export interface NewsArticleSavePayload {
  title: string
  summary: string
  coverImageUrl: string
  blocks: NewsBlock[]
  author: string
}
