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
    private List<Carte> piocheClan;     // Ancienne pioche
    private List<Carte> piocheTactique; // Nouvelle pioche
    private Joueur j1;
    private Joueur j2;
    private VueConsole vue;
    private StrategieIA strategieIA;
    private boolean modeTactique;
    private boolean modeExpert; // NOUVEAU : Active la variante revendication début de tour
    private Random random; 

    public ControleurJeu() {
        this.vue = new VueConsole();
        this.bornes = new ArrayList<>();
        // Création des 9 bornes
        for (int i = 0; i < 9; i++) {
            bornes.add(new Borne(i));
        }
        this.strategieIA = new IAAleatoire(); 
        this.random = new Random();
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

        // Choix du mode de jeu
        int choixMode = vue.demanderEntier("Mode de jeu :\n1. Classique (6 cartes)\n2. Tactique (7 cartes, cartes spéciales)\nVotre choix", 1, 2);
        this.modeTactique = (choixMode == 2);
        
        // Choix de la variante Expert
        int choixExpert = vue.demanderEntier("Variante Experts (Revendication au début du tour) ?\n1. Oui\n2. Non", 1, 2);
        this.modeExpert = (choixExpert == 1);

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

            // --- VARIANTE EXPERT : Revendication au DEBUT du tour ---
            if (modeExpert) {
                if (!joueurCourant.estIA()) vue.afficherMessage("Début du tour : Vérification des revendications...");
                boolean changement = verifierRevendications(joueurCourant);
                if (changement) vue.afficherPlateau(bornes, j1, j2); // Réafficher si une borne a été prise
                
                // Si on gagne au début du tour, on arrête tout de suite
                if (verifierVictoire(joueurCourant)) {
                    vue.afficherMessage("VICTOIRE DE " + joueurCourant.getNom().toUpperCase() + " !!!");
                    break;
                }
            }

            // Le joueur joue son tour
            jouerTour(joueurCourant);

            // --- MODE NORMAL : Revendication à la FIN du tour ---
            if (!modeExpert) {
                verifierRevendications(joueurCourant);
                if (verifierVictoire(joueurCourant)) {
                    vue.afficherPlateau(bornes, j1, j2);
                    vue.afficherMessage("VICTOIRE DE " + joueurCourant.getNom().toUpperCase() + " !!!");
                    enCours = false;
                    break;
                }
            }

            // Cas match nul (plus de cartes)
            if (piocheClan.isEmpty() && piocheTactique.isEmpty() && j1.getMain().isEmpty() && j2.getMain().isEmpty()) {
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
                // Stratégie IA (Corrigée pour éviter le crash Index -1)
                int essais = 0;
                do {
                    int[] action = strategieIA.jouerTour(p, bornes);
                    idxCarte = action[0];
                    idxBorne = action[1];
                    essais++;
                    // CORRECTION : On continue si l'index est -1 OU si le coup est invalide
                } while ((idxCarte == -1 || !estCoupValideIA(p, idxCarte, idxBorne)) && essais < 200);

                // SECURITE : Si après 200 essais l'IA n'a rien trouvé, on force le premier coup valide
                if (idxCarte == -1 || !estCoupValideIA(p, idxCarte, idxBorne)) {
                    boolean sauve = false;
                    for (int c = 0; c < p.getMain().size(); c++) {
                        for (int b = 0; b < 9; b++) {
                            if (estCoupValideIA(p, c, b)) {
                                idxCarte = c;
                                idxBorne = b;
                                sauve = true;
                                break;
                            }
                        }
                        if (sauve) break;
                    }
                    if (!sauve) {
                        vue.afficherMessage("L'IA ne peut plus jouer et passe son tour (Bug ou Blocage total).");
                        return; // Evite le crash
                    }
                }
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
                tourFini = appliquerRuse(p, (CarteTactique) carteChoisie);
                if (tourFini) {
                     p.jouerCarte(idxCarte); 
                     if(p.estIA()) vue.afficherMessage("L'IA a joué une Ruse : " + carteChoisie);
                }
            }
            // 2. Si c'est un MODE DE COMBAT
            else if (carteChoisie.estTactique() && ((CarteTactique)carteChoisie).getType().getCategorie() == TypeTactique.Categorie.MODE_COMBAT) {
                if (p.estIA()) {
                    List<Integer> bornesDispo = new ArrayList<>();
                    for(Borne b : bornes) if(b.getProprietaire() == null) bornesDispo.add(b.getId());
                    if (!bornesDispo.isEmpty()) idxBorne = bornesDispo.get(random.nextInt(bornesDispo.size()));
                    else idxBorne = -1;
                } else {
                    idxBorne = vue.demanderEntier("Sur quelle borne appliquer l'effet ?", 1, 9) - 1;
                }
                
                if (idxBorne != -1) {
                    Borne b = bornes.get(idxBorne);
                    TypeTactique type = ((CarteTactique)carteChoisie).getType();
                    if (type == TypeTactique.COLIN_MAILLARD) b.activerColinMaillard();
                    else if (type == TypeTactique.COMBAT_DE_BOUE) b.activerCombatBoue();
                    p.jouerCarte(idxCarte); 
                    if (!p.estIA()) vue.afficherMessage("Mode " + type + " activé sur la borne " + (idxBorne+1));
                    tourFini = true;
                }
            }
            // 3. Sinon (Clan ou Troupe d'Elite)
            else {
                if (!p.estIA()) idxBorne = vue.demanderEntier("Sur quelle borne poser la carte ?", 1, 9) - 1;
                
                if (idxBorne >= 0 && idxBorne < 9 && verifierPlace(p, bornes.get(idxBorne))) {
                    Carte c = p.jouerCarte(idxCarte);
                    bornes.get(idxBorne).ajouterCarte(p, c, p == j1);
                    if (p.estIA()) vue.afficherMessage("L'IA joue sur la borne " + (idxBorne+1));
                    tourFini = true;
                } else {
                    if (!p.estIA()) vue.afficherMessage("Impossible de jouer ici (borne pleine ou conquise).");
                }
            }
        }
        phaseDePioche(p);
    }

    private boolean appliquerRuse(Joueur p, CarteTactique ruse) {
        switch (ruse.getType()) {
            case CHASSEUR_DE_TETE:
                for (int i = 0; i < 3; i++) {
                    if(piocheClan.isEmpty() && piocheTactique.isEmpty()) break; 
                    Carte c = null;
                    if (p.estIA()) {
                         if (!piocheTactique.isEmpty() && random.nextBoolean()) c = tirerCarteTactique();
                         else c = tirerCarteClan();
                    } else {
                        vue.afficherMessage("Pioche n°" + (i+1) + "/3");
                        int choix = vue.demanderEntier("1. Clan  2. Tactique", 1, 2);
                        c = (choix == 1 && !piocheClan.isEmpty()) ? tirerCarteClan() : tirerCarteTactique();
                    }
                    if (c != null) p.piocherCarte(c);
                }
                if (!p.estIA()) {
                    vue.afficherMain(p);
                    vue.afficherMessage("Vous devez remettre 2 cartes sous la pioche.");
                }
                for (int i = 0; i < 2; i++) {
                    if(p.getMain().isEmpty()) break;
                    int idx = p.estIA() ? random.nextInt(p.getMain().size()) : vue.demanderEntier("Carte à rendre (index)", 0, p.getMain().size() - 1);
                    Carte rendue = p.jouerCarte(idx);
                    if (rendue.estTactique()) piocheTactique.add(rendue);
                    else piocheClan.add(rendue);
                }
                return true;

            case STRATEGE:
                Borne borneSrc = null;
                int idxCarteSrc = -1;
                if (p.estIA()) {
                    List<Borne> mesBornes = new ArrayList<>();
                    for(Borne b : bornes) {
                        List<Carte> cote = (p == j1) ? b.getCoteJoueur1() : b.getCoteJoueur2();
                        if(b.getProprietaire() == null && !cote.isEmpty()) mesBornes.add(b);
                    }
                    if(mesBornes.isEmpty()) return false;
                    borneSrc = mesBornes.get(random.nextInt(mesBornes.size()));
                    List<Carte> coteSrc = (p == j1) ? borneSrc.getCoteJoueur1() : borneSrc.getCoteJoueur2();
                    idxCarteSrc = random.nextInt(coteSrc.size());
                } else {
                    vue.afficherMessage("Choisissez une de vos cartes sur le plateau.");
                    int bOrigine = vue.demanderEntier("Borne source (1-9)", 1, 9) - 1;
                    borneSrc = bornes.get(bOrigine);
                    List<Carte> coteSrc = (p == j1) ? borneSrc.getCoteJoueur1() : borneSrc.getCoteJoueur2();
                    if (borneSrc.getProprietaire() != null || coteSrc.isEmpty()) return false;
                    for(int i=0; i<coteSrc.size(); i++) System.out.println(i + ": " + coteSrc.get(i));
                    idxCarteSrc = vue.demanderEntier("Quelle carte ?", 0, coteSrc.size() - 1);
                }
                
                List<Carte> coteSrc = (p == j1) ? borneSrc.getCoteJoueur1() : borneSrc.getCoteJoueur2();
                Carte cDeplacee = coteSrc.remove(idxCarteSrc); 
                
                boolean deplacer = p.estIA() ? random.nextBoolean() : (vue.demanderEntier("1. Déplacer  2. Défausser", 1, 2) == 1);
                if (deplacer) { 
                    if (p.estIA()) {
                         boolean placeTrouvee = false;
                         for(int k=0; k<10; k++){ 
                             Borne bDest = bornes.get(random.nextInt(9));
                             if (verifierPlace(p, bDest)) {
                                 bDest.ajouterCarte(p, cDeplacee, p == j1);
                                 placeTrouvee = true; break;
                             }
                         }
                         if(!placeTrouvee) return true; 
                    } else {
                        int bDest = vue.demanderEntier("Vers quelle borne (1-9)", 1, 9) - 1;
                        if (verifierPlace(p, bornes.get(bDest))) {
                            bornes.get(bDest).ajouterCarte(p, cDeplacee, p == j1);
                            vue.afficherMessage("Carte déplacée !");
                            return true;
                        } else {
                            coteSrc.add(idxCarteSrc, cDeplacee); 
                            vue.afficherMessage("Pas de place.");
                            return false;
                        }
                    }
                } else if (!p.estIA()) vue.afficherMessage("Carte défaussée.");
                return true;

            case BANSHEE:
                Borne borneAdv = null;
                List<Carte> coteAdv = null;
                if (p.estIA()) {
                    List<Borne> bornesEnnemies = new ArrayList<>();
                    for(Borne b : bornes) {
                        List<Carte> adv = (p == j1) ? b.getCoteJoueur2() : b.getCoteJoueur1();
                        if(b.getProprietaire() == null && !adv.isEmpty()) bornesEnnemies.add(b);
                    }
                    if(bornesEnnemies.isEmpty()) return false;
                    borneAdv = bornesEnnemies.get(random.nextInt(bornesEnnemies.size()));
                    coteAdv = (p == j1) ? borneAdv.getCoteJoueur2() : borneAdv.getCoteJoueur1();
                    coteAdv.remove(random.nextInt(coteAdv.size())); 
                } else {
                    vue.afficherMessage("Choisissez une carte ADVERSE à éliminer.");
                    int bAdv = vue.demanderEntier("Borne cible (1-9)", 1, 9) - 1;
                    borneAdv = bornes.get(bAdv);
                    coteAdv = (p == j1) ? borneAdv.getCoteJoueur2() : borneAdv.getCoteJoueur1(); 
                    if (borneAdv.getProprietaire() != null || coteAdv.isEmpty()) return false;
                    for(int i=0; i<coteAdv.size(); i++) System.out.println(i + ": " + coteAdv.get(i));
                    int idxSuppr = vue.demanderEntier("Quelle carte détruire ?", 0, coteAdv.size() - 1);
                    coteAdv.remove(idxSuppr);
                    vue.afficherMessage("Carte adverse détruite !");
                }
                return true;

            case TRAITRE:
                Borne bVol = null;
                List<Carte> cVolList = null;
                int idxVol = -1;
                if (p.estIA()) {
                    List<Borne> cibles = new ArrayList<>();
                    for(Borne b : bornes) {
                         List<Carte> adv = (p == j1) ? b.getCoteJoueur2() : b.getCoteJoueur1();
                         boolean aClan = adv.stream().anyMatch(c -> !c.estTactique());
                         if(b.getProprietaire() == null && aClan) cibles.add(b);
                    }
                    if(cibles.isEmpty()) return false;
                    bVol = cibles.get(random.nextInt(cibles.size()));
                    cVolList = (p == j1) ? bVol.getCoteJoueur2() : bVol.getCoteJoueur1();
                    for(int i=0; i<cVolList.size(); i++) {
                        if(!cVolList.get(i).estTactique()) { idxVol = i; break; }
                    }
                } else {
                    vue.afficherMessage("Choisissez une carte CLAN adverse à voler.");
                    int bVolNum = vue.demanderEntier("Borne source (1-9)", 1, 9) - 1;
                    bVol = bornes.get(bVolNum);
                    cVolList = (p == j1) ? bVol.getCoteJoueur2() : bVol.getCoteJoueur1();
                    if (bVol.getProprietaire() != null || cVolList.isEmpty()) return false;
                    for(int i=0; i<cVolList.size(); i++) System.out.println(i + ": " + cVolList.get(i));
                    idxVol = vue.demanderEntier("Quelle carte voler ?", 0, cVolList.size() - 1);
                }
                
                if (idxVol == -1) return false;
                Carte cVol = cVolList.get(idxVol);
                if (cVol.estTactique()) {
                    if(!p.estIA()) vue.afficherMessage("Le Traître ne peut voler que des cartes Clan !");
                    return false;
                }
                
                if (p.estIA()) {
                    for(Borne b : bornes) {
                        if(verifierPlace(p, b)) {
                            cVolList.remove(idxVol);
                            b.ajouterCarte(p, cVol, p == j1);
                            return true;
                        }
                    }
                    return false; 
                } else {
                    int bMienne = vue.demanderEntier("Sur quelle borne la placer chez vous ?", 1, 9) - 1;
                    if (verifierPlace(p, bornes.get(bMienne))) {
                        cVolList.remove(idxVol); 
                        bornes.get(bMienne).ajouterCarte(p, cVol, p == j1); 
                        vue.afficherMessage("Trahison réussie !");
                        return true;
                    } else {
                        vue.afficherMessage("Pas de place.");
                        return false;
                    }
                }
            default: return false;
        }
    }
    
    // Modification pour autoriser l'IA à jouer Ruse et Combat
    private boolean estCoupValideIA(Joueur p, int idxCarte, int idxBorne) {
        if (idxCarte < 0 || idxCarte >= p.getMain().size()) return false;
        Carte c = p.getMain().get(idxCarte);
        
        // Si c'est une RUSE ou un MODE COMBAT, le coup est valide (géré par appliquerRuse)
        if (c.estTactique()) {
            TypeTactique.Categorie cat = ((CarteTactique)c).getType().getCategorie();
            if (cat == TypeTactique.Categorie.RUSE || cat == TypeTactique.Categorie.MODE_COMBAT) return true;
        }
        return (idxBorne >= 0 && idxBorne < 9 && verifierPlace(p, bornes.get(idxBorne)));
    }

    private void phaseDePioche(Joueur p) {
        int limiteMain = modeTactique ? 7 : 6;
        if (p.getMain().size() >= limiteMain) return;

        Carte nouvelleCarte = null;
        if (modeTactique && !piocheTactique.isEmpty() && !piocheClan.isEmpty()) {
            if (p.estIA()) {
                // IA : 50% de chance de piocher Tactique pour tester
                if (random.nextDouble() > 0.5) nouvelleCarte = tirerCarteTactique();
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

    // Modification : prend le joueurCourant en paramètre pour gérer la règle Expert
    private boolean verifierRevendications(Joueur joueurCourant) {
        boolean changement = false;
        for (Borne b : bornes) {
            if (b.getProprietaire() == null && b.estPleine()) {
                int score1 = calculerScoreOptimal(b.getCoteJoueur1(), b);
                int score2 = calculerScoreOptimal(b.getCoteJoueur2(), b);

                Joueur gagnantPotentiel = null;
                if (score1 > score2) gagnantPotentiel = j1;
                else if (score2 > score1) gagnantPotentiel = j2;

                if (gagnantPotentiel != null) {
                    // EN MODE EXPERT : On ne revendique que si c'est NOTRE tour
                    if (modeExpert) {
                        if (gagnantPotentiel == joueurCourant) {
                            b.setProprietaire(gagnantPotentiel);
                            vue.afficherMessage("Borne " + (b.getId() + 1) + " REVENDIQUÉE par " + gagnantPotentiel.getNom() + " !");
                            changement = true;
                        }
                    } 
                    // EN MODE NORMAL : On revendique dès que c'est possible
                    else {
                        b.setProprietaire(gagnantPotentiel);
                        vue.afficherMessage("Borne " + (b.getId() + 1) + " gagnée par " + gagnantPotentiel.getNom());
                        changement = true;
                    }
                } else {
                    // Egalité : personne ne prend la borne
                }
            }
        }
        return changement;
    }

    private int calculerScoreOptimal(List<Carte> main, Borne b) {
        if (b.isColinMaillard()) return calculerSomme(main); 

        int indexTactique = -1;
        for (int i = 0; i < main.size(); i++) {
            if (main.get(i).estTactique()) { indexTactique = i; break; }
        }

        if (indexTactique == -1) return calculerScoreClassique(main);

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
        } else options.add(new CarteClan(Couleur.Rouge, 0));
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
