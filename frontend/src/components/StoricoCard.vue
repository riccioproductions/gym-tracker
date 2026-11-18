<script setup>
import { computed } from 'vue'
import { dataIt, oraIt, perEsercizio, riepilogo, trend, compatta, pesoLato } from '../utils'
import { storicoSoloMinimo } from '../composables/useSettings'

const props = defineProps({ allenamento: Object, serie: Array, precedente: Object, mostraGiorno: Boolean })
defineEmits(['elimina'])

const durata = computed(() => {
  const a = props.allenamento
  return a.dataFine ? `${Math.round((new Date(a.dataFine) - new Date(a.dataInizio)) / 60000)} min` : '-'
})
const completato = computed(() => props.allenamento.stato === 'COMPLETATO')
const esercizi = computed(() => {
  const att = riepilogo(props.serie) // il trend si basa sempre sul lato più debole
  // Spunta in Impostazioni: solo il valore minore per gli unilaterali, oppure entrambi i lati
  const mostrate = storicoSoloMinimo.value ? compatta(props.serie) : props.serie
  return [...perEsercizio(mostrate)].map(([id, lista]) => ({
    id,
    nome: lista[0].nomeEsercizio || lista[0].esercizioGiorno.esercizio.nome,
    serie: [...lista].sort((a, b) => a.numeroSerie - b.numeroSerie || pesoLato(a) - pesoLato(b)),
    trend: trend(att[id], props.precedente ? props.precedente[id] : null)
  }))
})
</script>

<template>
  <article class="card storico">
    <header class="storico-testa">
      <div>
        <strong>{{ dataIt(allenamento.dataInizio) }}</strong>
        <span class="dim"> — {{ oraIt(allenamento.dataInizio) }}</span>
      </div>
      <button class="btn btn-pericolo btn-sm" @click="$emit('elimina', allenamento.id)">Elimina</button>
    </header>
    <div class="storico-meta">
      <span v-if="mostraGiorno" class="tag">{{ allenamento.giorno.nome }}</span>
      <span class="tag" :class="completato ? 'tag-ok' : 'tag-wip'">{{ completato ? 'Completato' : 'In corso' }}</span>
      <span class="tag">Durata {{ durata }}</span>
    </div>
    <p v-if="!esercizi.length" class="dim">Nessuna serie registrata.</p>
    <div v-for="e in esercizi" :key="e.id" class="storico-esercizio">
      <strong>{{ e.nome }}</strong>
      <span v-if="e.trend" :class="'trend-' + e.trend.tipo" :title="e.trend.titolo" :aria-label="e.trend.titolo">{{ e.trend.simbolo }}</span>
      <div class="serie-chips">
        <span v-for="s in e.serie" :key="s.id" class="tag num">{{ s.lato ? s.lato + ' ' : '' }}{{ s.ripetizioniEffettuate }}×{{ s.caricoEffettivo ?? '-' }}kg</span>
      </div>
    </div>
  </article>
</template>
