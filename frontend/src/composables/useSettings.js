import { ref, watch } from 'vue'
import { ls } from '../utils'

export const PASSI_PESO = [0.25, 0.5, 1, 2.5, 5]
export const ACCENTI = {
  blu: { nome: 'Blu', c: '#3d7bff', ink: '#ffffff' },
  verde: { nome: 'Verde', c: '#34b76b', ink: '#07210f' },
  arancione: { nome: 'Arancione', c: '#ff8a3d', ink: '#1f0d00' },
  viola: { nome: 'Viola', c: '#9b6bff', ink: '#ffffff' }
}

export const passoKg = ref(Number(ls.get('gt_passo_peso', 0.5)))
const salvato = ls.get('gt_accento', 'blu')
export const accento = ref(ACCENTI[salvato] ? salvato : 'blu')

watch(passoKg, v => ls.set('gt_passo_peso', v))
watch(accento, v => {
  ls.set('gt_accento', v)
  const a = ACCENTI[v] || ACCENTI.blu
  document.documentElement.style.setProperty('--accent', a.c)
  document.documentElement.style.setProperty('--accent-ink', a.ink)
}, { immediate: true })

// Storico: true = per gli esercizi unilaterali mostra solo il lato più debole; false = mostra SX e DX
export const storicoSoloMinimo = ref(ls.get('gt_storico_min', true))
watch(storicoSoloMinimo, v => ls.set('gt_storico_min', v))
