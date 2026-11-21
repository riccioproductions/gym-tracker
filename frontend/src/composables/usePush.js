import { ref } from 'vue'
import { api } from '../api'
import { ls } from '../utils'

const K_ATTIVO = 'gt_push'
const K_ENDPOINT = 'gt_push_endpoint'

export const pushSupportato = 'serviceWorker' in navigator && 'PushManager' in window && 'Notification' in window

const ios = /iPad|iPhone|iPod/.test(navigator.userAgent) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1)
const installata = window.matchMedia?.('(display-mode: standalone)').matches || navigator.standalone === true
// Su iPhone/iPad le notifiche push funzionano solo con l'app aggiunta alla schermata Home
export const serveInstallazione = ios && !installata

export const pushAttivo = ref(ls.get(K_ATTIVO, false))
export const errorepush = ref('') // motivo dell'ultimo tentativo di attivazione fallito

const aBytes = b64 => {
  const s = (b64 + '='.repeat((4 - (b64.length % 4)) % 4)).replace(/-/g, '+').replace(/_/g, '/')
  return Uint8Array.from(atob(s), c => c.charCodeAt(0))
}

/** Chiede il permesso (se serve) e registra questo dispositivo sul server.
 *  Va chiamata dentro un tocco dell'utente: la richiesta di permesso parte subito, prima di ogni await. */
export async function attivaPush() {
  errorepush.value = ''
  if (!pushSupportato) {
    errorepush.value = serveInstallazione
      ? "Su iPhone/iPad le notifiche funzionano solo aprendo l'app dall'icona sulla schermata Home (iOS 16.4 o successivo)."
      : 'Questo browser non supporta le notifiche push.'
    return false
  }
  try {
    if (Notification.permission === 'default') await Notification.requestPermission()
    if (Notification.permission !== 'granted') {
      errorepush.value = `Permesso notifiche non concesso (stato: ${Notification.permission}). Abilitalo dalle impostazioni del dispositivo per questo sito/app.`
      return false
    }
    const reg = await Promise.race([
      navigator.serviceWorker.ready,
      new Promise((_, no) => setTimeout(() => no(new Error('Service worker non attivo (sw.js non registrato)')), 8000))
    ])
    let sub = await reg.pushManager.getSubscription()
    if (!sub) {
      const { chiave } = await api('/push/chiave')
      if (!chiave) { errorepush.value = 'Il server non ha le chiavi push configurate.'; return false }
      sub = await reg.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: aBytes(chiave) })
    }
    const j = sub.toJSON()
    await api('/push/sottoscrizione', 'POST', { endpoint: j.endpoint, p256dh: j.keys.p256dh, auth: j.keys.auth })
    ls.set(K_ATTIVO, true)
    ls.set(K_ENDPOINT, j.endpoint)
    pushAttivo.value = true
    return true
  } catch (e) {
    errorepush.value = 'Registrazione non riuscita: ' + (e?.message || e)
    return false
  }
}

/** All'avvio: se il permesso c'è già, riallinea la sottoscrizione col server (senza chiedere nulla). */
export async function assicuraPush() {
  if (!pushSupportato || !ls.get('gt_notifica', true) || Notification.permission !== 'granted') return
  await attivaPush()
}

export async function disattivaPush() {
  const endpoint = ls.get(K_ENDPOINT, null)
  pushAttivo.value = false
  ls.set(K_ATTIVO, false)
  ls.set(K_ENDPOINT, null)
  try {
    if (endpoint) await api('/push/sottoscrizione', 'DELETE', { endpoint })
    const reg = await navigator.serviceWorker.getRegistration()
    const sub = await reg?.pushManager.getSubscription()
    await sub?.unsubscribe()
  } catch { /* non bloccante */ }
}

/** Il server invierà la notifica tra `secondi` (sostituisce un eventuale invio già programmato). */
export function programmaRecupero(secondi) {
  if (pushAttivo.value) api('/push/recupero', 'POST', { secondi }).catch(() => {})
}
export function annullaRecupero() {
  if (pushAttivo.value) api('/push/recupero', 'DELETE').catch(() => {})
}
