import { reactive } from 'vue'

export const toasts = reactive([])
let contatore = 0

export function toast(testo, tipo = 'info', ms = 4000) {
  const t = { id: ++contatore, testo, tipo }
  toasts.push(t)
  setTimeout(() => {
    const i = toasts.findIndex(x => x.id === t.id)
    if (i >= 0) toasts.splice(i, 1)
  }, ms)
}
export const errore = e => toast(e?.message || String(e), 'errore', 6000)

export const confermaStato = reactive({ aperto: false, testo: '', etichetta: 'Conferma', pericolo: true, risolvi: null })

/** await conferma('Testo') → true/false (sostituisce window.confirm) */
export function conferma(testo, { etichetta = 'Elimina', pericolo = true } = {}) {
  return new Promise(risolvi => Object.assign(confermaStato, { aperto: true, testo, etichetta, pericolo, risolvi }))
}
export function chiudiConferma(esito) {
  confermaStato.aperto = false
  confermaStato.risolvi?.(esito)
}
