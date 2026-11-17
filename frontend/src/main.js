import { createApp } from 'vue'
import { createRouter, createWebHashHistory } from 'vue-router'
import App from './App.vue'
import { sbloccaAudio } from './composables/useRestTimer'
import './composables/useSettings' // applica subito il colore scelto
import './style.css'

const router = createRouter({
  history: createWebHashHistory(), // niente 404 al refresh quando servito da Spring
  routes: [
    { path: '/', redirect: '/allenamento' },
    { path: '/allenamento', component: () => import('./views/AllenamentoView.vue') },
    { path: '/schede', component: () => import('./views/SchedeView.vue') },
    { path: '/esercizi', component: () => import('./views/EserciziView.vue') },
    { path: '/storico', component: () => import('./views/StoricoView.vue') },
    { path: '/impostazioni', component: () => import('./views/ImpostazioniView.vue') },
    { path: '/:altro(.*)', redirect: '/allenamento' }
  ]
})

// iOS/Chrome sbloccano l'audio solo dopo un gesto dell'utente
document.addEventListener('pointerdown', sbloccaAudio, { passive: true })

createApp(App).use(router).mount('#app')
