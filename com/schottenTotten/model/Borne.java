package com.schottenTotten.model;

import java.util.ArrayList;
import java.util.List;

public class Borne {
    private int id;
    private List<Carte> coteJoueur1;
    private List<Carte> coteJoueur2;
    private Joueur proprietaire;
    
    // etats lies aux cartes tactiques
    private boolean modeColinMaillard; // si actif, seule la somme des cartes compte
    private boolean modeCombatBoue;    // si actif, il faut 4 cartes au lieu de 3

    public Borne(int id) {
        this.id = id;
        this.coteJoueur1 = new ArrayList<>();
        this.coteJoueur2 = new ArrayList<>();
        this.proprietaire = null;
        this.modeColinMaillard = false;
        this.modeCombatBoue = false;
    }

    // methodes modifiees

    // capacite de la borne selon la regle active
    public int getCapaciteMax() {
        return modeCombatBoue ? 4 : 3;
    }

    // indique si la borne est complete pour les deux joueurs
    public boolean estPleine() {
        return coteJoueur1.size() == getCapaciteMax() && coteJoueur2.size() == getCapaciteMax();
    }

    public boolean ajouterCarte(Joueur j, Carte c, boolean estJ1) {
        if (proprietaire != null) return false;
        List<Carte> cote = estJ1 ? coteJoueur1 : coteJoueur2;
        
        if (cote.size() < getCapaciteMax()) {
            cote.add(c);
            return true;
        }
        return false;
    }

    // gestion des modes tactiques

    public void activerColinMaillard() {
        this.modeColinMaillard = true;
    }

    public boolean isColinMaillard() {
        return modeColinMaillard;
    }

    public void activerCombatBoue() {
        this.modeCombatBoue = true;
    }

    public int getId() { return id; }
    public List<Carte> getCoteJoueur1() { return coteJoueur1; }
    public List<Carte> getCoteJoueur2() { return coteJoueur2; }
    public Joueur getProprietaire() { return proprietaire; }
    public void setProprietaire(Joueur proprietaire) { this.proprietaire = proprietaire; }
}
