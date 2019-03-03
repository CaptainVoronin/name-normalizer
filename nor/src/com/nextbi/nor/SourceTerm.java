package com.nextbi.nor;

public class SourceTerm {
    private String text;
    private int count;

    public String getText() {
        return text;
    }

    public int getCount() {
        return count;
    }

    public SourceTerm(String text, int count )
    {
        this.text = text;
        this.count = count;
    }

    public void setText(String text ) {
        this.text = text;
    }
}
