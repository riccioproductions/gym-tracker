<script setup>
import { rimanenti, inCorso, progresso, appenaFinito, inRitardo, ritardo, regola, ferma, chiudiFinito, azzeraRitardo, PASSO } from '../composables/useRestTimer'
import { mmss } from '../utils'
</script>

<template>
  <!-- Recupero in corso: barra in basso -->
  <div v-if="inCorso" class="dock" role="timer">
    <div class="dock-barra"><span :style="{ width: progresso * 100 + '%' }"></span></div>
    <button class="btn btn-sm" @click="regola(-PASSO)">−{{ PASSO }}s</button>
    <div class="dock-tempo"><small>Recupero</small><strong class="num">{{ mmss(rimanenti) }}</strong></div>
    <button class="btn btn-sm" @click="regola(PASSO)">+{{ PASSO }}s</button>
    <button class="btn btn-sm btn-ghost" @click="ferma()">Salta</button>
  </div>

  <!-- Recupero finito: popup a centro schermo, si chiude toccando ovunque -->
  <div v-else-if="appenaFinito" class="allarme" role="alertdialog" aria-live="assertive" aria-label="Recupero terminato" @click="chiudiFinito()">
    <div class="allarme-card">
      <strong>Recupero terminato</strong>
      <span>Tocca per fermare l'allarme</span>
    </div>
  </div>

  <!-- Dopo il popup: il ritardo continua a correre finché non spunti la serie -->
  <div v-else-if="inRitardo" class="dock dock-ritardo" role="timer" aria-label="Ritardo sul recupero">
    <div class="dock-tempo"><small>In ritardo: fai la serie!</small><strong class="num">+{{ mmss(ritardo) }}</strong></div>
    <button class="btn btn-sm btn-ghost" @click="azzeraRitardo()">Nascondi</button>
  </div>
</template>