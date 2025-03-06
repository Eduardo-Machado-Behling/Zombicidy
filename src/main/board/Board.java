package main.board;

import main.board.baseclasses.Grid;
import main.board.characters.*;
import main.board.combat.Combat;
import java.util.*;
import main.board.scenery.*;
import main.board.items.*;
import main.EventListener;

public class Board {
    private Grid[][] board = new Grid[10][10];
    private Combat combat;
    private Player player;
    private ArrayList<CommomZombie> zombies = new ArrayList<CommomZombie>();
    private ArrayList<CommomZombie> toRemoveZombie = new ArrayList<CommomZombie>();
    private Map<String, String[]> gameSettings;
    private EventListener eventListener;
    private String[][] map;

    public Board( String difficulty , boolean load , EventListener eventListener ) {
        this.eventListener = eventListener;
        FileReader fileReader = new FileReader();
        map = fileReader.ReadRamdomMap();
        gameSettings = fileReader.ReadSettings( difficulty );
        StartBoard();
    }

    public Grid GetGrid( int[] position ) {
        return board[position[0]][position[1]];
    }

    public void StartBoard() {
        int[] position = new int[2];
        for( int x = 0 ; x < 10 ; x++ ) {
            for( int y = 0 ; y < 10 ; y++ ) {
                switch ( map[x][y] ) {
                    case "Ground":
                        board[x][y] = new Ground();
                        break;
                    case "Wall":
                        board[x][y] = new Wall();
                        break;
                    case "Player":
                        board[x][y] = CreatePlayer();
                        player = ( Player )board[x][y];
                        break;
                    case "CommomZombie":
                    case "CrawlerZombie":
                    case "RunnerZombie":
                    case "GiantZombie":
                        board[x][y] = CreateZombie( map[x][y] );
                        zombies.add( ( CommomZombie )board[x][y] );
                        break;
                    case "ChestBandage":
                    case "ChestBeisebolBat":
                    case "ChestGun":
                        board[x][y] = CreateChest( map[x][y] );
                        break;
                    default:
                        board[x][y] = new Ground();
                        break;
                }
                position[0] = x;
                position[1] = y;
                board[x][y].setPosition( position );
            }  
        } 
    }

    public Chest CreateChest( String type ) {
        String[] data;
        switch ( type ) {
            case "ChestBandage":
                data = gameSettings.get( "Bandage" );
                int heal = Integer.parseInt( data[1] );
                Bandage bandage = new Bandage( heal ); 
                return new Chest( bandage , null);
            case "ChestBeisebolBat":
                data = gameSettings.get( "BeisebolBat" );
                int bonus = Integer.parseInt( data[1] );
                BeisebolBat beisebolBat = new BeisebolBat( bonus );
                return new Chest( beisebolBat , null);
            case "ChestGun":
                data = gameSettings.get( "Gun" );
                int damage = Integer.parseInt( data[1] );
                int ammo = Integer.parseInt( data[2] );
                Gun gun = new Gun( ammo, damage );
                CrawlerZombie zombie = ( CrawlerZombie ) CreateZombie( "CrawlerZombie" );
                return new Chest( gun , zombie );
        }
        return null;
    }

    public CommomZombie CreateZombie( String type ) {
        int health , movement;
        String[] data;
        switch ( type ) {
            case "CommomZombie":
                data = gameSettings.get( "CommomZombie" );
                health = Integer.parseInt( data[1] );
                movement = Integer.parseInt( data[2] );
                return new CommomZombie( health , movement );
            case "CrawlerZombie":
                data = gameSettings.get( "CrawlerZombie" );
                health = Integer.parseInt( data[1] );
                movement = Integer.parseInt( data[2] );
                return new CrawlerZombie( health , movement );
            case "RunnerZombie":
                data = gameSettings.get( "RunnerZombie" );
                health = Integer.parseInt( data[1] );
                movement = Integer.parseInt( data[2] );
                return new RunnerZombie( health , movement );
            case "GiantZombie":
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
            }
        }
        zombies.removeAll(toRemoveZombie);
        toRemoveZombie.clear();
        toRemoveZombie = new ArrayList<CommomZombie>();
        MoveZombies();
    }

    public void OpenChest( Chest chest ) {
        int[] position = chest.getPosition();
        Item item  = chest.Open();
        eventListener.GainedItem(item.GetType());
        player.GainItem( item );
        if( chest.getZombie() != null ) {
            board[position[0]][position[1]] = chest.getZombie();
            board[position[0]][position[1]].setPosition( position );
            eventListener.Redraw( position , board[position[0]][position[1]]);
            InitiateCombat( position , true );
        } else {
            board[position[0]][position[1]] = new Ground();
            board[position[0]][position[1]].setPosition( position );
            MovePlayer( position );
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
        int[] lastPosition = player.getPosition().clone();
        board[position[0]][position[1]] = board[lastPosition[0]][lastPosition[1]];
        board[lastPosition[0]][lastPosition[1]] = new Ground();
        board[lastPosition[0]][lastPosition[1]].setPosition( lastPosition );
        player.setPosition( position );
        eventListener.Redraw( position, board[position[0]][position[1]] );
        eventListener.Redraw( lastPosition, board[lastPosition[0]][lastPosition[1]]);
    }

    public void MoveZombies( ) {
        Dijkstra path;
        Stack<int[]> ans;
        int[] temp;
        for( CommomZombie zombie : zombies ) {
            path = new Dijkstra(player, zombie, board);
            ans = path.CalculatePath();
            for( int movement = zombie.getMovement() ; movement != 0 ; movement-- ) {
                if( ans.empty() ) {
                    break;
                }
                temp = ans.peek().clone();
                if( board[temp[0]][temp[1]] == player ) {
                    InitiateCombat( zombie.getPosition().clone() , true);
                    break;
                } else {
                    MoveZombie( zombie.getPosition().clone() , temp , zombie);
                }
                ans.pop();
            }
        }
    }

    public void MoveZombie( int[] source , int[] destiny , CommomZombie zombie ) {
        zombie.setPosition( destiny.clone() );
        board[destiny[0]][destiny[1]] = zombie;
        eventListener.Redraw( destiny.clone() , board[destiny[0]][destiny[1]] );
        board[source[0]][source[1]] = new Ground();
        eventListener.Redraw( source.clone() , board[source[0]][source[1]] );
    }


    public void FinishCombat( CommomZombie zombie ) {
        eventListener.FinishCombat();
        if( !zombie.IsAlive() ) {
            KillZombie( zombie ); 
        }
        combat = null;
    }

    public void KillZombie( CommomZombie zombie ) {
        toRemoveZombie.add(zombie);
        int[] position = zombie.getPosition().clone();
        board[position[0]][position[1]] = new Ground();
        board[position[0]][position[1]].setPosition( position );
        eventListener.Redraw(position, board[position[0]][position[1]]);
        eventListener.ZombieKilled();
    }

    public void CombatAction( String choise ) {
        combat.Action(choise);
    }
    
    public void InitiateCombat( int[] position , boolean surpriseEncounter ) {
        combat = new Combat( player , ( CommomZombie )board[position[0]][position[1]] , this ,eventListener );
        combat.Init(surpriseEncounter);
    }
}
