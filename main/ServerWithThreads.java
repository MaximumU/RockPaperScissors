package com.example;

import java.net.*;
import java.io.*;
import java.util.*;

public class ServerWithThreads {

    public static final int LISTENING_PORT = 52008;
    public static int numUsers = 0;

    // Game state variables managed by the server
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

    public static synchronized void addUser() {
        numUsers++;
    }

    // Synchronized method to handle choices as they arrive from threads
    public static synchronized void processChoice(String playerName, String choice, ArrayList<ConnectionHandler> connectionList) {
        // Assign choices to the first two players who arrive
        if (player1Name == null) {
            player1Name = playerName;
            player1Choice = choice;
            broadcast(playerName + " has made a choice!", connectionList);
        } else if (player1Name.equals(playerName)) {
            player1Choice = choice; // Allow player 1 to change mind before player 2 locks in
        } else if (player2Name == null) {
            player2Name = playerName;
            player2Choice = choice;
            broadcast(playerName + " has made a choice!", connectionList);
        } else if (player2Name.equals(playerName)) {
            player2Choice = choice; // Allow player 2 to change mind
        }

        // Evaluate the game once both choices are present
        if (player1Choice != null && player2Choice != null) {
            String result = evaluateGame();
            broadcast("\n--- GAME RESULT ---", connectionList);
            broadcast(player1Name + " picked: " + player1Choice, connectionList);
            broadcast(player2Name + " picked: " + player2Choice, connectionList);
            broadcast("Outcome: " + result, connectionList);
            broadcast("-------------------\nChoose rock, paper or scissors to play again!", connectionList);
            
            // Reset for the next round
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
        Socket client;
        ObjectOutputStream oos;
        ObjectInputStream ois;
        public static int idNum = 1;
        String name;

        ConnectionHandler(Socket socket) {
            client = socket;
            name = "Player " + idNum;
            idNum++;
            connectionList.add(this);
            try {
                ois = new ObjectInputStream(client.getInputStream());
                oos = new ObjectOutputStream(client.getOutputStream());
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
