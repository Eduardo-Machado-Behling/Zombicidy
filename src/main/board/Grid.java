package main.board;

public abstract class Grid {
    protected int[] position = new int[2];

    public int[] getPosition() {
        return position;
    }
    public void setPosition( int[] position ) {
        this.position = position;
    }
}
