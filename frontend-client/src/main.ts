import {createApp} from 'vue'
import {createPinia} from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import App from './App.vue'
import './style.css'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(ElementPlus)
app.use(pinia)
const setupSkeleton = (el: HTMLImageElement) => {
  if (el.tagName !== 'IMG') return
  if (el.complete && el.naturalWidth > 0) {
    el.classList.remove('bg-slate-200', 'animate-pulse')
    return
  }
  el.classList.add('bg-slate-200', 'animate-pulse')
  const handleLoad = () => {
    el.classList.remove('bg-slate-200', 'animate-pulse')
    el.removeEventListener('load', handleLoad)
    el.removeEventListener('error', handleError)
  }
  const handleError = () => {
    el.classList.remove('animate-pulse')
    el.classList.add('bg-slate-100')
    el.removeEventListener('load', handleLoad)
    el.removeEventListener('error', handleError)
  }
  el.addEventListener('load', handleLoad)
  el.addEventListener('error', handleError)
}

app.directive('avatar-skeleton', {
  mounted(el) {
    setupSkeleton(el)
  },
  updated(el) {
    setupSkeleton(el)
  }
})

app.use(router)

app.mount('#app')
