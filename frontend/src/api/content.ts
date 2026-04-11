import { http } from './http'
import type { HomeContentDocument, HomeContentForm } from '../types/content'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface SaveHomeContentPayload {
  form: BackendHomePageData
}

interface BackendButton {
  text: string
  actionType: 'route' | 'url'
  value: string
}

interface BackendHomePageData {
  hero: {
    slides: Array<{
      image: string
      alt: string
      title?: string
      subtitle?: string
      primaryButton?: BackendButton
      secondaryButton?: BackendButton
    }>
  }
  trackingSection: {
    title: string
    inputPlaceholder: string
    searchButtonText: string
    emptyText?: string
    notFoundText: string
    loadingText: string
  }
  businessSection: {
    title: string
    subtitle: string
    items: Array<{
      title: string
      description: string
      icon: string
      url: string
    }>
  }
  processSection: {
    title: string
    subtitle: string
    steps: Array<{
      title: string
      description: string
    }>
  }
  promiseSection: {
    title: string
    subtitle: string
    items: Array<{
      title: string
      description: string
      icon: string
      imageUrl: string
    }>
  }
  newsPreviewSection: {
    title: string
    subtitle: string
    viewAllText: string
    viewAllUrl: string
    items: unknown[]
  }
  seo?: {
    title: string
    description: string
    keywords: string
  }
}

interface BackendHomeContentDocument {
  pageCode: 'home'
  status: 'draft' | 'published'
  updatedAt: string
  publishedAt: string | null
  form: BackendHomePageData
}

function createButton(text = '', value = ''): BackendButton {
  return {
    text,
    actionType: value.startsWith('/') || !value ? 'route' : 'url',
    value,
  }
}

function toAdminDocument(document: BackendHomeContentDocument): HomeContentDocument {
  const slides = document.form.hero.slides ?? []
  const primarySlide = slides[0]

  return {
    pageCode: document.pageCode,
    status: document.status,
    updatedAt: document.updatedAt,
    publishedAt: document.publishedAt,
    form: {
      hero: {
        enabled: true,
        images: slides.map((slide, index) => ({
          id: `slide-${index + 1}`,
          name: slide.alt || `Banner ${index + 1}`,
          url: slide.image,
        })),
        title: primarySlide?.title ?? '',
        subtitle: primarySlide?.subtitle ?? '',
        primaryButtonText: primarySlide?.primaryButton?.text ?? '',
        primaryButtonLink: primarySlide?.primaryButton?.value ?? '',
        secondaryButtonText: primarySlide?.secondaryButton?.text ?? '',
        secondaryButtonLink: primarySlide?.secondaryButton?.value ?? '',
      },
      tracking: {
        enabled: true,
        title: document.form.trackingSection.title ?? '',
        placeholder: document.form.trackingSection.inputPlaceholder ?? '',
        buttonText: document.form.trackingSection.searchButtonText ?? '',
        emptyText: document.form.trackingSection.emptyText ?? '',
        notFoundText: document.form.trackingSection.notFoundText ?? '',
        loadingText: document.form.trackingSection.loadingText ?? '',
      },
      servicesSection: {
        enabled: true,
        title: document.form.businessSection.title ?? '',
        description: document.form.businessSection.subtitle ?? '',
        items: (document.form.businessSection.items ?? []).map((item, index) => ({
          id: `business-${index + 1}`,
          iconUrl: item.icon ?? '',
          name: item.title ?? '',
          description: item.description ?? '',
          link: item.url ?? '',
        })),
      },
      processSection: {
        enabled: true,
        title: document.form.processSection.title ?? '',
        subtitle: document.form.processSection.subtitle ?? '',
        steps: (document.form.processSection.steps ?? []).map((step, index) => ({
          id: `process-${index + 1}`,
          title: step.title ?? '',
          description: step.description ?? '',
        })),
      },
      promiseSection: {
        title: document.form.promiseSection.title ?? '',
        subtitle: document.form.promiseSection.subtitle ?? '',
        items: (document.form.promiseSection.items ?? []).map((item, index) => ({
          id: `promise-${index + 1}`,
          iconUrl: item.icon ?? '',
          title: item.title ?? '',
          subtitle: item.description ?? '',
          imageUrl: item.imageUrl ?? '',
        })),
      },
      newsPreviewSection: {
        title: document.form.newsPreviewSection.title ?? '',
        subtitle: document.form.newsPreviewSection.subtitle ?? '',
        viewAllText: document.form.newsPreviewSection.viewAllText ?? '',
        viewAllUrl: document.form.newsPreviewSection.viewAllUrl ?? '',
      },
      seo: {
        title: document.form.seo?.title ?? '',
        description: document.form.seo?.description ?? '',
        keywords: document.form.seo?.keywords ?? '',
      },
    },
  }
}

function toBackendForm(form: HomeContentForm): BackendHomePageData {
  return {
    hero: {
      slides: form.hero.images.map((image, index) => ({
        image: image.url,
        alt: image.name,
        title: index === 0 ? form.hero.title : '',
        subtitle: index === 0 ? form.hero.subtitle : '',
        primaryButton: index === 0 ? createButton(form.hero.primaryButtonText, form.hero.primaryButtonLink) : createButton(),
        secondaryButton: index === 0 ? createButton(form.hero.secondaryButtonText, form.hero.secondaryButtonLink) : createButton(),
      })),
    },
    trackingSection: {
      title: form.tracking.title,
      inputPlaceholder: form.tracking.placeholder,
      searchButtonText: form.tracking.buttonText,
      emptyText: form.tracking.emptyText,
      notFoundText: form.tracking.notFoundText,
      loadingText: form.tracking.loadingText,
    },
    businessSection: {
      title: form.servicesSection.title,
      subtitle: form.servicesSection.description,
      items: form.servicesSection.items.map((item) => ({
        title: item.name,
        description: item.description,
        icon: item.iconUrl,
        url: item.link,
      })),
    },
    processSection: {
      title: form.processSection.title,
      subtitle: form.processSection.subtitle,
      steps: form.processSection.steps.map((step) => ({
        title: step.title,
        description: step.description,
      })),
    },
    promiseSection: {
      title: form.promiseSection.title,
      subtitle: form.promiseSection.subtitle,
      items: form.promiseSection.items.map((item) => ({
        title: item.title,
        description: item.subtitle,
        icon: item.iconUrl,
        imageUrl: item.imageUrl,
      })),
    },
    newsPreviewSection: {
      title: form.newsPreviewSection.title,
      subtitle: form.newsPreviewSection.subtitle,
      viewAllText: form.newsPreviewSection.viewAllText,
      viewAllUrl: form.newsPreviewSection.viewAllUrl,
      items: [],
    },
    seo: {
      title: form.seo.title,
      description: form.seo.description,
      keywords: form.seo.keywords,
    },
  }
}

export async function fetchHomeContent(): Promise<HomeContentDocument> {
  const { data } = await http.get<ApiResponse<BackendHomeContentDocument>>('/content/home')
  return toAdminDocument(data.data)
}

export async function saveHomeContentDraft(form: HomeContentForm): Promise<HomeContentDocument> {
  const payload: SaveHomeContentPayload = { form: toBackendForm(form) }
  const { data } = await http.put<ApiResponse<BackendHomeContentDocument>>('/content/home/draft', payload)
  return toAdminDocument(data.data)
}

export async function publishHomeContent(form: HomeContentForm): Promise<HomeContentDocument> {
  const payload: SaveHomeContentPayload = { form: toBackendForm(form) }
  const { data } = await http.put<ApiResponse<BackendHomeContentDocument>>('/content/home/publish', payload)
  return toAdminDocument(data.data)
}
