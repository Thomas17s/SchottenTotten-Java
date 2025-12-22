package com.schottenTotten.model;

import java.util.ArrayList;
import java.util.List;

public class Borne {
    private int id;
    private List<Carte> coteJoueur1;
    private List<Carte> coteJoueur2;
    private Joueur proprietaire;
    
    // Nouveaux états pour les cartes Tactiques
    private boolean modeColinMaillard; // Si vrai : on compte juste la somme
    private boolean modeCombatBoue;    // Si vrai : il faut 4 cartes pour gagner

    public Borne(int id) {
        this.id = id;
        this.coteJoueur1 = new ArrayList<>();
        this.coteJoueur2 = new ArrayList<>();
        this.proprietaire = null;
        this.modeColinMaillard = false;
        this.modeCombatBoue = false;
    }

    // --- Méthodes modifiées ---

    // La capacité dépend maintenant du mode Combat de Boue (3 ou 4)
    public int getCapaciteMax() {
        return modeCombatBoue ? 4 : 3;
    }

    // Vérifie si la borne est pleine selon la règle active
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

    // --- Getters et Setters pour les modes ---

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