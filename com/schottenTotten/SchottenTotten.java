package com.schottenTotten;

import com.schottenTotten.controller.ControleurJeu;
import com.schottenTotten.controller.JeuFactory;

/**
 * Classe principale contenant le point d'entrée (main).
 * Elle délègue immédiatement la création et le lancement du jeu au contrôleur.
 */
public class SchottenTotten {

    public static void main(String[] args) {
        // 1. On utilise la Factory pour créer une instance du jeu (Variante "base" par défaut)
        // C'est ici qu'on respecte le pattern Factory demandé par le sujet[cite: 43].
        ControleurJeu jeu = JeuFactory.creerJeu("base");

        // 2. On lance la boucle principale du jeu
        jeu.lancerPartie();
    }
}
