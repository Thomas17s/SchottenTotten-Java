package com.schottenTotten.model;

public enum TypeTactique {
    // --- TROUPES D'ELITE (Se jouent comme une carte Clan) ---
    JOKER(Categorie.ELITE),
    ESPION(Categorie.ELITE),
    PORTE_BOUCLIER(Categorie.ELITE),

    // --- MODES DE COMBAT (Se jouent SUR une borne et changent la règle) ---
    COLIN_MAILLARD(Categorie.MODE_COMBAT),
    COMBAT_DE_BOUE(Categorie.MODE_COMBAT),

    // --- RUSES (Action immédiate puis défausse) ---
    CHASSEUR_DE_TETE(Categorie.RUSE),
    STRATEGE(Categorie.RUSE),
    BANSHEE(Categorie.RUSE),
    TRAITRE(Categorie.RUSE);

    // Définition de la catégorie
    public enum Categorie { ELITE, MODE_COMBAT, RUSE }

    private final Categorie categorie;

    TypeTactique(Categorie categorie) {
        this.categorie = categorie;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase().replace('_', ' ');
    }
}