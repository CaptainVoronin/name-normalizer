package com.nextbi.name_normalizer;

import java.util.ArrayList;
import java.util.List;

class Usage
{
    private float rate;
    private int total;
    String original;
    List<Position > positions;

    public float getRate() {
        return rate;
    }

    public int getTotal() {
        return total;
    }

    public int arraySize;

    public Usage (int arraySize, String original )
    {
        rate = 0;
        total = 0;
        this.arraySize = arraySize;
        this.original = original;
        positions = new ArrayList<>();
    }

    public void incUsage( )
    {
        total++;
        rate = (( float ) total ) / arraySize;
    }

    public String getOriginal()
    {
        return original;
    }

    public void addPos( Position pos )
    {
        positions.add( pos );
    }

    public List<Position> getPositions()
    {
        return positions;
    }
}
