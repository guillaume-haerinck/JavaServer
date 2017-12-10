package com.g8.tp2.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.ObjectInputStream;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.security.PrivateKey;
import java.security.PublicKey;

import static com.g8.tp2.common.CryptoUtil.encryptFromPrivateRsa;
import static com.g8.tp2.common.CryptoUtil.encryptFromPublicRsa;
import static com.g8.tp2.common.CryptoUtil.hashMessage;

public class SouthPoleServer
{

    public static Socket socket;
    public static ServerSocket listener;
    public static String interfaceListen;
    public static int portListen;
    public static String interfaceSend;
    public static int portSend = 8080;

    public static void main(String[] args) throws IOException, InterruptedException
    {
        String[] ipAndPort = {"", ""};

        // check if there are command line arguments
        if(args.length != 0)
        {
            ipAndPort = args[0].split(":");
            interfaceListen = ipAndPort[0];
            portListen =  Integer.parseInt(ipAndPort[1]);

            if (args.length > 1)
            {
                interfaceSend = args[1];
            }
        }
        // if there are no command line arguments, ask for them
        else
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter the interface to listen with, and the port number to listen to");
            System.out.println("TCP is port 29");
            System.out.println("ip:port");
            System.out.printf("> ");

            // Get the arguments
            ipAndPort[0] = br.readLine();
            ipAndPort = ipAndPort[0].split(":");
            interfaceListen = ipAndPort[0];
            portListen =  Integer.parseInt(ipAndPort[1]);

            System.out.println("\nEnter the interface used to forward the messages");
            System.out.printf("> ");
            interfaceSend = br.readLine();
        }

        // Listen on a port
        try {
            listener = new ServerSocket(portListen);
        } catch (IOException e) {
        }

        System.out.println("\nServer listening on port " + listener.getLocalPort() + " with the interface " + interfaceListen);

        // Starting a thread pool. One thread that is being called 5 times to connect to 5 SouthPoleClients
        ExecutorService executor = Executors.newFixedThreadPool(1);

        System.out.printf("Waiting for connection...\n");

        // starting thread pool, that will take up to 5 connections
        for(int i = 0; i<5; i++)
        {
            executor.submit(new WaitForConnection(i));
        }

        // shutting down thread pool once the work is done
        executor.shutdown();

    }

    // Thread for waiting connection
    public static class WaitForConnection implements Runnable {
        private int id;

        // Constructor
        public WaitForConnection(int id)
        {
            // SouthPoleClient number (whether first or second or third ...)
            this.id = id;
        }

        public void run()
        {
            // Listening on port 29 for TCP connections
            try {
                socket = listener.accept();
            } catch (IOException e) {
            }

            System.out.printf("Connection accepted from SouthPoleClient %d\n", id + 1);

            // start new thread for receiving messages
            Thread t2 = new Thread(new ReceiveMsgs());
            t2.start();
        }
    }

    public static class ReceiveMsgs implements Runnable {

        public void run()
        {
            // message can be of maximum length 200
            String[] msg = new String[200];

            // number of messages received
            int countMsg = 0;

            // for reading messages from socket
            BufferedReader br = null;

            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
            }

            while(true)
            {
                // read line
                try {
                    msg[countMsg] = br.readLine();
                } catch (IOException e) {
                }

                // end the connection when receive 'q' in message
                if(msg[countMsg].equals("q"))
                {
                    System.out.println("Connection terminated with a SouthPoleClient\n");
                    break;
                }

                countMsg++;
            }

            // send all the messages received to destination ip and port after encrypting
            Thread forwarding = new Thread(new ForwardTheMsg(msg, countMsg));

            // start thread
            forwarding.start();

            // close socket with current SouthPoleClient
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    public static class ForwardTheMsg implements Runnable {

        private String[] m_allMsg = new String[200];
        private int m_countMsg;

        // Constructor
        ForwardTheMsg(String[] allMsg, int countMsg)
        {
            m_allMsg = allMsg;
            m_countMsg = countMsg;
        }

        public void run() {
            Socket outputSocket = null;

            System.out.printf("Messages being sent to %s port %d\n\n", SouthPoleServer.interfaceSend, SouthPoleServer.portSend);

            try {
                outputSocket = new Socket(SouthPoleServer.interfaceSend, SouthPoleServer.portSend);
            } catch (UnknownHostException e) {
                System.out.println("Either IP " + SouthPoleServer.interfaceSend + " is wrong or port " + SouthPoleServer.portSend + " or host is not up yet.");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }

            // declare outputstream to send message
            OutputStream oStream = null;
            try {
                oStream = outputSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // define printwriter to send message
            PrintWriter pwrite = new PrintWriter(oStream, true);

            // Get the RSA public Key of the remote server from the the selected file
            ObjectInputStream inputStream = null;
            PublicKey publicKey = null;
            try {
                inputStream = new ObjectInputStream(new FileInputStream("./keys/public-ottawa.key"));
            } catch (FileNotFoundException e) {
                System.out.println("RSA ottawa public key file not found on the server");
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                publicKey = (PublicKey) inputStream.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println("Ottawa Public Key: " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));

            // Get the RSA private key from selected file
            PrivateKey privateKey = null;
            try {
                inputStream = new ObjectInputStream(new FileInputStream("./keys/private-south-pole.key"));
            } catch (FileNotFoundException e) {
                System.out.println("RSA my private key file not found on the server");
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                privateKey = (PrivateKey) inputStream.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            //System.out.println("My Private Key: " + Base64.getEncoder().encodeToString(privateKey.getEncoded()));

            //Start encryption
            byte[] rsaHash = null;
            byte[] rsaMessage = null;
            String messageHash = "";

            System.out.println("\n-- Encrypted messages of SouthPoleClient --\n");
            for (int i = 0; i < m_countMsg; i++)
            {
                try {
                    messageHash = hashMessage(m_allMsg[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    rsaHash = encryptFromPrivateRsa(privateKey, messageHash);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    rsaMessage = encryptFromPublicRsa(publicKey, m_allMsg[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // write encrypted messages to the stream
                pwrite.println(Base64.getEncoder().encodeToString(rsaMessage));
                System.out.println("Message n" + i + " : " + Base64.getEncoder().encodeToString(rsaMessage));

                // write the encrypted hashes of the messages to the stream
                pwrite.println(Base64.getEncoder().encodeToString(rsaHash));
                //System.out.println("Hash of message n" + i + " : " + messageHash + "\n");
            }
            System.out.println("\n-------------------------------------------\n");
            pwrite.println("q");
            try {
                outputSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

