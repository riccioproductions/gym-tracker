export const ls = {
  get(k, d) { try { const v = localStorage.getItem(k); return v === null ? d : JSON.parse(v) } catch { return d } },
  set(k, v) { try { localStorage.setItem(k, JSON.stringify(v)) } catch { /* storage non disponibile */ } }
}

const due = n => String(n).padStart(2, '0')
export const mmss = s => `${due(Math.floor(s / 60))}:${due(s % 60)}`
export const hhmmss = s => `${due(Math.floor(s / 3600))}:${mmss(s % 3600)}`
export const dataIt = iso => new Date(iso).toLocaleDateString('it-IT', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })
export const oraIt = iso => new Date(iso).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })

/** Raggruppa le serie per esercizio-del-giorno, mantenendo l'ordine di esecuzione. */
export function perEsercizio(serie) {
  const m = new Map()
  serie.forEach(s => {
    const id = s.esercizioGiorno.id
    if (!m.has(id)) m.set(id, [])
    m.get(id).push(s)
  })
  return m
}

/** { [esercizioGiornoId]: { nome, totaleRipetizioni, caricoMassimo } } */
export function riepilogo(serie) {
  const r = {}
  serie.forEach(s => {
    const id = s.esercizioGiorno.id
    const nome = s.nomeEsercizio || s.esercizioGiorno.esercizio.nome
    if (!r[id]) r[id] = { nome, totaleRipetizioni: 0, caricoMassimo: 0 }
    r[id].totaleRipetizioni += s.ripetizioniEffettuate || 0
    r[id].caricoMassimo = Math.max(r[id].caricoMassimo, s.caricoEffettivo || 0)
  })
  return r
}

/** Il carico ha priorità sulle ripetizioni. null = nessun confronto possibile. */
export function trend(att, prec) {
  if (!att || !prec) return null
  if (att.caricoMassimo > prec.caricoMassimo) return { tipo: 'su', simbolo: '▲', titolo: 'Carico aumentato' }
  if (att.caricoMassimo < prec.caricoMassimo) return { tipo: 'giu', simbolo: '▼', titolo: 'Carico diminuito' }
  if (att.totaleRipetizioni > prec.totaleRipetizioni) return { tipo: 'su', simbolo: '▲', titolo: 'Ripetizioni aumentate' }
  if (att.totaleRipetizioni < prec.totaleRipetizioni) return { tipo: 'giu', simbolo: '▼', titolo: 'Ripetizioni diminuite' }
  return { tipo: 'uguale', simbolo: '=', titolo: 'Invariato rispetto alla sessione precedente' }
}
