package com.schottenTotten.model;

public abstract class Carte implements Comparable<Carte> {
    
    // methode abstraite pour savoir si la carte est tactique
    public abstract boolean estTactique();

    public abstract Couleur getCouleur();
    public abstract int getValeur();
    
    @Override
    public abstract String toString();

    @Override
    public int compareTo(Carte autre) {
        // les cartes tactiques sont placees apres les cartes classiques
        if (this.estTactique() && !autre.estTactique()) return 1;
        if (!this.estTactique() && autre.estTactique()) return -1;
        
        // si meme type de carte, tri par valeur
        return Integer.compare(this.getValeur(), autre.getValeur());
    }
}
