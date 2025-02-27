package main.board.items;

public class Gun extends Item {
    private int ammo;
    private int damage;

    public Gun( int ammo , int damage) {
        this.ammo = ammo;
        this.damage = damage;
    }
    public int GetAmmo() {
        return ammo;
    }
    public void AddAmmo() {
        ammo += 1;
    }
    public int Attack() {
        if( ammo > 0 ) {
            ammo -= 1;
            return damage;
        } else {
            return 0;
        }
    }

    @Override 
    public String toString() {
        return "Gun";
    }
}
