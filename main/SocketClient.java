package com.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class SocketClient {

   public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
       InetAddress host = InetAddress.getLocalHost();
       Socket socket = new Socket(host.getHostName(), 52004);
       
       ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
       ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
       
       String assignedName = (String) ois.readObject();
       
       JFrame frame = new JFrame("RPS Client - " + assignedName);
       frame.setLayout(new FlowLayout());
       frame.setSize(360, 260);
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
       JTextArea label = new JTextArea(7, 28);
       label.setEditable(false);
       label.setBackground(Color.lightGray);
       label.setText("Choose rock, paper or scissors\n"); 
       frame.add(label);
       
       JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
       JButton rockButton = new JButton("Rock");
       JButton paperButton = new JButton("Paper");
       JButton scissorsButton = new JButton("Scissors");
       
       buttonPanel.add(rockButton);
       buttonPanel.add(paperButton);
       buttonPanel.add(scissorsButton);
       frame.add(buttonPanel);
       
       frame.setVisible(true);

       ActionListener buttonListener = new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               try {
                   JButton clickedButton = (JButton) e.getSource();
                   String move = clickedButton.getText();
                   
                   oos.writeObject(move);
                   oos.flush();
               } catch (IOException e1) {
                   e1.printStackTrace();
                }
           }
       };

       rockButton.addActionListener(buttonListener);
       paperButton.addActionListener(buttonListener);
       scissorsButton.addActionListener(buttonListener);
       
       while(true){
           String input = (String)ois.readObject();
           
           // FIX: Detect combined clear and outcome transmission
           if (input != null && input.startsWith("[CLEAR]")) {
               label.setText(""); // Wipe window text area
               
               // Split out and preserve the raw outcome message
               String[] parts = input.split("\\|");
               if (parts.length > 1) {
                   label.append("Last Round: " + parts[1] + "\n\n");
               }
           } else {
               label.append(input + "\n");
           }
       }
   }
}
