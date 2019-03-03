package com.nextbi.nor;

import java.util.ArrayList;
import java.util.List;

class Usage
{
    private float weight;
    String original;
    List<Position > positions;

    public float getRate() {
        return weight;
    }


    public int arraySize;

    public Usage (int arraySize, String original )
    {
        weight = 0;
        this.arraySize = arraySize;
        this.original = original;
        positions = new ArrayList<>();
    }

    public void incWeight( float weight )
    {
        this.weight += weight;
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
