package com.schottenTotten;

import com.schottenTotten.controller.ControleurJeu;
import com.schottenTotten.controller.JeuFactory;


// classe principale avec le point d entree du programme elle cree le jeu et lance la partie
public class SchottenTotten {

    public static void main(String[] args) {
        // creation du jeu via la factory avec la variante de base
        ControleurJeu jeu = JeuFactory.creerJeu("base");

        // lancement de la partie
        jeu.lancerPartie();
    }
}
