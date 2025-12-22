package com.schottenTotten.controller;

import com.schottenTotten.model.*;
import com.schottenTotten.view.VueConsole;
import com.schottenTotten.ai.StrategieIA;
import com.schottenTotten.ai.IAAleatoire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ControleurJeu {
    private List<Borne> bornes;
    private List<Carte> piocheClan;     // Ancienne pioche
    private List<Carte> piocheTactique; // Nouvelle pioche
    private Joueur j1;
    private Joueur j2;
    private VueConsole vue;
    private StrategieIA strategieIA;
    private boolean modeTactique;

    public ControleurJeu() {
        this.vue = new VueConsole();
        this.bornes = new ArrayList<>();
        // Création des 9 bornes
        for (int i = 0; i < 9; i++) {
            bornes.add(new Borne(i));
        }
        this.strategieIA = new IAAleatoire(); 
    }

    // Crée et mélange le paquet de 54 cartes (6 couleurs et 9 valeurs).
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

    // Crée le paquet de cartes tactiques
    private List<Carte> creerPiocheTactique() {
        List<Carte> nouvellePioche = new ArrayList<>();
        for (TypeTactique t : TypeTactique.values()) {
            nouvellePioche.add(new CarteTactique(t));
        }
        Collections.shuffle(nouvellePioche);
        return nouvellePioche;
    }

    // Méthode principale qui lance la boucle de jeu.
    public void lancerPartie() {
        vue.afficherMessage("Bienvenue dans Schotten-Totten !");

        // Choix du mode
        int choixMode = vue.demanderEntier("Mode de jeu :\n1. Classique (6 cartes)\n2. Tactique (7 cartes, cartes spéciales)\nVotre choix", 1, 2);
        this.modeTactique = (choixMode == 2);

        j1 = new Joueur("Joueur 1 (Humain)", false);
        
        int typeAdv = vue.demanderEntier("1. vs IA\n2. vs Humain \nChoix", 1, 2);
        j2 = new Joueur(typeAdv == 1 ? "Ordinateur" : "Joueur 2", typeAdv == 1);

        // Initialisation des pioches
        this.piocheClan = creerPiocheClan();
        if (modeTactique) {
            this.piocheTactique = creerPiocheTactique();
        } else {
            this.piocheTactique = new ArrayList<>();
        }

        // Main de départ
        int tailleMain = modeTactique ? 7 : 6;
        for (int i = 0; i < tailleMain; i++) {
            j1.piocherCarte(tirerCarteClan());
            j2.piocherCarte(tirerCarteClan());
        }

        boolean enCours = true;
        Joueur joueurCourant = j1;

        // Boucle de jeu
        while (enCours) {
            vue.afficherPlateau(bornes, j1, j2);

            // Le joueur joue son tour
            jouerTour(joueurCourant);

            // Vérification des bornes conquises
            verifierRevendications();

            // Vérification de la victoire finale
            if (verifierVictoire(joueurCourant)) {
                vue.afficherPlateau(bornes, j1, j2);
                vue.afficherMessage("VICTOIRE DE " + joueurCourant.getNom().toUpperCase() + " !!!");
                enCours = false;
            } 
            // Cas match nul (plus de cartes)
            else if (piocheClan.isEmpty() && piocheTactique.isEmpty() && j1.getMain().isEmpty() && j2.getMain().isEmpty()) {
                vue.afficherMessage("Plus de cartes ! Match nul.");
                enCours = false;
            }

            // Changement de joueur
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
                // Stratégie IA (ne joue pas les Ruses pour l'instant pour éviter les erreurs)
                int essais = 0;
                do {
                    int[] action = strategieIA.jouerTour(p, bornes);
                    idxCarte = action[0];
                    idxBorne = action[1];
                    essais++;
                } while (idxCarte != -1 && !estCoupValideIA(p, idxCarte, idxBorne) && essais < 200);
            } else {
                // Joueur Humain
                vue.afficherMain(p);
                idxCarte = vue.demanderEntier("Carte à jouer (index)", 0, p.getMain().size() - 1);
            }

            // Récupération de la carte choisie
            Carte carteChoisie = p.getMain().get(idxCarte);
            
            // --- GESTION DES TYPES DE CARTES ---
            
            // 1. Si c'est une RUSE (Action spéciale)
            if (carteChoisie.estTactique() && ((CarteTactique)carteChoisie).getType().getCategorie() == TypeTactique.Categorie.RUSE) {
                if (!p.estIA()) {
                    vue.afficherMessage("RUSE activée : " + carteChoisie);
                    tourFini = appliquerRuse(p, (CarteTactique) carteChoisie);
                    if (tourFini) p.jouerCarte(idxCarte); // La carte est consommée (défaussée)
                } else {
                    // L'IA défausse juste la ruse pour le moment
                    p.jouerCarte(idxCarte);
                    tourFini = true;
                }
            }
            // 2. Si c'est un MODE DE COMBAT (Sur une borne)
            else if (carteChoisie.estTactique() && ((CarteTactique)carteChoisie).getType().getCategorie() == TypeTactique.Categorie.MODE_COMBAT) {
                if (!p.estIA()) idxBorne = vue.demanderEntier("Sur quelle borne appliquer l'effet ?", 1, 9) - 1;
                
                Borne b = bornes.get(idxBorne);
                TypeTactique type = ((CarteTactique)carteChoisie).getType();
                
                if (type == TypeTactique.COLIN_MAILLARD) b.activerColinMaillard();
                else if (type == TypeTactique.COMBAT_DE_BOUE) b.activerCombatBoue();
                
                p.jouerCarte(idxCarte); 
                if (!p.estIA()) vue.afficherMessage("Mode " + type + " activé sur la borne " + (idxBorne+1));
                tourFini = true;
            }
            // 3. Sinon (Clan ou Troupe d'Elite) -> Se joue DANS la borne
            else {
                if (!p.estIA()) idxBorne = vue.demanderEntier("Sur quelle borne poser la carte ?", 1, 9) - 1;
                
                if (idxBorne >= 0 && idxBorne < 9 && verifierPlace(p, bornes.get(idxBorne))) {
                    Carte c = p.jouerCarte(idxCarte);
                    bornes.get(idxBorne).ajouterCarte(p, c, p == j1);
                    tourFini = true;
                } else {
                    if (!p.estIA()) vue.afficherMessage("Impossible de jouer ici (borne pleine ou conquise).");
                }
            }
        }

        // --- PHASE DE PIOCHE ---
        phaseDePioche(p);
    }

    /**
     * Applique l'effet spécifique d'une carte RUSE.
     * @return true si l'action a réussi, false si annulée/impossible
     */
    private boolean appliquerRuse(Joueur p, CarteTactique ruse) {
        switch (ruse.getType()) {
            case CHASSEUR_DE_TETE:
                // Piocher 3 cartes, puis en remettre 2 sous la pioche
                for (int i = 0; i < 3; i++) {
                    // Si les pioches sont vides, on arrête
                    if(piocheClan.isEmpty() && piocheTactique.isEmpty()) break; 
                    
                    vue.afficherMessage("Pioche n°" + (i+1) + "/3");
                    int choix = vue.demanderEntier("1. Clan  2. Tactique", 1, 2);
                    Carte c = (choix == 1 && !piocheClan.isEmpty()) ? tirerCarteClan() : tirerCarteTactique();
                    if (c != null) p.piocherCarte(c);
                }
                vue.afficherMain(p);
                vue.afficherMessage("Vous devez remettre 2 cartes sous la pioche.");
                for (int i = 0; i < 2; i++) {
                    if(p.getMain().isEmpty()) break;
                    int idx = vue.demanderEntier("Carte à rendre (index)", 0, p.getMain().size() - 1);
                    Carte rendue = p.jouerCarte(idx);
                    // On remet sous la pioche correspondante (au fond de la liste)
                    if (rendue.estTactique()) piocheTactique.add(rendue);
                    else piocheClan.add(rendue);
                }
                return true;

            case STRATEGE:
                // Déplacer une de ses cartes ou la défausser
                vue.afficherMessage("Choisissez une de vos cartes sur le plateau (Borne non conquise).");
                int bOrigine = vue.demanderEntier("Borne source (1-9)", 1, 9) - 1;
                Borne borneSrc = bornes.get(bOrigine);
                List<Carte> coteSrc = (p == j1) ? borneSrc.getCoteJoueur1() : borneSrc.getCoteJoueur2();
                
                if (borneSrc.getProprietaire() != null || coteSrc.isEmpty()) {
                    vue.afficherMessage("Aucune carte à déplacer ici.");
                    return false;
                }
                
                // Afficher les cartes de la borne pour choisir
                for(int i=0; i<coteSrc.size(); i++) System.out.println(i + ": " + coteSrc.get(i));
                int idxCarte = vue.demanderEntier("Quelle carte ?", 0, coteSrc.size() - 1);
                
                int action = vue.demanderEntier("1. Déplacer  2. Défausser", 1, 2);
                Carte cDeplacee = coteSrc.remove(idxCarte); // On l'enlève temporairement
                
                if (action == 1) { // Déplacer
                    int bDest = vue.demanderEntier("Vers quelle borne (1-9)", 1, 9) - 1;
                    if (verifierPlace(p, bornes.get(bDest))) {
                        bornes.get(bDest).ajouterCarte(p, cDeplacee, p == j1);
                        vue.afficherMessage("Carte déplacée !");
                        return true;
                    } else {
                        coteSrc.add(idxCarte, cDeplacee); // On annule si impossible
                        vue.afficherMessage("Pas de place sur la destination.");
                        return false;
                    }
                } else { // Défausser
                    vue.afficherMessage("Carte défaussée.");
                    return true;
                }

            case BANSHEE:
                // Défausser une carte adverse
                vue.afficherMessage("Choisissez une carte ADVERSE à éliminer.");
                int bAdv = vue.demanderEntier("Borne cible (1-9)", 1, 9) - 1;
                Borne borneAdv = bornes.get(bAdv);
                List<Carte> coteAdv = (p == j1) ? borneAdv.getCoteJoueur2() : borneAdv.getCoteJoueur1(); // Inverse
                
                if (borneAdv.getProprietaire() != null || coteAdv.isEmpty()) {
                    vue.afficherMessage("Rien à cibler ici.");
                    return false;
                }
                
                for(int i=0; i<coteAdv.size(); i++) System.out.println(i + ": " + coteAdv.get(i));
                int idxSuppr = vue.demanderEntier("Quelle carte détruire ?", 0, coteAdv.size() - 1);
                coteAdv.remove(idxSuppr);
                vue.afficherMessage("Carte adverse détruite !");
                return true;

            case TRAITRE:
                // Voler une carte adverse (Clan uniquement)
                vue.afficherMessage("Choisissez une carte CLAN adverse à voler.");
                int bVol = vue.demanderEntier("Borne source (1-9)", 1, 9) - 1;
                Borne borneVol = bornes.get(bVol);
                List<Carte> coteVol = (p == j1) ? borneVol.getCoteJoueur2() : borneVol.getCoteJoueur1();
                
                if (borneVol.getProprietaire() != null || coteVol.isEmpty()) return false;
                
                // Filtrer pour ne montrer que les cartes Clan
                // Simplification : on laisse choisir, et on vérifie après
                for(int i=0; i<coteVol.size(); i++) System.out.println(i + ": " + coteVol.get(i));
                int idxVol = vue.demanderEntier("Quelle carte voler ?", 0, coteVol.size() - 1);
                
                Carte cVol = coteVol.get(idxVol);
                if (cVol.estTactique()) {
                    vue.afficherMessage("Le Traître ne peut voler que des cartes Clan !");
                    return false;
                }
                
                int bMienne = vue.demanderEntier("Sur quelle borne la placer chez vous ?", 1, 9) - 1;
                if (verifierPlace(p, bornes.get(bMienne))) {
                    coteVol.remove(idxVol); // On l'enlève de chez l'ennemi
                    bornes.get(bMienne).ajouterCarte(p, cVol, p == j1); // On l'ajoute chez nous
                    vue.afficherMessage("Trahison réussie !");
                    return true;
                } else {
                    vue.afficherMessage("Pas de place chez vous.");
                    return false;
                }
                
            default:
                return false;
        }
    }
    
    // Empêche l'IA de jouer des coups interdits (Ruses) pour l'instant
    private boolean estCoupValideIA(Joueur p, int idxCarte, int idxBorne) {
        if (idxCarte < 0 || idxCarte >= p.getMain().size()) return false;
        Carte c = p.getMain().get(idxCarte);
        
        if (c.estTactique()) {
            TypeTactique.Categorie cat = ((CarteTactique)c).getType().getCategorie();
            if (cat == TypeTactique.Categorie.RUSE || cat == TypeTactique.Categorie.MODE_COMBAT) return false;
        }
        return (idxBorne >= 0 && idxBorne < 9 && verifierPlace(p, bornes.get(idxBorne)));
    }

    private void phaseDePioche(Joueur p) {
        int limiteMain = modeTactique ? 7 : 6;
        
        // Si le joueur a déjà sa main pleine (ex: après Chasseur de Tête), il ne pioche pas
        if (p.getMain().size() >= limiteMain) return;

        Carte nouvelleCarte = null;
        if (modeTactique && !piocheTactique.isEmpty() && !piocheClan.isEmpty()) {
            if (p.estIA()) {
                if (Math.random() > 0.9) nouvelleCarte = tirerCarteTactique();
                else nouvelleCarte = tirerCarteClan();
            } else {
                vue.afficherMessage("1. Pioche Clan  2. Pioche Tactique");
                int choix = vue.demanderEntier("Choix", 1, 2);
                nouvelleCarte = (choix == 1) ? tirerCarteClan() : tirerCarteTactique();
            }
        } else if (!piocheClan.isEmpty()) {
            nouvelleCarte = tirerCarteClan();
        } else if (modeTactique && !piocheTactique.isEmpty()) {
            nouvelleCarte = tirerCarteTactique();
        }

        if (nouvelleCarte != null) {
            p.piocherCarte(nouvelleCarte);
            if (!p.estIA()) vue.afficherMessage("Vous avez pioché : " + nouvelleCarte);
        }
    }
    
    private boolean verifierPlace(Joueur p, Borne b) {
        if (b.getProprietaire() != null) return false;
        List<Carte> cote = (p == j1) ? b.getCoteJoueur1() : b.getCoteJoueur2();
        return cote.size() < b.getCapaciteMax(); 
    }

    private void verifierRevendications() {
        for (Borne b : bornes) {
            if (b.getProprietaire() == null && b.estPleine()) {
                int score1 = calculerScoreOptimal(b.getCoteJoueur1(), b);
                int score2 = calculerScoreOptimal(b.getCoteJoueur2(), b);

                if (score1 > score2) {
                    b.setProprietaire(j1);
                    vue.afficherMessage("Borne " + (b.getId() + 1) + " gagnée par " + j1.getNom() + " (Score: " + score1 + ")");
                } else if (score2 > score1) {
                    b.setProprietaire(j2);
                    vue.afficherMessage("Borne " + (b.getId() + 1) + " gagnée par " + j2.getNom() + " (Score: " + score2 + ")");
                } else {
                    vue.afficherMessage("Egalité sur la borne " + (b.getId() + 1));
                }
            }
        }
    }

    // Calcul intelligent adapté aux règles de la borne
    private int calculerScoreOptimal(List<Carte> main, Borne b) {
        if (b.isColinMaillard()) {
            return calculerSomme(main); 
        }

        int indexTactique = -1;
        for (int i = 0; i < main.size(); i++) {
            if (main.get(i).estTactique()) {
                indexTactique = i;
                break;
            }
        }

        if (indexTactique == -1) {
            return calculerScoreClassique(main);
        }

        CarteTactique tactique = (CarteTactique) main.get(indexTactique);
        int meilleurScore = 0;
        List<Carte> options = genererOptionsPour(tactique);

        for (Carte option : options) {
            List<Carte> mainTest = new ArrayList<>(main);
            mainTest.set(indexTactique, option);
            int score = calculerScoreOptimal(mainTest, b);
            if (score > meilleurScore) meilleurScore = score;
        }
        return meilleurScore;
    }
    
    private int calculerSomme(List<Carte> main) {
        int somme = 0;
        for(Carte c : main) {
            if (c.getValeur() > 0) somme += c.getValeur();
            else if (c.estTactique()) somme += 9; 
        }
        return somme;
    }

    private List<Carte> genererOptionsPour(CarteTactique t) {
        List<Carte> options = new ArrayList<>();
        if (t.getType() == TypeTactique.JOKER) {
            for (Couleur c : Couleur.values()) for (int v = 1; v <= 9; v++) options.add(new CarteClan(c, v));
        } else if (t.getType() == TypeTactique.ESPION) {
            for (Couleur c : Couleur.values()) options.add(new CarteClan(c, 7));
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
            if (c.getCouleur() == null || c.getCouleur() != ref) memeCouleur = false;
        }

        boolean suite = true;
        for(int i=0; i < triee.size()-1; i++) {
            if(triee.get(i).getValeur() + 1 != triee.get(i+1).getValeur()) suite = false;
        }
        
        boolean memeValeur = true;
        int valRef = triee.get(0).getValeur();
        for(Carte c : triee) {
            if(c.getValeur() != valRef) memeValeur = false;
        }
        
        int somme = triee.stream().mapToInt(Carte::getValeur).sum();

        if (memeCouleur && suite) return 5000 + somme;
        if (memeValeur) return 4000 + somme;
        if (memeCouleur) return 3000 + somme;
        if (suite) return 2000 + somme;
        return 1000 + somme;
    }

    private boolean verifierVictoire(Joueur p) {
        long bornesPossedees = bornes.stream().filter(b -> b.getProprietaire() == p).count();
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