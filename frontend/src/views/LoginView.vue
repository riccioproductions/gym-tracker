<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'
import { errore, toast } from '../composables/useUi'
import { entra } from '../composables/useAuth'

const modo = ref('accedi') // 'accedi' | 'registrati' | 'recupera'
const passo = ref(1)       // recupero: 1 = richiesta del codice, 2 = codice + nuova password
const username = ref('')
const email = ref('')
const password = ref('')
const codice = ref('')     // codice di invito (registrazione) o codice ricevuto per email (recupero)
const primoUtente = ref(false)
const occupato = ref(false)

onMounted(async () => {
  try {
    primoUtente.value = (await api('/auth/stato')).primoUtente
    if (primoUtente.value) modo.value = 'registrati' // nessun account ancora: si parte dalla registrazione
  } catch (e) { errore(e) }
})

function vai(m) {
  modo.value = m
  passo.value = 1
  codice.value = ''
  password.value = ''
}

async function invia() {
  occupato.value = true
  try {
    if (modo.value === 'recupera') {
      if (!email.value.trim()) throw new Error('Inserisci la tua email.')
      if (passo.value === 1) {
        await api('/auth/recupera/richiedi', 'POST', { email: email.value })
        toast("Se l'email è registrata, ti abbiamo inviato un codice (controlla anche lo spam). Vale 15 minuti.", 'info', 9000)
        passo.value = 2
        occupato.value = false
        return
      }
      if (!codice.value.trim() || !password.value) throw new Error('Inserisci il codice ricevuto e la nuova password.')
      const r = await api('/auth/recupera/conferma', 'POST', { email: email.value, codice: codice.value, nuovaPassword: password.value })
      entra(r.token, r.username) // già collegato: ricarica la pagina
      return
    }

    if (!username.value.trim() || !password.value) throw new Error('Inserisci username e password.')
    if (modo.value === 'registrati' && !email.value.trim()) throw new Error("Inserisci l'email: serve per recuperare la password.")
    const r = await api(modo.value === 'accedi' ? '/auth/accedi' : '/auth/registrati', 'POST', {
      username: username.value, password: password.value, codiceInvito: codice.value, email: email.value
    })
    entra(r.token, r.username)
  } catch (e) {
    errore(e)
    occupato.value = false
  }
}
</script>

<template>
  <section>
    <header class="titolo-pagina">
      <h1>{{ modo === 'accedi' ? 'Accedi' : modo === 'registrati' ? 'Crea account' : 'Password dimenticata' }}</h1>
    </header>

    <div class="card">
      <div v-if="modo !== 'recupera'" class="segmenti" role="group" aria-label="Accedi o registrati" style="margin-bottom: 14px">
        <button type="button" :aria-pressed="modo === 'accedi'" @click="vai('accedi')">Accedi</button>
        <button type="button" :aria-pressed="modo === 'registrati'" @click="vai('registrati')">Registrati</button>
      </div>

      <p v-if="modo === 'registrati' && primoUtente" class="dim spiegazione">
        È il primo account: erediterà le schede, gli esercizi e lo storico già presenti.
      </p>
      <p v-if="modo === 'recupera'" class="dim spiegazione">
        {{ passo === 1 ? "Inserisci l'email del tuo account: ti inviamo un codice per scegliere una nuova password."
                       : 'Inserisci il codice ricevuto per email e scegli la nuova password.' }}
      </p>

      <form @submit.prevent="invia">
        <label v-if="modo !== 'recupera'" class="campo">
          <span>Username</span>
          <input v-model="username" autocomplete="username" autocapitalize="none" autocorrect="off" spellcheck="false">
        </label>
        <label v-if="modo !== 'accedi'" class="campo">
          <span>Email</span>
          <input v-model="email" type="email" autocomplete="email" autocapitalize="none" autocorrect="off" spellcheck="false" :readonly="modo === 'recupera' && passo === 2">
        </label>
        <label v-if="modo === 'recupera' && passo === 2" class="campo">
          <span>Codice ricevuto per email</span>
          <input v-model="codice" autocomplete="one-time-code" autocapitalize="characters" autocorrect="off" spellcheck="false" placeholder="XXXX-XXXX">
        </label>
        <label v-if="modo !== 'recupera' || passo === 2" class="campo">
          <span>{{ modo === 'recupera' ? 'Nuova password' : 'Password' }}</span>
          <input v-model="password" type="password" :autocomplete="modo === 'accedi' ? 'current-password' : 'new-password'">
        </label>
        <p v-if="modo === 'registrati' || (modo === 'recupera' && passo === 2)" class="dim spiegazione">Password di almeno 8 caratteri.</p>
        <label v-if="modo === 'registrati' && !primoUtente" class="campo">
          <span>Codice di invito</span>
          <input v-model="codice" autocomplete="off" autocapitalize="none" spellcheck="false">
        </label>
        <button type="submit" class="btn btn-primario btn-blocco" :disabled="occupato">
          {{ modo === 'accedi' ? 'Accedi' : modo === 'registrati' ? 'Crea account' : passo === 1 ? 'Invia codice' : 'Reimposta password' }}
        </button>
      </form>

      <button v-if="modo === 'accedi'" type="button" class="btn btn-ghost btn-blocco" style="margin-top: 10px" @click="vai('recupera')">Password dimenticata?</button>
      <button v-if="modo === 'recupera'" type="button" class="btn btn-ghost btn-blocco" style="margin-top: 10px" @click="vai('accedi')">Torna all'accesso</button>
    </div>
  </section>
</template>
