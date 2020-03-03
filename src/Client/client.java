package Client;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import Message.*;

public class client {

    private static int waitForServer = 15;

    private static Scanner scanner = new Scanner(System.in);
    private static ArrayList<InetAddress> ips = new ArrayList();
    private static DatagramSocket socket;

    public static void main(String args[]){

        System.out.println("Hello, welcome to '" + message.ourTeamName + "' team");

        String hashed;
        while(true) {
            System.out.println("Please enter the hash:");
            try {
                hashed = scanner.next();
                if(hashed.length()==40) break;
                else throw new Exception();
            } catch(Exception e){
                System.out.println("Illegal hash, please try again");
            }
        }

        int hashLength;
        while(true) {
            System.out.println("Please enter the input string length:");
            try {
                hashLength = Integer.parseInt(scanner.next());
                break;
            } catch (Exception e) {
                System.out.println("Illegal hash, please try again");
            }
        }
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Shenhav failed to connect to the server, as she usually does, so we can't help you today.. sorry, bye");
            return;
        }

        findServers();
        String[] ranges = getRanges(hashLength, ips.size());
        sendRanges(hashed,hashLength,ranges);
        String output = getMessagesFromServers();
        System.out.println("The input string is "+output);
    }

    private static String getMessagesFromServers() {
        try {
            socket.setSoTimeout(waitForServer * 1000);
            int ans = 0;
            try{
                System.out.println("Waiting for the servers to respond");
                while(true) {
                    message m = messageFactory.getMessageFromSocket(socket);
                    if (m.getType() == 5)
                        ans++;
                    else if(m.getType()==4){
                        return m.getStart();
                    }
                }
            } catch(SocketTimeoutException e){}
            if(ans!=ips.size())
                throw new Exception();
            else
                throw new RuntimeException("Shenhav's servers were unable to find the key.. sorry, bye");
        } catch (Exception e) {
            throw new RuntimeException("Shenhav has faced some difficulties with the servers.. sorry, bye");
        }
    }


    private static void sendRanges(String hashed,int length, String[] ranges) {
        for(int i = 0; i < ips.size(); i++){
            message req = messageFactory.getRequestMessage(hashed, length, ranges[2 * i], ranges[2 * i + 1]);
            DatagramPacket dp = req.getPacket();
            dp.setAddress(ips.get(i));
            dp.setPort(3117);
            try {
                socket.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void findServers(){
        message msg = messageFactory.getDiscoverMessage();
        try {
            socket.setBroadcast(true);
            DatagramPacket dp = msg.getPacket();
            dp.setAddress(InetAddress.getByName("255.255.255.255"));
            dp.setPort(3117);
            socket.send(dp);
//            networkBroadcast(socket,dp);
            socket.setSoTimeout(1000);
            try {
                while(true) {
                    message m = messageFactory.getMessageFromSocket(socket);
                    if (m.getType() == 2)
                        ips.add(m.getIp());
                }
            } catch(SocketTimeoutException e){}
            if(ips.size() == 0)
                throw new RuntimeException("Shenhav was unable to find any servers right now.. sorry, bye");
        } catch (Exception e) {
            throw new RuntimeException("Shenhav has faced some difficulties with the servers.. sorry, bye");
        }
    }

    /*private static void networkBroadcast(DatagramSocket socket,DatagramPacket dp) throws SocketException {
        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue; // Don't want to broadcast to the loopback interface
            }
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast == null) {
                    continue;
                }
                try {
                    socket.send(dp);
                } catch (Exception e) {
                }
            }
        }
    }*/

    private static String[] getRanges(int hashLength, int numServers){
        String[] ranges = new String[numServers * 2];
        int numOfStrings = (int)Math.pow(26, hashLength);
        int rangePerServer = numOfStrings / numServers;
        int counter = 0;
        for (int i = 0; i < numServers; i++){
            ranges[2 * i] = backToString(counter, hashLength);
            counter += rangePerServer;
            ranges[2 * i + 1] = backToString(counter, hashLength);
            counter++;
        }
        ranges[ranges.length - 1] = finalString(hashLength);
        return ranges;
    }

    private static String finalString(int hashLength) {
        char[] endOfString = new char[hashLength];
        for (int i = 0; i < hashLength; i++){
            endOfString[i] = 'z';
        }
        return new String(endOfString);
    }

    private static String backToString(int num, int hashLength){
        char[] output = new char[hashLength];
        for (int i = 0; i < hashLength; i++){
            output[i] = (char)('a' + (num % 26));
            num = num / 26;
        }
        return new String(output);
    }

}
