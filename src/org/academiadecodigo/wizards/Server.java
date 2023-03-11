package org.academiadecodigo.wizards;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
TODO: instanciar a thread;
TODO: menu;
TODO: após adicionar as theards e ter vários clientes:
  TODO: método showHand;
    - ter vários jogadores - À medida que se vão juntando até chegra a 4
    - o primeiro jogador só joga quando todos os players tiverem colocado o user name - dar print para play;

------------ COMPLEXIDADE --------------------
- ter que dizer UNO para ganhar;
- anunciar o numero da cartas de cada jogador a cada jogada;
- add: reverse, +2 e block;

 */
public class Server {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    public static int port;
    public static int playerNum;
    private LinkedList<Player> players;
    private ExecutorService fixedPool;
    private Deck deck;
    private LinkedList<Card> discardedPile;
    private Card lastCardPlayed;
    private Card cardPlayed;

    public static void main(String[] args) {
        // Grab port number and max player count from args, else initialize with some defaults
        if (args.length != 2) {
            port = 8080;
            playerNum = 2;
        } else {
            port = Integer.parseInt(args[0]);
            playerNum = Integer.parseInt(args[1]);
        }

        Server server = new Server();
        try {
            server.listen();
            server.gameLoop();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Server() {
        try {
            serverSocket = new ServerSocket(port);
            players = new LinkedList<>();
            deck = new Deck();
            discardedPile = new LinkedList<>();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fixedPool = Executors.newFixedThreadPool(playerNum);
    }

    public void listen() throws IOException {

        String artUno = "\n" +
                "                                           ,╓╔╗@▒▒╣╣╣╬╬╬╣╣╣▒▒@╗µ,\n" +
                "                                     ,╓@▒╣╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╣╝╙└╓▄▄▄▄▄▄,\n" +
                "                                 ,╗▒╣╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╣╙ ▄▌▀▀▄≥≥φφ≥≥╦╫▀▌▄\n" +
                "                             ,╔▒╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╣╝╙╩╬╬╝└╓▓▀╓φ░ΓΓΓΓΓΓΓΓΓΓΓφ≥▀▓\n" +
                "                          .#╣╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╝╙└,▄▄▀█ ╚`╓██;φΓΓΓ╙Q▄▓▓▓▓▄Γ╙ΓΓΓ░[▀▌\n" +
                "                        ╔╣╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╜ ▄█▌╠≤φφ/█ ▓██«ΓΓΓ╙▄██████████▄╚ΓΓΓφ╨▓\n" +
                "                     ,@╬╬╬╬╬╬╬╬╬╬╝╜╙└╙╣╬╬╬ ▓█▓▓█'ΓΓΓφ╙███┌ΓΓΓΓ▐█╙╓╓╙▀██▓▓▓█▌7ΓΓΓφ╟▌\n" +
                "                   .║╬╬╬╬╬╬╣╨╙,▄▄Æ▀▀▀▀▄,└╝⌐╫█▓▓██\"ΓΓΓφ╙██]ΓΓΓε╫⌐╞╬╬╬╣╕╙█████µ░ΓΓΓ █\n" +
                "                 ,å╬╬╬╣╙└▄▄▌▀▀██'░░ΓΓφ≥╠▀▌▄ ▓████▌7ΓΓΓ≥╫█ ΓΓΓ░╙▌ ╣╬╬╬╬▒ ████▌░ΓΓΓ¬█\n" +
                "                é╬╬╬╩ ▄██┘≥░░░┘██\"ΓΓΓΓΓΓ░φ╥▀▓█████▄╚ΓΓΓε█▌╙ΓΓΓ≥▀▌ ╢╬╬╬╬ ╫███┌ΓΓΓΓ]█\n" +
                "             ,▄µ╙╬╬µ'██▓▓█\"ΓΓΓφ╙█▌7ΓΓΓ░7░ΓΓΓφ≥╫████µφΓΓΓ~█▄╙ΓΓΓφ╙▓▄,└╙╙,███╓φΓΓΓ⌐█\n" +
                "        ▄▄▀▀▀▄[█ ╚╬╣ ▓█▓██▓7ΓΓΓ≥╫█▄╚ΓΓΓε¥▄²φΓΓΓφ≥╠▀█─░ΓΓ░└█▌\"φΓΓΓφ≥╠▀▀▀▀▄φφΓΓΓδ,█¬\n" +
                "      ▄███⌐░ΓΓ░/█ ╢╬▒ █████▌╚ΓΓΓε██µφΓΓΓ,███▄└φΓΓΓ░φ┐\"ΓΓΓφ╙██▄└╙ΓΓΓΓΓΓΓΓΓΓΓ░╙╓▓▀\n" +
                "     ╙█████\"ΓΓΓφ╙█ ╣╬▒ █████▄φΓΓΓ,██ ░ΓΓ░└█████▄Γ╙ΓΓΓΓΓΓΓΓ≥╙████▌▄Γ\"²²²²\"▄▄▓██─\n" +
                "      ╫█████\\ΓΓΓφ╫▌`╬╬╕ █████⌐░ΓΓ░⌐██\"ΓΓΓφ╙███████▓▄7φΓΓΓΓΓε▓██████████████▀\n" +
                "       █████▌╙ΓΓΓ≥▓▄└╬╬⌐╙█████\"ΓΓΓφ╙██7ΓΓΓ≥╟█████████▓▄└φ░╚²,██▀██████▀▀└\n" +
                "        ███▓█▄φΓΓΓ»█µ╚╬╣ ╟████▌φΓΓΓ~██▌╙ΓΓΓε▓µ ╙█████████████╙┌▒@@╗@@▒Å\n" +
                "         █▓▓▓█µ░ΓΓΓ⌐█─╙╬╣ ████Ö░ΓΓΓ⌐███▄φΓΓΓ,█ ╙▒╓╙▀█████▀▀├╓╣╬╬╬╬╬╬╬`\n" +
                "         └█████└░ΓΓΓ≈▀▓▄▄╓▓██╓φΓΓΓδ╓████⌐²│▄▄██ ╫╬╬▒µ,╓╓#▒╣╬╬╬╬╬╬╬╝└\n" +
                "          ╙█████▄φΓΓΓ░φ≥╡╡≥φφΓΓΓΓ∩▄█████████▀┌╔╣╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╩\n" +
                "           ▀██████▄╙ΓΓΓΓΓΓΓΓΓδ²▐▄▓└ ╫███▀╙╓╓#╣╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╝╙\n" +
                "            ╙███████▓▄▄▄▄▄▄▄▓██▀ ╔╣╣╓╓╗▒╣╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╝╙\n" +
                "              ╙█████████████▀╙,@╣╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╣╜`\n" +
                "                 └╙▀▀▀▀▀╙╙╓╔▒╣╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╣╩╙\n" +
                "                     \"╚╣╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╣╩╙└\n" +
                "                            └└╙╙╙╙╙╙└└\n" +
                "    \n\n\n";
        while (players.size() != playerNum) {
            clientSocket = serverSocket.accept();
            Player player = new Player(clientSocket);
            players.add(player);
            fixedPool.submit(player);
            player.getOut().write("You have successfully joined!\n\n");
            player.getOut().write(artUno);
            player.getOut().flush();
        }
    }

    public void gameLoop() throws IOException {
        while (!readyToStart()) {
            readyToStart();
        }
        getFirstCard();
        giveHands();

        while (!checkWin()) {
            for (Player player : players) {
                player.getOut().write("\nIt's your turn to play!\n");
                player.getOut().flush();

                for (Player p : players) {
                    checkWin();
                    if (!p.equals(player)) {
                        p.getOut().write("\nIt's " + player.getName() + "'s turn to play!\n");
                        p.getOut().flush();
                    }
                }
                compareCards(player, player.chooseCard());
                showLastCard();
                // TODO:do something else
            }
        }
    }

    public boolean readyToStart() {
        int playersReady = 0;
        for (Player p : players) {
            if (p.getName() != null) {
                playersReady++;
            }
        }
        return playersReady == players.size();
    }

    public void getFirstCard() throws IOException {
        int num = (int) (Math.random() * deck.getCards().size());
        lastCardPlayed = deck.getCards().get(num);
        deck.getCards().remove(num);
        for (Player p : players) {
            p.getOut().write("\nStarting card: \n[" + lastCardPlayed.toString() + "]\n");
            p.getOut().flush();
        }
    }

    public void giveHands() {
        for (int i = 0; i < players.size(); i++) {
            for (int j = 0; j < 1; j++) {
                int num = (int) (Math.random() * deck.getCards().size());
                players.get(i).getHand().add(deck.getCards().get(num));
                deck.getCards().remove(num);
            }
        }
    }

    public void showLastCard() throws IOException {
        for (Player p : players) {
            p.getOut().write("\n------------ NEXT PLAY ------------\n");
            p.getOut().write("\nCURRENT CARD: \n[" + lastCardPlayed + "]\n");
            p.getOut().flush();
        }
    }

    public void draw(Player player) throws IOException {
        int num = (int) (Math.random() * deck.getCards().size());
        player.getHand().add(deck.getCards().get(num));
        Card card = deck.getCards().remove(num);
        player.getOut().write("You drew [" + card.toString() + "]\n\n");
        player.getOut().flush();
        for (Player p : players) {
            if (!p.equals(player)) {
                p.getOut().write(player.getName() + " drew a card!\n");
            }
        }
    }

    public void compareCards(Player player, String card) throws IOException {
        if (card.toUpperCase().equals("DRAW")) {
            draw(player);
            return;
        }
        for (int i = 0; i < player.getHand().size(); i++) {
            if (player.getHand().get(i).toString().equals(card)) {
                cardPlayed = player.getHand().get(i);
                break;
            }
        }
        if (cardPlayed.getColor().equals(lastCardPlayed.getColor()) || cardPlayed.getNum() == lastCardPlayed.getNum()) {
            playCard(player);
        } else {
            player.getOut().write("You can't play that card!");
            // sus v
            compareCards(player, player.chooseCard());
        }

    }

    public void playCard(Player player) throws IOException {
        discardedPile.add(cardPlayed);
        lastCardPlayed = cardPlayed;
        player.getOut().write("You played [" + cardPlayed.toString() + "].\n");

        for (Player p : players) {
            if (!p.equals(player)) {
                p.getOut().write(player.getName() + " played [" + cardPlayed.toString() + "].\n");
                p.getOut().flush();
            }
        }
        for (int i = 0; i < player.getHand().size(); i++) {
            if (player.getHand().get(i).equals(cardPlayed)) {
                player.getHand().remove(i);
                break; //TODO: não está a dar remove;
            }
        }
    }


    public boolean checkWin() throws IOException {
        boolean win = false;
        String winner = null;
        for (Player p : players) {
            if (p.getHand().size() == 0) {
                winner = p.getName();
                win = true;
                for (Player p2 : players) {
                    p2.getOut().write("\n" + winner + " won the game!");
                    p2.getOut().flush();
                }
                fixedPool.shutdown();
                serverSocket.close();
                System.exit(0);
            }
        }
        return win;
    }
}