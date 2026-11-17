<script setup>
import { ref, watch, onActivated } from 'vue'
import { api } from '../api'
import { errore, conferma } from '../composables/useUi'
import { riepilogo } from '../utils'
import StoricoCard from '../components/StoricoCard.vue'

const schede = ref([])
const giorni = ref([])
const schedaId = ref('')
const giornoId = ref('')
const voci = ref([])
const messaggio = ref('')
const caricamento = ref(false)

async function init() {
  try {
    schede.value = await api('/schede')
    if (schedaId.value && !schede.value.some(s => s.id === schedaId.value)) schedaId.value = ''
    await ricarica()
  } catch (e) { errore(e) }
}
onActivated(init)

// Vista predefinita: ultimi 8 allenamenti. Il trend confronta ogni sessione con la precedente dello STESSO giorno.
async function caricaRecenti() {
  caricamento.value = true
  try {
    const recenti = await api('/allenamenti/recenti?limite=8')
    if (!recenti.length) { voci.value = []; messaggio.value = 'Nessun allenamento registrato ancora.'; return }
    const ids = [...new Set(recenti.map(a => a.giorno.id))]
    const storici = Object.fromEntries(await Promise.all(ids.map(async id => [id, await api(`/allenamenti/storico/giorno/${id}`)])))
    voci.value = await Promise.all(recenti.map(async a => {
      const lista = storici[a.giorno.id]
      const prec = lista[lista.findIndex(x => x.id === a.id) + 1] || null
      const [serie, seriePrec] = await Promise.all([
        api(`/allenamenti/${a.id}/serie`),
        prec ? api(`/allenamenti/${prec.id}/serie`) : null
      ])
      return { allenamento: a, serie, precedente: seriePrec ? riepilogo(seriePrec) : null, mostraGiorno: true }
    }))
    messaggio.value = ''
  } finally { caricamento.value = false }
}

async function caricaGiorno() {
  caricamento.value = true
  try {
    const lista = await api(`/allenamenti/storico/giorno/${giornoId.value}`)
    if (!lista.length) { voci.value = []; messaggio.value = 'Nessun allenamento registrato per questo giorno.'; return }
    const conSerie = await Promise.all(lista.map(async a => ({ allenamento: a, serie: await api(`/allenamenti/${a.id}/serie`) })))
    const riepiloghi = conSerie.map(x => riepilogo(x.serie))
    voci.value = conSerie.map((x, i) => ({ ...x, precedente: riepiloghi[i + 1] || null, mostraGiorno: false }))
    messaggio.value = ''
  } finally { caricamento.value = false }
}

async function ricarica() {
  try {
    if (giornoId.value) await caricaGiorno()
    else if (schedaId.value) { voci.value = []; messaggio.value = 'Seleziona un giorno per vedere lo storico.' }
    else await caricaRecenti()
  } catch (e) { errore(e) }
}

// Nei select si usano watcher (non @change) per essere sicuri che il v-model sia già aggiornato
watch(schedaId, async () => {
  giornoId.value = ''
  giorni.value = []
  try {
    if (schedaId.value) giorni.value = await api(`/schede/${schedaId.value}/giorni`)
    await ricarica()
  } catch (e) { errore(e) }
})
watch(giornoId, () => { if (giornoId.value) ricarica() })

function mostraRecenti() {
  schedaId.value = ''
  giornoId.value = ''
}

async function elimina(id) {
  if (!await conferma("Confermi l'eliminazione definitiva di questo log allenamento? L'operazione non è reversibile.")) return
  try { await api(`/allenamenti/${id}`, 'DELETE'); await ricarica() } catch (e) { errore(e) }
}
</script>

<template>
  <section>
    <header class="titolo-pagina"><h1>Storico allenamenti</h1></header>

    <div class="card filtri">
      <label class="campo">
        <span>Filtra per scheda</span>
        <select v-model="schedaId">
          <option value="">Tutte (ultimi allenamenti)</option>
          <option v-for="s in schede" :key="s.id" :value="s.id">{{ s.nome }}</option>
        </select>
      </label>
      <label v-if="schedaId" class="campo">
        <span>Filtra per giorno</span>
        <select v-model="giornoId">
          <option value="">Seleziona giorno…</option>
          <option v-for="g in giorni" :key="g.id" :value="g.id">{{ g.nome }}</option>
        </select>
      </label>
      <button v-if="schedaId || giornoId" class="btn btn-ghost btn-blocco" @click="mostraRecenti">Mostra ultimi allenamenti</button>
    </div>

    <p v-if="caricamento" class="vuoto">Caricamento…</p>
    <p v-else-if="messaggio" class="vuoto">{{ messaggio }}</p>
    <StoricoCard v-for="v in voci" :key="v.allenamento.id" v-bind="v" @elimina="elimina" />
  </section>
</template>
