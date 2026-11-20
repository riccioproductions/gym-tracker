import { ref } from 'vue'
import { ls } from '../utils'

const K_TOKEN = 'gt_token'
const K_UTENTE = 'gt_utente'
// Dati legati all'utente: vanno tolti all'uscita, così il prossimo che entra da questo dispositivo non li eredita
const K_DA_PULIRE = [K_TOKEN, K_UTENTE, 'gt_recupero_fine', 'gt_scheda']

export const token = ref(ls.get(K_TOKEN, null))
export const nomeUtente = ref(ls.get(K_UTENTE, ''))

// Dopo l'accesso si ricarica la pagina: nessuno stato dell'utente precedente resta in memoria (viste, timer, bozze)
export function entra(t, username) {
  ls.set(K_TOKEN, t)
  ls.set(K_UTENTE, username)
  token.value = t
  nomeUtente.value = username
  location.hash = '#/allenamento'
  location.reload()
}

export function esci() {
  for (const k of K_DA_PULIRE) { try { localStorage.removeItem(k) } catch { /* storage non disponibile */ } }
  token.value = null
  nomeUtente.value = ''
  location.hash = '#/login'
  location.reload()
}
