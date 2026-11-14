
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

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(1);
        page.setPadding(16, 24, 16, 16);
        page.setBackgroundColor(Color.rgb(12, 20, 32));

        page.addView(texte("⚽ BARRY FOOT AI", 26));
        page.addView(texte("Matchs de football", 19));

        HorizontalScrollView defilement =
                new HorizontalScrollView(this);
        LinearLayout boutons = new LinearLayout(this);

        String[] jours = {
            "Aujourd'hui", "Demain", "J+2", "J+3"
        };

        for (int i = 0; i < 4; i++) {
            final int numero = i;
            Button bouton = new Button(this);
            bouton.setText(jours[i]);
            bouton.setAllCaps(false);
            bouton.setOnClickListener(v -> charger(numero));
            boutons.addView(bouton);
        }

        defilement.addView(boutons);
        page.addView(defilement);

        statut = texte("Connexion...", 16);
        page.addView(statut);

        ScrollView scroll = new ScrollView(this);
        liste = new LinearLayout(this);
        liste.setOrientation(1);
        scroll.addView(liste);

        page.addView(scroll,
            new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(page);
        charger(0);
    }

    TextView texte(String contenu, int taille) {
        TextView t = new TextView(this);
        t.setText(contenu);
        t.setTextSize(taille);
        t.setTextColor(Color.WHITE);
        t.setPadding(8, 14, 8, 14);
        return t;
    }

    void charger(int numero) {
        TimeZone guinee =
            TimeZone.getTimeZone("Africa/Conakry");

        Calendar calendrier = Calendar.getInstance(guinee);
        calendrier.add(Calendar.DAY_OF_MONTH, numero);

        SimpleDateFormat format =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setTimeZone(guinee);

        String date = format.format(calendrier.getTime());

        statut.setText("Chargement du " + date + "...");
        liste.removeAllViews();

        new Thread(() -> {
            try {
                String adresse =
                    "https://www.thesportsdb.com/api/v1/json/123/"
                    + "eventsday.php?d=" + date + "&s=Soccer";

                HttpURLConnection connexion =
                    (HttpURLConnection)
                    new URL(adresse).openConnection();

                connexion.setConnectTimeout(15000);
                connexion.setReadTimeout(15000);

                if (connexion.getResponseCode() != 200) {
                    throw new IOException("Erreur du serveur");
                }

                BufferedReader lecteur = new BufferedReader(
                    new InputStreamReader(
                        connexion.getInputStream(), "UTF-8"));

                StringBuilder reponse = new StringBuilder();
                String ligne;

                while ((ligne = lecteur.readLine()) != null) {
                    reponse.append(ligne);
                }

                lecteur.close();
                connexion.disconnect();

                JSONArray matchs =
                    new JSONObject(reponse.toString())
                    .optJSONArray("events");

                runOnUiThread(() -> {
                    statut.setText("Matchs du " + date);

                    if (matchs == null || matchs.length() == 0) {
                        liste.addView(texte(
                            "Aucun match disponible pour cette date.",
                            17));
                        return;
                    }

                    for (int i = 0; i < matchs.length(); i++) {
                        JSONObject m = matchs.optJSONObject(i);
                        if (m == null) continue;

                        String domicile =
                            m.optString("strHomeTeam", "?");
                        String exterieur =
                            m.optString("strAwayTeam", "?");
                        String ligue =
                            m.optString("strLeague", "Football");

                        String horodatage =
                            m.optString("strTimestamp", "");
                        String heure = horodatage.length() >= 16
                            ? horodatage.substring(11, 16)
                            : "";

                        if (heure.isEmpty()) {
                            String brut = m.optString("strTime", "");
                            heure = brut.length() >= 5
                                ? brut.substring(0, 5)
                                : "Heure indisponible";
                        }

                        liste.addView(texte(
                            "⚽ " + domicile + " - " + exterieur
                            + "\n🏆 " + ligue
                            + "\n🕒 " + heure + " (UTC)"
                            + "\n────────────────",
                            17));
                    }
                });

            } catch (Exception erreur) {
                runOnUiThread(() -> {
                    statut.setText("Connexion impossible");
                    liste.addView(texte(
                        "Vérifie Internet puis réessaie.",
                        17));
                });
            }
        }).start();
    }
}

