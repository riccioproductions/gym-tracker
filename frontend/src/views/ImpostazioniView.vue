<script setup>
import { passoKg, accento, PASSI_PESO, ACCENTI, storicoSoloMinimo } from '../composables/useSettings'
import { provaSuono, allarmeAttivo, notificaAttiva, chiediNotifiche, durata, imposta, durataUnilaterale, impostaUnilaterale, PASSO } from '../composables/useRestTimer'
import { nomeUtente, esci } from '../composables/useAuth'
import { mmss } from '../utils'
import { api } from '../api'
import { toast, errore } from '../composables/useUi'

import { attivaPush, disattivaPush, pushAttivo, serveInstallazione, errorepush } from '../composables/usePush'

const formatta = p => String(p).replace('.', ',')

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
</script>

<template>
  <section>
    <header class="titolo-pagina"><h1>Impostazioni</h1></header>

    <div class="card">
      <h3>Account</h3>
      <p class="dim spiegazione">Sei collegato come <strong>{{ nomeUtente }}</strong>.</p>
      <button class="btn btn-pericolo" @click="esci">Esci</button>
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
      <p class="dim spiegazione">Scarica un file con le tue schede, i tuoi esercizi e tutto il tuo storico degli allenamenti.</p>
      <button class="btn btn-primario" @click="scaricaBackup">Scarica backup</button>
    </div>
  </section>
</template>
