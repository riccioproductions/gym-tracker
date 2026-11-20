import { token, esci } from './composables/useAuth'

// In produzione è relativo (stesso server di Spring). Per un backend altrove: VITE_API_BASE=http://host:8080/api
const BASE = import.meta.env.VITE_API_BASE || '/api'

export async function api(percorso, metodo = 'GET', corpo = null) {
  let risposta
  try {
    risposta = await fetch(BASE + percorso, {
      method: metodo,
      headers: {
        'Content-Type': 'application/json',
        ...(token.value ? { Authorization: `Bearer ${token.value}` } : {})
      },
      body: corpo ? JSON.stringify(corpo) : undefined
    })
  } catch {
    throw new Error('Server non raggiungibile. Controlla la connessione.')
  }
  const testo = await risposta.text()
  let dati = null
  if (testo) { try { dati = JSON.parse(testo) } catch { dati = testo } }
  // 401 con un token salvato = sessione scaduta: si torna al login. Senza token (login sbagliato) resta il messaggio del server.
  if (risposta.status === 401 && token.value) {
    esci()
    throw new Error('Sessione scaduta: accedi di nuovo.')
  }
  if (!risposta.ok) throw new Error((dati && dati.messaggio) || `Errore ${risposta.status} su ${percorso}`)
  return dati
}
