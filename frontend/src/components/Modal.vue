<script>
let aperti = [] // pila dei popup aperti, condivisa tra tutte le istanze
</script>

<script setup>
import { watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({ aperto: Boolean, titolo: String })
const emit = defineEmits(['chiudi'])
const id = Symbol()

function sincronizza(v) {
  aperti = aperti.filter(x => x !== id)
  if (v) aperti.push(id)
  document.body.style.overflow = aperti.length ? 'hidden' : ''
}
watch(() => props.aperto, sincronizza)
if (props.aperto) sincronizza(true)

// Esc chiude solo il popup in cima
const esc = e => { if (e.key === 'Escape' && props.aperto && aperti[aperti.length - 1] === id) emit('chiudi') }
onMounted(() => document.addEventListener('keydown', esc))
onUnmounted(() => { document.removeEventListener('keydown', esc); sincronizza(false) })
</script>

<template>
  <Teleport to="body">
    <div v-if="aperto" class="sipario" @click.self="emit('chiudi')">
      <section class="foglio" role="dialog" aria-modal="true" :aria-label="titolo">
        <header class="foglio-testa">
          <h2>{{ titolo }}</h2>
          <button class="btn btn-ghost btn-sm" @click="emit('chiudi')" aria-label="Chiudi">Chiudi</button>
        </header>
        <div class="foglio-corpo"><slot /></div>
        <footer v-if="$slots.piede" class="foglio-piede"><slot name="piede" /></footer>
      </section>
    </div>
  </Teleport>
</template>
