package com.schottenTotten.model;

public enum TypeTactique {
    // troupes d elite jouees comme des cartes clan
    JOKER(Categorie.ELITE),
    ESPION(Categorie.ELITE),
    PORTE_BOUCLIER(Categorie.ELITE),

    // modes de combat appliques sur une borne
    COLIN_MAILLARD(Categorie.MODE_COMBAT),
    COMBAT_DE_BOUE(Categorie.MODE_COMBAT),

    // ruses avec effet immediat puis defausse
    CHASSEUR_DE_TETE(Categorie.RUSE),
    STRATEGE(Categorie.RUSE),
    BANSHEE(Categorie.RUSE),
    TRAITRE(Categorie.RUSE);

    // categorie de la carte tactique
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
