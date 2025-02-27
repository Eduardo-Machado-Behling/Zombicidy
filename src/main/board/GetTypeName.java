package main.board;

public abstract class GetTypeName {
    public String GetType() {
        return this.getClass().getSimpleName();
    }
}
