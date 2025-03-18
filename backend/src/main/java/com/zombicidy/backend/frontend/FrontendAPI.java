package com.zombicidy.backend.frontend;

import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.backend.board.combat.Combat;

public interface FrontendAPI {
  public abstract void FinishCombat();

  public abstract void Combat(Combat combat);

  public abstract void ZombieKilled();

  public abstract void UseBandage(boolean actionWasMade);

  public abstract void SurpriseEncounter();

  public abstract void PlayerDealtDamage(int damage);

  public abstract void PlayerTookDamage(int damage);

  public abstract void Redraw(int[] position, Grid grid);

  public abstract void GainedItem(String item);

  public abstract void GameWin();

  public abstract void GameLose();

  public abstract void PlayerGunNoAmmo();

  public abstract void PlayerNoGun();
}
