<script setup>
import { computed, ref, watch } from 'vue'
import Stepper from './Stepper.vue'
import { pesoLato } from '../utils'
import { usaRange } from '../composables/useSettings'

const props = defineProps({
  eg: Object, serie: Array, bozze: Object, passoKg: Number, occupato: Boolean,
  totale: Number,       // serie totali (scheda + aggiuntive)
  rimovibile: Boolean,  // l'ultima serie aggiuntiva si può togliere
  ultima: Array // es. ['8×20', '8×20', '7×20']: sessione precedente
})
const emit = defineEmits(['spunta', 'annulla', 'aggiungi', 'togli'])

const totale = computed(() => props.totale || props.eg.serie || 1)
const unilaterale = computed(() => !!props.eg.unilaterale)

// Esercizio normale: 1, 2, 3… — unilaterale: 1 SX, 1 DX, 2 SX, 2 DX…
const righe = computed(() => {
  const r = []
  for (let n = 1; n <= totale.value; n++) {
    if (unilaterale.value) r.push({ n, lato: 'SX' }, { n, lato: 'DX' })
    else r.push({ n, lato: null })
  }
  return r
})

const mie = computed(() => props.serie.filter(s => s.esercizioGiorno.id === props.eg.id))
const chiave = (n, lato) => `${props.eg.id}_${n}${lato ? '_' + lato : ''}`
const fatta = (n, lato) => mie.value.find(s => s.numeroSerie === n && (s.lato || null) === lato)
const bozza = (n, lato) => props.bozze[chiave(n, lato)]
const etichetta = r => `Serie ${r.n}${r.lato ? ' · ' + r.lato : ''}`

// Esercizio completato: si riduce a una riga (si può riaprire per correggere)
const completato = computed(() => righe.value.every(r => fatta(r.n, r.lato)))
const espansa = ref(false)
watch(completato, c => { if (c) espansa.value = false })
const riassunto = computed(() => [...mie.value]
  .sort((a, b) => a.numeroSerie - b.numeroSerie || pesoLato(a) - pesoLato(b))
  .map(s => `${s.lato ? s.lato + ' ' : ''}${s.ripetizioniEffettuate}×${s.caricoEffettivo ?? '-'} kg`))

const obiettivo = computed(() => {
  const p = []
  const rip = props.eg.ripetizioniTarget
  const range = usaRange.value && props.eg.ripetizioniMax && rip ? `${rip}-${props.eg.ripetizioniMax}` : rip
  if (props.eg.serie && range) {
    p.push(`${props.eg.serie} × ${range}${unilaterale.value ? ' per lato' : ''}`)
  }
  if (props.eg.carico != null) p.push(`${props.eg.carico} kg`)
  if (props.eg.note) p.push(props.eg.note)
  return p.join(' · ')
})

function modifica(n, lato, campo, v) {
  const b = bozza(n, lato)
  b[campo] = v
  b.toccato = true
}
</script>

<template>
  <!-- Completato e compresso -->
  <article v-if="completato && !espansa" :id="'eg-' + eg.id" class="card esercizio" style="scroll-margin-top: 12px">
    <div class="serie fatta">
      <span class="pallino" aria-hidden="true">✓</span>
      <div class="serie-esito">
        <strong>{{ eg.esercizio.nome }}</strong>
        <div class="serie-riepilogo">
          <span v-for="(r, i) in riassunto" :key="i" class="num">{{ r }}</span>
        </div>
      </div>
      <div class="riga-azioni" style="flex-wrap: nowrap">
        <button class="btn btn-ghost btn-sm" :disabled="occupato" @click="emit('aggiungi', eg)">+ Serie</button>
        <button class="btn btn-ghost btn-sm" @click="espansa = true">Modifica</button>
      </div>
    </div>
  </article>

  <!-- Da fare (o riaperto) -->
  <article v-else :id="'eg-' + eg.id" class="card esercizio" style="scroll-margin-top: 12px">
    <header>
      <h3>{{ eg.esercizio.nome }}</h3>
      <p v-if="obiettivo" class="nota">{{ obiettivo }}</p>
      <p v-if="ultima && ultima.length" class="nota">Ultima volta: {{ ultima.join(' · ') }} kg</p>
    </header>

    <div v-for="r in righe" :key="chiave(r.n, r.lato)" class="serie" :class="{ fatta: fatta(r.n, r.lato) }">
      <template v-if="fatta(r.n, r.lato)">
        <span class="pallino" aria-hidden="true">✓</span>
        <div class="serie-esito">
          <strong>{{ etichetta(r) }}</strong>
          <span class="num">{{ fatta(r.n, r.lato).ripetizioniEffettuate }} rip · {{ fatta(r.n, r.lato).caricoEffettivo ?? '-' }} kg</span>
        </div>
        <button class="btn btn-ghost btn-sm" :disabled="occupato" @click="emit('annulla', fatta(r.n, r.lato))">Annulla</button>
      </template>

      <template v-else-if="bozza(r.n, r.lato)">
        <div class="serie-testa">
          <strong class="serie-n">{{ etichetta(r) }}</strong>
          <button class="btn btn-fatto btn-sm" :disabled="occupato" @click="emit('spunta', eg, r.n, r.lato)">✔ Fatta</button>
        </div>
        <div class="serie-campi">
          <Stepper etichetta="Ripetizioni" :model-value="bozza(r.n, r.lato).rip" :step="1"
                   @update:model-value="v => modifica(r.n, r.lato, 'rip', v)" />
          <Stepper etichetta="Peso (kg)" :model-value="bozza(r.n, r.lato).car" :step="passoKg" decimale
                   @update:model-value="v => modifica(r.n, r.lato, 'car', v)" />
        </div>
      </template>
    </div>

    <div class="riga-azioni" style="margin-top: 10px">
      <button class="btn btn-ghost btn-sm" :disabled="occupato" @click="emit('aggiungi', eg)">+ Serie</button>
      <button v-if="rimovibile" class="btn btn-ghost btn-sm" @click="emit('togli', eg)">− Serie extra</button>
      <button v-if="completato" class="btn btn-ghost btn-sm" @click="espansa = false">Comprimi</button>
    </div>
  </article>
</template>
