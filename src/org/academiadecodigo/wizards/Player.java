package org.academiadecodigo.wizards;

import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.string.StringInputScanner;
import org.academiadecodigo.bootcamp.scanners.string.StringSetInputScanner;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/*
user name
hand - tipo cards

 */
public class Player implements Runnable {

    private String name;
    private LinkedList<Card> hand;
    private Socket playerSocket;
    private Prompt prompt; //só um prompt e vários scanners;

    private BufferedReader in;
    private BufferedWriter out;

    public Player(Socket playerSocket) {
        this.playerSocket = playerSocket;
        hand = new LinkedList<>();
        try {
            prompt = new Prompt(playerSocket.getInputStream(), new PrintStream(playerSocket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void chooseName() {
        StringInputScanner nameScanner = new StringInputScanner();
        nameScanner.setMessage("What's your user name?\n");
        name = prompt.getUserInput(nameScanner);
    }

    public BufferedReader getIn() {
        return in;
    }


    public String getName() {
        return name;
    }

    public BufferedWriter getOut() {
        return out;
    }

    public String chooseCard() throws IOException {
        Set<String> handSet = new HashSet<>();
        out.write("\nYour cards: \n");
        handSet.add("DRAW");
        handSet.add("draw");
        handSet.add("Draw");
        for (Card card : hand) {
            handSet.add(card.toString());
            handSet.add(card.toString().toLowerCase());
            out.write("[" + card + "]" + " ");
            out.flush();
        }
        StringSetInputScanner cardScanner = new StringSetInputScanner(handSet);
        cardScanner.setMessage("\n\nPlay a card:\n");
        cardScanner.setError("You don't have that card!");
        String cardString = prompt.getUserInput(cardScanner).toUpperCase();
        return cardString;
    }

    public String readCardChosen() throws IOException {
        return in.readLine();
    }

    public LinkedList<Card> getHand() {
        return hand;
    }

    @Override
    public void run() {
        chooseName();
        try {
            out.write("\nWaiting for other players...\n\n");
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
