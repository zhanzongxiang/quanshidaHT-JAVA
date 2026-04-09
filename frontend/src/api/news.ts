import { http } from './http'
import type { NewsArticle, NewsArticleSavePayload, NewsBlock } from '../types/news'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface NewsArticleApiModel {
  id: number
  title: string
  summary: string
  coverImageUrl: string
  content: string
  author: string
  status: 'draft' | 'published'
  publishedAt: string | null
  updatedAt: string
}

interface NewsArticleSaveApiPayload {
  title: string
  summary: string
  coverImageUrl: string
  content: string
  author: string
}

function createBlockId(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

function createParagraphBlock(content = ''): NewsBlock {
  return {
    id: createBlockId('block'),
    type: 'paragraph',
    content,
  }
}

function parseBlocks(rawContent: string): NewsBlock[] {
  if (!rawContent.trim()) {
    return [createParagraphBlock()]
  }

  try {
    const parsed = JSON.parse(rawContent) as { blocks?: Array<Partial<NewsBlock>> }
    const blocks = Array.isArray(parsed.blocks) ? parsed.blocks : []
    const normalized = blocks
      .map((block, index) => {
        const type = block.type
        if (type !== 'paragraph' && type !== 'heading' && type !== 'image' && type !== 'image_caption') {
          return null
        }

        return {
          id: block.id || `block-${index + 1}`,
          type,
          content: typeof block.content === 'string' ? block.content : '',
        } satisfies NewsBlock
      })
      .filter((block): block is NewsBlock => Boolean(block))

    return normalized.length > 0 ? normalized : [createParagraphBlock()]
  } catch {
    return [createParagraphBlock(rawContent)]
  }
}

function serializeBlocks(blocks: NewsBlock[]): string {
  return JSON.stringify({
    blocks: blocks.map((block) => ({
      id: block.id,
      type: block.type,
      content: block.content,
    })),
  })
}

function toNewsArticle(model: NewsArticleApiModel): NewsArticle {
  return {
    id: model.id,
    title: model.title,
    summary: model.summary,
    coverImageUrl: model.coverImageUrl,
    blocks: parseBlocks(model.content),
    author: model.author,
    status: model.status,
    publishedAt: model.publishedAt,
    updatedAt: model.updatedAt,
  }
}

function toSavePayload(payload: NewsArticleSavePayload): NewsArticleSaveApiPayload {
  return {
    title: payload.title,
    summary: payload.summary,
    coverImageUrl: payload.coverImageUrl,
    content: serializeBlocks(payload.blocks),
    author: payload.author,
  }
}

export function createEmptyNewsBlock(type: NewsBlock['type'] = 'paragraph'): NewsBlock {
  return {
    id: createBlockId('block'),
    type,
    content: '',
  }
}

export async function fetchNewsArticles(): Promise<NewsArticle[]> {
  const { data } = await http.get<ApiResponse<NewsArticleApiModel[]>>('/news')
  return data.data.map(toNewsArticle)
}

export async function fetchNewsArticle(id: number): Promise<NewsArticle> {
  const { data } = await http.get<ApiResponse<NewsArticleApiModel>>(`/news/${id}`)
  return toNewsArticle(data.data)
}

export async function createNewsArticle(payload: NewsArticleSavePayload): Promise<NewsArticle> {
  const { data } = await http.post<ApiResponse<NewsArticleApiModel>>('/news', toSavePayload(payload))
  return toNewsArticle(data.data)
}

export async function updateNewsArticle(id: number, payload: NewsArticleSavePayload): Promise<NewsArticle> {
  const { data } = await http.put<ApiResponse<NewsArticleApiModel>>(`/news/${id}`, toSavePayload(payload))
  return toNewsArticle(data.data)
}

export async function publishNewsArticle(id: number): Promise<NewsArticle> {
  const { data } = await http.post<ApiResponse<NewsArticleApiModel>>(`/news/${id}/publish`)
  return toNewsArticle(data.data)
}

export async function deleteNewsArticle(id: number): Promise<void> {
  await http.delete(`/news/${id}`)
}
