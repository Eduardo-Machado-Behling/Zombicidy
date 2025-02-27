package main.board.scenery;
 
import main.board.Grid;
import main.board.items.*;
import main.board.characters.*;

public class Chest extends Grid {
    private Item content;
    private CrawlerZombie zombie;

    public Chest( Item content , CrawlerZombie zombie ) {
        this.content = content;
        this.zombie = zombie;
    }

    public Item Open() {
        return content;
    }

    public CrawlerZombie getZombie() {
        return zombie;
    }

}
