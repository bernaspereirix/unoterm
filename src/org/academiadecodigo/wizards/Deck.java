package org.academiadecodigo.wizards;

import java.util.LinkedList;

public class Deck {

    private LinkedList<Card> cards;

    public Deck() {
        cards = new LinkedList<>();
        createDeck();
    }

    public void createDeck(){
        for (int i = 1; i <= 36; i++) {
            if(i<=9){
                cards.add(new Card("R", i));
                cards.add(new Card("R", i));
            }
            else if(i<=18){
                cards.add(new Card("B", i-9));
                cards.add(new Card("B", i-9));
            }
            else if(i<=27){
                cards.add(new Card("Y", i-18));
                cards.add(new Card("Y", i-18));
            } else {
                cards.add(new Card("G", i-27));
                cards.add(new Card("G", i-27));
            }
        }
    }

    public LinkedList<Card> getCards() {
        return cards;
    }
}
