package Server;

import Message.message;
import Message.messageFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class server {

    private BlockingQueue<message> InputTasks = new LinkedBlockingQueue<>();
    private BlockingQueue<message> OutputTasks = new LinkedBlockingQueue<>();
    private DatagramSocket socket;
    private int numOfThreads = 6;
    private long MaxProccessTime = 10;

    public void start(){
        startSocket();
        setInputQueueThread();
        setOutputQueueThread();
        setMessageThreads();
    }

    private void startSocket() {
        try {
            socket = new DatagramSocket(3117);
            socket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    private void setOutputQueueThread() {
        Thread thread = new Thread(() -> {
            try {
                while(true) {
                    message m = OutputTasks.take();
                    promptText("Sending Message");
                    DatagramPacket dp = m.getPacket();
                    dp.setAddress(m.getIp());
                    dp.setPort(m.getPort());
                    socket.send(dp);
                    promptText("Message Sent");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void setInputQueueThread() {
        Thread thread = new Thread(() -> {
            try {
                while(true) {
                    promptText("Waiting for messages...");
                    InputTasks.put(messageFactory.getMessageFromSocket(socket));
                    promptText("Received message");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }



    private void setMessageThreads() {
        for(int i = 0; i < numOfThreads; i++){
            Thread thread = new Thread(()->{
                while(true) {
                    try {
                        message m = InputTasks.take();
                        message respond = null;
                        if (m.getType() == 1) {
                            promptText("Handling Discover message from team " + m.getTeamName());
                            respond = messageFactory.getOfferMessage();
                        }
                        else if (m.getType() == 3){
                            promptText("Handling Request message from team " + m.getTeamName());
                            String ans = findHashed(m.getStart(), m.getEnd(), m.getHash());
                            if (ans == null) {
                                respond = messageFactory.getNackMessage();
                                promptText("Didn't found hash for team " + m.getTeamName());
                            }
                            else {
                                respond = messageFactory.getAckMessage(ans);
                                promptText("Found hash for team " + m.getTeamName());

                            }
                        }
                        else{
                            promptText("Can't handle message of type "+m.getType()+" for team "+m.getTeamName());
                            continue;
                        }
                        respond.setIp(m.getIp());
                        respond.setPort(m.getPort());
                        OutputTasks.put(respond);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
            thread.start();
        }
    }


    private String findHashed(String start, String end, String hashed){
        LocalDateTime startTime = LocalDateTime.now();
        String curr = start;
        while (!curr.equals(end)){
            if(ChronoUnit.SECONDS.between(startTime,LocalDateTime.now())>MaxProccessTime)
                return null;
            if (SHA1(curr).equals(hashed))
                return curr;
            curr = nextString(curr);
        }
        if (SHA1(curr).equals(hashed))
            return curr;
        return null;
    }

    private String nextString(String s){
        char[] sArray = s.toCharArray();
        int index = sArray.length - 1;
        while (sArray[index] == 'z'){
            sArray[index] = 'a';
            index --;
        }
        sArray[index] ++;
        return new String(sArray);
    }

    private String SHA1(String text) {
        MessageDigest message = null;
        try {
            message = MessageDigest.getInstance("SHA-1");
            byte[] messagesArray = message.digest(text.getBytes());
            BigInteger i = new BigInteger(1, messagesArray);
            StringBuilder hashed = new StringBuilder(i.toString(16));
            while (hashed.length() < 40) {
                hashed.insert(0, "0");
            }
            return hashed.toString();
        } catch (NoSuchAlgorithmException e) {
            throw  new RuntimeException(e);
        }
    }

    private void promptText(String text){
        String name = Thread.currentThread().getName();
        System.out.println(name + " -- " + text);
    }

}
