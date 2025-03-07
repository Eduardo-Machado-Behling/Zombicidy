package com.zombicidy.backend.board.scenery;

import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.backend.board.characters.*;
import com.zombicidy.backend.board.items.*;

public class Chest extends Grid {
  private Item content;
  private CrawlerZombie zombie;

  public Chest(Item content, CrawlerZombie zombie) {
    this.content = content;
    this.zombie = zombie;
  }

  public Item Open() { return content; }

  public CrawlerZombie getZombie() { return zombie; }
}
