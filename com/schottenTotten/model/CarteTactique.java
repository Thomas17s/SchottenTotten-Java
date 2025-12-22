package com.schottenTotten.model;

public class CarteTactique extends Carte {
    private final TypeTactique type;

    public CarteTactique(TypeTactique type) {
        this.type = type;
    }

    public TypeTactique getType() {
        return type;
    }

    @Override
    public boolean estTactique() {
        return true;
    }

    @Override
    public Couleur getCouleur() {
        return null; // Pas de couleur par défaut
    }

    @Override
    public int getValeur() {
        return 10; // Valeur fictive pour le tri
    }

    @Override
    public String toString() {
        return type.name().substring(0, 3);
    }
}