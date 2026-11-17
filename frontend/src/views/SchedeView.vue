<script setup>
import { ref, reactive, onActivated } from 'vue'
import { api } from '../api'
import { errore, conferma } from '../composables/useUi'
import Modal from '../components/Modal.vue'
import EserciziGiornoModal from '../components/EserciziGiornoModal.vue'

const schede = ref([])
const giorniPer = ref({})
const giornoAperto = ref(null)
const fScheda = reactive({ aperto: false, id: null, nome: '', descrizione: '' })
const fGiorno = reactive({ aperto: false, id: null, schedaId: null, nome: '', ordine: 1 })

async function carica() {
  try {
    const elenco = await api('/schede')
    const giorni = await Promise.all(elenco.map(s => api(`/schede/${s.id}/giorni`)))
    schede.value = elenco
    giorniPer.value = Object.fromEntries(elenco.map((s, i) => [s.id, giorni[i]]))
  } catch (e) { errore(e) }
}
onActivated(carica)

// ---- Scheda ----
function apriScheda(s = null) {
  Object.assign(fScheda, { aperto: true, id: s?.id ?? null, nome: s?.nome ?? '', descrizione: s?.descrizione ?? '' })
}
async function salvaScheda() {
  const nome = fScheda.nome.trim()
  if (!nome) return errore(new Error('Il nome della scheda è obbligatorio.'))
  const dato = { nome, descrizione: fScheda.descrizione.trim() }
  try {
    if (fScheda.id) await api(`/schede/${fScheda.id}`, 'PUT', dato)
    else await api('/schede', 'POST', dato)
    fScheda.aperto = false
    await carica()
  } catch (e) { errore(e) }
}
async function eliminaScheda(s) {
  if (!await conferma("Confermi l'eliminazione della scheda? Verranno eliminati anche tutti i giorni collegati.")) return
  try { await api(`/schede/${s.id}`, 'DELETE'); await carica() } catch (e) { errore(e) }
}

// ---- Giorno ----
function apriGiorno(schedaId, g = null) {
  Object.assign(fGiorno, {
    aperto: true, id: g?.id ?? null, schedaId, nome: g?.nome ?? '',
    ordine: g?.ordine ?? (giorniPer.value[schedaId]?.length ?? 0) + 1
  })
}
async function salvaGiorno() {
  const nome = fGiorno.nome.trim()
  if (!nome) return errore(new Error('Il nome del giorno è obbligatorio.'))
  const dato = { nome, ordine: parseInt(fGiorno.ordine) || 1 }
  try {
    if (fGiorno.id) await api(`/schede/${fGiorno.schedaId}/giorni/${fGiorno.id}`, 'PUT', dato)
    else await api(`/schede/${fGiorno.schedaId}/giorni`, 'POST', dato)
    fGiorno.aperto = false
    await carica()
  } catch (e) { errore(e) }
}
async function eliminaGiorno(g, schedaId) {
  if (!await conferma("Confermi l'eliminazione del giorno?")) return
  try { await api(`/schede/${schedaId}/giorni/${g.id}`, 'DELETE'); await carica() } catch (e) { errore(e) }
}
</script>

<template>
  <section>
    <header class="titolo-pagina">
      <h1>Le tue schede</h1>
      <button class="btn btn-primario" @click="apriScheda()">Nuova scheda</button>
    </header>

    <p v-if="!schede.length" class="card vuoto">Nessuna scheda. Creane una per iniziare.</p>

    <article v-for="s in schede" :key="s.id" class="card scheda">
      <header class="scheda-testa">
        <div>
          <h2>{{ s.nome }}</h2>
          <p v-if="s.descrizione" class="dim">{{ s.descrizione }}</p>
        </div>
        <div class="riga-azioni">
          <button class="btn btn-sm" @click="apriScheda(s)">Modifica</button>
          <button class="btn btn-pericolo btn-sm" @click="eliminaScheda(s)">Elimina</button>
        </div>
      </header>

      <ul class="righe">
        <li v-for="g in giorniPer[s.id]" :key="g.id" class="riga">
          <strong class="riga-testo">{{ g.nome }}</strong>
          <div class="riga-azioni">
            <button class="btn btn-primario btn-sm" @click="giornoAperto = { id: g.id, nome: g.nome }">Esercizi</button>
            <button class="btn btn-sm" @click="apriGiorno(s.id, g)">Modifica</button>
            <button class="btn btn-pericolo btn-sm" @click="eliminaGiorno(g, s.id)">Elimina</button>
          </div>
        </li>
      </ul>
      <button class="btn btn-ghost btn-sm" @click="apriGiorno(s.id)">Aggiungi giorno</button>
    </article>

    <Modal :aperto="fScheda.aperto" :titolo="fScheda.id ? 'Modifica scheda' : 'Nuova scheda'" @chiudi="fScheda.aperto = false">
      <label class="campo"><span>Nome</span><input v-model="fScheda.nome" required></label>
      <label class="campo"><span>Descrizione</span><textarea v-model="fScheda.descrizione"></textarea></label>
      <template #piede>
        <button class="btn btn-ghost" @click="fScheda.aperto = false">Annulla</button>
        <button class="btn btn-primario" @click="salvaScheda">Salva</button>
      </template>
    </Modal>

    <Modal :aperto="fGiorno.aperto" :titolo="fGiorno.id ? 'Modifica giorno' : 'Nuovo giorno'" @chiudi="fGiorno.aperto = false">
      <label class="campo"><span>Nome giorno</span><input v-model="fGiorno.nome" placeholder="es. Giorno A - Petto/Tricipiti"></label>
      <label class="campo"><span>Ordine</span><input v-model="fGiorno.ordine" type="number" inputmode="numeric"></label>
      <template #piede>
        <button class="btn btn-ghost" @click="fGiorno.aperto = false">Annulla</button>
        <button class="btn btn-primario" @click="salvaGiorno">Salva</button>
      </template>
    </Modal>

    <EserciziGiornoModal :giorno="giornoAperto" @chiudi="giornoAperto = null" />
  </section>
</template>
