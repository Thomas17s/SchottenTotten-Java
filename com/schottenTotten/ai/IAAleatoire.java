package com.schottenTotten.ai;

import com.schottenTotten.model.Borne;
import com.schottenTotten.model.Joueur;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// IA basique qui joue une carte au hasard sur une borne non conquise au hasard.

public class IAAleatoire implements StrategieIA {
    private Random rand = new Random();

    @Override
    public int[] jouerTour(Joueur ia, List<Borne> bornes) {
        // Identifier les bornes jouables (celles qui n'ont pas de propriétaire)
        List<Integer> bornesLibres = new ArrayList<>();
        
        for (Borne b : bornes) {
            // Si la borne n'est pas encore gagnée, on peut jouer dessus
            if (b.getProprietaire() == null) {
                bornesLibres.add(b.getId());
            }
        }

        // Cas de sécurité : Si toutes les bornes sont prises (fin de partie)
        if (bornesLibres.isEmpty() || ia.getMain().isEmpty()) {
            return new int[]{-1, -1};
        }

        // Choisir une borne au hasard parmi celles disponibles
        int indexBorne = bornesLibres.get(rand.nextInt(bornesLibres.size()));
        
        // Choisir une carte au hasard dans la main
        int indexCarte = rand.nextInt(ia.getMain().size());

        // On renvoie les coordonnées : [Quelle Carte, Quelle Borne]
        return new int[]{indexCarte, indexBorne};
    }
}