import { createApp } from 'vue'
import { createRouter, createWebHashHistory } from 'vue-router'
import App from './App.vue'
import { sbloccaAudio } from './composables/useRestTimer'
import { token } from './composables/useAuth'
import { assicuraPush } from './composables/usePush'
import './composables/useSettings' // applica subito il colore scelto
import './style.css'

const router = createRouter({
  history: createWebHashHistory(), // niente 404 al refresh quando servito da Spring
  routes: [
    { path: '/', redirect: '/allenamento' },
    { path: '/login', component: () => import('./views/LoginView.vue') },
    { path: '/allenamento', component: () => import('./views/AllenamentoView.vue') },
    { path: '/schede', component: () => import('./views/SchedeView.vue') },
    { path: '/esercizi', component: () => import('./views/EserciziView.vue') },
    { path: '/storico', component: () => import('./views/StoricoView.vue') },
    { path: '/impostazioni', component: () => import('./views/ImpostazioniView.vue') },
    { path: '/:altro(.*)', redirect: '/allenamento' }
  ]
})

// Senza token si può vedere solo il login; con il token il login non serve
router.beforeEach(to => {
  if (!token.value && to.path !== '/login') return '/login'
  if (token.value && to.path === '/login') return '/allenamento'
})

// iOS/Chrome sbloccano l'audio solo dopo un gesto dell'utente
document.addEventListener('pointerdown', sbloccaAudio, { passive: true })

createApp(App).use(router).mount('#app')

// Service worker per le notifiche push; se il permesso c'è già, riallinea la sottoscrizione col server
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .then(() => { if (token.value) assicuraPush() })
      .catch(() => { /* push non disponibile */ })
  })
}
