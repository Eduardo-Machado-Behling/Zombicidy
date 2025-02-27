package main.board;
import main.board.characters.*;

public class Combat {
    private Player player;
    private CommomZombie zombie;
    private Dice dice;
    private Board board;

    public Combat( Player player , CommomZombie zombie , Board board , boolean surpriseEncounter ) {
        this.player = player;
        this.zombie = zombie;
        this.board = board;
        dice = new Dice();
        if( surpriseEncounter ) {
            player.TestPerception( dice.RollD3() );
        }
    }

    public void Attack( String choise ) {
        String type = zombie.GetType();
        switch ( choise ) {
            case "Attack":
                if( type.equals( "GiantZombie" ) && player.getPlayerBeisebalBat() == null ) {
                    break;
                }
                zombie.TakeDamage( player.Attack( dice.RollD6() ) );
                break;

            case "Shoot":
            if( player.getPlayerGun() == null ) {
                return;
            }
            if( type.equals( "RunnerZombie" ) ) {
                player.Shoot();
                break;
            }
            zombie.TakeDamage( player.Shoot() );
                break;
        }
        if( zombie.IsAlive() ) {
            player.TestPerception( dice.RollD3() );
        } else {
            board.FinishCombat( zombie );
        }
    }

    public void Run() {
        board.FinishCombat( zombie );
    }
}