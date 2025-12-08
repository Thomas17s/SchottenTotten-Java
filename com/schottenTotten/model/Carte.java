package com.schottenTotten.model;

import com.schottenTotten.model.Couleur;

public class Carte implements Comparable<Carte> {

    private final int valeur;
    private final Couleur couleur;

    public Carte(Couleur couleur, int valeur){
        this.couleur = couleur;
        this.valeur = valeur;
    }
    public Couleur getCouleur(){return couleur;}
    public int getValeur(){return valeur;}
    @Override
    public String toString(){
        return couleur.name().substring(0, 1) + valeur;
    } 
    @Override
    public int compareTo(Carte autre) {
        return Integer.compare(this.valeur, autre.valeur);
    }
}