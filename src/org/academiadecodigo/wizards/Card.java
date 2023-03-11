package org.academiadecodigo.wizards;

/*
----------- Complexity ---------
- have an Enum with the card art; Each card would have the "art"!
 */
public class Card {

    private String color;
    private int num;

    public Card(String color, int num) {
        this.color = color;
        this.num = num;
    }

    public String getColor() {
        return color;
    }

    public int getNum() {
        return num;
    }

    @Override
    public String toString() {
        return color + num;
    }
}
