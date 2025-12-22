package com.schottenTotten.model;

public abstract class Carte implements Comparable<Carte> {
    
    // Méthode abstraite : chaque enfant devra dire s'il est une tactique
    public abstract boolean estTactique();

    public abstract Couleur getCouleur();
    public abstract int getValeur();
    
    @Override
    public abstract String toString();

    @Override
    public int compareTo(Carte autre) {
        // Les cartes Tactiques sont mises à la fin de la main
        if (this.estTactique() && !autre.estTactique()) return 1;
        if (!this.estTactique() && autre.estTactique()) return -1;
        
        // Si même type, on trie par valeur
        return Integer.compare(this.getValeur(), autre.getValeur());
    }
}