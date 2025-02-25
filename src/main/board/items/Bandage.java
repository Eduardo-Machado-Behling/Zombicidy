package main.board.items;

public class Bandage extends Item {
    private int heal;

    public Bandage( int heal ) { 
        this.heal = heal;
    }

    public int Heal() {
        heal = 0;
        return heal;
    }
}
