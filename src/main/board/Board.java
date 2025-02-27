package main.board;

import main.board.characters.*; 
import java.util.ArrayList;
import main.board.scenery.*;


public class Board {
    private Grid[][] board = new Grid[10][10];
    private Combat combat;
    private Player player;
    private ArrayList<CommomZombie> zombies;

    public Board( Grid[][] board ) {
        this.board = board;
    }

    public String test() {
        return this.getClass().getSimpleName();
    }

    public void Input( int[] position ) {
        Grid grid = board[position[0]][position[1]];
        String type = grid.GetType();
        if( IsValidMovement( position ) ) {
            switch ( type ) {
                case "Ground":
                    MovePlayer( position );
                    break;
                case "Chest":
                    OpenChest( ( Chest )grid );
                    break;
                case "GiantZombie":
                case "RunnerZombie":
                case "CrawlerZombie":
                case "CommomZombie":
                    InitiateCombat( position , false );
                    break;
                default:
                    break;
            }
        }
    }

    public void OpenChest( Chest chest ) {
        int[] position = chest.getPosition();
        player.GainItem( chest.Open() );
        if( chest.getZombie() != null ) {
            board[position[0]][position[1]] = chest.getZombie();
            chest.getZombie().setPosition( position );
            InitiateCombat( position , true );
        } else {
            board[position[0]][position[1]] = new Ground();
            board[position[0]][position[1]].setPosition( position );
        }
    }   

    public boolean IsValidMovement( int[] position ) {
        int deltaX = player.getPosition()[0] - position[0];
        int deltaY = player.getPosition()[1] - position[1];
        Grid grid = board[position[0]][position[1]];
        if( deltaX <= 1 && deltaX >= -1 && deltaY <= 1 && deltaY >= -1 && !( grid.GetType().equals( "Wall") ) ) {
            return true;
        } else {
            return false;
        }
    }

    public void MovePlayer( int[] position ) {
        int[] lastPositon = player.getPosition();
        player.setPosition( position );
        board[position[0]][position[1]] = player;
        board[lastPositon[0]][lastPositon[1]] = new Ground();
        board[lastPositon[0]][lastPositon[1]].setPosition( position );
    }

    public void MoveZombies( ) {

    }

    public void FinishCombat( CommomZombie zombie ) {
        if( !zombie.IsAlive() ) {
            KillZombie( zombie ); 
        }
        combat = null;
    }

    public void KillZombie( CommomZombie zombie ) {
        int[] position = zombie.getPosition();
        board[position[0]][position[1]] = new Ground();
        board[position[0]][position[1]].setPosition( position );
    }

    
    public void InitiateCombat( int[] position , boolean surpriseEncounter ) {
        combat = new Combat( player , ( CommomZombie )board[position[0]][position[1]] , this , surpriseEncounter );
    }
}
