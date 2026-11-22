<script setup>
import { ref, reactive, computed, onActivated } from 'vue'
import { passoKg, accento, PASSI_PESO, ACCENTI, storicoSoloMinimo } from '../composables/useSettings'
import { provaSuono, allarmeAttivo, notificaAttiva, chiediNotifiche, durata, imposta, durataUnilaterale, impostaUnilaterale, PASSO } from '../composables/useRestTimer'
import { nomeUtente, esci } from '../composables/useAuth'
import { mmss, ls } from '../utils'
import { api } from '../api'
import { toast, errore, conferma } from '../composables/useUi'
import Modal from '../components/Modal.vue'

import { attivaPush, disattivaPush, pushAttivo, serveInstallazione, errorepush } from '../composables/usePush'

const formatta = p => String(p).replace('.', ',')

// Email dell'account (serve per recuperare la password)
const emailAccount = ref(null)
const emailCaricata = ref(false)
async function caricaAccount() {
  try { emailAccount.value = (await api('/auth/io')).email ?? null } catch { /* non bloccante */ } finally { emailCaricata.value = true }
}
onActivated(caricaAccount)

// Diagnostica: dice cosa non va, oppure invia una notifica di prova tra 10 secondi (da provare a schermo bloccato)
async function provaPush() {
  try {
    const r = await api('/push/prova', 'POST')
    if (!r.configurato) toast('Il server non ha le chiavi push configurate.', 'errore', 8000)
    else if (!r.dispositivi) toast('Nessun dispositivo registrato: spegni e riaccendi la notifica di sistema.', 'errore', 8000)
    else toast('Tutto ok: blocca lo schermo, la notifica arriva tra 10 secondi.', 'info', 8000)
  } catch (e) { errore(e) }
}

// Attiva/disattiva la notifica di fine recupero anche a schermo spento (push dal server)
async function cambiaNotifica(attiva) {
  if (!attiva) return disattivaPush()
  if (!await attivaPush()) toast(errorepush.value || 'Notifiche non attivate.', 'errore', 12000)
}

// Scarica schede, esercizi e storico in un unico file JSON
async function scaricaBackup() {
  try {
    const dati = await api('/export')
    const blob = new Blob([JSON.stringify(dati, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `gymtracker-backup-${new Date().toISOString().slice(0, 10)}.json`
    document.body.appendChild(a)
    a.click()
    a.remove()
    setTimeout(() => URL.revokeObjectURL(url), 1000)
    toast('Backup scaricato.')
  } catch (e) { errore(e) }
}

// Importa un backup: SOSTITUISCE tutti i dati (esercizi, schede, storico) con quelli del file
async function importaBackup(ev) {
  const file = ev.target.files[0]
  ev.target.value = '' // permette di riscegliere lo stesso file
  if (!file) return
  let dati
  try { dati = JSON.parse(await file.text()) } catch { return errore(new Error('Il file non è un JSON valido.')) }
  const data = dati?.esportatoIl ? ` del ${String(dati.esportatoIl).slice(0, 10)}` : ''
  if (!await conferma(`Stai per SOSTITUIRE tutti i tuoi dati attuali (schede, esercizi, storico) con quelli del backup${data}. Quello che hai fatto dopo quella data andrà perso. L'operazione non è reversibile: se vuoi tenere i dati attuali, scarica prima un backup. Procedere?`, { etichetta: 'Sostituisci tutto' })) return
  try {
    const r = await api('/import', 'POST', dati)
    // Si ripulisce lo stato locale legato ai vecchi dati (serie extra, scheda scelta) e si ricarica l'app
    ls.set('gt_extra', null)
    ls.set('gt_scheda', '')
    toast(`Ripristinati: ${r.schede} schede, ${r.esercizi} esercizi, ${r.allenamenti} allenamenti.`, 'info', 6000)
    setTimeout(() => location.reload(), 1800)
  } catch (e) { errore(e) }
}

// Azioni protette dalla password: cambio email ed eliminazione account
const pw = reactive({ aperto: false, tipo: '', password: '', email: '', occupato: false })
const titoloPw = computed(() => (pw.tipo === 'elimina' ? 'Elimina account' : 'Email di recupero'))

function apriPw(tipo) {
  Object.assign(pw, { aperto: true, tipo, password: '', email: tipo === 'email' ? (emailAccount.value ?? '') : '', occupato: false })
}
const chiudiPw = () => Object.assign(pw, { aperto: false, password: '' })

async function procediPw() {
  if (pw.tipo === 'email' && !pw.email.trim()) return errore(new Error("Inserisci l'email."))
  if (!pw.password) return errore(new Error('Inserisci la password.'))
  pw.occupato = true
  try {
    if (pw.tipo === 'email') {
      await api('/auth/email', 'POST', { email: pw.email, password: pw.password })
      emailAccount.value = pw.email.trim().toLowerCase()
      chiudiPw()
      toast('Email salvata.')
    } else {
      await api('/auth/account/elimina', 'POST', { password: pw.password })
      esci() // pulisce il dispositivo e torna al login
    }
  } catch (e) { errore(e) } finally { pw.occupato = false }
}
</script>

<template>
  <section>
    <header class="titolo-pagina"><h1>Impostazioni</h1></header>

    <div class="card">
      <h3>Account</h3>
      <p class="dim spiegazione">Sei collegato come <strong>{{ nomeUtente }}</strong>.</p>
      <p v-if="emailCaricata && emailAccount" class="dim spiegazione">Email di recupero: <strong>{{ emailAccount }}</strong></p>
      <p v-else-if="emailCaricata" class="spiegazione">Non hai ancora un'email: aggiungila per poter recuperare la password se la dimentichi.</p>
      <div class="riga-azioni">
        <button class="btn" @click="apriPw('email')">{{ emailAccount ? 'Cambia email' : 'Aggiungi email' }}</button>
        <button class="btn btn-pericolo" @click="esci">Esci</button>
      </div>
    </div>

    <div class="card">
      <h3>Passo del peso</h3>
      <p class="dim spiegazione">Di quanto cambiano i pulsanti − e + del peso durante l'allenamento.</p>
      <div class="segmenti" role="group" aria-label="Passo del peso">
        <button v-for="p in PASSI_PESO" :key="p" :aria-pressed="passoKg === p" @click="passoKg = p">{{ formatta(p) }} kg</button>
      </div>
    </div>

    <div class="card">
      <h3>Colore dell'app</h3>
      <p class="dim spiegazione">Il colore usato per pulsanti, timer e serie completate.</p>
      <div class="swatches" role="group" aria-label="Colore dell'app">
        <button v-for="(a, chiave) in ACCENTI" :key="chiave" class="swatch" :aria-pressed="accento === chiave"
                :aria-label="a.nome" :title="a.nome" :style="{ background: a.c }" @click="accento = chiave"></button>
      </div>
    </div>

    <div class="card">
      <h3>Recupero predefinito</h3>
      <p class="dim spiegazione">Recupero dopo ogni serie degli esercizi normali. Durante l'allenamento si può ritoccare dal timer.</p>
      <div class="recupero-riga" style="grid-template-columns: auto 1fr auto">
        <button class="btn btn-sm" @click="imposta(durata - PASSO)" aria-label="Riduci recupero di 15 secondi">−{{ PASSO }}s</button>
        <div class="recupero-tempo"><small>Recupero</small><strong class="num">{{ mmss(durata) }}</strong></div>
        <button class="btn btn-sm" @click="imposta(durata + PASSO)" aria-label="Aumenta recupero di 15 secondi">+{{ PASSO }}s</button>
      </div>
    </div>

    <div class="card">
      <h3>Esercizi unilaterali</h3>
      <p class="dim spiegazione">Recupero predefinito dopo ogni sottoserie (SX o DX).</p>
      <div class="recupero-riga" style="grid-template-columns: auto 1fr auto; margin-bottom: 16px">
        <button class="btn btn-sm" @click="impostaUnilaterale(durataUnilaterale - PASSO)" aria-label="Riduci recupero unilaterali di 15 secondi">−{{ PASSO }}s</button>
        <div class="recupero-tempo"><small>Recupero unilaterali</small><strong class="num">{{ mmss(durataUnilaterale) }}</strong></div>
        <button class="btn btn-sm" @click="impostaUnilaterale(durataUnilaterale + PASSO)" aria-label="Aumenta recupero unilaterali di 15 secondi">+{{ PASSO }}s</button>
      </div>
      <label class="interruttore campo"><input type="checkbox" v-model="storicoSoloMinimo"><span>Nello storico mostra solo il valore minore</span></label>
      <p class="dim spiegazione">Attiva: per ogni serie compare solo il lato con meno ripetizioni. Disattiva: compaiono entrambi i lati (SX e DX).</p>
    </div>

    <div class="card">
      <h3>Avviso di fine recupero</h3>
      <p class="dim spiegazione">Suono, popup a centro schermo e, dove supportata, vibrazione, ripetuti ogni 4 secondi finché non tocchi lo schermo. Prova l'avviso e regola il volume del telefono.</p>
      <label class="interruttore campo"><input type="checkbox" v-model="allarmeAttivo"><span>Allarme sonoro in app</span></label>
      <label class="interruttore campo"><input type="checkbox" v-model="notificaAttiva" @change="cambiaNotifica($event.target.checked)"><span>Notifica di sistema (anche a schermo spento)</span></label>
      <p v-if="serveInstallazione" class="dim spiegazione">Su iPhone/iPad, per ricevere l'avviso a schermo spento: Condividi → Aggiungi alla schermata Home, apri l'app da lì e riattiva qui la notifica.</p>
      <p v-else-if="notificaAttiva && pushAttivo" class="dim spiegazione">Attiva anche a schermo spento su questo dispositivo.</p>
      <div class="riga-azioni">
        <button class="btn btn-primario" @click="provaSuono">Prova avviso</button>
        <button class="btn" @click="provaPush">Prova notifica a schermo spento</button>
      </div>
    </div>

    <div class="card">
      <h3>Backup dei dati</h3>
      <p class="dim spiegazione">Scarica un file con le tue schede, i tuoi esercizi e tutto il tuo storico. Importando un backup, tutti i dati attuali vengono sostituiti con quelli del file.</p>
      <div class="riga-azioni">
        <button class="btn btn-primario" @click="scaricaBackup">Scarica backup</button>
        <label class="btn btn-pericolo" style="cursor: pointer">
          Importa backup
          <input type="file" accept=".json,application/json" hidden @change="importaBackup">
        </label>
      </div>
    </div>

    <div class="card">
      <h3>Elimina account</h3>
      <p class="dim spiegazione">Cancella per sempre il tuo account e tutti i tuoi dati: schede, esercizi e storico. Scarica prima un backup se vuoi conservarli.</p>
      <button class="btn btn-pericolo" @click="apriPw('elimina')">Elimina il mio account</button>
    </div>

    <!-- Conferma con password: cambio email / eliminazione account -->
    <Modal :aperto="pw.aperto" :titolo="titoloPw" @chiudi="chiudiPw">
      <p v-if="pw.tipo === 'elimina'" class="testo-conferma" style="margin-bottom: 14px">
        Stai per eliminare definitivamente il tuo account e tutti i dati collegati. <strong>L'operazione non è reversibile.</strong> Inserisci la password per confermare.
      </p>
      <label v-if="pw.tipo === 'email'" class="campo">
        <span>Email</span>
        <input v-model="pw.email" type="email" autocomplete="email" autocapitalize="none" autocorrect="off" spellcheck="false">
      </label>
      <label class="campo">
        <span>Password</span>
        <input v-model="pw.password" type="password" autocomplete="current-password" @keyup.enter="procediPw">
      </label>
      <template #piede>
        <button class="btn btn-ghost" @click="chiudiPw">Annulla</button>
        <button class="btn" :class="pw.tipo === 'elimina' ? 'btn-pericolo-pieno' : 'btn-primario'" :disabled="pw.occupato" @click="procediPw">
          {{ pw.tipo === 'elimina' ? 'Elimina definitivamente' : 'Salva email' }}
        </button>
      </template>
    </Modal>
  </section>
</template>
