import { ref, computed, watch } from 'vue'
import { now } from './useNow'
import { ls } from '../utils'

const K_DURATA = 'gt_recupero_durata'
const K_FINE = 'gt_recupero_fine'
const K_AUTO = 'gt_recupero_auto'

export const PRESET = [90, 180, 300]
export const PASSO = 15

export const durata = ref(Number(ls.get(K_DURATA, 180)))   // durata predefinita (default 3:00)
export const autoAvvio = ref(ls.get(K_AUTO, true))         // parte da solo dopo ogni serie
export const fine = ref(null)                              // orario assoluto di fine (ms)
export const totale = ref(durata.value)
export const appenaFinito = ref(false)

export const inCorso = computed(() => fine.value !== null)
export const rimanenti = computed(() => (fine.value ? Math.max(0, Math.ceil((fine.value - now.value) / 1000)) : 0))
export const progresso = computed(() => (inCorso.value ? Math.min(1, 1 - rimanenti.value / Math.max(1, totale.value)) : 0))

watch(durata, v => ls.set(K_DURATA, v))
export const durataUnilaterale = ref(Number(ls.get('gt_recupero_durata_uni', 90))) // recupero dopo ogni sottoserie SX/DX
watch(durataUnilaterale, v => ls.set('gt_recupero_durata_uni', v))
export const allarmeAttivo = ref(ls.get('gt_allarme_app', true))   // suono + vibrazione in app
export const notificaAttiva = ref(ls.get('gt_notifica', true))     // notifica di sistema

watch(autoAvvio, v => ls.set(K_AUTO, v))
watch(allarmeAttivo, v => ls.set('gt_allarme_app', v))
watch(notificaAttiva, v => ls.set('gt_notifica', v))

export function imposta(secondi) {
  durata.value = Math.min(900, Math.max(15, secondi))
}

export function impostaUnilaterale(secondi) {
  durataUnilaterale.value = Math.min(900, Math.max(15, secondi))
}

export function avvia(secondi = durata.value, fineForzata = null) {
  totale.value = secondi
  fine.value = fineForzata ?? Date.now() + secondi * 1000
  appenaFinito.value = false
  ls.set(K_FINE, { fine: fine.value, totale: totale.value })
}

export function ferma() {
  fine.value = null
  ls.set(K_FINE, { fine: 0, stop: Date.now() })
}

/** Aggiusta il recupero in corso (+/- scaglioni da 15s). */
export function regola(delta) {
  if (!inCorso.value) return
  const nuovaFine = fine.value + delta * 1000
  if (nuovaFine <= Date.now() + 1000) return ferma()
  fine.value = nuovaFine
  totale.value = Math.max(15, totale.value + delta)
  ls.set(K_FINE, { fine: fine.value, totale: totale.value })
}

/** Dopo un refresh/riapertura: riprende il recupero ancora attivo. */
export function ripristina(serie) {
  const salvato = ls.get(K_FINE, null)
  let ripresa = salvato && salvato.fine > Date.now() ? salvato : null
  if (!ripresa && serie?.length) {
    const stop = salvato?.stop ?? 0
    const ultimaSerie = serie.reduce((a, b) => (new Date(b.timestampCompletamento) > new Date(a.timestampCompletamento) ? b : a))
    const ultima = new Date(ultimaSerie.timestampCompletamento).getTime()
    const dur = ultimaSerie.lato ? durataUnilaterale.value : durata.value
    const t = ultima + dur * 1000
    if (ultima > stop && t > Date.now()) ripresa = { fine: t, totale: dur }
  }
  if (ripresa) avvia(ripresa.totale, ripresa.fine)
}

export function chiudiFinito() { appenaFinito.value = false }

// ---- Avviso di fine recupero: suono + vibrazione + notifica + banner (niente alert bloccante) ----
let audio = null
export function sbloccaAudio() {
  try {
    audio ??= new (window.AudioContext || window.webkitAudioContext)()
    if (audio.state !== 'running') audio.resume() // su iOS lo stato può essere anche 'interrupted'
  } catch { /* audio non disponibile */ }
}

// Suono tipo sveglia: 3 raffiche da 3 beep acuti ad onda quadra (molto più penetrante di un tono dolce)
async function suona() {
  if (!audio) return
  if (audio.state !== 'running') { try { await audio.resume() } catch { /* ignora */ } }
  if (audio.state !== 'running') return
  const inizio = audio.currentTime + 0.03
  for (let raffica = 0; raffica < 3; raffica++) {
    for (let beep = 0; beep < 3; beep++) {
      const t = inizio + raffica * 0.9 + beep * 0.2
      const o = audio.createOscillator()
      const g = audio.createGain()
      o.type = 'square'
      o.frequency.value = beep % 2 ? 2400 : 1800
      g.gain.setValueAtTime(0.0001, t)
      g.gain.exponentialRampToValueAtTime(0.45, t + 0.01)
      g.gain.setValueAtTime(0.45, t + 0.11)
      g.gain.exponentialRampToValueAtTime(0.0001, t + 0.14)
      o.connect(g); g.connect(audio.destination)
      o.start(t); o.stop(t + 0.15)
    }
  }
}

// Anteprima dell'avviso completo (suono + vibrazione + popup) senza aspettare il timer
export function provaSuono() {
  sbloccaAudio()
  appenaFinito.value = true
}

// L'avviso si ripete ogni 6 secondi finché non tocchi lo schermo (chiudiFinito) o non parte un nuovo recupero
let ripetizione = null
watch(appenaFinito, attivo => {
  clearInterval(ripetizione)
  ripetizione = null
  if (!attivo) return
  const avviso = () => {
    if (allarmeAttivo.value) suona()
    try { if (allarmeAttivo.value) navigator.vibrate?.([400, 150, 400, 150, 400, 150, 700]) } catch { /* ignora */ }
  }
  avviso()
  ripetizione = setInterval(avviso, 4000)
})

export function chiediNotifiche() {
  try { if ('Notification' in window && Notification.permission === 'default') Notification.requestPermission() } catch { /* ignora */ }
}

function finito() {
  ferma()
  appenaFinito.value = true
  try {
    if (notificaAttiva.value && 'Notification' in window && Notification.permission === 'granted') {
      const titolo = 'Recupero terminato'
      const opz = { body: 'Puoi iniziare la serie successiva.', tag: 'recupero' }
      ;(navigator.serviceWorker?.getRegistration() ?? Promise.resolve())
        .then(r => (r ? r.showNotification(titolo, opz) : new Notification(titolo, opz)))
        .catch(() => {})
    }
  } catch { /* ignora */ }
}

watch(now, n => { if (fine.value !== null && n >= fine.value) finito() })

// Al ritorno in primo piano: riattiva l'audio (iOS lo sospende) e, se l'allarme è già scattato, riparte il suono
document.addEventListener('visibilitychange', () => {
  if (document.hidden) return
  sbloccaAudio()
  if (appenaFinito.value && allarmeAttivo.value) setTimeout(suona, 200)
})
