package main.board.characters;
import main.board.Grid;

public abstract class Character extends Grid {
    protected int healthPoints;
    protected int movement;

    public Character( int healthPoints , int movement ) {
        this.healthPoints = healthPoints;
        this.movement = movement;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public void TakeDamage( int damage ) {
        healthPoints -= damage;
    }

    public int getMovement() {
        return movement;
    }

    public boolean IsAlive() {
        if( healthPoints <= 0 ) {
            return false;
        } else {
            return true;
        }
    }
}
