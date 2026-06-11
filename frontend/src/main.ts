import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import { createPinia } from 'pinia'
import { router, resetMenuRoutes } from './router'
import { ADMIN_AUTH_EXPIRED_EVENT } from './api/http'
import { useAuthStore } from './stores/auth'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

window.addEventListener(ADMIN_AUTH_EXPIRED_EVENT, () => {
  const auth = useAuthStore(pinia)
  const currentPath = router.currentRoute.value.fullPath

  auth.clearSession()
  resetMenuRoutes()

  if (currentPath !== '/login') {
    void router.replace({
      path: '/login',
      query: currentPath ? { redirect: currentPath } : undefined,
    })
  }
})

app.mount('#app')
