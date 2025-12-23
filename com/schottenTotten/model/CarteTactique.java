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
        return null; // pas de couleur associee
    }

    @Override
    public int getValeur() {
        return 10; // valeur utilisee uniquement pour le tri
    }

    @Override
    public String toString() {
        return type.name().substring(0, 3);
    }
}
