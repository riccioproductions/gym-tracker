<script setup>
import { ref, reactive, computed, watch, onActivated } from 'vue'
import { api } from '../api'
import { now } from '../composables/useNow'
import { ferma, avvia as avviaRecupero, autoAvvio, ripristina, chiediNotifiche } from '../composables/useRestTimer'
import { toast, errore, conferma } from '../composables/useUi'
import { ls, hhmmss } from '../utils'
import { passoKg } from '../composables/useSettings'
import ExerciseCard from '../components/ExerciseCard.vue'
import RestPanel from '../components/RestPanel.vue'

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
let ripristinatoPer = null

watch(schedaId, async v => {
  giornoId.value = ''
  ls.set('gt_scheda', v)
  await caricaGiorni()
})

const trascorso = computed(() => allenamento.value
  ? Math.max(0, Math.floor((now.value - new Date(allenamento.value.dataInizio)) / 1000)) : 0)
const serieTotali = computed(() => esercizi.value.reduce((t, eg) => t + (eg.serie || 1), 0))
const percentuale = computed(() => serieTotali.value ? Math.min(100, (serie.value.length / serieTotali.value) * 100) : 0)

// ---- caricamento ----
async function carica() {
  try {
    const inCorso = await api('/allenamenti/in-corso')
    if (inCorso) {
      allenamento.value = inCorso
      await caricaEsercizi()
      if (ripristinatoPer !== inCorso.id) { ripristina(serie.value); ripristinatoPer = inCorso.id }
      chiediNotifiche()
    } else {
      allenamento.value = null
      await caricaSchede()
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

async function caricaEsercizi() {
  const [eg, se] = await Promise.all([
    api(`/giorni/${allenamento.value.giorno.id}/esercizi`),
    api(`/allenamenti/${allenamento.value.id}/serie`)
  ])
  esercizi.value = eg
  serie.value = se
  eg.forEach(e => {
    for (let n = 1; n <= (e.serie || 1); n++) {
      const k = `${e.id}_${n}`
      if (!bozze[k]) bozze[k] = { rip: e.ripetizioniTarget ?? null, car: e.carico ?? null, toccato: false }
    }
  })
}

// ---- azioni ----
async function avvia() {
  if (!giornoId.value) return toast('Seleziona prima una scheda e un giorno.', 'errore')
  occupato.value = true
  try {
    allenamento.value = await api(`/allenamenti/avvia/${giornoId.value}`, 'POST')
    Object.keys(bozze).forEach(k => delete bozze[k])
    ferma()
    ripristinatoPer = allenamento.value.id
    await caricaEsercizi()
    chiediNotifiche()
  } catch (e) { errore(e) } finally { occupato.value = false }
}

async function spunta(eg, n) {
  const b = bozze[`${eg.id}_${n}`]
  if (b.rip === null || b.rip === '') return toast('Inserisci le ripetizioni effettuate.', 'errore')
  occupato.value = true
  try {
    const nuova = await api(`/allenamenti/${allenamento.value.id}/serie`, 'POST', {
      esercizioGiornoId: eg.id, numeroSerie: n, ripetizioniEffettuate: b.rip, caricoEffettivo: b.car
    })
    serie.value.push(nuova)
    // le serie successive non ancora toccate ereditano i valori appena confermati
    for (let k = n + 1; k <= (eg.serie || 1); k++) {
      const nb = bozze[`${eg.id}_${k}`]
      if (nb && !nb.toccato) { nb.rip = b.rip; nb.car = b.car }
    }
    if (autoAvvio.value) avviaRecupero()
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
    const b = bozze[`${s.esercizioGiorno.id}_${s.numeroSerie}`]
    if (b) { b.rip = s.ripetizioniEffettuate; b.car = s.caricoEffettivo; b.toccato = true } // si riparte dai valori da correggere
    if (eraUltima) ferma()
  } catch (e) { errore(e) } finally { occupato.value = false }
}

async function termina() {
  if (!await conferma("Confermi di voler terminare l'allenamento?", { etichetta: 'Termina', pericolo: false })) return
  try {
    await api(`/allenamenti/${allenamento.value.id}/termina`, 'PUT')
    ferma()
    allenamento.value = null
    esercizi.value = []; serie.value = []
    Object.keys(bozze).forEach(k => delete bozze[k])
    await caricaSchede()
  } catch (e) { errore(e) }
}

onActivated(carica)
</script>

<template>
  <section>
    <p v-if="caricamento" class="vuoto">Caricamento…</p>

    <!-- Selezione scheda e giorno -->
    <template v-else-if="!allenamento">
      <header class="titolo-pagina"><h1>Allenamento</h1></header>
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
      <ExerciseCard v-for="eg in esercizi" :key="eg.id" :eg="eg" :serie="serie" :bozze="bozze"
                    :passo-kg="passoKg" :occupato="occupato" @spunta="spunta" @annulla="annulla" />

      <button class="btn btn-pericolo btn-blocco termina" @click="termina">Termina allenamento</button>
    </template>
  </section>
</template>
