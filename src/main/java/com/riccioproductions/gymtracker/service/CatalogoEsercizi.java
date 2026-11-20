package com.riccioproductions.gymtracker.service;

import com.riccioproductions.gymtracker.model.Esercizio;
import com.riccioproductions.gymtracker.model.Utente;
import java.util.ArrayList;
import java.util.List;

// Catalogo di esercizi che ogni nuovo utente trova già nel proprio elenco.
// Per aggiungerne o toglierne basta modificare questa tabella: {gruppo muscolare, nome}.
// I nomi devono essere unici (senza distinguere maiuscole/minuscole).
public final class CatalogoEsercizi {

    private CatalogoEsercizi() {}

    private static final String[][] CATALOGO = {
        // Petto
        {"Petto", "Panca piana con bilanciere"},
        {"Petto", "Panca inclinata con bilanciere"},
        {"Petto", "Panca declinata con bilanciere"},
        {"Petto", "Panca piana con manubri"},
        {"Petto", "Panca inclinata con manubri"},
        {"Petto", "Croci con manubri"},
        {"Petto", "Croci ai cavi"},
        {"Petto", "Chest press"},
        {"Petto", "Pectoral machine"},
        {"Petto", "Piegamenti sulle braccia"},
        {"Petto", "Pullover con manubrio"},

        // Schiena
        {"Schiena", "Trazioni alla sbarra presa prona"},
        {"Schiena", "Trazioni alla sbarra presa supina"},
        {"Schiena", "Lat machine presa larga"},
        {"Schiena", "Lat machine presa stretta"},
        {"Schiena", "Rematore con bilanciere"},
        {"Schiena", "Rematore con manubrio"},
        {"Schiena", "Rematore al cavo basso"},
        {"Schiena", "Rematore con T-bar"},
        {"Schiena", "Rematore alla macchina"},
        {"Schiena", "Pulldown a braccia tese"},
        {"Schiena", "Stacco da terra con bilanciere"},
        {"Schiena", "Iperestensioni"},

        // Spalle
        {"Spalle", "Military press con bilanciere"},
        {"Spalle", "Lento avanti con manubri"},
        {"Spalle", "Arnold press"},
        {"Spalle", "Shoulder press alla macchina"},
        {"Spalle", "Alzate laterali con manubri"},
        {"Spalle", "Alzate laterali ai cavi"},
        {"Spalle", "Alzate frontali con manubri"},
        {"Spalle", "Alzate posteriori con manubri"},
        {"Spalle", "Pectoral machine inversa"},
        {"Spalle", "Face pull"},
        {"Spalle", "Tirate al mento"},
        {"Spalle", "Scrollate con manubri"},

        // Bicipiti
        {"Bicipiti", "Curl con bilanciere"},
        {"Bicipiti", "Curl con bilanciere EZ"},
        {"Bicipiti", "Curl con manubri alternato"},
        {"Bicipiti", "Curl a martello"},
        {"Bicipiti", "Curl su panca inclinata"},
        {"Bicipiti", "Curl alla panca Scott"},
        {"Bicipiti", "Curl concentrato"},
        {"Bicipiti", "Curl al cavo basso"},
        {"Bicipiti", "Curl alla macchina"},

        // Tricipiti
        {"Tricipiti", "French press"},
        {"Tricipiti", "Push down ai cavi con corda"},
        {"Tricipiti", "Push down ai cavi con barra"},
        {"Tricipiti", "Estensioni sopra la testa con manubrio"},
        {"Tricipiti", "Estensioni sopra la testa al cavo"},
        {"Tricipiti", "Dip alle parallele"},
        {"Tricipiti", "Dip alla panca"},
        {"Tricipiti", "Panca presa stretta"},
        {"Tricipiti", "Kick back con manubrio"},

        // Gambe
        {"Gambe", "Squat con bilanciere"},
        {"Gambe", "Front squat"},
        {"Gambe", "Squat al multipower"},
        {"Gambe", "Goblet squat"},
        {"Gambe", "Squat bulgaro"},
        {"Gambe", "Hack squat"},
        {"Gambe", "Pressa orizzontale"},
        {"Gambe", "Pressa a 45 gradi"},
        {"Gambe", "Leg extension"},
        {"Gambe", "Leg curl sdraiato"},
        {"Gambe", "Leg curl da seduto"},
        {"Gambe", "Affondi con manubri"},
        {"Gambe", "Affondi camminati"},
        {"Gambe", "Step-up"},
        {"Gambe", "Stacco rumeno"},
        {"Gambe", "Good morning"},
        {"Gambe", "Abductor machine"},

        // Glutei
        {"Glutei", "Hip thrust con bilanciere"},
        {"Glutei", "Glute bridge"},
        {"Glutei", "Kickback ai cavi"},
        {"Glutei", "Kettlebell swing"},

        // Polpacci
        {"Polpacci", "Calf in piedi"},
        {"Polpacci", "Calf da seduto"},
        {"Polpacci", "Calf alla pressa"},

        // Addominali
        {"Addominali", "Crunch a terra"},
        {"Addominali", "Crunch ai cavi"},
        {"Addominali", "Crunch inverso"},
        {"Addominali", "Sollevamento gambe alla sbarra"},
        {"Addominali", "Sollevamento gambe a terra"},
        {"Addominali", "Plank"},
        {"Addominali", "Plank laterale"},
        {"Addominali", "Russian twist"},
        {"Addominali", "Ruota addominale"},
        {"Addominali", "Rotazioni al cavo"},

        // Avambracci
        {"Avambracci", "Curl polsi con bilanciere"},
        {"Avambracci", "Curl inverso con bilanciere"},
        {"Avambracci", "Camminata del contadino"}
    };

    /** Gli esercizi del catalogo, già intestati all'utente (da salvare). */
    public static List<Esercizio> per(Utente utente) {
        List<Esercizio> lista = new ArrayList<>();
        for (String[] riga : CATALOGO) {
            Esercizio e = new Esercizio();
            e.setGruppoMuscolare(riga[0]);
            e.setNome(riga[1]);
            e.setUtente(utente);
            lista.add(e);
        }
        return lista;
    }
}
