package main.board.baseclasses;

public abstract class GetTypeName {
    public String GetType() {
        return this.getClass().getSimpleName();
    }
}
