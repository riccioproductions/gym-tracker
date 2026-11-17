import { ref } from 'vue'

// Un unico "orologio" condiviso: cronometri e timer calcolano sempre da orari assoluti,
// quindi restano corretti anche se il tab viene sospeso.
export const now = ref(Date.now())
setInterval(() => { now.value = Date.now() }, 500)
document.addEventListener('visibilitychange', () => { now.value = Date.now() })
