<script setup>
import { passoKg, accento, PASSI_PESO, ACCENTI } from '../composables/useSettings'
import { provaSuono, allarmeAttivo, notificaAttiva, chiediNotifiche } from '../composables/useRestTimer'

const formatta = p => String(p).replace('.', ',')
</script>

<template>
  <section>
    <header class="titolo-pagina"><h1>Impostazioni</h1></header>

    <div class="card">
      <h3>Passo del peso</h3>
      <p class="dim spiegazione">Di quanto cambiano i pulsanti − e + del peso durante l'allenamento.</p>
      <div class="segmenti" role="group" aria-label="Passo del peso">
        <button v-for="p in PASSI_PESO" :key="p" :aria-pressed="passoKg === p" @click="passoKg = p">{{ formatta(p) }} kg</button>
      </div>
    </div>

    <div class="card">
      <h3>Colore dell'app</h3>
      <p class="dim spiegazione">Il colore usato per pulsanti, timer e serie completate.</p>
      <div class="swatches" role="group" aria-label="Colore dell'app">
        <button v-for="(a, chiave) in ACCENTI" :key="chiave" class="swatch" :aria-pressed="accento === chiave"
                :aria-label="a.nome" :title="a.nome" :style="{ background: a.c }" @click="accento = chiave"></button>
      </div>
    </div>

    <div class="card">
      <h3>Avviso di fine recupero</h3>
      <p class="dim spiegazione">Suono, popup a centro schermo e, dove supportata, vibrazione, ripetuti ogni 4 secondi finché non tocchi lo schermo. Prova l'avviso e regola il volume del telefono.</p>
      <label class="interruttore campo"><input type="checkbox" v-model="allarmeAttivo"><span>Allarme sonoro in app</span></label>
      <label class="interruttore campo"><input type="checkbox" v-model="notificaAttiva" @change="notificaAttiva && chiediNotifiche()"><span>Notifica di sistema</span></label>
      <button class="btn btn-primario" @click="provaSuono">Prova avviso</button>
    </div>
  </section>
</template>
