package com.g8.tp2.server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.g8.tp2.common.CryptoUtil.decryptWithPrivateRsa;
import static com.g8.tp2.common.CryptoUtil.decryptWithPublicRsa;
import static com.g8.tp2.common.CryptoUtil.hashMessage;

public class OttawaServer
{
    static Socket socket;
    static ServerSocket listener;
    static int portListen = 8080;
    final static String destinationIp = "239.255.1.1";
    final static int portSend = 6666;

    public static void main(String[] args) throws IOException
    {
        // listen to socket for connections
        listener = new ServerSocket(portListen);

        System.out.println("\nServer listening on port " + listener.getLocalPort());

        // start pool of threads
        ExecutorService executor = Executors.newFixedThreadPool(1);

        System.out.println("Waiting for connection...\n");

        // run the pool of threads 5 times
        for(int i = 0; i<5; i++)
        {
            executor.submit(new waitForConnection());
        }

        // shut down once done
        executor.shutdown();
    }

    public static class waitForConnection implements Runnable
    {
        public void run() {

            try {
                socket = listener.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedReader br = null;

            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            InetAddress addr = null;
            try {
                addr = InetAddress.getByName(destinationIp);
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }

            DatagramSocket serverSocket = null;
            try {
                serverSocket = new DatagramSocket();
            } catch (SocketException e1) {
                e1.printStackTrace();
            }

            // Get the RSA public Key of the remote server from the the selected file
            ObjectInputStream inputStream = null;
            PublicKey publicKey = null;
            try {
                inputStream = new ObjectInputStream(new FileInputStream("./keys/public-south-pole.key"));
            } catch (FileNotFoundException e) {
                System.out.println("RSA SouthPole public key file not found on the server");
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
            System.out.println("SouthPole Public Key: " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));

            // Get the RSA private key from selected file
            PrivateKey privateKey = null;
            try {
                inputStream = new ObjectInputStream(new FileInputStream("./keys/private-ottawa.key"));
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


            System.out.println("\n-- Decrypted messages from military base --\n");
            String cipherMessage = "";
            byte[] messageBytes = null;
            byte[] receivedHashBytes = null;
            String message = "";
            String receivedHash = "";
            String newHash = "";
            boolean isHash = false;
            String command = "";

            while(true)
            {
                //Read the line
                try {
                    cipherMessage = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                command = cipherMessage;

                // Create the structure to forward messages
                DatagramPacket msgPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, addr, OttawaServer.portSend);

                //Check if it is the final message
                if(command.equals("q"))
                {
                    //Stops the while true
                    break;
                }

                //Start decryption, one line on two is a message
                if (isHash == false) 
                {
                    try {
                        messageBytes = decryptWithPrivateRsa(privateKey, cipherMessage);
                    } catch (Exception e) {
                        System.out.println("Décryptage du message en RSA échouée");
                        e.printStackTrace();
                    }
                    isHash = true;
                    message = new String(messageBytes);
                    System.out.println(new String(messageBytes));
                }
                else
                {
                    // Decrypt the received hash
                    try {
                        receivedHashBytes = decryptWithPublicRsa(publicKey, cipherMessage);
                    } catch (Exception e) {
                        System.out.println("Décryptage du hash en RSA échouée");
                        e.printStackTrace();
                    }
                    isHash = false;
                    receivedHash = new String(receivedHashBytes);
                    //System.out.println("Hash original du message : " + receivedHash);

                    //Hash the decrypted message
                    try {
                        newHash = hashMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //System.out.println("Hash du message : " + newHash);

                    // Compare hash
                    if(receivedHash.equals(newHash))
                    {
                        System.out.println("[ce message est le même que celui envoyé]\n");

                        // send messages to multicast IP
                        try {
                            serverSocket.send(msgPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        System.out.println("[-- ! ce message a été modifié depuis son envoi ! --]\n");
                    }
                }
            }

            System.out.println("\n-------------------------------------------\n");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

