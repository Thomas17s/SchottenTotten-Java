package com.schottenTotten.controller;

import com.schottenTotten.model.*;
import com.schottenTotten.view.VueConsole;
import com.schottenTotten.ai.StrategieIA;
import com.schottenTotten.ai.IAAleatoire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random; 

public class ControleurJeu {
    private List<Borne> bornes;
    private List<Carte> piocheClan;     
    private List<Carte> piocheTactique; 
    private Joueur j1;
    private Joueur j2;
    private VueConsole vue;
    private StrategieIA strategieIA;
    private boolean modeTactique;
    private boolean modeExpert; // active la revendication en debut de tour
    private Random random; 

    public ControleurJeu() {
        this.vue = new VueConsole();
        this.bornes = new ArrayList<>();
        // creation des 9 bornes
        for (int i = 0; i < 9; i++) {
            bornes.add(new Borne(i));
        }
        this.strategieIA = new IAAleatoire(); 
        this.random = new Random();
    }

    // creation et melange des cartes clan
    private List<Carte> creerPiocheClan() {
        List<Carte> nouvellePioche = new ArrayList<>();
        for (Couleur c : Couleur.values()) {
            for (int i = 1; i <= 9; i++) {
                nouvellePioche.add(new CarteClan(c, i)); 
            }
        }
        Collections.shuffle(nouvellePioche);
        return nouvellePioche;
    }

    // creation du paquet de cartes tactiques
    private List<Carte> creerPiocheTactique() {
        List<Carte> nouvellePioche = new ArrayList<>();
        for (TypeTactique t : TypeTactique.values()) {
            nouvellePioche.add(new CarteTactique(t));
        }
        Collections.shuffle(nouvellePioche);
        return nouvellePioche;
    }

    // lance une partie complete
    public void lancerPartie() {
        vue.afficherMessage("Bienvenue dans Schotten-Totten !");

        // choix du mode de jeu
        int choixMode = vue.demanderEntier(
            "Mode de jeu :\n1. Classique (6 cartes)\n2. Tactique (7 cartes, cartes speciales)\nVotre choix",
            1, 2
        );
        this.modeTactique = (choixMode == 2);
        
        // choix de la variante expert
        int choixExpert = vue.demanderEntier(
            "Variante Experts (Revendication au debut du tour) ?\n1. Oui\n2. Non",
            1, 2
        );
        this.modeExpert = (choixExpert == 1);

        j1 = new Joueur("Joueur 1 (Humain)", false);
        
        int typeAdv = vue.demanderEntier("1. vs IA\n2. vs Humain \nChoix", 1, 2);
        j2 = new Joueur(typeAdv == 1 ? "Ordinateur" : "Joueur 2", typeAdv == 1);

        // initialisation des pioches
        this.piocheClan = creerPiocheClan();
        if (modeTactique) {
            this.piocheTactique = creerPiocheTactique();
        } else {
            this.piocheTactique = new ArrayList<>();
        }

        // distribution des cartes de depart
        int tailleMain = modeTactique ? 7 : 6;
        for (int i = 0; i < tailleMain; i++) {
            j1.piocherCarte(tirerCarteClan());
            j2.piocherCarte(tirerCarteClan());
        }

        boolean enCours = true;
        Joueur joueurCourant = j1;

        // boucle principale
        while (enCours) {
            vue.afficherPlateau(bornes, j1, j2);

            // revendications en debut de tour en mode expert
            if (modeExpert) {
                if (!joueurCourant.estIA())
                    vue.afficherMessage("Debut du tour : verification des revendications...");
                boolean changement = verifierRevendications(joueurCourant);
                if (changement)
                    vue.afficherPlateau(bornes, j1, j2);
                
                if (verifierVictoire(joueurCourant)) {
                    vue.afficherMessage(
                        "VICTOIRE DE " + joueurCourant.getNom().toUpperCase() + " !!!"
                    );
                    break;
                }
            }

            // tour du joueur courant
            jouerTour(joueurCourant);

            // revendications en fin de tour en mode normal
            if (!modeExpert) {
                verifierRevendications(joueurCourant);
                if (verifierVictoire(joueurCourant)) {
                    vue.afficherPlateau(bornes, j1, j2);
                    vue.afficherMessage(
                        "VICTOIRE DE " + joueurCourant.getNom().toUpperCase() + " !!!"
                    );
                    enCours = false;
                    break;
                }
            }

            // plus aucune carte disponible
            if (piocheClan.isEmpty() && piocheTactique.isEmpty()
                && j1.getMain().isEmpty() && j2.getMain().isEmpty()) {
                vue.afficherMessage("Plus de cartes ! Match nul.");
                enCours = false;
            }

            // changement de joueur
            joueurCourant = (joueurCourant == j1) ? j2 : j1;
        }
    }

    private Carte tirerCarteClan() {
        return piocheClan.isEmpty() ? null : piocheClan.remove(0);
    }

    private Carte tirerCarteTactique() {
        return piocheTactique.isEmpty() ? null : piocheTactique.remove(0);
    }

    private void jouerTour(Joueur p) {
        vue.afficherMessage(">>> Tour de " + p.getNom());
        boolean tourFini = false;

        while (!tourFini) {
            int idxCarte = -1;
            int idxBorne = -1;

            if (p.estIA()) {
                int essais = 0;
                do {
                    int[] action = strategieIA.jouerTour(p, bornes);
                    idxCarte = action[0];
                    idxBorne = action[1];
                    essais++;
                } while ((idxCarte == -1 || !estCoupValideIA(p, idxCarte, idxBorne)) && essais < 200);

                if (idxCarte == -1 || !estCoupValideIA(p, idxCarte, idxBorne)) {
                    boolean trouve = false;
                    for (int c = 0; c < p.getMain().size(); c++) {
                        for (int b = 0; b < 9; b++) {
                            if (estCoupValideIA(p, c, b)) {
                                idxCarte = c;
                                idxBorne = b;
                                trouve = true;
                                break;
                            }
                        }
                        if (trouve) break;
                    }
                    if (!trouve) {
                        vue.afficherMessage("L ordinateur ne peut plus jouer.");
                        return;
                    }
                }
            } else {
                vue.afficherMain(p);
                idxCarte = vue.demanderEntier(
                    "Carte a jouer (index)", 0, p.getMain().size() - 1
                );
            }

            Carte carteChoisie = p.getMain().get(idxCarte);
            
            // gestion selon le type de carte
            if (carteChoisie.estTactique()
                && ((CarteTactique)carteChoisie).getType().getCategorie()
                   == TypeTactique.Categorie.RUSE) {

                tourFini = appliquerRuse(p, (CarteTactique) carteChoisie);
                if (tourFini) {
                    p.jouerCarte(idxCarte); 
                    if(p.estIA())
                        vue.afficherMessage("L ordinateur a joue une ruse : " + carteChoisie);
                }
            }
            else if (carteChoisie.estTactique()
                && ((CarteTactique)carteChoisie).getType().getCategorie()
                   == TypeTactique.Categorie.MODE_COMBAT) {

                if (p.estIA()) {
                    List<Integer> bornesDispo = new ArrayList<>();
                    for(Borne b : bornes)
                        if(b.getProprietaire() == null)
                            bornesDispo.add(b.getId());
                    if (!bornesDispo.isEmpty())
                        idxBorne = bornesDispo.get(random.nextInt(bornesDispo.size()));
                    else idxBorne = -1;
                } else {
                    idxBorne = vue.demanderEntier(
                        "Sur quelle borne appliquer l effet ?", 1, 9
                    ) - 1;
                }
                
                if (idxBorne != -1) {
                    Borne b = bornes.get(idxBorne);
                    TypeTactique type =
                        ((CarteTactique)carteChoisie).getType();
                    if (type == TypeTactique.COLIN_MAILLARD)
                        b.activerColinMaillard();
                    else if (type == TypeTactique.COMBAT_DE_BOUE)
                        b.activerCombatBoue();
                    p.jouerCarte(idxCarte); 
                    if (!p.estIA())
                        vue.afficherMessage(
                            "Mode " + type + " active sur la borne " + (idxBorne+1)
                        );
                    tourFini = true;
                }
            }
            else {
                if (!p.estIA())
                    idxBorne = vue.demanderEntier(
                        "Sur quelle borne poser la carte ?", 1, 9
                    ) - 1;
                
                if (idxBorne >= 0 && idxBorne < 9
                    && verifierPlace(p, bornes.get(idxBorne))) {
                    Carte c = p.jouerCarte(idxCarte);
                    bornes.get(idxBorne).ajouterCarte(p, c, p == j1);
                    if (p.estIA())
                        vue.afficherMessage(
                            "L ordinateur joue sur la borne " + (idxBorne+1)
                        );
                    tourFini = true;
                } else {
                    if (!p.estIA())
                        vue.afficherMessage(
                            "Impossible de jouer ici."
                        );
                }
            }
        }
        phaseDePioche(p);
    }

    // verifie si un coup est autorise pour le joueur automatique
    private boolean estCoupValideIA(Joueur p, int idxCarte, int idxBorne) {
        if (idxCarte < 0 || idxCarte >= p.getMain().size()) return false;
        Carte c = p.getMain().get(idxCarte);
        
        if (c.estTactique()) {
            TypeTactique.Categorie cat =
                ((CarteTactique)c).getType().getCategorie();
            if (cat == TypeTactique.Categorie.RUSE
                || cat == TypeTactique.Categorie.MODE_COMBAT)
                return true;
        }
        return (idxBorne >= 0 && idxBorne < 9
                && verifierPlace(p, bornes.get(idxBorne)));
    }

    // pioche de fin de tour
    private void phaseDePioche(Joueur p) {
        int limiteMain = modeTactique ? 7 : 6;
        if (p.getMain().size() >= limiteMain) return;

        Carte nouvelleCarte = null;
        if (modeTactique && !piocheTactique.isEmpty()
            && !piocheClan.isEmpty()) {
            if (p.estIA()) {
                if (random.nextDouble() > 0.5)
                    nouvelleCarte = tirerCarteTactique();
                else nouvelleCarte = tirerCarteClan();
            } else {
                vue.afficherMessage("1. Pioche Clan  2. Pioche Tactique");
                int choix = vue.demanderEntier("Choix", 1, 2);
                nouvelleCarte =
                    (choix == 1) ? tirerCarteClan() : tirerCarteTactique();
            }
        } else if (!piocheClan.isEmpty()) {
            nouvelleCarte = tirerCarteClan();
        } else if (modeTactique && !piocheTactique.isEmpty()) {
            nouvelleCarte = tirerCarteTactique();
        }

        if (nouvelleCarte != null) {
            p.piocherCarte(nouvelleCarte);
            if (!p.estIA())
                vue.afficherMessage("Vous avez pioche : " + nouvelleCarte);
        }
    }
    
    private boolean verifierPlace(Joueur p, Borne b) {
        if (b.getProprietaire() != null) return false;
        List<Carte> cote =
            (p == j1) ? b.getCoteJoueur1() : b.getCoteJoueur2();
        return cote.size() < b.getCapaciteMax(); 
    }

    // verifie les revendications possibles
    private boolean verifierRevendications(Joueur joueurCourant) {
        boolean changement = false;
        for (Borne b : bornes) {
            if (b.getProprietaire() == null && b.estPleine()) {
                int score1 =
                    calculerScoreOptimal(b.getCoteJoueur1(), b);
                int score2 =
                    calculerScoreOptimal(b.getCoteJoueur2(), b);

                Joueur gagnantPotentiel = null;
                if (score1 > score2) gagnantPotentiel = j1;
                else if (score2 > score1) gagnantPotentiel = j2;

                if (gagnantPotentiel != null) {
                    if (modeExpert) {
                        if (gagnantPotentiel == joueurCourant) {
                            b.setProprietaire(gagnantPotentiel);
                            vue.afficherMessage(
                                "Borne " + (b.getId() + 1)
                                + " revendiquee par "
                                + gagnantPotentiel.getNom()
                                + " !"
                            );
                            changement = true;
                        }
                    } 
                    else {
                        b.setProprietaire(gagnantPotentiel);
                        vue.afficherMessage(
                            "Borne " + (b.getId() + 1)
                            + " gagnee par "
                            + gagnantPotentiel.getNom()
                        );
                        changement = true;
                    }
                }
            }
        }
        return changement;
    }

    private int calculerScoreOptimal(List<Carte> main, Borne b) {
        if (b.isColinMaillard())
            return calculerSomme(main); 

        int indexTactique = -1;
        for (int i = 0; i < main.size(); i++) {
            if (main.get(i).estTactique()) {
                indexTactique = i;
                break;
            }
        }

        if (indexTactique == -1)
            return calculerScoreClassique(main);

        CarteTactique tactique =
            (CarteTactique) main.get(indexTactique);
        int meilleurScore = 0;
        List<Carte> options = genererOptionsPour(tactique);

        for (Carte option : options) {
            List<Carte> mainTest = new ArrayList<>(main);
            mainTest.set(indexTactique, option);
            int score = calculerScoreOptimal(mainTest, b);
            if (score > meilleurScore)
                meilleurScore = score;
        }
        return meilleurScore;
    }
    
    private int calculerSomme(List<Carte> main) {
        int somme = 0;
        for(Carte c : main) {
            if (c.getValeur() > 0)
                somme += c.getValeur();
            else if (c.estTactique())
                somme += 9; 
        }
        return somme;
    }

    private List<Carte> genererOptionsPour(CarteTactique t) {
        List<Carte> options = new ArrayList<>();
        if (t.getType() == TypeTactique.JOKER) {
            for (Couleur c : Couleur.values())
                for (int v = 1; v <= 9; v++)
                    options.add(new CarteClan(c, v));
        } else if (t.getType() == TypeTactique.ESPION) {
            for (Couleur c : Couleur.values())
                options.add(new CarteClan(c, 7));
        } else if (t.getType() == TypeTactique.PORTE_BOUCLIER) {
            for (Couleur c : Couleur.values()) {
                options.add(new CarteClan(c, 1));
                options.add(new CarteClan(c, 2));
                options.add(new CarteClan(c, 3));
            }
        } else {
            options.add(new CarteClan(Couleur.Rouge, 0));
        }
        return options;
    }

    private int calculerScoreClassique(List<Carte> main) {
        if (main.size() < 3) return 0; 
        List<Carte> triee = new ArrayList<>(main);
        Collections.sort(triee); 
        
        boolean memeCouleur = true;
        Couleur ref = triee.get(0).getCouleur();
        for (Carte c : triee) {
            if (c.getCouleur() == null || c.getCouleur() != ref)
                memeCouleur = false;
        }

        boolean suite = true;
        for(int i=0; i < triee.size()-1; i++) {
            if(triee.get(i).getValeur() + 1
               != triee.get(i+1).getValeur())
                suite = false;
        }
        
        boolean memeValeur = true;
        int valRef = triee.get(0).getValeur();
        for(Carte c : triee) {
            if(c.getValeur() != valRef)
                memeValeur = false;
        }
        
        int somme =
            triee.stream().mapToInt(Carte::getValeur).sum();
        if (memeCouleur && suite) return 5000 + somme;
        if (memeValeur) return 4000 + somme;
        if (memeCouleur) return 3000 + somme;
        if (suite) return 2000 + somme;
        return 1000 + somme;
    }

    private boolean verifierVictoire(Joueur p) {
        long bornesPossedees =
            bornes.stream()
                  .filter(b -> b.getProprietaire() == p)
                  .count();
        if (bornesPossedees >= 5) return true;
        for (int i = 0; i < 7; i++) { 
            if (bornes.get(i).getProprietaire() == p &&
                bornes.get(i + 1).getProprietaire() == p &&
                bornes.get(i + 2).getProprietaire() == p) {
                return true;
            }
        }
        return false;
    }
}
