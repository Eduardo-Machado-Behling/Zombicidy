package com.zombicidy.frontend;

import com.zombicidy.backend.EventListener;
import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.backend.board.characters.CommomZombie;
import com.zombicidy.backend.board.combat.Combat;
import com.zombicidy.backend.frontend.BaseFrontend;
import com.zombicidy.frontend.scenes.Play;

public class FrontEnd extends BaseFrontend {
  private EventListener el;

  public FrontEnd(EventListener eventListener) {
    super(eventListener);
    eventListener.setFrontend(this);
    el = eventListener;
  }

  public void main() {
    Window win = Window.get();
    win.setEventListener(el);
    el.run("hard");
    win.setScene(new Play(true));

    win.run();
  }

  @Override
  public void FinishCombat() {
    Window.get().FinishCombat();
  }

  @Override
  public void Combat(Combat combat) {
    Window.get().Combat(combat);
  }

  @Override
  public void ZombieKilled(CommomZombie zombie) {
    Window.get().ZombieKilled(zombie);
  }

  @Override
  public void UseBandage(boolean actionWasMade) {
    Window.get().UseBandage(actionWasMade);
  }

  @Override
  public void SurpriseEncounter() {
    Window.get().SurpriseEncounter();
  }

  @Override
  public void PlayerDealtDamage(int damage) {
    Window.get().PlayerDealtDamage(damage);
  }

  @Override
  public void PlayerTookDamage(int damage) {
    Window.get().PlayerTookDamage(damage);
  }

  @Override
  public void Redraw(int[] position, Grid grid) {
    Window.get().Redraw(position, grid);
  }

  @Override
  public void GainedItem(String item) {
    Window.get().GainedItem(item);
  }

  @Override
  public void GameWin() {
    Window.get().GameWin();
  }

  @Override
  public void GameLose() {
    Window.get().GameLose();
  }

  @Override
  public void PlayerGunNoAmmo() {
    Window.get().PlayerGunNoAmmo();
  }

  @Override
  public void PlayerNoGun() {
    Window.get().PlayerNoGun();
  }
}
