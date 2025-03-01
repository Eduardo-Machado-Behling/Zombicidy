package main.board;

import main.board.characters.*; 
import java.util.ArrayList;
import java.util.Map;

import javax.print.DocFlavor.INPUT_STREAM;

import main.board.scenery.*;
import main.board.items.*;
import main.FileReader;
import main.EventListener;

public class Board {
    private Grid[][] board = new Grid[10][10];
    private Combat combat;
    private Player player;
    private ArrayList<CommomZombie> zombies;
    private Map<String, String[]> gameSettings;
    private EventListener eventListener;

    public Board( String difficulty , boolean load , EventListener eventListener ) {
        this.eventListener = eventListener;
        FileReader fileReader = new FileReader();
        String[][] map = fileReader.ReadRamdomMap();
        gameSettings = fileReader.ReadSettings( difficulty );
        int[] position = new int[2];
        for( int x = 0 ; x < 10 ; x++ ) {
            for( int y = 0 ; y < 10 ; y++ ) {
                switch ( map[x][y] ) {
                    case "null":
                        board[x][y] = new Ground();
                        break;
                    case "W":
                        board[x][y] = new Wall();
                        break;
                    case "P":
                        board[x][y] = CreatePlayer();
                        player = ( Player )board[x][y];
                        break;
                    case "Z":
                    case "ZC":
                    case "ZR":
                    case "ZG":
                        board[x][y] = CreateZombie( map[x][y] );
                        break;
                    case "BB":
                    case "BBB":
                    case "BG":
                        board[x][y] = CreateChest( map[x][y] );
        
                }
                position[0] = x;
                position[1] = y;
                board[x][y].setPosition( position );
            }    
        } 
    }

    public Grid GetGrid( int[] position ) {
        return board[position[0]][position[1]];
    }

    public Chest CreateChest( String type ) {
        String[] data;
        switch ( type ) {
            case "BB":
                data = gameSettings.get( "Bandage" );
                int heal = Integer.parseInt( data[1] );
                Bandage bandage = new Bandage( heal ); 
                return new Chest( bandage , null);
            case "BBB":
                data = gameSettings.get( "BeisebalBat" );
                int bonus = Integer.parseInt( data[1] );
                BeisebolBat beisebolBat = new BeisebolBat( bonus );
                return new Chest( beisebolBat , null);
            case "BG":
                data = gameSettings.get( "Gun" );
                int damage = Integer.parseInt( data[1] );
                int ammo = Integer.parseInt( data[2] );
                Gun gun = new Gun( ammo, damage );
                CrawlerZombie zombie = ( CrawlerZombie ) CreateZombie( "ZC" );
                return new Chest( gun , zombie );
        }
        return null;
    }

    public CommomZombie CreateZombie( String type ) {
        int health , movement;
        String[] data;
        switch ( type ) {
            case "Z":
                data = gameSettings.get( "CommomZombie" );
                health = Integer.parseInt( data[1] );
                movement = Integer.parseInt( data[2] );
                return new CommomZombie( health , movement );
            case "ZC":
                data = gameSettings.get( "CrawlerZombie" );
                health = Integer.parseInt( data[1] );
                movement = Integer.parseInt( data[2] );
                return new CrawlerZombie( health , movement );
            case "ZR":
                data = gameSettings.get( "RunnerZombie" );
                health = Integer.parseInt( data[1] );
                movement = Integer.parseInt( data[2] );
                return new RunnerZombie( health , movement );
            case "ZG":
                data = gameSettings.get( "GiantZombie" );
                health = Integer.parseInt( data[1] );
                movement = Integer.parseInt( data[2] );
                return new GiantZombie( health , movement );
        }
        return null;
    }

    public Player CreatePlayer() {
        String[] array = gameSettings.get("Player");
        int health = Integer.parseInt( array[1] );
        int movement = Integer.parseInt( array[2] );
        int perception = Integer.parseInt( array[3] );
        return new Player(health, movement, perception);
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
        int movement = player.getMovement();
        Grid grid = board[position[0]][position[1]];
        if( ( deltaX != 0 && deltaY != 0 ) || ( deltaX == 0 && deltaY == 0 ) ) {
            return false;
        }
        if( deltaX <= movement && deltaX >= movement * -1 && deltaY <= movement && deltaY >= movement * -1 && !( grid.GetType().equals( "Wall") ) ) {
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

    
    public Combat InitiateCombat( int[] position , boolean surpriseEncounter ) {
        combat = new Combat( player , ( CommomZombie )board[position[0]][position[1]] , this , surpriseEncounter );
        return combat;
    }
}
