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
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SocketClient{
   
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException{
        //get the localhost IP address, if server is running on some other IP, you need to use that
        ChatServerWithThreads.addUser();
        InetAddress host = InetAddress.getLocalHost();
        try (Socket socket = new Socket(host.getHostName(), 52000)) {
            //write to socket using ObjectOutputStream
        ObjectOutputStream   oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
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
        } catch (HeadlessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}