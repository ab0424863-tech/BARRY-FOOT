
package com.barryfoot.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(30, 60, 30, 30);
        page.setBackgroundColor(Color.rgb(12, 20, 32));

        TextView titre = new TextView(this);
        titre.setText("⚽ BARRY FOOT AI");
        titre.setTextSize(28);
        titre.setTextColor(Color.WHITE);
        titre.setGravity(Gravity.CENTER);
        page.addView(titre);

        ajouterTexte(page, "Bienvenue sur BARRY FOOT AI");
        ajouterTexte(page, "⚽ Matchs du jour");
        ajouterTexte(page, "🔴 Scores en direct");
        ajouterTexte(page, "📊 Analyses et statistiques");
        ajouterTexte(page, "🤖 Pronostics IA");
        ajouterTexte(page, "🏆 Classements");

        setContentView(page);
    }

    private void ajouterTexte(LinearLayout page, String texte) {
        TextView vue = new TextView(this);
        vue.setText(texte);
        vue.setTextSize(20);
        vue.setTextColor(Color.WHITE);
        vue.setPadding(10, 28, 10, 28);
        page.addView(vue);
    }
}
