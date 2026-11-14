
package com.barryfoot.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.*;
import org.json.*;
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout liste;
    TextView statut;
    int jourChoisi = 0;
    int requeteActive = 0;

    final TimeZone guinee =
        TimeZone.getTimeZone("Africa/Conakry");

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(1);
        page.setPadding(16, 24, 16, 16);
        page.setBackgroundColor(Color.rgb(10, 20, 30));

        TextView titre = texte("⚽ BARRY FOOT AI", 26);
        titre.setTextColor(Color.rgb(70, 225, 145));
        page.addView(titre);

        page.addView(texte(
            "Matchs • Scores • Statistiques", 17));

        page.addView(texte(
            "Heure de Guinée 🇬🇳 • GMT",
            14));

        HorizontalScrollView defilement =
            new HorizontalScrollView(this);

        LinearLayout boutons = new LinearLayout(this);

        String[] jours = {
            "Aujourd'hui", "Demain", "J+2", "J+3"
        };

        for (int i = 0; i < jours.length; i++) {
            final int n = i;

            Button bouton = new Button(this);
            bouton.setText(jours[i]);
            bouton.setAllCaps(false);
            bouton.setOnClickListener(v -> charger(n));

            boutons.addView(bouton);
        }

        defilement.addView(boutons);
        page.addView(defilement);

        Button actualiser = new Button(this);
        actualiser.setText("↻ Actualiser les matchs");
        actualiser.setAllCaps(false);
        actualiser.setOnClickListener(v ->
            charger(jourChoisi));

        page.addView(actualiser);

        statut = texte("Chargement...", 16);
        page.addView(statut);

        ScrollView scroll = new ScrollView(this);

        liste = new LinearLayout(this);
        liste.setOrientation(1);

        scroll.addView(liste);

        page.addView(scroll,
            new LinearLayout.LayoutParams(-1, 0, 1));

        page.addView(texte(
            "Source : TheSportsDB • Liste gratuite limitée",
            12));

        setContentView(page);
        charger(0);
    }

    TextView texte(String valeur, int taille) {
        TextView t = new TextView(this);
        t.setText(valeur);
        t.setTextSize(taille);
        t.setTextColor(Color.WHITE);
        t.setPadding(8, 10, 8, 10);
        return t;
    }

    String etat(String brut) {
        String s = brut.trim().toUpperCase(Locale.ROOT);

        if (s.equals("NS") || s.equals("NOT STARTED"))
            return "À venir";

        if (s.equals("FT") || s.equals("AET")
                || s.equals("PEN") || s.equals("AP")
                || s.equals("MATCH FINISHED"))
            return "Terminé";

        if (s.equals("1H") || s.equals("2H")
                || s.equals("HT") || s.equals("ET")
                || s.equals("BT") || s.equals("P"))
            return "En cours selon la source";

        if (s.equals("PST") || s.equals("POST"))
            return "Reporté";

        if (s.equals("CANC"))
            return "Annulé";

        if (s.equals("TBD"))
            return "Horaire à confirmer";

        if (s.equals("SUSP") || s.equals("INT"))
            return "Interrompu";

        return "Statut non communiqué";
    }

    String heureGuinee(JSONObject m) {
        String ts = m.optString("strTimestamp", "");

        if (ts.length() >= 16 && ts.charAt(10) == 'T') {
            return ts.substring(11, 16);
        }

        String h = m.optString("strTime", "");

        if (h.length() >= 5 && h.charAt(2) == ':') {
            return h.substring(0, 5);
        }

        return "Heure indisponible";
    }

    String score(JSONObject m) {
        String a = m.optString("intHomeScore", "");
        String b = m.optString("intAwayScore", "");
        String s = m.optString("strStatus", "")
            .trim().toUpperCase(Locale.ROOT);

        if (s.equals("NS") || s.equals("TBD")
                || s.equals("NOT STARTED"))
            return " - ";

        if (a.matches("\\d+") && b.matches("\\d+"))
            return " " + a + " - " + b + " ";

        return " - ";
    }

    void afficherMatch(JSONObject m) {
        String domicile =
            m.optString("strHomeTeam", "Équipe inconnue");
        String exterieur =
            m.optString("strAwayTeam", "Équipe inconnue");
        String ligue =
            m.optString("strLeague", "Football");

        LinearLayout carte = new LinearLayout(this);
        carte.setOrientation(1);
        carte.setPadding(10, 10, 10, 10);
        carte.setBackgroundColor(Color.rgb(27, 41, 57));

        TextView championnat = texte("🏆 " + ligue, 14);
        championnat.setTextColor(
            Color.rgb(90, 220, 150));
        carte.addView(championnat);

        carte.addView(texte(
            domicile + score(m) + exterieur, 18));

        carte.addView(texte(
            "🕒 " + heureGuinee(m) + " • Guinée", 15));

        carte.addView(texte(
            "● " + etat(m.optString("strStatus", "")),
            14));

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = 12;

        liste.addView(carte, p);
    }

    void charger(int numero) {
        jourChoisi = numero;
        final int ticket = ++requeteActive;

        Calendar calendrier = Calendar.getInstance(guinee);
        calendrier.add(Calendar.DAY_OF_MONTH, numero);

        SimpleDateFormat format =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setTimeZone(guinee);

        final String date =
            format.format(calendrier.getTime());

        liste.removeAllViews();
        statut.setText("Chargement du " + date + "...");

        new Thread(() -> {
            HttpURLConnection connexion = null;

            try {
                String adresse =
                    "https://www.thesportsdb.com/api/v1/json/123/"
                    + "eventsday.php?d=" + date + "&s=Soccer";

                connexion = (HttpURLConnection)
                    new URL(adresse).openConnection();

                connexion.setConnectTimeout(15000);
                connexion.setReadTimeout(15000);

                if (connexion.getResponseCode() != 200)
                    throw new IOException("Erreur du serveur");

                BufferedReader lecteur = new BufferedReader(
                    new InputStreamReader(
                        connexion.getInputStream(), "UTF-8"));

                StringBuilder contenu = new StringBuilder();
                String ligne;

                while ((ligne = lecteur.readLine()) != null)
                    contenu.append(ligne);

                lecteur.close();

                JSONArray donnees =
                    new JSONObject(contenu.toString())
                    .optJSONArray("events");

                ArrayList<JSONObject> matchs =
                    new ArrayList<>();

                if (donnees != null) {
                    for (int i = 0; i < donnees.length(); i++) {
                        JSONObject m = donnees.optJSONObject(i);
                        if (m != null) matchs.add(m);
                    }
                }

                Collections.sort(matchs, (a, b) ->
                    a.optString("strTimestamp", "")
                     .compareTo(
                        b.optString("strTimestamp", "")));

                runOnUiThread(() -> {
                    if (ticket != requeteActive) return;

                    statut.setText(
                        date + " • " + matchs.size()
                        + " matchs retournés (liste partielle)");

                    if (matchs.isEmpty()) {
                        liste.addView(texte(
                            "Aucune rencontre renvoyée par la source.",
                            16));
                    }

                    for (JSONObject m : matchs)
                        afficherMatch(m);
                });

            } catch (Exception erreur) {
                runOnUiThread(() -> {
                    if (ticket != requeteActive) return;

                    statut.setText("Connexion impossible");
                    liste.addView(texte(
                        "Vérifie Internet et appuie sur Actualiser.",
                        16));
                });

            } finally {
                if (connexion != null)
                    connexion.disconnect();
            }
        }).start();
    }
}
