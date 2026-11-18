<script setup>
import { durata, durataUnilaterale, PRESET, PASSO, imposta, impostaUnilaterale, avvia, autoAvvio, inCorso } from '../composables/useRestTimer'
import { mmss } from '../utils'

defineProps({ mostraUni: Boolean }) // true se l'allenamento ha esercizi unilaterali
</script>

<template>
  <div class="card recupero">
    <div class="recupero-riga">
      <button class="btn btn-sm" @click="imposta(durata - PASSO)" aria-label="Riduci recupero di 15 secondi">−{{ PASSO }}s</button>
      <div class="recupero-tempo"><small>Recupero</small><strong class="num">{{ mmss(durata) }}</strong></div>
      <button class="btn btn-sm" @click="imposta(durata + PASSO)" aria-label="Aumenta recupero di 15 secondi">+{{ PASSO }}s</button>
      <button class="btn btn-primario btn-sm" @click="avvia()">{{ inCorso ? 'Riavvia' : 'Avvia' }}</button>
    </div>
    <div class="chips">
      <button v-for="p in PRESET" :key="p" class="chip" :aria-pressed="durata === p" @click="imposta(p)">{{ mmss(p) }}</button>
    </div>
    <div v-if="mostraUni" class="recupero-riga" style="grid-template-columns: auto 1fr auto; margin-bottom: 12px">
      <button class="btn btn-sm" @click="impostaUnilaterale(durataUnilaterale - PASSO)" aria-label="Riduci recupero unilaterali di 15 secondi">−{{ PASSO }}s</button>
      <div class="recupero-tempo"><small>Recupero unilaterali</small><strong class="num">{{ mmss(durataUnilaterale) }}</strong></div>
      <button class="btn btn-sm" @click="impostaUnilaterale(durataUnilaterale + PASSO)" aria-label="Aumenta recupero unilaterali di 15 secondi">+{{ PASSO }}s</button>
    </div>
    <label class="interruttore"><input type="checkbox" v-model="autoAvvio"><span>Avvia dopo ogni serie</span></label>
  </div>
</template>
