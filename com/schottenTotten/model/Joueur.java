package com.schottenTotten.model;
import com.schottenTotten.model.Couleur;
import java.util.ArrayList;
import java.util.List;

public class Joueur{
    protected String nom;
    protected List<Carte> main;
    protected boolean estIA;

    public Joueur(String nom, boolean estIA) {
        this.nom = nom;
        this.estIA = estIA;
        this.main = new ArrayList<>();
    }
    public void piocherCarte(Carte carte) {
        if (carte != null && main.size() < 6) {
            main.add(carte);
        }
    }
    public Carte jouerCarte(int index) {
        if (index >= 0 && index < main.size()) {
            return main.remove(index);
        }
        return null;
    }
    public List<Carte> getMain() { return main; }
    public String getNom() { return nom; }
    public boolean estIA() { return estIA; }
}