package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.*;
import com.riccioproductions.gymtracker.repository.AllenamentoRepository;
import com.riccioproductions.gymtracker.repository.EsercizioRepository;
import com.riccioproductions.gymtracker.repository.SchedaRepository;
import com.riccioproductions.gymtracker.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Importa il file prodotto da /api/export nell'account dell'utente collegato.
// SOSTITUISCE tutti i dati dell'utente (esercizi, schede, storico) con quelli del file: niente doppioni,
// e un backup più vecchio cancella anche quanto è stato fatto dopo.
// Tutto in un'unica transazione: se il file è danneggiato o qualcosa va storto, i dati attuali restano intatti.
@Service
@SuppressWarnings("unchecked")
public class ImportService {

    private final UtenteRepository utenti;
    private final EsercizioRepository esercizi;
    private final SchedaRepository schede;
    private final AllenamentoRepository allenamenti;
    private final AccountService account;

    public ImportService(UtenteRepository utenti, EsercizioRepository esercizi,
                         SchedaRepository schede, AllenamentoRepository allenamenti, AccountService account) {
        this.account = account;
        this.utenti = utenti;
        this.esercizi = esercizi;
        this.schede = schede;
        this.allenamenti = allenamenti;
    }

    @Transactional
    public Map<String, Integer> importa(Long utenteId, Map<String, Object> radice) {
        if (radice == null || !Integer.valueOf(1).equals(intero(radice, "versione"))) {
            throw new RuntimeException("File non valido: non è un backup di GymTracker.");
        }
        // Il file deve avere le tre sezioni, altrimenti si rischierebbe di svuotare l'account per niente
        if (!(radice.get("esercizi") instanceof List) || !(radice.get("schede") instanceof List)
                || !(radice.get("allenamenti") instanceof List)) {
            throw new RuntimeException("File non valido: mancano esercizi, schede o allenamenti.");
        }
        Utente utente = utenti.getReferenceById(utenteId);

        account.svuotaDati(utenteId); // sostituzione totale: via tutto, poi si ricostruisce dal file

        Map<String, Esercizio> catalogo = new HashMap<>(); // per nome, senza distinguere maiuscole
        int esNuovi = 0;

        // Esercizi del backup
        for (Object o : lista(radice.get("esercizi"))) {
            if (trovaOCrea(obj(o), utente, catalogo)) esNuovi++;
        }

        // Schede -> giorni -> esercizi del giorno. Le mappe traducono i vecchi id nei nuovi oggetti.
        Map<Long, Giorno> giorni = new HashMap<>();
        Map<Long, EsercizioGiorno> eserciziGiorno = new HashMap<>();
        int nSchede = 0;

        for (Object so : lista(radice.get("schede"))) {
            Map<String, Object> sm = obj(so);
            String nomeScheda = txt(sm, "nome");
            if (nomeScheda == null) continue;

            Scheda scheda = new Scheda();
            scheda.setNome(nomeScheda);
            scheda.setDescrizione(txt(sm, "descrizione"));
            LocalDate creata = dataSola(sm, "dataCreazione");
            if (creata != null) scheda.setDataCreazione(creata);
            scheda.setUtente(utente);
            scheda.setGiorni(new ArrayList<>());

            for (Object go : lista(sm.get("giorni"))) {
                Map<String, Object> gm = obj(go);
                String nomeGiorno = txt(gm, "nome");
                if (nomeGiorno == null) continue;

                Giorno giorno = new Giorno();
                giorno.setNome(nomeGiorno);
                giorno.setOrdine(intero(gm, "ordine"));
                giorno.setScheda(scheda);
                giorno.setEsercizi(new ArrayList<>());

                for (Object eo : lista(gm.get("esercizi"))) {
                    Map<String, Object> em = obj(eo);
                    Map<String, Object> esMap = obj(em.get("esercizio"));
                    if (txt(esMap, "nome") == null) continue;
                    if (trovaOCrea(esMap, utente, catalogo)) esNuovi++;

                    EsercizioGiorno eg = new EsercizioGiorno();
                    eg.setGiorno(giorno);
                    eg.setEsercizio(catalogo.get(chiave(txt(esMap, "nome"))));
                    eg.setSerie(intero(em, "serie"));
                    eg.setRipetizioniTarget(intero(em, "ripetizioniTarget"));
                    eg.setCarico(reale(em, "carico"));
                    eg.setNote(txt(em, "note"));
                    eg.setOrdine(intero(em, "ordine"));
                    eg.setUnilaterale(Boolean.TRUE.equals(em.get("unilaterale")));
                    giorno.getEsercizi().add(eg);

                    Long vecchioEg = id(em);
                    if (vecchioEg != null) eserciziGiorno.put(vecchioEg, eg);
                }
                scheda.getGiorni().add(giorno);
                Long vecchioGiorno = id(gm);
                if (vecchioGiorno != null) giorni.put(vecchioGiorno, giorno);
            }
            schede.save(scheda); // il cascade salva giorni ed esercizi del giorno
            nSchede++;
        }

        // Allenamenti
        int nAllenamenti = 0;
        for (Object ao : lista(radice.get("allenamenti"))) {
            Map<String, Object> am = obj(ao);
            Long vecchioGiorno = id(obj(am.get("giorno")));
            Giorno giorno = vecchioGiorno == null ? null : giorni.get(vecchioGiorno);
            LocalDateTime inizio = dataOra(am, "dataInizio");
            if (giorno == null || inizio == null) continue;

            Allenamento a = new Allenamento();
            a.setUtente(utente);
            a.setGiorno(giorno);
            a.setDataInizio(inizio);
            a.setDataFine(dataOra(am, "dataFine"));
            a.setStato("IN_CORSO".equals(txt(am, "stato")) ? StatoAllenamento.IN_CORSO : StatoAllenamento.COMPLETATO);

            for (Object seo : lista(am.get("serieEseguite"))) {
                Map<String, Object> sm = obj(seo);
                Long vecchioEg = id(obj(sm.get("esercizioGiorno")));
                EsercizioGiorno eg = vecchioEg == null ? null : eserciziGiorno.get(vecchioEg);
                Integer numero = intero(sm, "numeroSerie");
                Integer rip = intero(sm, "ripetizioniEffettuate");
                if (eg == null || numero == null || rip == null) continue;

                SerieEseguita s = new SerieEseguita();
                s.setAllenamento(a);
                s.setEsercizioGiorno(eg);
                String nomeEs = txt(sm, "nomeEsercizio");
                s.setNomeEsercizio(nomeEs != null ? nomeEs : eg.getEsercizio().getNome());
                s.setNumeroSerie(numero);
                String lato = txt(sm, "lato");
                s.setLato("SX".equalsIgnoreCase(lato) ? "SX" : "DX".equalsIgnoreCase(lato) ? "DX" : null);
                s.setRipetizioniEffettuate(rip);
                s.setCaricoEffettivo(reale(sm, "caricoEffettivo"));
                LocalDateTime ts = dataOra(sm, "timestampCompletamento");
                s.setTimestampCompletamento(ts != null ? ts : inizio);
                a.getSerieEseguite().add(s);
            }
            allenamenti.save(a);
            nAllenamenti++;
        }

        Map<String, Integer> esito = new LinkedHashMap<>();
        esito.put("esercizi", esNuovi);
        esito.put("schede", nSchede);
        esito.put("allenamenti", nAllenamenti);
        return esito;
    }

    /** Porta nel catalogo l'esercizio descritto da `m`; true se è stato creato (non esisteva). */
    private boolean trovaOCrea(Map<String, Object> m, Utente utente, Map<String, Esercizio> catalogo) {
        String nome = txt(m, "nome");
        if (nome == null) return false;
        String k = chiave(nome);
        if (catalogo.containsKey(k)) return false;
        Esercizio e = new Esercizio();
        e.setNome(nome);
        e.setGruppoMuscolare(txt(m, "gruppoMuscolare"));
        e.setUtente(utente);
        catalogo.put(k, esercizi.save(e));
        return true;
    }

    // ---- utilità di lettura (il JSON arriva come Map/List) ----
    private static String chiave(String nome) { return nome.trim().toLowerCase(Locale.ROOT); }

    private static Map<String, Object> obj(Object o) {
        return o instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }
    private static List<Object> lista(Object o) {
        return o instanceof List<?> l ? (List<Object>) l : List.of();
    }
    private static String txt(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null || v.toString().isBlank() ? null : v.toString().trim();
    }
    private static Integer intero(Map<String, Object> m, String k) {
        return m.get(k) instanceof Number n ? Integer.valueOf(n.intValue()) : null;
    }
    private static Double reale(Map<String, Object> m, String k) {
        return m.get(k) instanceof Number n ? Double.valueOf(n.doubleValue()) : null;
    }
    private static Long id(Map<String, Object> m) {
        return m.get("id") instanceof Number n ? Long.valueOf(n.longValue()) : null;
    }
    private static LocalDateTime dataOra(Map<String, Object> m, String k) {
        String t = txt(m, k);
        try { return t == null ? null : LocalDateTime.parse(t); } catch (DateTimeParseException e) { return null; }
    }
    private static LocalDate dataSola(Map<String, Object> m, String k) {
        String t = txt(m, k);
        try { return t == null ? null : LocalDate.parse(t); } catch (DateTimeParseException e) { return null; }
    }
}
