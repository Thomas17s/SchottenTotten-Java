package com.schottenTotten.controller;

public class JeuFactory {

    public static ControleurJeu creerJeu(String variante) {
        // Pour l'instant, pas de variantes
        return new ControleurJeu();
    }
}