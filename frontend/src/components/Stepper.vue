<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: { default: null },
  step: { type: Number, default: 1 },
  min: { type: Number, default: 0 },
  max: { type: Number, default: 9999 },
  etichetta: String,
  decimale: Boolean
})
const emit = defineEmits(['update:modelValue'])

const testo = ref(props.modelValue ?? '')
const leggi = t => {
  const s = String(t).replace(',', '.').trim()
  if (s === '') return null
  const n = Number(s)
  return Number.isNaN(n) ? undefined : n
}
// Il testo digitato (es. "62.") non viene sovrascritto finché il valore numerico coincide
watch(() => props.modelValue, v => { if (leggi(testo.value) !== v) testo.value = v ?? '' })

function digita(e) {
  testo.value = e.target.value
  const n = leggi(testo.value)
  if (n !== undefined) emit('update:modelValue', n)
}
function cambia(delta) {
  const base = Number(props.modelValue ?? 0)
  const nuovo = Math.round((base + delta) * 100) / 100
  emit('update:modelValue', Math.min(props.max, Math.max(props.min, nuovo)))
}
</script>

<template>
  <div class="stepper-blocco">
    <span v-if="etichetta" class="cap">{{ etichetta }}</span>
    <div class="stepper">
      <button type="button" class="stepper-btn" :aria-label="`Diminuisci ${etichetta || ''}`" @click="cambia(-step)">−</button>
      <input :value="testo" :inputmode="decimale ? 'decimal' : 'numeric'" :aria-label="etichetta"
             @input="digita" @focus="$event.target.select()">
      <button type="button" class="stepper-btn" :aria-label="`Aumenta ${etichetta || ''}`" @click="cambia(step)">+</button>
    </div>
  </div>
</template>
