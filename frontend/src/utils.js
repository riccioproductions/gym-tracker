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

/** Unisce le sottoserie SX/DX di una stessa serie in una sola, tenendo il lato più debole (meno ripetizioni).
 *  Le serie senza lato passano invariate. La serie unita conserva i due lati in `dettaglio`. */
export function compatta(serie) {
  const gruppi = new Map()
  const chiave = s => `${s.esercizioGiorno.id}_${s.numeroSerie}`
  serie.forEach(s => {
    if (!s.lato) return
    if (!gruppi.has(chiave(s))) gruppi.set(chiave(s), [])
    gruppi.get(chiave(s)).push(s)
  })
  const fatti = new Set()
  const out = []
  serie.forEach(s => {
    if (!s.lato) { out.push(s); return }
    const k = chiave(s)
    if (fatti.has(k)) return
    fatti.add(k)
    const lista = gruppi.get(k)
    const debole = lista.reduce((a, b) => ((b.ripetizioniEffettuate ?? 0) < (a.ripetizioniEffettuate ?? 0) ? b : a))
    out.push({ ...debole, lato: null, dettaglio: lista })
  })
  return out
}

/** Per ordinare SX prima di DX */
export const pesoLato = s => (s.lato === 'DX' ? 1 : 0)

/** { [esercizioGiornoId]: { nome, totaleRipetizioni, caricoMassimo } } */
export function riepilogo(serie) {
  const r = {}
  serie = compatta(serie) // per gli unilaterali conta il lato più debole
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

export function suggerimenti(esercizi, serie, incremento) {
  const f = n => String(n).replace('.', ',')
  const out = []
  for (const eg of esercizi) {
    const min = eg.ripetizioniTarget, max = eg.ripetizioniMax
    if (!min || !max) continue
    const fatte = compatta(serie.filter(s => s.esercizioGiorno.id === eg.id))
    if (!fatte.length) continue
    const rip = fatte.map(s => s.ripetizioniEffettuate ?? 0)
    const carichi = fatte.map(s => s.caricoEffettivo).filter(c => c != null)
    const base = carichi.length ? Math.max(...carichi) : null
    const nome = eg.esercizio.nome
    const nuovo = d => (base === null ? null : Math.max(0, Math.round((base + d) * 100) / 100))

    if (rip.some(r => r < min)) {
      const n = nuovo(-incremento)
      out.push({ id: eg.id, nome, tipo: 'giu', simbolo: '▼',
        testo: `Eseguite serie sotto le ${min} rip. ` + (n === null ? 'Abbassa il carico.' : `Prova ${f(n)} kg (−${f(incremento)}kg).`) })
    } else if (fatte.length >= (eg.serie || 1) && rip.every(r => r >= max)) {
      const n = nuovo(incremento)
      out.push({ id: eg.id, nome, tipo: 'su', simbolo: '▲',
        testo: `Raggiunte ${max} rip in tutte le serie. ` + (n === null ? 'Aumenta il carico.' : `Prova ${f(n)} kg (+${f(incremento)}kg).`) })
    }
  }
  return out
}
