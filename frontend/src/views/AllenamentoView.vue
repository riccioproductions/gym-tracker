<script setup>
import { ref, reactive, computed, watch, nextTick, onActivated } from 'vue'
import { api } from '../api'
import { now } from '../composables/useNow'
import { ferma, avvia as avviaRecupero, autoAvvio, ripristina, notificaAttiva, durataUnilaterale } from '../composables/useRestTimer'
import { attivaPush, assicuraPush, pushAttivo } from '../composables/usePush'
import { toast, errore, conferma } from '../composables/useUi'
import { ls, hhmmss, dataIt, compatta, suggerimenti } from '../utils'
import { passoKg, usaRange, incrementoKg, applicaSuggerimenti } from '../composables/useSettings'
import ExerciseCard from '../components/ExerciseCard.vue'
import RestPanel from '../components/RestPanel.vue'
import Modal from '../components/Modal.vue'

const caricamento = ref(true)
const occupato = ref(false)
const allenamento = ref(null)
const schede = ref([])
const giorni = ref([])
const schedaId = ref(ls.get('gt_scheda', ''))
const giornoId = ref('')
const esercizi = ref([])          // EsercizioGiorno del giorno in corso
const serie = ref([])             // serie già registrate
const bozze = reactive({})        // valori in digitazione, per "esercizioGiorno_numeroSerie"
const extra = reactive({})        // serie aggiuntive di questo allenamento: { [esercizioGiornoId]: n }
const ultimo = ref(null)          // ultimo allenamento eseguito (mostrato prima di iniziarne uno)
const consigli = ref([]) 
let ripristinatoPer = null

const lati = eg => (eg.unilaterale ? ['SX', 'DX'] : [null])   // un esercizio unilaterale ha una sottoserie per lato
const chiave = (egId, n, lato) => `${egId}_${n}${lato ? '_' + lato : ''}`
const ultima = ref({})   // sessione precedente dello stesso giorno: { [esercizioGiornoId]: ['8×20', ...] }

// Numero di serie di un esercizio: quelle in scheda + le aggiuntive (mai meno di quelle già registrate)
const totaleSerie = eg => {
  const fatte = serie.value.filter(s => s.esercizioGiorno.id === eg.id).map(s => s.numeroSerie)
  return Math.max((eg.serie || 1) + (extra[eg.id] || 0), ...fatte)
}
const eCompleto = eg => serie.value.filter(s => s.esercizioGiorno.id === eg.id).length >= totaleSerie(eg) * lati(eg).length
// L'ultima serie aggiuntiva si può togliere solo se non è ancora stata registrata
const rimovibile = eg => (extra[eg.id] || 0) > 0
  && !serie.value.some(s => s.esercizioGiorno.id === eg.id && s.numeroSerie === totaleSerie(eg))

watch(schedaId, async v => {
  giornoId.value = ''
  ls.set('gt_scheda', v)
  await caricaGiorni()
})

const trascorso = computed(() => allenamento.value
  ? Math.max(0, Math.floor((now.value - new Date(allenamento.value.dataInizio)) / 1000)) : 0)
const serieTotali = computed(() => esercizi.value.reduce((t, eg) => t + totaleSerie(eg) * lati(eg).length, 0))
const percentuale = computed(() => serieTotali.value ? Math.min(100, (serie.value.length / serieTotali.value) * 100) : 0)

// ---- serie aggiuntive (salvate sul dispositivo, legate a questo allenamento) ----
function salvaExtra() {
  ls.set('gt_extra', { allenamento: allenamento.value.id, extra: { ...extra } })
}
function caricaExtra() {
  Object.keys(extra).forEach(k => delete extra[k])
  const s = ls.get('gt_extra', null)
  if (s && s.allenamento === allenamento.value.id) Object.assign(extra, s.extra)
}
function creaBozze(e) {
  for (let n = 1; n <= totaleSerie(e); n++) {
    for (const lato of lati(e)) {
      const k = chiave(e.id, n, lato)
      if (bozze[k]) continue
      const prec = bozze[chiave(e.id, n - 1, lato)] // la nuova serie parte dai valori della precedente
      bozze[k] = { rip: prec ? prec.rip : (e.ripetizioniTarget ?? null), car: prec ? prec.car : (e.carico ?? null), toccato: false }
    }
  }
}
function aggiungiSerie(eg) {
  extra[eg.id] = (extra[eg.id] || 0) + 1
  creaBozze(eg)
  salvaExtra()
}
function togliSerie(eg) {
  const n = totaleSerie(eg)
  extra[eg.id] -= 1
  if (extra[eg.id] <= 0) delete extra[eg.id]
  for (const lato of lati(eg)) delete bozze[chiave(eg.id, n, lato)]
  salvaExtra()
}

// ---- caricamento ----
async function carica() {
  try {
    const inCorso = await api('/allenamenti/in-corso')
    if (inCorso) {
      allenamento.value = inCorso
      caricaExtra()
      await caricaEsercizi()
      caricaUltima() // senza await: non blocca la schermata
      if (ripristinatoPer !== inCorso.id) { ripristina(serie.value); ripristinatoPer = inCorso.id }
      assicuraPush()
    } else {
      allenamento.value = null
      await caricaSchede()
      caricaUltimo()
    }
  } catch (e) { errore(e) } finally { caricamento.value = false }
}

async function caricaSchede() {
  schede.value = await api('/schede')
  if (!schede.value.some(s => s.id === schedaId.value)) schedaId.value = ''
  await caricaGiorni()
}
async function caricaGiorni() {
  try { giorni.value = schedaId.value ? await api(`/schede/${schedaId.value}/giorni`) : [] } catch (e) { errore(e) }
}

// Ultimo allenamento eseguito: solo il giorno (es. "Pull B") e la data. Non bloccante.
async function caricaUltimo() {
  try { ultimo.value = (await api('/allenamenti/recenti?limite=1'))[0] ?? null } catch { ultimo.value = null }
}

async function caricaEsercizi() {
  const [eg, se] = await Promise.all([
    api(`/giorni/${allenamento.value.giorno.id}/esercizi`),
    api(`/allenamenti/${allenamento.value.id}/serie`)
  ])
  esercizi.value = eg
  serie.value = se
  eg.forEach(creaBozze)
}

// "Ultima volta": serie dell'ultimo allenamento completato dello stesso giorno (per gli unilaterali, il lato più debole)
async function caricaUltima() {
  ultima.value = {}
  try {
    const lista = await api(`/allenamenti/storico/giorno/${allenamento.value.giorno.id}`)
    const prec = lista.find(a => a.id !== allenamento.value.id && a.stato === 'COMPLETATO')
    if (!prec) return
    const se = compatta(await api(`/allenamenti/${prec.id}/serie`))
    const r = {}
    se.sort((a, b) => a.numeroSerie - b.numeroSerie).forEach(s => {
      (r[s.esercizioGiorno.id] ??= []).push(`${s.ripetizioniEffettuate}×${s.caricoEffettivo ?? '-'}`)
    })
    ultima.value = r
  } catch { /* non bloccante */ }
}

// Finito un esercizio, scorre al prossimo da fare (o al pulsante Termina se è tutto completo)
async function vaiAlProssimo(eg) {
  if (!eCompleto(eg)) return
  const i = esercizi.value.findIndex(e => e.id === eg.id)
  const prossimo = esercizi.value.slice(i + 1).find(e => !eCompleto(e)) ?? esercizi.value.find(e => !eCompleto(e))
  await nextTick()
  const el = document.getElementById(prossimo ? `eg-${prossimo.id}` : 'btn-termina')
  el?.scrollIntoView({ behavior: 'smooth', block: prossimo ? 'start' : 'center' })
}

// ---- azioni ----
async function avvia() {
  if (!giornoId.value) return toast('Seleziona prima una scheda e un giorno.', 'errore')
  occupato.value = true
  // Dentro il tocco su "Avvia": è il momento giusto per chiedere il permesso notifiche (solo se non ancora attive)
  if (notificaAttiva.value && !pushAttivo.value) attivaPush()
  try {
    allenamento.value = await api(`/allenamenti/avvia/${giornoId.value}`, 'POST')
    Object.keys(bozze).forEach(k => delete bozze[k])
    caricaExtra() // nuovo allenamento: nessuna serie aggiuntiva
    ferma()
    ripristinatoPer = allenamento.value.id
    caricaUltima()
    await caricaEsercizi()
  } catch (e) { errore(e) } finally { occupato.value = false }
}

async function spunta(eg, n, lato = null) {
  const b = bozze[chiave(eg.id, n, lato)]
  if (b.rip === null || b.rip === '') return toast('Inserisci le ripetizioni effettuate.', 'errore')
  occupato.value = true
  try {
    const nuova = await api(`/allenamenti/${allenamento.value.id}/serie`, 'POST', {
      esercizioGiornoId: eg.id, numeroSerie: n, ripetizioniEffettuate: b.rip, caricoEffettivo: b.car, lato
    })
    serie.value.push(nuova)
    // le serie successive non ancora toccate ereditano i valori appena confermati
    for (let k = n + 1; k <= totaleSerie(eg); k++) {
      const nb = bozze[chiave(eg.id, k, lato)]
      if (nb && !nb.toccato) { nb.rip = b.rip; nb.car = b.car }
    }
    if (lato === 'SX') { // il peso dell'altro lato di questa serie parte da quello appena confermato
      const dx = bozze[chiave(eg.id, n, 'DX')]
      if (dx && !dx.toccato) dx.car = b.car
    }
    if (esercizi.value.every(eCompleto)) ferma() // allenamento completo: niente recupero inutile
    else if (autoAvvio.value) avviaRecupero(eg.unilaterale ? durataUnilaterale.value : undefined)
    vaiAlProssimo(eg)
  } catch (e) {
    errore(e)
    serie.value = await api(`/allenamenti/${allenamento.value.id}/serie`).catch(() => serie.value)
  } finally { occupato.value = false }
}

async function annulla(s) {
  occupato.value = true
  try {
    await api(`/allenamenti/${allenamento.value.id}/serie/${s.id}`, 'DELETE')
    const eraUltima = serie.value.every(x => new Date(x.timestampCompletamento) <= new Date(s.timestampCompletamento))
    serie.value = serie.value.filter(x => x.id !== s.id)
    const b = bozze[chiave(s.esercizioGiorno.id, s.numeroSerie, s.lato || null)]
    if (b) { b.rip = s.ripetizioniEffettuate; b.car = s.caricoEffettivo; b.toccato = true } // si riparte dai valori da correggere
    if (eraUltima) ferma()
  } catch (e) { errore(e) } finally { occupato.value = false }
}

async function termina() {
  const mancano = Math.max(0, serieTotali.value - serie.value.length)
  const testo = mancano
    ? (mancano === 1 ? 'Manca 1 serie' : 'Mancano ' + mancano + ' serie') + ". Terminare comunque l'allenamento?"
    : "Confermi di voler terminare l'allenamento?"
  if (!await conferma(testo, { etichetta: 'Termina', pericolo: false })) return
  const sugg = usaRange.value ? suggerimenti(esercizi.value, serie.value, incrementoKg.value) : []
  const giornoCorrente = allenamento.value.giorno.id
  const egs = [...esercizi.value]
  try {
    await api(`/allenamenti/${allenamento.value.id}/termina`, 'PUT')
    ferma()
    allenamento.value = null
    esercizi.value = []; serie.value = []
    Object.keys(bozze).forEach(k => delete bozze[k])
    Object.keys(extra).forEach(k => delete extra[k])
    ls.set('gt_extra', null)
    await caricaSchede()
    caricaUltimo()
    consigli.value = sugg.map(c => ({ ...c, giornoId: giornoCorrente, eg: egs.find(e => e.id === c.id) }))
  } catch (e) { errore(e) }
}

const puoApplicare = computed(() => applicaSuggerimenti.value && consigli.value.some(c => c.nuovoCarico != null && c.eg))

async function applicaConsigli() {
  const lista = consigli.value.filter(c => c.nuovoCarico != null && c.eg)
  occupato.value = true
  try {
    await Promise.all(lista.map(c => api(`/giorni/${c.giornoId}/esercizi/${c.eg.id}/${c.eg.esercizio.id}`, 'PUT', {
      serie: c.eg.serie, ripetizioniTarget: c.eg.ripetizioniTarget, ripetizioniMax: c.eg.ripetizioniMax,
      carico: c.nuovoCarico, note: c.eg.note, ordine: c.eg.ordine, unilaterale: !!c.eg.unilaterale
    })))
    toast(lista.length === 1 ? 'Carico aggiornato nella scheda.' : `Carichi aggiornati nella scheda (${lista.length} esercizi).`)
    consigli.value = []
  } catch (e) { errore(e) } finally { occupato.value = false }
}

onActivated(carica)
</script>

<template>
  <section>
    <p v-if="caricamento" class="vuoto">Caricamento…</p>

    <!-- Selezione scheda e giorno -->
    <template v-else-if="!allenamento">
      <header class="titolo-pagina"><h1>Allenamento</h1></header>
      <p v-if="ultimo" class="dim spiegazione">
        Ultimo allenamento eseguito: <strong>{{ ultimo.giorno.nome }}</strong> · {{ dataIt(ultimo.dataInizio) }}
      </p>
      <div v-if="!schede.length" class="card vuoto">
        Non hai ancora nessuna scheda. <RouterLink to="/schede">Crea la prima scheda</RouterLink>
      </div>
      <div v-else class="card">
        <label class="campo">
          <span>Scheda</span>
          <select v-model="schedaId">
            <option value="">Seleziona scheda…</option>
            <option v-for="s in schede" :key="s.id" :value="s.id">{{ s.nome }}</option>
          </select>
        </label>
        <div v-if="schedaId" class="elenco-scelte">
          <p v-if="!giorni.length" class="dim">Questa scheda non ha ancora giorni.</p>
          <button v-for="g in giorni" :key="g.id" class="scelta" :aria-pressed="giornoId === g.id" @click="giornoId = g.id">{{ g.nome }}</button>
        </div>
        <button class="btn btn-fatto btn-blocco" :disabled="!giornoId || occupato" @click="avvia">Avvia allenamento</button>
      </div>
    </template>

    <!-- Allenamento in corso -->
    <template v-else>
      <header class="titolo-pagina">
        <h1>{{ allenamento.giorno.nome }}</h1>
        <div class="cronometro num" aria-label="Tempo trascorso">{{ hhmmss(trascorso) }}</div>
      </header>
      <div class="avanzamento" role="progressbar" :aria-valuenow="Math.round(percentuale)" aria-valuemin="0" aria-valuemax="100">
        <span :style="{ width: percentuale + '%' }" :class="{ completo: percentuale >= 100 }"></span>
      </div>
      <p class="dim conteggio">{{ serie.length }} di {{ serieTotali }} serie</p>

      <RestPanel />

      <p v-if="!esercizi.length" class="card vuoto">Questo giorno non ha esercizi. Aggiungili da Schede.</p>
      <ExerciseCard v-for="eg in esercizi" :key="eg.id" :eg="eg" :serie="serie" :bozze="bozze" :ultima="ultima[eg.id]"
                    :passo-kg="passoKg" :occupato="occupato" :totale="totaleSerie(eg)" :rimovibile="rimovibile(eg)"
                    @spunta="spunta" @annulla="annulla" @aggiungi="aggiungiSerie" @togli="togliSerie" />

      <button id="btn-termina" class="btn btn-pericolo btn-blocco termina" @click="termina">Termina allenamento</button>
    </template>
    <Modal :aperto="consigli.length > 0" titolo="Consigli per la prossima sessione" @chiudi="consigli = []">
      <ul class="righe">
        <li v-for="c in consigli" :key="c.id" class="riga">
          <div class="riga-testo">
            <strong><span :class="'trend-' + c.tipo">{{ c.simbolo }}</span> {{ c.nome }}</strong>
            <span class="dim">{{ c.testo }}</span>
          </div>
        </li>
      </ul>
      <p v-if="puoApplicare" class="testo-conferma">Applicare le modifiche suggerite alla scheda?</p>
      <template #piede>
        <template v-if="puoApplicare">
          <button class="btn btn-ghost" @click="consigli = []">Non ora</button>
          <button class="btn btn-primario" :disabled="occupato" @click="applicaConsigli">Applica alla scheda</button>
        </template>
        <button v-else class="btn btn-primario" @click="consigli = []">Ok</button>
      </template>
    </Modal>
  </section>
</template>
