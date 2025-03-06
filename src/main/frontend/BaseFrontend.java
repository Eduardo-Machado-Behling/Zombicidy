package main.frontend;

import main.EventListener;
import main.board.baseclasses.Grid;
import main.board.combat.Combat;

/* 
Deve Chamar as seguintes funções:
    CombatAction( String choise ); 
        Deve mandar qual ação no combate o player escolheu, podendo ser Attack, Shoot, Bandage e Run.
    getGrid( int[] position ); 
        Retorna o grid que está na posição passada.
    MovePlayer( int[] position );
        Manda uma posição para o player para se mover.
    RestartBoard();
        Reinicia o board para o estado inicial com a mesma dificuldade e mapa.   
*/

public abstract class BaseFrontend {
    protected EventListener eventListener;

    public BaseFrontend( EventListener eventListener ) {
        this.eventListener = eventListener;
    }
    
    public abstract void FinishCombat();

    public abstract void Combat( Combat combat );

    public abstract void ZombieKilled();

    public abstract void UseBandage( boolean actionWasMade );

    public abstract void SurpriseEncounter();
    
    public abstract void PlayerDealtDamage( int damage );

    public abstract void PlayerTookDamage( int damage );

    public abstract void Redraw( int[] position , Grid grid );

    public abstract void GainedItem( String item );

    public abstract void GameWin();

    public abstract void GameLose();

    public abstract void PlayerGunNoAmmo();

    public abstract void PlayerNoGun();
}
