package com.example;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * This program is a server that takes connection requests on
 * the port specified by the constant LISTENING_PORT.  When a
 * connection is opened, the program should allow the client to send it messages. The messages should then 
 * become visible to all other clients.  The program will continue to receive
 * and process connections until it is killed (by a CONTROL-C,
 * for example). 
 * 
 * This version of the program creates a new thread for
 * every connection request.
 */
public class ChatServerWithThreads {

    public static final int LISTENING_PORT = 52002;
    public static int numUsers;

    public static void main(String[] args) {

       

        ServerSocket listener;  // Listens for incoming connections.
        Socket connection;      // For communication with the connecting program.

        /* Accept and process connections forever, or until some error occurs. */

        try {
            listener = new ServerSocket(LISTENING_PORT);
            System.out.println("Listening on port " + LISTENING_PORT);
            while (true) {
                  connection = listener.accept();
                  ConnectionHandler h = new ConnectionHandler(connection);
                  h.start();
            }
        }
        catch (Exception e) {
            System.out.println("Sorry, the server has shut down.");
            System.out.println("Error:  " + e);
            return;
        }

    }  // end main()

    public static void addUser(){
        numUsers ++;
    }
    /**
     *  Defines a thread that handles the connection with one
     *  client.
     */
    private static class ConnectionHandler extends Thread {
        private static ArrayList<ConnectionHandler> connectionList;
        Socket client;
        ObjectOutputStream oos;
        ObjectInputStream ois;
        public static int idNum;
        String name;

        ConnectionHandler(Socket socket) {
            client = socket;
            name = "User" + idNum;
            idNum ++;
            if(connectionList == null)
                connectionList = new ArrayList<ConnectionHandler>();
            connectionList.add(this);
            try{
                ois = new ObjectInputStream(client.getInputStream());
                oos = new ObjectOutputStream(client.getOutputStream());
                //buffered stuff goes here if you want
            }
            catch(IOException e){}
        }
        public void run() {
            String clientAddress = client.getInetAddress().toString();
            
            while(true) {
                int i=0;
	            try {
	            	String message = (String)ois.readObject();
                    while(i < connectionList.size()){
                        ConnectionHandler h = connectionList.get(i);
                        h.oos.writeObject(name + ": " + message);
                        h.oos.flush();
                        i++;
                    }
	            }
	            catch (EOFException e){
	                connectionList.remove(i);
	            }
                catch (Exception e){
                    System.out.println("Cry");
                }
            }
        }
    }
}