<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { toasts, confermaStato, chiudiConferma } from './composables/useUi'
import Modal from './components/Modal.vue'
import RestDock from './components/RestDock.vue'

const route = useRoute()
const senzaNav = computed(() => route.path === '/login') // al login la barra di navigazione non serve

const voci = [
  { to: '/allenamento', nome: 'Allenamento', icona: '<line x1="10" x2="14" y1="2" y2="2"/><line x1="12" x2="15" y1="14" y2="11"/><circle cx="12" cy="14" r="8"/>' },
  { to: '/schede', nome: 'Schede', icona: '<rect width="8" height="4" x="8" y="2" rx="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M12 11h4"/><path d="M12 16h4"/><path d="M8 11h.01"/><path d="M8 16h.01"/>' },
  { to: '/esercizi', nome: 'Esercizi', icona: '<path d="m6.5 6.5 11 11"/><path d="m21 21-1-1"/><path d="m3 3 1 1"/><path d="m18 22 4-4"/><path d="m2 6 4-4"/><path d="m3 10 7-7"/><path d="m14 21 7-7"/>' },
  { to: '/storico', nome: 'Storico', icona: '<path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/>' },
  { to: '/impostazioni', nome: 'Impostazioni', icona: '<line x1="21" x2="14" y1="4" y2="4"/><line x1="10" x2="3" y1="4" y2="4"/><line x1="21" x2="12" y1="12" y2="12"/><line x1="8" x2="3" y1="12" y2="12"/><line x1="21" x2="16" y1="20" y2="20"/><line x1="12" x2="3" y1="20" y2="20"/><line x1="14" x2="14" y1="2" y2="6"/><line x1="8" x2="8" y1="10" y2="14"/><line x1="16" x2="16" y1="18" y2="22"/>' }
]
</script>

<template>
  <div class="shell" :style="senzaNav ? { display: 'block' } : null">
    <nav v-if="!senzaNav" class="nav" aria-label="Navigazione principale">
      <RouterLink v-for="v in voci" :key="v.to" :to="v.to">
        <svg viewBox="0 0 24 24" aria-hidden="true" v-html="v.icona"></svg>
        <span>{{ v.nome }}</span>
      </RouterLink>
    </nav>

    <main class="contenuto">
      <div class="marchio"><span>GYM⚡️TRACKER</span><small>RICCIO PRODUCTIONS</small></div>
      <RouterView v-slot="{ Component }">
        <KeepAlive><component :is="Component" /></KeepAlive>
      </RouterView>
    </main>
  </div>

  <RestDock />

  <div class="toasts" aria-live="polite">
    <div v-for="t in toasts" :key="t.id" class="toast" :class="t.tipo">{{ t.testo }}</div>
  </div>

  <Modal :aperto="confermaStato.aperto" titolo="Conferma" @chiudi="chiudiConferma(false)">
    <p class="testo-conferma">{{ confermaStato.testo }}</p>
    <template #piede>
      <button class="btn btn-ghost" @click="chiudiConferma(false)">Annulla</button>
      <button class="btn" :class="confermaStato.pericolo ? 'btn-pericolo-pieno' : 'btn-primario'" @click="chiudiConferma(true)">
        {{ confermaStato.etichetta }}
      </button>
    </template>
  </Modal>
</template>
