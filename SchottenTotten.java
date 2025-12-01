import java.util.*;

/**
 * PROJET SCHOTTEN-TOTTEN (Version Française - Prototype Monolithique)
 * Copiez ce code dans un fichier nommé : SchottenTotten.java
 * * Ce code respecte la nomenclature du diagramme de classes "français".
 */
public class SchottenTotten {
    public static void main(String[] args) {
        // Lancement via la Factory (JeuFactory)
        ControleurJeu jeu = JeuFactory.creerJeu("base");
        jeu.lancerPartie();
    }
}

// ===================== PACKAGE MODEL (com.schottenTotten.model) =====================

enum Couleur {
    ROUGE, VERT, BLEU, JAUNE, MARRON, VIOLET;
    
    @Override
    public String toString() {
        // Affiche "Rouge", "Vert"...
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}

class Carte {
    private final Couleur couleur;
    private final int valeur;

    public Carte(Couleur couleur, int valeur) {
        this.couleur = couleur;
        this.valeur = valeur;
    }

    public Couleur getCouleur() { return couleur; }
    public int getValeur() { return valeur; }

    @Override
    public String toString() {
        // Exemple : R9 (Rouge 9)
        return couleur.name().substring(0, 1) + valeur;
    }
}

class Joueur {
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

class Borne {
    private final int id;
    private final List<Carte> coteJoueur1;
    private final List<Carte> coteJoueur2;
    private Joueur proprietaire;

    public Borne(int id) {
        this.id = id;
        this.coteJoueur1 = new ArrayList<>();
        this.coteJoueur2 = new ArrayList<>();
        this.proprietaire = null;
    }

    public boolean ajouterCarte(Joueur joueur, Carte carte, boolean estJoueur1) {
        if (proprietaire != null) return false; // Borne déjà conquise
        
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

// ===================== PACKAGE VIEW (com.schottenTotten.view) =====================

class VueConsole {
    // Codes ANSI pour la couleur dans le terminal
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";

    private Scanner scanner;

    public VueConsole() {
        this.scanner = new Scanner(System.in);
    }

    // Colorisation des cartes pour l'affichage
    private String coloriser(Carte carte) {
        String code = RESET;
        switch (carte.getCouleur()) {
            case ROUGE: code = RED; break;
            case VERT: code = GREEN; break;
            case BLEU: code = BLUE; break;
            case JAUNE: code = YELLOW; break;
            case MARRON: code = CYAN; break; // Cyan pour Marron
            case VIOLET: code = PURPLE; break;
        }
        return code + "[" + carte.toString() + "]" + RESET;
    }

    public void afficherPlateau(List<Borne> bornes, Joueur j1, Joueur j2) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                   PLATEAU DE JEU SCHOTTEN-TOTTEN");
        System.out.println("=".repeat(70));
        System.out.println(String.format("%-30s | %s | %30s", j1.getNom(), "BORNES", j2.getNom()));
        System.out.println("-".repeat(70));

        for (Borne borne : bornes) {
            StringBuilder sb = new StringBuilder();
            
            // Côté J1 (Gauche)
            sb.append(String.format("%-30s", formaterCote(borne.getCoteJoueur1())));
            
            // Borne centrale
            String symbole;
            if (borne.getProprietaire() == j1) symbole = GREEN + "< J1 " + RESET;
            else if (borne.getProprietaire() == j2) symbole = RED + " J2 >" + RESET;
            else symbole = YELLOW + "( " + (borne.getId() + 1) + " )" + RESET;
            
            sb.append(" | ").append(symbole).append(" | ");

            // Côté J2 (Droite)
            sb.append(String.format("%30s", formaterCote(borne.getCoteJoueur2())));
            
            System.out.println(sb.toString());
        }
        System.out.println("=".repeat(70));
    }

    private String formaterCote(List<Carte> cartes) {
        StringBuilder sb = new StringBuilder();
        for (Carte c : cartes) {
            sb.append(coloriser(c)).append(" ");
        }
        return sb.toString();
    }

    public void afficherMain(Joueur p) {
        System.out.println("\nMain de " + p.getNom() + " :");
        for (int i = 0; i < p.getMain().size(); i++) {
            // Note: On utilise get(i)
            System.out.print(i + ":" + coloriser(p.getMain().get(i)) + "  ");
        }
        System.out.println();
    }

    public void afficherMessage(String msg) {
        System.out.println(">> " + msg);
    }

    public int demanderEntier(String message, int min, int max) {
        int input = -1;
        while (true) {
            System.out.print(message + " (" + min + "-" + max + ") : ");
            try {
                String line = scanner.nextLine();
                input = Integer.parseInt(line);
                if (input >= min && input <= max) break;
            } catch (NumberFormatException e) {
                // ignore
            }
            System.out.println("Entrée invalide.");
        }
        return input;
    }
}

// ===================== PACKAGE AI (com.schottenTotten.ai) =====================

interface StrategieIA {
    int[] jouerTour(Joueur ia, List<Borne> bornes);
}

class IAAleatoire implements StrategieIA {
    private Random rand = new Random();

    @Override
    public int[] jouerTour(Joueur ia, List<Borne> bornes) {
        if (ia.getMain().isEmpty()) return new int[]{-1, -1};

        // Essai simple : carte aléatoire sur borne aléatoire
        for (int i = 0; i < 50; i++) {
            int indexCarte = rand.nextInt(ia.getMain().size());
            int indexBorne = rand.nextInt(bornes.size());
            return new int[]{indexCarte, indexBorne};
        }
        return new int[]{0, 0}; 
    }
}

// ===================== PACKAGE CONTROLLER (com.schottenTotten.controller) =====================

class JeuFactory {
    public static ControleurJeu creerJeu(String variante) {
        // Ici, on pourrait utiliser un 'switch' si on avait plusieurs variantes
        return new ControleurJeu();
    }
}

class ControleurJeu {
    private List<Borne> bornes;
    private List<Carte> pioche;
    private Joueur j1;
    private Joueur j2;
    private VueConsole vue;
    private StrategieIA strategieIA;

    public ControleurJeu() {
        this.vue = new VueConsole();
        this.bornes = new ArrayList<>();
        for (int i = 0; i < 9; i++) bornes.add(new Borne(i));
        this.pioche = creerPioche();
        this.strategieIA = new IAAleatoire();
    }

    private List<Carte> creerPioche() {
        List<Carte> nouvellePioche = new ArrayList<>();
        for (Couleur c : Couleur.values()) {
            for (int i = 1; i <= 9; i++) nouvellePioche.add(new Carte(c, i));
        }
        Collections.shuffle(nouvellePioche);
        return nouvellePioche;
    }

    public void lancerPartie() {
        vue.afficherMessage("Bienvenue à Schotten-Totten !");
        
        // Configuration Joueurs
        j1 = new Joueur("Joueur 1 (Humain)", false);
        int mode = vue.demanderEntier("1. vs IA\n2. vs Humain (Hotseat)\nChoix", 1, 2);
        j2 = new Joueur(mode == 1 ? "Ordinateur" : "Joueur 2", mode == 1);

        // Distribution initiale (6 cartes chacun)
        for (int i = 0; i < 6; i++) {
            j1.piocherCarte(tirerCarte());
            j2.piocherCarte(tirerCarte());
        }

        boolean enCours = true;
        Joueur courant = j1;

        while (enCours) {
            vue.afficherPlateau(bornes, j1, j2);
            
            // Tour de jeu
            jouerTour(courant);

            // Vérification des bornes
            verifierRevendications();

            // Vérification Victoire
            if (verifierVictoire(courant)) {
                vue.afficherPlateau(bornes, j1, j2);
                vue.afficherMessage("VICTOIRE DE " + courant.getNom().toUpperCase() + " !!!");
                enCours = false;
            } else if (pioche.isEmpty() && j1.getMain().isEmpty() && j2.getMain().isEmpty()) {
                vue.afficherMessage("Plus de cartes ! Match nul.");
                enCours = false;
            }

            // Changement de joueur
            courant = (courant == j1) ? j2 : j1;
        }
    }

    private Carte tirerCarte() {
        return pioche.isEmpty() ? null : pioche.remove(0);
    }

    private void jouerTour(Joueur p) {
        vue.afficherMessage(">>> Tour de " + p.getNom());
        boolean valide = false;
        
        while (!valide) {
            int idxCarte, idxBorne;

            if (p.estIA()) {
                // IA très simple : boucle jusqu'à trouver un coup légal
                int essais = 0;
                do {
                    int[] coup = strategieIA.jouerTour(p, bornes);
                    idxCarte = coup[0];
                    idxBorne = coup[1];
                    essais++;
                } while (!coupValide(p, idxBorne) && essais < 100);
                
                if (essais >= 100) {
                    vue.afficherMessage("L'IA ne peut plus jouer.");
                    return; 
                }
                vue.afficherMessage("L'IA joue la carte " + idxCarte + " sur la borne " + (idxBorne + 1));
            } else {
                vue.afficherMain(p);
                idxCarte = vue.demanderEntier("Carte à jouer (index)", 0, p.getMain().size() - 1);
                idxBorne = vue.demanderEntier("Sur quelle borne (1-9)", 1, 9) - 1;
            }

            if (coupValide(p, idxBorne)) {
                Carte c = p.jouerCarte(idxCarte);
                bornes.get(idxBorne).ajouterCarte(p, c, p == j1);
                
                Carte nouvelleCarte = tirerCarte();
                if (nouvelleCarte != null) {
                    p.piocherCarte(nouvelleCarte);
                    if (!p.estIA()) vue.afficherMessage("Vous avez pioché : " + nouvelleCarte);
                }
                valide = true;
            } else {
                if (!p.estIA()) vue.afficherMessage("Impossible de jouer ici (borne pleine ou conquise).");
            }
        }
    }

    private boolean coupValide(Joueur p, int idxBorne) {
        if (idxBorne < 0 || idxBorne >= 9) return false;
        Borne b = bornes.get(idxBorne);
        if (b.getProprietaire() != null) return false;
        
        List<Carte> cote = (p == j1) ? b.getCoteJoueur1() : b.getCoteJoueur2();
        return cote.size() < 3;
    }

    private void verifierRevendications() {
        for (Borne b : bornes) {
            // Simplification : revendication automatique quand les 2 côtés sont pleins
            if (b.getProprietaire() == null && b.estPleine()) {
                int score1 = calculerScore(b.getCoteJoueur1());
                int score2 = calculerScore(b.getCoteJoueur2());
                
                if (score1 > score2) {
                    b.setProprietaire(j1);
                    vue.afficherMessage("Borne " + (b.getId()+1) + " gagnée par " + j1.getNom());
                } else if (score2 > score1) {
                    b.setProprietaire(j2);
                    vue.afficherMessage("Borne " + (b.getId()+1) + " gagnée par " + j2.getNom());
                } else {
                    // En cas d'égalité, premier arrivé premier servi (non géré ici pour simplicité)
                    vue.afficherMessage("Egalité sur la borne " + (b.getId()+1));
                }
            }
        }
    }

    // Calcul de force : Type * 1000 + Somme
    // 5=Suite-Couleur, 4=Brelan, 3=Couleur, 2=Suite, 1=Somme
    private int calculerScore(List<Carte> main) {
        List<Carte> triee = new ArrayList<>(main);
        triee.sort(Comparator.comparingInt(Carte::getValeur));

        boolean memeCouleur = (triee.get(0).getCouleur() == triee.get(1).getCouleur() && triee.get(1).getCouleur() == triee.get(2).getCouleur());
        boolean suite = (triee.get(0).getValeur() + 1 == triee.get(1).getValeur() && triee.get(1).getValeur() + 1 == triee.get(2).getValeur());
        boolean memeValeur = (triee.get(0).getValeur() == triee.get(1).getValeur() && triee.get(1).getValeur() == triee.get(2).getValeur());
        int somme = triee.stream().mapToInt(Carte::getValeur).sum();

        int type = 1;
        if (memeCouleur && suite) type = 5;      // Suite-Couleur
        else if (memeValeur) type = 4;           // Brelan
        else if (memeCouleur) type = 3;          // Couleur
        else if (suite) type = 2;                // Suite

        return type * 1000 + somme;
    }

    private boolean verifierVictoire(Joueur p) {
        // Condition 1 : 5 bornes au total
        long compte = bornes.stream().filter(b -> b.getProprietaire() == p).count();
        if (compte >= 5) return true;
        
        // Condition 2 : 3 bornes adjacentes
        for (int i = 0; i < 7; i++) {
            if (bornes.get(i).getProprietaire() == p && 
                bornes.get(i+1).getProprietaire() == p && 
                bornes.get(i+2).getProprietaire() == p)
                return true;
        }
        return false;
    }
}