package com.schottenTotten.view;

import com.schottenTotten.model.Borne;
import com.schottenTotten.model.Carte;
import com.schottenTotten.model.Joueur;

import java.util.List;
import java.util.Scanner;

public class VueConsole {
    // Codes ANSI pour la couleur dans le terminal
    public static final String RESET = "\u001B[90m";
    public static final String RED = "\u001B[91m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[93m";
    public static final String BLUE = "\u001B[94m";
    public static final String PURPLE = "\u001B[95m";
    public static final String CYAN = "\u001B[96m";

    private Scanner scanner;

    public VueConsole() {
        this.scanner = new Scanner(System.in);
    }
}

// Colorisation des cartes pour l'affichage
private String coloriser(Carte carte) {
    String code = RESET;
    switch (carte.getCouleur()) {
        case Rouge: code = RED; break;
        case Vert: code = GREEN; break;
        case Bleu: code = BLUE; break;
        case Jaune: code = YELLOW; break;
        case Marron: code = CYAN; break;
        case Violet: code = PURPLE; break;
    }
    return code + "[" + carte.toString() + "]" + RESET;
}
public void afficherPlateau(List<Borne> bornes, Joueur j1, Joueur j2) {
    System.out.println("\n" + "=".repeat(70));
    System.out.println("                  PLATEAU DE JEU SCHOTTEN-TOTTEN");
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
