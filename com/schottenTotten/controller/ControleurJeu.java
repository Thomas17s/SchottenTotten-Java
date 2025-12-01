package com.schottenTotten.controller;

import com.schottenTotten.model.Borne;
import com.schottenTotten.model.Carte;
import com.schottenTotten.model.Couleur;
import com.schottenTotten.model.Joueur;
import com.schottenTotten.view.VueConsole;
import com.schottenTotten.ai.StrategieIA;
import com.schottenTotten.ai.IAAleatoire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ControleurJeu  {
    private List<Borne> Bornes;
    private List<Carte> pioche;
    private Joueur j1;
    private Joueur j2;
    private VueConsole vue;
    private StrategieIA strategieIA;

    public ControleurJeu() {
        this.vue = new VueConsole();
        this.bornes = new ArrayList<>;
        // Création des 9 bornes 
        for (int i = 0; i < 9; i++) {
            bornes.add(new Borne(i));
        }
        this.pioche = creerPioche();
        this.strategieIA = new IAAleatoire();   
    }

    // Créer et mélange le paquet de 54 cartes (9 valeurs pour 6 couleurs)
}

