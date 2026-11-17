<script setup>
import { computed } from 'vue'
import Stepper from './Stepper.vue'

const props = defineProps({ eg: Object, serie: Array, bozze: Object, passoKg: Number, occupato: Boolean })
const emit = defineEmits(['spunta', 'annulla'])

const totale = computed(() => props.eg.serie || 1)
const mie = computed(() => props.serie.filter(s => s.esercizioGiorno.id === props.eg.id))
const fatta = n => mie.value.find(s => s.numeroSerie === n)
const bozza = n => props.bozze[`${props.eg.id}_${n}`]
const obiettivo = computed(() => {
  const p = []
  if (props.eg.serie && props.eg.ripetizioniTarget) p.push(`${props.eg.serie} × ${props.eg.ripetizioniTarget}`)
  if (props.eg.carico != null) p.push(`${props.eg.carico} kg`)
  if (props.eg.note) p.push(props.eg.note)
  return p.join(' · ')
})
function modifica(n, campo, v) {
  const b = bozza(n)
  b[campo] = v
  b.toccato = true
}
</script>

<template>
  <article class="card esercizio">
    <header>
      <h3>{{ eg.esercizio.nome }}</h3>
      <p v-if="obiettivo" class="nota">{{ obiettivo }}</p>
    </header>

    <div v-for="n in totale" :key="n" class="serie" :class="{ fatta: fatta(n) }">
      <template v-if="fatta(n)">
        <span class="pallino" aria-hidden="true">✓</span>
        <div class="serie-esito">
          <strong>Serie {{ n }}</strong>
          <span class="num">{{ fatta(n).ripetizioniEffettuate }} rip · {{ fatta(n).caricoEffettivo ?? '-' }} kg</span>
        </div>
        <button class="btn btn-ghost btn-sm" :disabled="occupato" @click="emit('annulla', fatta(n))">Annulla</button>
      </template>

      <template v-else-if="bozza(n)">
        <div class="serie-testa">
          <strong class="serie-n">Serie {{ n }}</strong>
          <button class="btn btn-fatto btn-sm" :disabled="occupato" @click="emit('spunta', eg, n)">✔ Fatta</button>
        </div>
        <div class="serie-campi">
          <Stepper etichetta="Ripetizioni" :model-value="bozza(n).rip" :step="1"
                   @update:model-value="v => modifica(n, 'rip', v)" />
          <Stepper etichetta="Peso (kg)" :model-value="bozza(n).car" :step="passoKg" decimale
                   @update:model-value="v => modifica(n, 'car', v)" />
        </div>
      </template>
    </div>
  </article>
</template>
