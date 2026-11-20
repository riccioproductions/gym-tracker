<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'
import { errore } from '../composables/useUi'
import { entra } from '../composables/useAuth'

const modo = ref('accedi') // 'accedi' | 'registrati'
const username = ref('')
const password = ref('')
const codice = ref('')
const primoUtente = ref(false)
const occupato = ref(false)

onMounted(async () => {
  try {
    primoUtente.value = (await api('/auth/stato')).primoUtente
    if (primoUtente.value) modo.value = 'registrati' // nessun account ancora: si parte dalla registrazione
  } catch (e) { errore(e) }
})

async function invia() {
  if (!username.value.trim() || !password.value) return errore(new Error('Inserisci username e password.'))
  occupato.value = true
  try {
    const r = await api(modo.value === 'accedi' ? '/auth/accedi' : '/auth/registrati', 'POST', {
      username: username.value, password: password.value, codiceInvito: codice.value
    })
    entra(r.token, r.username) // ricarica la pagina
  } catch (e) {
    errore(e)
    occupato.value = false
  }
}
</script>

<template>
  <section>
    <header class="titolo-pagina"><h1>{{ modo === 'accedi' ? 'Accedi' : 'Crea account' }}</h1></header>

    <div class="card">
      <div class="segmenti" role="group" aria-label="Accedi o registrati" style="margin-bottom: 14px">
        <button type="button" :aria-pressed="modo === 'accedi'" @click="modo = 'accedi'">Accedi</button>
        <button type="button" :aria-pressed="modo === 'registrati'" @click="modo = 'registrati'">Registrati</button>
      </div>

      <p v-if="modo === 'registrati' && primoUtente" class="dim spiegazione">
        È il primo account: erediterà le schede, gli esercizi e lo storico già presenti.
      </p>

      <form @submit.prevent="invia">
        <label class="campo">
          <span>Username</span>
          <input v-model="username" autocomplete="username" autocapitalize="none" autocorrect="off" spellcheck="false">
        </label>
        <label class="campo">
          <span>Password</span>
          <input v-model="password" type="password" :autocomplete="modo === 'accedi' ? 'current-password' : 'new-password'">
        </label>
        <p v-if="modo === 'registrati'" class="dim spiegazione">Almeno 8 caratteri.</p>
        <label v-if="modo === 'registrati' && !primoUtente" class="campo">
          <span>Codice di invito</span>
          <input v-model="codice" autocomplete="off" autocapitalize="none" spellcheck="false">
        </label>
        <button type="submit" class="btn btn-primario btn-blocco" :disabled="occupato">
          {{ modo === 'accedi' ? 'Accedi' : 'Crea account' }}
        </button>
      </form>
    </div>
  </section>
</template>
