export interface AdminMenu {
  id: number
  name: string
  path: string
  component: string
  icon?: string
  children: AdminMenu[]
}

export interface MeInfo {
  userId: number
  username: string
  permissions: string[]
  menus: AdminMenu[]
}
