package com.zombicidy.backend.frontend;

import com.zombicidy.backend.EventListener;
import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.backend.board.combat.Combat;

/*
Deve Chamar as seguintes funções:
    CombatAction( String choise );
        Deve mandar qual ação no combate o player escolheu, podendo ser Attack,
Shoot, Bandage e Run. getGrid( int[] position ); Retorna o grid que está na
posição passada. MovePlayer( int[] position ); Manda uma posição para o player
para se mover. RestartBoard(); Reinicia o board para o estado inicial com a
mesma dificuldade e mapa.
*/

public abstract class BaseFrontend implements FrontendAPI {
  protected EventListener eventListener;

  public BaseFrontend(EventListener eventListener) {
    this.eventListener = eventListener;
  }
}
