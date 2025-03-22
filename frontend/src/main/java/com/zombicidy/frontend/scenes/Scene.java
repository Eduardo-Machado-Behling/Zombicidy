package com.zombicidy.frontend.scenes;

import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.backend.board.characters.CommomZombie;
import com.zombicidy.backend.board.combat.Combat;
import com.zombicidy.backend.frontend.FrontendAPI;

public abstract class Scene implements FrontendAPI {
  public abstract void init();
  public abstract void update(double elapsed_time);
  public abstract void display(double elapsed_time);

  public abstract void onKeyEvent(long window, int key, int scancode,
                                  int action, int mods);

  public abstract void onMouseEvent(long window, int button, int action,
                                    int mods);

  public abstract void onResize(long window, int width, int height);
  public abstract void onMouseMove(long window, double xpos, double ypos);
  public abstract void onMouseScroll(long window, double xoffset,
                                     double yoffset);

  public abstract void clean();

  public abstract void FinishCombat();

  public abstract void Combat(Combat combat);

  public abstract void ZombieKilled(CommomZombie zombie);

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