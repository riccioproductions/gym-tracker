<script setup>
import { ref, reactive, watch } from 'vue'
import { api } from '../api'
import { errore, conferma } from '../composables/useUi'
import { passoKg } from '../composables/useSettings'
import Modal from './Modal.vue'
import Stepper from './Stepper.vue'

const props = defineProps({ giorno: Object }) // { id, nome } oppure null
const emit = defineEmits(['chiudi'])

const catalogo = ref([])
const lista = ref([])
const vuotoForm = () => ({ aperto: false, id: null, esercizioId: '', serie: null, rip: null, carico: null, note: '', ordine: null, unilaterale: false })
const form = reactive(vuotoForm())

watch(() => props.giorno, async g => {
  Object.assign(form, vuotoForm())
  if (!g) return
  try {
    catalogo.value = await api('/esercizi')
    await carica()
  } catch (e) { errore(e) }
})

async function carica() {
  lista.value = await api(`/giorni/${props.giorno.id}/esercizi`)
}

function apriForm(eg = null) {
  Object.assign(form, vuotoForm(), eg ? {
    id: eg.id, esercizioId: eg.esercizio.id, serie: eg.serie, rip: eg.ripetizioniTarget,
    carico: eg.carico, note: eg.note ?? '', ordine: eg.ordine, unilaterale: !!eg.unilaterale
  } : {}, { aperto: true })
}
const chiudiForm = () => Object.assign(form, vuotoForm())

async function salva() {
  if (!form.esercizioId) return errore(new Error('Seleziona un esercizio dal catalogo.'))
  const dato = {
    serie: form.serie ?? null, ripetizioniTarget: form.rip ?? null, carico: form.carico ?? null,
    note: form.note.trim() || null,
    unilaterale: form.unilaterale,
    ordine: form.id ? form.ordine : lista.value.length + 1 // l'ordine va sempre inviato, altrimenti si azzera
  }
  try {
    if (form.id) await api(`/giorni/${props.giorno.id}/esercizi/${form.id}/${form.esercizioId}`, 'PUT', dato)
    else await api(`/giorni/${props.giorno.id}/esercizi/${form.esercizioId}`, 'POST', dato)
    chiudiForm()
    await carica()
  } catch (e) { errore(e) }
}

async function elimina(eg) {
  if (!await conferma("Confermi l'eliminazione di questo esercizio dal giorno?")) return
  try {
    await api(`/giorni/${props.giorno.id}/esercizi/${eg.id}`, 'DELETE')
    await carica()
  } catch (e) { errore(e) }
}

async function sposta(i, delta) {
  const j = i + delta
  if (j < 0 || j >= lista.value.length) return
  const l = [...lista.value]
  ;[l[i], l[j]] = [l[j], l[i]]
  try {
    await Promise.all(l.map((eg, idx) => eg.ordine === idx + 1 ? null
      : api(`/giorni/${props.giorno.id}/esercizi/${eg.id}/${eg.esercizio.id}`, 'PUT', {
          serie: eg.serie, ripetizioniTarget: eg.ripetizioniTarget, carico: eg.carico, note: eg.note, ordine: idx + 1, unilaterale: !!eg.unilaterale
        })))
    await carica()
  } catch (e) { errore(e) }
}
</script>

<template>
  <Modal :aperto="!!giorno" :titolo="giorno ? `Esercizi · ${giorno.nome}` : ''" @chiudi="emit('chiudi')">
    <p v-if="!lista.length" class="dim">Nessun esercizio in questo giorno.</p>
    <ul class="righe">
      <li v-for="(eg, i) in lista" :key="eg.id" class="riga">
        <div class="riga-testo">
          <strong>{{ eg.esercizio.nome }}</strong>
          <span class="dim num">{{ eg.serie ?? '-' }} × {{ eg.ripetizioniTarget ?? '-' }} · {{ eg.carico ?? '-' }} kg</span>
          <span v-if="eg.unilaterale" class="tag">Unilaterale</span>
          <span v-if="eg.note" class="dim">{{ eg.note }}</span>
        </div>
        <div class="riga-azioni">
          <button class="btn btn-ghost btn-sm" :disabled="i === 0" @click="sposta(i, -1)" aria-label="Sposta su">↑</button>
          <button class="btn btn-ghost btn-sm" :disabled="i === lista.length - 1" @click="sposta(i, 1)" aria-label="Sposta giù">↓</button>
          <button class="btn btn-sm" @click="apriForm(eg)">Modifica</button>
          <button class="btn btn-pericolo btn-sm" @click="elimina(eg)">Elimina</button>
        </div>
      </li>
    </ul>
    <template #piede>
      <button class="btn btn-primario" @click="apriForm()">Aggiungi esercizio</button>
    </template>
  </Modal>

  <Modal :aperto="form.aperto" :titolo="form.id ? 'Modifica esercizio' : 'Nuovo esercizio'" @chiudi="chiudiForm">
    <p v-if="!catalogo.length" class="dim">Il catalogo è vuoto: aggiungi prima un esercizio da Esercizi.</p>
    <label class="campo">
      <span>Esercizio</span>
      <select v-model="form.esercizioId">
        <option value="">Seleziona esercizio…</option>
        <option v-for="e in catalogo" :key="e.id" :value="e.id">{{ e.nome }}</option>
      </select>
    </label>
    <div class="tre-colonne">
      <Stepper v-model="form.serie" etichetta="Serie" :min="1" />
      <Stepper v-model="form.rip" etichetta="Rip. target" :min="1" />
      <Stepper v-model="form.carico" etichetta="Carico (kg)" :step="passoKg" decimale />
    </div>
    <label class="campo"><span>Note (facoltative)</span><input v-model="form.note"></label>
    <label class="interruttore campo"><input type="checkbox" v-model="form.unilaterale"><span>Esercizio unilaterale (serie separate per SX e DX)</span></label>
    <template #piede>
      <button class="btn btn-ghost" @click="chiudiForm">Annulla</button>
      <button class="btn btn-primario" @click="salva">Salva</button>
    </template>
  </Modal>
</template>
