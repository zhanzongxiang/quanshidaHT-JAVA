export interface HomeBannerImage {
  id: string
  name: string
  url: string
}

export interface HomeHeroContent {
  enabled: boolean
  images: HomeBannerImage[]
  title: string
  subtitle: string
  primaryButtonText: string
  primaryButtonLink: string
  secondaryButtonText: string
  secondaryButtonLink: string
}

export interface HomeTrackingContent {
  enabled: boolean
  title: string
  placeholder: string
  buttonText: string
  emptyText: string
  notFoundText: string
  loadingText: string
}

export interface HomeServiceItem {
  id: string
  iconUrl: string
  name: string
  description: string
  link: string
}

export interface HomeProcessStepItem {
  id: string
  title: string
  description: string
}

export interface HomePromiseItem {
  id: string
  iconUrl: string
  title: string
  subtitle: string
  imageUrl: string
}

export interface HomeNewsPreviewSection {
  title: string
  subtitle: string
  viewAllText: string
  viewAllUrl: string
}

export interface HomeServicesSection {
  enabled: boolean
  title: string
  description: string
  items: HomeServiceItem[]
}

export interface HomeProcessSection {
  enabled: boolean
  title: string
  subtitle: string
  steps: HomeProcessStepItem[]
}

export interface HomePromiseSection {
  title: string
  subtitle: string
  items: HomePromiseItem[]
}

export interface HomeSeoContent {
  title: string
  description: string
  keywords: string
}

export interface HomeContentForm {
  hero: HomeHeroContent
  tracking: HomeTrackingContent
  servicesSection: HomeServicesSection
  processSection: HomeProcessSection
  promiseSection: HomePromiseSection
  newsPreviewSection: HomeNewsPreviewSection
  seo: HomeSeoContent
}

export interface HomeContentDocument {
  pageCode: 'home'
  status: 'draft' | 'published'
  updatedAt: string
  publishedAt: string | null
  form: HomeContentForm
}
