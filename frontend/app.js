//URL base del backend Spring Boot
const API_BASE = `http://${window.location.hostname}:8080/api`;

//Cache in memoria per evitare chiamate di rete ridondanti (Esercizi)
let eserciziCorrenti = [];

// ------- Navigazione tra sezioni -------
function mostraSezione(nomeSezione) {
    document.querySelectorAll('.app-section').forEach(sezione => {
        sezione.style.display = 'none';
    });

    const target = document.getElementById(`section-${nomeSezione}`);
    if (target) {
        target.style.display = 'block';
    }

    document.querySelectorAll('.nav-link').forEach(link => link.classList.remove('active'));
    document.querySelectorAll(`[data-nav="${nomeSezione}"]`).forEach(link => link.classList.add('active'));

    //Carica i dati specifici della sezione
    if (nomeSezione === 'esercizi') caricaEsercizi();
	if (nomeSezione === 'schede') caricaSchede();
	if (nomeSezione === 'allenamento') inizializzaSezioneAllenamento();
	if (nomeSezione === 'storico') inizializzaSezioneStorico();
}

document.querySelectorAll('[data-nav]').forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();
        const sezione = link.getAttribute('data-nav');
        mostraSezione(sezione);
    });
});

// ------- Helper generico per chiamate al backend -------
async function apiCall(endpoint, metodo = 'GET', corpo = null) {
    const opzioni = {
        method: metodo,
        headers: { 'Content-Type': 'application/json' }
    };
    if (corpo) {
        opzioni.body = JSON.stringify(corpo);
    }

    const risposta = await fetch(`${API_BASE}${endpoint}`, opzioni);

    if (!risposta.ok) {
        throw new Error(`Errore ${risposta.status} su ${endpoint}`);
    }
    const testo = await risposta.text();
    return testo ? JSON.parse(testo) : null;
}

function mostraErrore(messaggio) {
    const container = document.getElementById('alertContainer');
    container.innerHTML = `
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${messaggio}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
}

// ------- CRUD Esercizi -------
let modalEsercizio;

async function caricaEsercizi() {
    try {
        const esercizi = await apiCall('/esercizi');
        const tbody = document.getElementById('tabellaEsercizi');
        tbody.innerHTML = '';

        esercizi.forEach(es => {
            const riga = document.createElement('tr');
            riga.innerHTML = `
                <td>${es.nome}</td>
                <td>${es.gruppoMuscolare || '-'}</td>
                <td>
                    <button class="btn btn-sm btn-outline-secondary" onclick="apriFormEsercizio(${es.id})">Modifica</button>
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminaEsercizio(${es.id})">Elimina</button>
                </td>
            `;
            tbody.appendChild(riga);
        });
    } catch (errore) {
        mostraErrore('Errore nel caricamento degli esercizi: ' + errore.message);
    }
}

async function apriFormEsercizio(id = null) {
    document.getElementById('esercizioId').value = '';
    document.getElementById('esercizioNome').value = '';
    document.getElementById('esercizioGruppo').value = '';
    document.getElementById('modalEsercizioTitolo').textContent = id ? 'Modifica Esercizio' : 'Nuovo Esercizio';

    if (id) {
        const esercizi = await apiCall('/esercizi');
        const es = esercizi.find(e => e.id === id);
        if (es) {
            document.getElementById('esercizioId').value = es.id;
            document.getElementById('esercizioNome').value = es.nome;
            document.getElementById('esercizioGruppo').value = es.gruppoMuscolare || '';
        }
    }

    modalEsercizio.show();
}

async function salvaEsercizio() {
    const id = document.getElementById('esercizioId').value;
    const nome = document.getElementById('esercizioNome').value.trim();
    const gruppoMuscolare = document.getElementById('esercizioGruppo').value.trim();

    if (!nome) {
        mostraErrore('Il nome dell\'esercizio è obbligatorio.');
        return;
    }

    const dato = { nome, gruppoMuscolare };

    try {
        if (id) {
            await apiCall(`/esercizi/${id}`, 'PUT', dato);
        } else {
            await apiCall('/esercizi', 'POST', dato);
        }
        modalEsercizio.hide();
        caricaEsercizi();
    } catch (errore) {
        mostraErrore('Errore nel salvataggio: ' + errore.message);
    }
}

async function eliminaEsercizio(id) {
    if (!confirm('Confermi l\'eliminazione di questo esercizio?')) return;

    try {
        await apiCall(`/esercizi/${id}`, 'DELETE');
        caricaEsercizi();
    } catch (errore) {
        mostraErrore('Errore nell\'eliminazione: ' + errore.message);
    }
}

// ------- CRUD Schede + Giorni -------
let modalScheda, modalGiorno;
let cacheSchede = []; 

async function caricaSchede() {
    try {
        cacheSchede = await apiCall('/schede');
        const container = document.getElementById('listaSchede');
        container.innerHTML = '';

        for (const scheda of cacheSchede) {
            const giorni = await apiCall(`/schede/${scheda.id}/giorni`);

            const card = document.createElement('div');
            card.className = 'card mb-3';
            card.innerHTML = `
                <div class="card-header d-flex justify-content-between align-items-center">
                    <div>
                        <strong>${scheda.nome}</strong>
                        <div class="text-muted small">${scheda.descrizione || ''}</div>
                    </div>
                    <div>
                        <button class="btn btn-sm btn-outline-secondary" onclick="apriFormScheda(${scheda.id})">Modifica</button>
                        <button class="btn btn-sm btn-outline-danger" onclick="eliminaScheda(${scheda.id})">Elimina</button>
                    </div>
                </div>
                <div class="card-body">
                    <h6>Giorni</h6>
                    <ul class="list-group mb-2">
                        ${giorni.map(g => `
                            <li class="list-group-item d-flex justify-content-between align-items-center">
                                ${g.nome}
                                <span>
                                    <button class="btn btn-sm btn-outline-primary" onclick="apriEserciziGiorno(${g.id}, '${g.nome.replace(/'/g, "\\'")}')">Esercizi</button>
                                    <button class="btn btn-sm btn-outline-secondary" onclick="apriFormGiorno(${scheda.id}, ${g.id})">Modifica</button>
                                    <button class="btn btn-sm btn-outline-danger" onclick="eliminaGiorno(${g.id}, ${scheda.id})">Elimina</button>
                                </span>
                            </li>
                        `).join('')}
                    </ul>
                    <button class="btn btn-sm btn-primary" onclick="apriFormGiorno(${scheda.id})">+ Aggiungi Giorno</button>
                </div>
            `;
            container.appendChild(card);
        }
    } catch (errore) {
        mostraErrore('Errore nel caricamento delle schede: ' + errore.message);
    }
}

// --- Form Scheda ---
function apriFormScheda(id = null) {
    document.getElementById('schedaId').value = '';
    document.getElementById('schedaNome').value = '';
    document.getElementById('schedaDescrizione').value = '';
    document.getElementById('modalSchedaTitolo').textContent = id ? 'Modifica Scheda' : 'Nuova Scheda';

    if (id) {
        const scheda = cacheSchede.find(s => s.id === id);
        if (scheda) {
            document.getElementById('schedaId').value = scheda.id;
            document.getElementById('schedaNome').value = scheda.nome;
            document.getElementById('schedaDescrizione').value = scheda.descrizione || '';
        }
    }

    modalScheda.show();
}

async function salvaScheda() {
    const id = document.getElementById('schedaId').value;
    const nome = document.getElementById('schedaNome').value.trim();
    const descrizione = document.getElementById('schedaDescrizione').value.trim();

    if (!nome) {
        mostraErrore('Il nome della scheda è obbligatorio.');
        return;
    }

    const dato = { nome, descrizione };

    try {
        if (id) {
            await apiCall(`/schede/${id}`, 'PUT', dato);
        } else {
            await apiCall('/schede', 'POST', dato);
        }
        modalScheda.hide();
        caricaSchede();
    } catch (errore) {
        mostraErrore('Errore nel salvataggio della scheda: ' + errore.message);
    }
}

async function eliminaScheda(id) {
    if (!confirm('Confermi l\'eliminazione della scheda? Verranno eliminati anche tutti i giorni collegati.')) return;

    try {
        await apiCall(`/schede/${id}`, 'DELETE');
        caricaSchede();
    } catch (errore) {
        mostraErrore('Errore nell\'eliminazione della scheda: ' + errore.message);
    }
}

// --- Form Giorno ---
async function apriFormGiorno(schedaId, giornoId = null) {
    document.getElementById('giornoId').value = '';
    document.getElementById('giornoSchedaId').value = schedaId;
    document.getElementById('giornoNome').value = '';
    document.getElementById('giornoOrdine').value = 1;
    document.getElementById('modalGiornoTitolo').textContent = giornoId ? 'Modifica Giorno' : 'Nuovo Giorno';

    if (giornoId) {
        const giorni = await apiCall(`/schede/${schedaId}/giorni`);
        const giorno = giorni.find(g => g.id === giornoId);
        if (giorno) {
            document.getElementById('giornoId').value = giorno.id;
            document.getElementById('giornoNome').value = giorno.nome;
            document.getElementById('giornoOrdine').value = giorno.ordine || 1;
        }
    }

    modalGiorno.show();
}

async function salvaGiorno() {
    const id = document.getElementById('giornoId').value;
    const schedaId = document.getElementById('giornoSchedaId').value;
    const nome = document.getElementById('giornoNome').value.trim();
    const ordine = parseInt(document.getElementById('giornoOrdine').value) || 1;

    if (!nome) {
        mostraErrore('Il nome del giorno è obbligatorio.');
        return;
    }

    const dato = { nome, ordine };

    try {
        if (id) {
            await apiCall(`/schede/${schedaId}/giorni/${id}`, 'PUT', dato);
        } else {
            await apiCall(`/schede/${schedaId}/giorni`, 'POST', dato);
        }
        modalGiorno.hide();
        caricaSchede();
    } catch (errore) {
        mostraErrore('Errore nel salvataggio del giorno: ' + errore.message);
    }
}

async function eliminaGiorno(giornoId, schedaId) {
    if (!confirm('Confermi l\'eliminazione del giorno?')) return;

    try {
        await apiCall(`/schede/${schedaId}/giorni/${giornoId}`, 'DELETE');
        caricaSchede();
    } catch (errore) {
        mostraErrore('Errore nell\'eliminazione del giorno: ' + errore.message);
    }
}

// ------- Esercizi dentro un Giorno -------
let modalEserciziGiorno;
let cacheEserciziCatalogo = [];
let egGiornoIdCorrente = null;

async function apriEserciziGiorno(giornoId, nomeGiorno) {
    egGiornoIdCorrente = giornoId;
    document.getElementById('egGiornoId').value = giornoId;
    document.getElementById('modalEserciziGiornoTitolo').textContent = `Esercizi - ${nomeGiorno}`;

    cacheEserciziCatalogo = await apiCall('/esercizi');
    const select = document.getElementById('egEsercizioId');
    select.innerHTML = '<option value="">Seleziona esercizio...</option>' +
        cacheEserciziCatalogo.map(e => `<option value="${e.id}">${e.nome}</option>`).join('');

    resettaFormEsercizioGiorno();
    await caricaEserciziGiorno(giornoId);

    modalEserciziGiorno.show();
}

async function caricaEserciziGiorno(giornoId) {
    const lista = await apiCall(`/giorni/${giornoId}/esercizi`);
    const tbody = document.getElementById('tabellaEserciziGiorno');
    tbody.innerHTML = '';

    lista.forEach(eg => {
        const riga = document.createElement('tr');
        riga.innerHTML = `
            <td>${eg.esercizio.nome}</td>
            <td>${eg.serie ?? '-'}</td>
            <td>${eg.ripetizioniTarget ?? '-'}</td>
            <td>${eg.carico ?? '-'}</td>
            <td>${eg.note ?? ''}</td>
            <td>
                <button class="btn btn-sm btn-outline-secondary" onclick="modificaEsercizioGiorno(${eg.id})">Mod.</button>
                <button class="btn btn-sm btn-outline-danger" onclick="eliminaEsercizioGiorno(${eg.id})">Elim.</button>
            </td>
        `;
        tbody.appendChild(riga);
    });
    window._cacheEserciziGiornoCorrente = lista;
}

function resettaFormEsercizioGiorno() {
    document.getElementById('egId').value = '';
    document.getElementById('egEsercizioId').value = '';
    document.getElementById('egSerie').value = '';
    document.getElementById('egRipetizioni').value = '';
    document.getElementById('egCarico').value = '';
    document.getElementById('egNote').value = '';
    document.getElementById('egFormTitolo').textContent = 'Aggiungi Esercizio';
}

function modificaEsercizioGiorno(id) {
    const eg = window._cacheEserciziGiornoCorrente.find(e => e.id === id);
    if (!eg) return;

    document.getElementById('egId').value = eg.id;
    document.getElementById('egEsercizioId').value = eg.esercizio.id;
    document.getElementById('egSerie').value = eg.serie ?? '';
    document.getElementById('egRipetizioni').value = eg.ripetizioniTarget ?? '';
    document.getElementById('egCarico').value = eg.carico ?? '';
    document.getElementById('egNote').value = eg.note ?? '';
    document.getElementById('egFormTitolo').textContent = 'Modifica Esercizio';
}

async function salvaEsercizioGiorno() {
    const id = document.getElementById('egId').value;
    const giornoId = document.getElementById('egGiornoId').value;
    const esercizioId = document.getElementById('egEsercizioId').value;
    const serie = document.getElementById('egSerie').value;
    const ripetizioniTarget = document.getElementById('egRipetizioni').value;
    const carico = document.getElementById('egCarico').value;
    const note = document.getElementById('egNote').value.trim();

    if (!esercizioId) {
        mostraErrore('Seleziona un esercizio dal catalogo.');
        return;
    }

    const dato = {
        serie: serie ? parseInt(serie) : null,
        ripetizioniTarget: ripetizioniTarget ? parseInt(ripetizioniTarget) : null,
        carico: carico ? parseFloat(carico) : null,
        note: note || null
    };

    try {
        if (id) {
            await apiCall(`/giorni/${giornoId}/esercizi/${id}/${esercizioId}`, 'PUT', dato);
        } else {
            await apiCall(`/giorni/${giornoId}/esercizi/${esercizioId}`, 'POST', dato);
        }
        resettaFormEsercizioGiorno();
        await caricaEserciziGiorno(giornoId);
    } catch (errore) {
        mostraErrore('Errore nel salvataggio: ' + errore.message);
    }
}

async function eliminaEsercizioGiorno(id) {
    if (!confirm('Confermi l\'eliminazione di questo esercizio dal giorno?')) return;

    try {
        await apiCall(`/giorni/${egGiornoIdCorrente}/esercizi/${id}`, 'DELETE');
        await caricaEserciziGiorno(egGiornoIdCorrente);
    } catch (errore) {
        mostraErrore('Errore nell\'eliminazione: ' + errore.message);
    }
}

// ------- Sezione Allenamento -------

let allenamentoCorrente = null;
let intervalloCronometro = null;

async function inizializzaSezioneAllenamento() {
    const inCorso = await apiCall('/allenamenti/in-corso');

    if (inCorso) {
        allenamentoCorrente = inCorso;
        await mostraAllenamentoInCorso();
    } else {
        document.getElementById('allenamentoSelezione').style.display = 'block';
        document.getElementById('allenamentoInCorso').style.display = 'none';
        await popolaSelectSchedeAllenamento();
    }
}

async function popolaSelectSchedeAllenamento() {
    const schede = await apiCall('/schede');
    const select = document.getElementById('selezioneScheda');
    select.innerHTML = '<option value="">Seleziona scheda...</option>' +
        schede.map(s => `<option value="${s.id}">${s.nome}</option>`).join('');
    document.getElementById('selezioneGiorno').innerHTML = '<option value="">Seleziona giorno...</option>';
}

async function caricaGiorniPerAllenamento() {
    const schedaId = document.getElementById('selezioneScheda').value;
    const selectGiorno = document.getElementById('selezioneGiorno');
    selectGiorno.innerHTML = '<option value="">Seleziona giorno...</option>';

    if (!schedaId) return;

    const giorni = await apiCall(`/schede/${schedaId}/giorni`);
    selectGiorno.innerHTML += giorni.map(g => `<option value="${g.id}">${g.nome}</option>`).join('');
}

async function avviaAllenamento() {
    const giornoId = document.getElementById('selezioneGiorno').value;
    if (!giornoId) {
        mostraErrore('Seleziona prima una scheda e un giorno.');
        return;
    }

    try {
        allenamentoCorrente = await apiCall(`/allenamenti/avvia/${giornoId}`, 'POST');
        await mostraAllenamentoInCorso();
    } catch (errore) {
        mostraErrore('Errore nell\'avvio dell\'allenamento: ' + errore.message);
    }
}

async function mostraAllenamentoInCorso() {
    document.getElementById('allenamentoSelezione').style.display = 'none';
    document.getElementById('allenamentoInCorso').style.display = 'block';
    document.getElementById('allenamentoTitoloGiorno').textContent = allenamentoCorrente.giorno.nome;

    avviaCronometroTotale(allenamentoCorrente.dataInizio);
}

// ------- Sezione Storico -------

async function inizializzaSezioneStorico() {
    const schede = await apiCall('/schede');
    const select = document.getElementById('storicoSelezioneScheda');
    select.innerHTML = '<option value="">Seleziona scheda...</option>' +
        schede.map(s => `<option value="${s.id}">${s.nome}</option>`).join('');

    document.getElementById('storicoSelezioneGiorno').innerHTML = '<option value="">Seleziona giorno...</option>';
    document.getElementById('listaStorico').innerHTML = '';
}

async function caricaGiorniPerStorico() {
    const schedaId = document.getElementById('storicoSelezioneScheda').value;
    const selectGiorno = document.getElementById('storicoSelezioneGiorno');
    selectGiorno.innerHTML = '<option value="">Seleziona giorno...</option>';
    document.getElementById('listaStorico').innerHTML = '';

    if (!schedaId) return;

    const giorni = await apiCall(`/schede/${schedaId}/giorni`);
    selectGiorno.innerHTML += giorni.map(g => `<option value="${g.id}">${g.nome}</option>`).join('');
}

async function caricaStoricoGiorno() {
    const giornoId = document.getElementById('storicoSelezioneGiorno').value;
    const container = document.getElementById('listaStorico');
    container.innerHTML = '';

    if (!giornoId) return;

    const allenamenti = await apiCall(`/allenamenti/storico/giorno/${giornoId}`);

    if (allenamenti.length === 0) {
        container.innerHTML = '<p class="text-muted">Nessun allenamento registrato per questo giorno.</p>';
        return;
    }

    const allenamentiConSerie = await Promise.all(
        allenamenti.map(async a => ({
            allenamento: a,
            serie: await apiCall(`/allenamenti/${a.id}/serie`)
        }))
    );

    const riepiloghi = allenamentiConSerie.map(({ serie }) => calcolaRiepilogoPerEsercizio(serie));

    allenamentiConSerie.forEach(({ allenamento, serie }, indice) => {
        const riepilogoPrecedente = riepiloghi[indice + 1] || null;
        container.appendChild(creaCardStoricoAllenamento(allenamento, serie, riepilogoPrecedente));
    });
}

function calcolaRiepilogoPerEsercizio(serie) {
    const riepilogo = {}; 

    serie.forEach(s => {
        const id = s.esercizioGiorno.esercizio.id;
        const nome = s.esercizioGiorno.esercizio.nome;

        if (!riepilogo[id]) {
            riepilogo[id] = { nome, totaleRipetizioni: 0, caricoMassimo: 0 };
        }

        riepilogo[id].totaleRipetizioni += s.ripetizioniEffettuate || 0;
        riepilogo[id].caricoMassimo = Math.max(riepilogo[id].caricoMassimo, s.caricoEffettivo || 0);
    });

    return riepilogo;
}

function calcolaIconaTrend(attuale, precedente) {
    if (!precedente) return ''; 

    if (attuale.caricoMassimo > precedente.caricoMassimo) {
        return '<span class="trend-up" title="Carico aumentato">▲</span>';
    }
    if (attuale.caricoMassimo < precedente.caricoMassimo) {
        return '<span class="trend-down" title="Carico diminuito">▼</span>';
    }

    if (attuale.totaleRipetizioni > precedente.totaleRipetizioni) {
        return '<span class="trend-up" title="Ripetizioni aumentate">▲</span>';
    }
    if (attuale.totaleRipetizioni < precedente.totaleRipetizioni) {
        return '<span class="trend-down" title="Ripetizioni diminuite">▼</span>';
    }

    return '<span class="trend-equal" title="Invariato rispetto alla sessione precedente">=</span>';
}

function creaCardStoricoAllenamento(allenamento, serie, riepilogoPrecedente) {
    const card = document.createElement('div');
    card.className = 'card mb-3';

    const dataInizio = new Date(allenamento.dataInizio);
    const dataFine = allenamento.dataFine ? new Date(allenamento.dataFine) : null;

    let durataTesto = '-';
    if (dataFine) {
        const minuti = Math.round((dataFine - dataInizio) / 60000);
        durataTesto = `${minuti} min`;
    }

    const riepilogoAttuale = calcolaRiepilogoPerEsercizio(serie);

    const serieRaggruppate = {};
    serie.forEach(s => {
        const id = s.esercizioGiorno.esercizio.id;
        if (!serieRaggruppate[id]) serieRaggruppate[id] = [];
        serieRaggruppate[id].push(s);
    });

    let corpoEsercizi = '';
    for (const [esercizioId, serieDelEsercizio] of Object.entries(serieRaggruppate)) {
        const nomeEsercizio = serieDelEsercizio[0].esercizioGiorno.esercizio.nome;
        const dettaglioSerie = serieDelEsercizio
            .sort((a, b) => a.numeroSerie - b.numeroSerie)
            .map(s => `${s.ripetizioniEffettuate}×${s.caricoEffettivo ?? '-'}kg`)
            .join(', ');

        const icona = calcolaIconaTrend(
            riepilogoAttuale[esercizioId],
            riepilogoPrecedente ? riepilogoPrecedente[esercizioId] : null
        );

        corpoEsercizi += `<div><strong>${nomeEsercizio}:</strong> ${dettaglioSerie} ${icona}</div>`;
    }

    card.innerHTML = `
        <div class="card-header d-flex justify-content-between align-items-center">
            <span>${dataInizio.toLocaleDateString('it-IT')} — ${dataInizio.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })}</span>
            <span class="badge ${allenamento.stato === 'COMPLETATO' ? 'bg-success' : 'bg-warning'}">${allenamento.stato}</span>
            <span class="text-muted">Durata: ${durataTesto}</span>
            <button class="btn btn-sm btn-outline-danger" onclick="eliminaLogAllenamento(${allenamento.id})">Elimina</button>
        </div>
        <div class="card-body">
            ${corpoEsercizi || '<span class="text-muted">Nessuna serie registrata.</span>'}
        </div>
    `;

    return card;
}

async function eliminaLogAllenamento(allenamentoId) {
    if (!confirm('Confermi l\'eliminazione definitiva di questo log allenamento? L\'operazione non è reversibile.')) return;

    try {
        await apiCall(`/allenamenti/${allenamentoId}`, 'DELETE');
        await caricaStoricoGiorno();
    } catch (errore) {
        mostraErrore('Errore nell\'eliminazione del log: ' + errore.message);
    }
}

async function terminaAllenamento() {
    if (!confirm('Confermi di voler terminare l\'allenamento?')) return;

    try {
        await apiCall(`/allenamenti/${allenamentoCorrente.id}/termina`, 'PUT');
        clearInterval(intervalloCronometro);
        clearInterval(intervalloRecupero);
        document.getElementById('barraRecupero').style.display = 'none';
        allenamentoCorrente = null;
        document.getElementById('allenamentoSelezione').style.display = 'block';
        document.getElementById('allenamentoInCorso').style.display = 'none';
        await popolaSelectSchedeAllenamento();
    } catch (errore) {
        mostraErrore('Errore nella terminazione: ' + errore.message);
    }
}

// ------- Esercizi + Serie durante l'Allenamento -------

let esercizioGiornoAllenamento = [];
let serieEseguiteAllenamento = [];
let intervalloRecupero = null;
const DURATA_RECUPERO_SECONDI = 180;

async function mostraAllenamentoInCorso() {
    document.getElementById('allenamentoSelezione').style.display = 'none';
    document.getElementById('allenamentoInCorso').style.display = 'block';
    document.getElementById('allenamentoTitoloGiorno').textContent = allenamentoCorrente.giorno.nome;

    avviaCronometroTotale(allenamentoCorrente.dataInizio);

    if (Notification.permission === 'default') {
        Notification.requestPermission();
    }

    await caricaEserciziAllenamento();
    ripristinaTimerRecuperoSeAttivo();
}

async function caricaEserciziAllenamento() {
    esercizioGiornoAllenamento = await apiCall(`/giorni/${allenamentoCorrente.giorno.id}/esercizi`);
    serieEseguiteAllenamento = await apiCall(`/allenamenti/${allenamentoCorrente.id}/serie`);

    const container = document.getElementById('listaEserciziAllenamento');
    container.innerHTML = '';

    esercizioGiornoAllenamento.forEach(eg => {
        container.appendChild(creaCardEsercizioAllenamento(eg));
    });
}

function creaCardEsercizioAllenamento(eg) {
    const card = document.createElement('div');
    card.className = 'card mb-2';

    const numeroSerieTotali = eg.serie || 1;
    let righeSerie = '';

    for (let n = 1; n <= numeroSerieTotali; n++) {
        const eseguita = serieEseguiteAllenamento.find(
            s => s.esercizioGiorno.id === eg.id && s.numeroSerie === n
        );

        if (eseguita) {
            righeSerie += `
                <tr class="table-success">
                    <td>Serie ${n}</td>
                    <td colspan="2">✔ ${eseguita.ripetizioniEffettuate} rip. — ${eseguita.caricoEffettivo ?? '-'} kg</td>
                </tr>
            `;
        } else {
            righeSerie += `
                <tr>
                    <td>Serie ${n}</td>
                    <td>
                        <input type="number" class="form-control form-control-sm" style="width:80px"
                               id="rip_${eg.id}_${n}" placeholder="Rip." value="${eg.ripetizioniTarget ?? ''}">
                    </td>
                    <td>
                        <input type="number" class="form-control form-control-sm" style="width:80px" step="0.5"
                               id="car_${eg.id}_${n}" placeholder="Carico" value="${eg.carico ?? ''}">
                    </td>
                    <td>
                        <button class="btn btn-sm btn-success" onclick="spuntaSerie(${eg.id}, ${n})">✔ Fatta</button>
                    </td>
                </tr>
            `;
        }
    }

    card.innerHTML = `
        <div class="card-header">
            <strong>${eg.esercizio.nome}</strong>
            ${eg.note ? `<span class="text-muted small"> — ${eg.note}</span>` : ''}
        </div>
        <div class="card-body p-2">
            <table class="table table-sm mb-0">
                <tbody>${righeSerie}</tbody>
            </table>
        </div>
    `;

    return card;
}

async function spuntaSerie(esercizioGiornoId, numeroSerie) {
    const ripetizioni = document.getElementById(`rip_${esercizioGiornoId}_${numeroSerie}`).value;
    const carico = document.getElementById(`car_${esercizioGiornoId}_${numeroSerie}`).value;

    if (!ripetizioni) {
        mostraErrore('Inserisci le ripetizioni effettuate.');
        return;
    }

    try {
        await apiCall(`/allenamenti/${allenamentoCorrente.id}/serie`, 'POST', {
            esercizioGiornoId: esercizioGiornoId,
            numeroSerie: numeroSerie,
            ripetizioniEffettuate: parseInt(ripetizioni),
            caricoEffettivo: carico ? parseFloat(carico) : null
        });

        await caricaEserciziAllenamento();
        avviaTimerRecupero(DURATA_RECUPERO_SECONDI);
    } catch (errore) {
        mostraErrore('Errore nel salvataggio della serie: ' + errore.message);
    }
}

// ------- Cronometro totale (robusto a sospensioni del tab) -------

let dataInizioAllenamentoCorrente = null;

function avviaCronometroTotale(dataInizioIso) {
    if (intervalloCronometro) clearInterval(intervalloCronometro);
    dataInizioAllenamentoCorrente = new Date(dataInizioIso);
    aggiornaCronometroTotale();
    intervalloCronometro = setInterval(aggiornaCronometroTotale, 1000);
}

function aggiornaCronometroTotale() {
    if (!dataInizioAllenamentoCorrente) return;

    const differenzaMs = new Date() - dataInizioAllenamentoCorrente;
    const totaleSecondi = Math.max(0, Math.floor(differenzaMs / 1000));
    const ore = Math.floor(totaleSecondi / 3600);
    const minuti = Math.floor((totaleSecondi % 3600) / 60);
    const secondi = totaleSecondi % 60;

    const formatta = n => String(n).padStart(2, '0');
    document.getElementById('cronometroTotale').textContent =
        `${formatta(ore)}:${formatta(minuti)}:${formatta(secondi)}`;
}

// ------- Timer di recupero (basato su orario di fine assoluto, non su un contatore) -------

let tempoFineRecupero = null;

function avviaTimerRecupero(secondiTotali, tempoFineForzato = null) {
    if (intervalloRecupero) clearInterval(intervalloRecupero);

    tempoFineRecupero = tempoFineForzato || new Date(Date.now() + secondiTotali * 1000);
    document.getElementById('barraRecupero').style.display = 'flex';

    aggiornaDisplayRecuperoDaFine();
    intervalloRecupero = setInterval(aggiornaDisplayRecuperoDaFine, 1000);
}

function aggiornaDisplayRecuperoDaFine() {
    if (!tempoFineRecupero) return;

    const secondiRimanenti = Math.round((tempoFineRecupero - new Date()) / 1000);

    if (secondiRimanenti <= 0) {
        clearInterval(intervalloRecupero);
        tempoFineRecupero = null;
        document.getElementById('barraRecupero').style.display = 'none';
        suonaAllarme();
        mostraNotificaRecuperoFinito();
        return;
    }

    aggiornaDisplayRecupero(secondiRimanenti);
}

function aggiornaDisplayRecupero(secondi) {
    const minuti = Math.floor(secondi / 60);
    const sec = secondi % 60;
    document.getElementById('cronometroRecupero').textContent =
        `${String(minuti).padStart(2, '0')}:${String(sec).padStart(2, '0')}`;
}

function ripristinaTimerRecuperoSeAttivo() {
    if (serieEseguiteAllenamento.length === 0) return;

    const ultimaSerie = serieEseguiteAllenamento.reduce((piuRecente, s) => {
        return new Date(s.timestampCompletamento) > new Date(piuRecente.timestampCompletamento) ? s : piuRecente;
    });

    const tempoFine = new Date(new Date(ultimaSerie.timestampCompletamento).getTime() + DURATA_RECUPERO_SECONDI * 1000);

    if (tempoFine > new Date()) {
        avviaTimerRecupero(null, tempoFine);
    }
}

// ------- Ricalcolo immediato quando l'app torna in primo piano (fix iOS/mobile) -------

document.addEventListener('visibilitychange', () => {
    if (!document.hidden) {
        aggiornaCronometroTotale();
        aggiornaDisplayRecuperoDaFine();
    }
});

// ------- Avvio applicazione -------
document.addEventListener('DOMContentLoaded', () => {
    modalEsercizio = new bootstrap.Modal(document.getElementById('modalEsercizio'));
    modalScheda = new bootstrap.Modal(document.getElementById('modalScheda'));
    modalGiorno = new bootstrap.Modal(document.getElementById('modalGiorno'));
    modalEserciziGiorno = new bootstrap.Modal(document.getElementById('modalEserciziGiorno'));
    mostraSezione('allenamento');
});

function suonaAllarme() {
    try {
        const contesto = new (window.AudioContext || window.webkitAudioContext)();
        const oscillatore = contesto.createOscillator();
        const guadagno = contesto.createGain();

        oscillatore.connect(guadagno);
        guadagno.connect(contesto.destination);

        oscillatore.frequency.value = 880;
        oscillatore.type = 'sine';
        guadagno.gain.setValueAtTime(0.3, contesto.currentTime);

        oscillatore.start();
        oscillatore.stop(contesto.currentTime + 0.6);
    } catch (errore) {
        console.warn('Audio non disponibile:', errore);
    }
}

function mostraNotificaRecuperoFinito() {
    if (Notification.permission === 'granted') {
        new Notification('GymTracker', {
            body: 'Recupero terminato!'
        });
    }
    alert('⏰ Recupero terminato!');
}