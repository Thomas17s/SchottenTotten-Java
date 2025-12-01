package com.schottenTotten.model;
import com.schottenTotten.model.Couleur;
import java.util.ArrayList;
import java.util.List;

public class Borne{
    private final int id;
    private final List<Carte> coteJoueur1;
    private final List<Carte> coteJoueur2;
    private Joueur proprietaire;
    
    public Borne(int id){
        this.id =id;
        this.coteJoueur1=new ArrayList<>();
        this.coteJoueur2=new ArrayList<>();
        this.proprietaire = null;

    }
    public boolean ajouterCarte(Joueur joueur,Carte carte, boolean estJoueur1){

        if (proprietaire!=null)
            return false;
        List<Carte> cote = estJoueur1 ? coteJoueur1 : coteJoueur2;
        if (cote.size() < 3) {
            cote.add(carte);
            return true;
        }
        return false;


    }
    public List<Carte> getCoteJoueur1() { return coteJoueur1; }
    public List<Carte> getCoteJoueur2() { return coteJoueur2; }
    public Joueur getProprietaire() { return proprietaire; }
    public void setProprietaire(Joueur p) { this.proprietaire = p; }
    public int getId() { return id; }
    
    public boolean estPleine() {
        return coteJoueur1.size() == 3 && coteJoueur2.size() == 3;
    }
}
