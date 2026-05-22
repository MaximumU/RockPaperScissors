package com.example;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SocketClient{
   
   
    /*
     * Modify this example so that it opens a dialogue window using java swing,
     * takes in a user message and sends it
     * to the server. The server should output the message back to all connected clients
     * (you should see your own message pop up in your client as well when you send it!).
     *  We will build on this project in the future to make a full fledged server based game,
     *  so make sure you can read your code later! Use good programming practices.
     *  ****HINT**** you may wish to have a thread be in charge of sending information
     *  and another thread in charge of receiving information.
    */

    


    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException{
        //get the localhost IP address, if server is running on some other IP, you need to use that
        ChatServerWithThreads.addUser();
        int clientNum = ChatServerWithThreads.numUsers;
        InetAddress host = InetAddress.getLocalHost();
        Socket socket = new Socket(host.getHostName(), 52000);
            //write to socket using ObjectOutputStream
        ObjectOutputStream   oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        String line = "";
        JFrame frame = new JFrame("Chat Client");
        frame.setLayout(new FlowLayout());
        JTextField textField = new JTextField(20); 
        JTextArea label = new JTextArea(5,20);
        label.setEditable(false);
        label.setBackground(Color.lightGray);
        frame.setSize(300, 200);
        frame.add(label);
        frame.add(textField);
   
        frame.setVisible(true);


        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    oos.writeObject(textField.getText());
                    oos.flush();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                
                textField.setText("");
            }
        });
        while(true){
            String input = (String)ois.readObject();
            label.append(input + "\n");
        }
    }
}