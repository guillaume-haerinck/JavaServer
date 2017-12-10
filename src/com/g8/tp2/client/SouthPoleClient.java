package com.g8.tp2.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static com.g8.tp2.common.CryptoUtil.generateRsaKeyPair;

public class SouthPoleClient
{
    public static String destinationIp;
    public static int destinationPort;

    public static void main(String[] args) throws UnknownHostException, IOException
    {
        String[] ipAndPort = {"", ""};

        // check if there are command line arguments
        if(args.length != 0)
        {
            ipAndPort = args[0].split(":");
            destinationIp = ipAndPort[0];
            destinationPort =  Integer.parseInt(ipAndPort[1]);
        }
        // if there are no command line arguments, ask for them
        else
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter the IP of the server, and the port number");
            System.out.println("If server is local enter 127.0.0.1, the port number is 29 for TCP");
            System.out.println("ip:port");
            System.out.printf("> ");

            // Get the arguments
            ipAndPort[0] = br.readLine();
            ipAndPort = ipAndPort[0].split(":");
            destinationIp = ipAndPort[0];
            destinationPort =  Integer.parseInt(ipAndPort[1]);
        }

        // connect to local port 29 TCP protocol
        System.out.println("\nConnecting to " + destinationIp + " with the port " + destinationPort + "...");
        Socket socket = new Socket(destinationIp, destinationPort);
        System.out.println("Connected");

        // Define output stream to send message
        OutputStream ostream = socket.getOutputStream();
        PrintWriter pwrite = new PrintWriter(ostream, true);

        System.out.println("\nEnter 'q' to send the message");

        Scanner in = new Scanner(System.in);
        String msg = "";

        while(true)
        {
            System.out.printf("> ");
            msg = in.nextLine();
            pwrite.println(msg);

            // end connection
            if(msg.equals("q"))
            {
                System.out.println("Closing the connection on SouthPoleClient");
                socket.close();
                break;
            }

            // flush pwrite
            pwrite.flush();
        }
    }
}

