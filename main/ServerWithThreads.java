package com.example;

import java.net.*;
import java.io.*;
import java.util.*;

public class ServerWithThreads {

    public static final int LISTENING_PORT = 52004;

    private static String player1Choice = null;
    private static String player1Name = null;
    private static String player2Choice = null;
    private static String player2Name = null;

    public static void main(String[] args) {
        ServerSocket listener;
        Socket connection;

        try {
            listener = new ServerSocket(LISTENING_PORT);
            System.out.println("RPS Server listening on port " + LISTENING_PORT);
            while (true) {
                connection = listener.accept();
                ConnectionHandler h = new ConnectionHandler(connection);
                h.start();
            }
        } catch (Exception e) {
            System.out.println("Sorry, the server has shut down.");
            System.out.println("Error:  " + e);
        }
    }

    public static synchronized void processChoice(String playerName, String choice, ArrayList<ConnectionHandler> connectionList) {
        if (player1Name == null) {
            player1Name = playerName;
            player1Choice = choice;
            broadcast(playerName + " has made a choice", connectionList);
        } else if (player1Name.equals(playerName)) {
            player1Choice = choice; 
        } else if (player2Name == null) {
            player2Name = playerName;
            player2Choice = choice;
            broadcast(playerName + " has made a choice", connectionList);
        } else if (player2Name.equals(playerName)) {
            player2Choice = choice; 
        }

        if (player1Choice != null && player2Choice != null) {
            String result = evaluateGame();
            
            // Send round summary
            broadcast("\n--- GAME RESULT ---", connectionList);
            broadcast(player1Name + " picked: " + player1Choice, connectionList);
            broadcast(player2Name + " picked: " + player2Choice, connectionList);
            broadcast("Outcome: " + result, connectionList);
            broadcast("-------------------", connectionList);
            
            // FIX: Bundle the specific outcome with the clear signal string
            String outcomeSummary = player1Name + " (" + player1Choice + ") vs " + player2Name + " (" + player2Choice + ") -> " + result;
            broadcast("[CLEAR]|" + outcomeSummary, connectionList);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            broadcast("Choose rock, paper or scissors to play again", connectionList);
            
            player1Choice = null;
            player1Name = null;
            player2Choice = null;
            player2Name = null;
        }
    }

    private static String evaluateGame() {
        if (player1Choice.equalsIgnoreCase(player2Choice)) {
            return "It's a TIE!";
        }
        
        boolean p1Wins = (player1Choice.equals("Rock") && player2Choice.equals("Scissors")) ||
                         (player1Choice.equals("Paper") && player2Choice.equals("Rock")) ||
                         (player1Choice.equals("Scissors") && player2Choice.equals("Paper"));

        if (p1Wins) {
            return player1Name + " WINS!";
        } else {
            return player2Name + " WINS!";
        }
    }

    private static void broadcast(String msg, ArrayList<ConnectionHandler> connectionList) {
        int i = 0;
        while (i < connectionList.size()) {
            try {
                ConnectionHandler h = connectionList.get(i);
                h.oos.writeObject(msg);
                h.oos.flush();
                i++;
            } catch (Exception e) {
                connectionList.remove(i);
            }
        }
    }

    private static class ConnectionHandler extends Thread {
        private static ArrayList<ConnectionHandler> connectionList = new ArrayList<>();
        private static final Object idLock = new Object();
        public static int idNum = 1;
        
        Socket client;
        ObjectOutputStream oos;
        ObjectInputStream ois;
        String name;

        ConnectionHandler(Socket socket) {
            client = socket;
            
            synchronized(idLock) {
                name = "Player " + idNum;
                idNum++;
            }
            
            connectionList.add(this);
            try {
                oos = new ObjectOutputStream(client.getOutputStream());
                ois = new ObjectInputStream(client.getInputStream());
                
                oos.writeObject(name);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                while (true) {
                    String choice = (String) ois.readObject();
                    processChoice(name, choice, connectionList);
                }
            } catch (EOFException e) {
                connectionList.remove(this);
            } catch (Exception e) {
                System.out.println(name + " disconnected.");
                connectionList.remove(this);
            }
        }
    }
}
