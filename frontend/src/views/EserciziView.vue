<script setup>
import { ref, reactive, computed, onActivated } from 'vue'
import { api } from '../api'
import { errore, conferma } from '../composables/useUi'
import Modal from '../components/Modal.vue'

const esercizi = ref([])
const ricerca = ref('')
const form = reactive({ aperto: false, id: null, nome: '', gruppo: '' })

const filtrati = computed(() => {
  const q = ricerca.value.trim().toLowerCase()
  return esercizi.value
    .filter(e => !q || e.nome.toLowerCase().includes(q) || (e.gruppoMuscolare || '').toLowerCase().includes(q))
    .sort((a, b) => a.nome.localeCompare(b.nome, 'it'))
})

async function carica() {
  try { esercizi.value = await api('/esercizi') } catch (e) { errore(e) }
}
onActivated(carica)

function apri(e = null) {
  Object.assign(form, { aperto: true, id: e?.id ?? null, nome: e?.nome ?? '', gruppo: e?.gruppoMuscolare ?? '' })
}
async function salva() {
  const nome = form.nome.trim()
  if (!nome) return errore(new Error("Il nome dell'esercizio è obbligatorio."))
  const dato = { nome, gruppoMuscolare: form.gruppo.trim() }
  try {
    if (form.id) await api(`/esercizi/${form.id}`, 'PUT', dato)
    else await api('/esercizi', 'POST', dato)
    form.aperto = false
    await carica()
  } catch (e) { errore(e) }
}
async function elimina(e) {
  if (!await conferma("Confermi l'eliminazione di questo esercizio?")) return
  try { await api(`/esercizi/${e.id}`, 'DELETE'); await carica() } catch (err) { errore(err) }
}
</script>

<template>
  <section>
    <header class="titolo-pagina">
      <h1>Catalogo esercizi</h1>
      <button class="btn btn-primario" @click="apri()">Nuovo esercizio</button>
    </header>

    <input v-model="ricerca" class="ricerca" type="search" placeholder="Cerca per nome o gruppo muscolare" aria-label="Cerca esercizio">

    <p v-if="!filtrati.length" class="card vuoto">{{ esercizi.length ? 'Nessun risultato.' : 'Il catalogo è vuoto. Aggiungi il primo esercizio.' }}</p>

    <ul v-else class="card righe">
      <li v-for="e in filtrati" :key="e.id" class="riga">
        <div class="riga-testo">
          <strong>{{ e.nome }}</strong>
          <span v-if="e.gruppoMuscolare" class="tag">{{ e.gruppoMuscolare }}</span>
        </div>
        <div class="riga-azioni">
          <button class="btn btn-sm" @click="apri(e)">Modifica</button>
          <button class="btn btn-pericolo btn-sm" @click="elimina(e)">Elimina</button>
        </div>
      </li>
    </ul>

    <Modal :aperto="form.aperto" :titolo="form.id ? 'Modifica esercizio' : 'Nuovo esercizio'" @chiudi="form.aperto = false">
      <label class="campo"><span>Nome</span><input v-model="form.nome" required></label>
      <label class="campo"><span>Gruppo muscolare</span><input v-model="form.gruppo" placeholder="es. Petto, Gambe…"></label>
      <template #piede>
        <button class="btn btn-ghost" @click="form.aperto = false">Annulla</button>
        <button class="btn btn-primario" @click="salva">Salva</button>
      </template>
    </Modal>
  </section>
</template>
