package com.nextbi.name_normalizer;

public class Position {

    private int total;
    private int pos;

    public int getTotal() {
        return total;
    }

    public int getPos() {
        return pos;
    }

    public Position(int total, int pos )
    {
        this.pos = pos;
        this.total = total;
    }
}
