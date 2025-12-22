package com.schottenTotten.model;

public class CarteClan extends Carte {
    private final Couleur couleur;
    private final int valeur;

    public CarteClan(Couleur couleur, int valeur) {
        this.couleur = couleur;
        this.valeur = valeur;
    }

    @Override
    public boolean estTactique() {
        return false;
    }

    @Override
    public Couleur getCouleur() {
        return couleur;
    }

    @Override
    public int getValeur() {
        return valeur;
    }

    @Override
    public String toString() {
        return couleur.name().substring(0, 1) + valeur;
    }
}