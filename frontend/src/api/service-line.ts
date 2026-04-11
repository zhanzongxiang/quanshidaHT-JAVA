import { http } from './http'
import {
  createEmptyServiceLineForm,
  createHighlight,
  createMetric,
  createTextItem,
  type ServiceLineCode,
  type ServiceLineDocument,
  type ServiceLineForm,
  type ServiceLineSummary,
} from '../types/service-line'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface SaveServiceLinePayload {
  form: BackendServiceLinePageData
}

interface BackendServiceLinePageData {
  key: ServiceLineCode
  eyebrow: string
  title: string
  subtitle: string
  description: string
  heroImage: string
  heroTags: string[]
  metrics: Array<{
    label: string
    value: string
  }>
  highlightsTitle: string
  highlightsSubtitle: string
  highlights: Array<{
    title: string
    description: string
    icon: string
  }>
  processTitle: string
  processSubtitle: string
  processSteps: string[]
  scopeTitle: string
  scopeSubtitle: string
  scopeImage: string
  scopeItems: string[]
  supportTitle: string
  supportDescription: string
  supportItems: string[]
  ctaTitle: string
  ctaSubtitle: string
}

interface BackendServiceLineDocument {
  pageCode: string
  lineCode: ServiceLineCode
  lineName: string
  linePath: string
  status: 'draft' | 'published'
  updatedAt: string
  publishedAt: string | null
  form: BackendServiceLinePageData
}

function mapToAdminForm(form: BackendServiceLinePageData): ServiceLineForm {
  return {
    key: form.key,
    eyebrow: form.eyebrow ?? '',
    title: form.title ?? '',
    subtitle: form.subtitle ?? '',
    description: form.description ?? '',
    heroImage: form.heroImage ?? '',
    heroTags: (form.heroTags ?? []).map((value) => createTextItem('hero-tag', value)),
    metrics: (form.metrics ?? []).map((item) => ({
      ...createMetric(),
      label: item.label ?? '',
      value: item.value ?? '',
    })),
    highlightsTitle: form.highlightsTitle ?? '',
    highlightsSubtitle: form.highlightsSubtitle ?? '',
    highlights: (form.highlights ?? []).map((item) => ({
      ...createHighlight(),
      title: item.title ?? '',
      description: item.description ?? '',
      icon: item.icon ?? '',
    })),
    processTitle: form.processTitle ?? '',
    processSubtitle: form.processSubtitle ?? '',
    processSteps: (form.processSteps ?? []).map((value) => createTextItem('process', value)),
    scopeTitle: form.scopeTitle ?? '',
    scopeSubtitle: form.scopeSubtitle ?? '',
    scopeImage: form.scopeImage ?? '',
    scopeItems: (form.scopeItems ?? []).map((value) => createTextItem('scope', value)),
    supportTitle: form.supportTitle ?? '',
    supportDescription: form.supportDescription ?? '',
    supportItems: (form.supportItems ?? []).map((value) => createTextItem('support', value)),
    ctaTitle: form.ctaTitle ?? '',
    ctaSubtitle: form.ctaSubtitle ?? '',
  }
}

function normalizeAdminForm(form: ServiceLineForm): ServiceLineForm {
  const fallback = createEmptyServiceLineForm()
  return {
    ...fallback,
    ...form,
    heroTags: form.heroTags.length > 0 ? form.heroTags : fallback.heroTags,
    metrics: form.metrics.length > 0 ? form.metrics : fallback.metrics,
    highlights: form.highlights.length > 0 ? form.highlights : fallback.highlights,
    processSteps: form.processSteps.length > 0 ? form.processSteps : fallback.processSteps,
    scopeItems: form.scopeItems.length > 0 ? form.scopeItems : fallback.scopeItems,
    supportItems: form.supportItems.length > 0 ? form.supportItems : fallback.supportItems,
  }
}

function mapToBackendForm(form: ServiceLineForm): BackendServiceLinePageData {
  return {
    key: form.key,
    eyebrow: form.eyebrow,
    title: form.title,
    subtitle: form.subtitle,
    description: form.description,
    heroImage: form.heroImage,
    heroTags: form.heroTags.map((item) => item.value),
    metrics: form.metrics.map((item) => ({
      label: item.label,
      value: item.value,
    })),
    highlightsTitle: form.highlightsTitle,
    highlightsSubtitle: form.highlightsSubtitle,
    highlights: form.highlights.map((item) => ({
      title: item.title,
      description: item.description,
      icon: item.icon,
    })),
    processTitle: form.processTitle,
    processSubtitle: form.processSubtitle,
    processSteps: form.processSteps.map((item) => item.value),
    scopeTitle: form.scopeTitle,
    scopeSubtitle: form.scopeSubtitle,
    scopeImage: form.scopeImage,
    scopeItems: form.scopeItems.map((item) => item.value),
    supportTitle: form.supportTitle,
    supportDescription: form.supportDescription,
    supportItems: form.supportItems.map((item) => item.value),
    ctaTitle: form.ctaTitle,
    ctaSubtitle: form.ctaSubtitle,
  }
}

export async function fetchServiceLineSummaries(): Promise<ServiceLineSummary[]> {
  const { data } = await http.get<ApiResponse<ServiceLineSummary[]>>('/content/service-lines')
  return data.data
}

export async function fetchServiceLineContent(lineCode: ServiceLineCode): Promise<ServiceLineDocument> {
  const { data } = await http.get<ApiResponse<BackendServiceLineDocument>>(`/content/service-lines/${lineCode}`)
  return {
    ...data.data,
    form: normalizeAdminForm(mapToAdminForm(data.data.form)),
  }
}

export async function saveServiceLineDraft(lineCode: ServiceLineCode, form: ServiceLineForm): Promise<ServiceLineDocument> {
  const payload: SaveServiceLinePayload = { form: mapToBackendForm(form) }
  const { data } = await http.put<ApiResponse<BackendServiceLineDocument>>(`/content/service-lines/${lineCode}/draft`, payload)
  return {
    ...data.data,
    form: normalizeAdminForm(mapToAdminForm(data.data.form)),
  }
}

export async function publishServiceLine(lineCode: ServiceLineCode, form: ServiceLineForm): Promise<ServiceLineDocument> {
  const payload: SaveServiceLinePayload = { form: mapToBackendForm(form) }
  const { data } = await http.put<ApiResponse<BackendServiceLineDocument>>(`/content/service-lines/${lineCode}/publish`, payload)
  return {
    ...data.data,
    form: normalizeAdminForm(mapToAdminForm(data.data.form)),
  }
}
