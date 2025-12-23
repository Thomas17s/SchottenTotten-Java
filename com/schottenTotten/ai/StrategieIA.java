package com.schottenTotten.ai;

import com.schottenTotten.model.Borne;
import com.schottenTotten.model.Joueur;
import java.util.List;


public interface StrategieIA {
    
    /**
     * calcule le prochain coup de l'IA.
     * @param ia Le joueur IA qui doit jouer
     * @param bornes L'état actuel du plateau
     * @return un tableau d'entiers [indexCarte, indexBorne]
     */
    int[] jouerTour(Joueur ia, List<Borne> bornes);
}
