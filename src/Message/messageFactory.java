package Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class messageFactory {

    public static message getMessageFromSocket(DatagramSocket socket) throws IOException {
        DatagramPacket dp = new DatagramPacket(new byte[586], 586);
        socket.receive(dp);
        return new message(dp.getData(), dp.getAddress(), dp.getPort());
    }

    public static message getDiscoverMessage(){
        String hash = new String(new char[40]);
        return new message((char)1,hash,(char)1, "a", "z");
    }

    public static message getOfferMessage(){
        String hash = new String(new char[40]);
        return new message((char)2,hash,(char)1, "a", "z");
    }

    public static message getRequestMessage(String hashed, int length, String start, String end) {
        return new message((char)3,hashed,(char)length,start,end);
    }

    public static message getAckMessage(String answer){
        String hash = new String(new char[40]);
        return new message((char)4,hash,(char)answer.length(),answer,answer);
    }

    public static message getNackMessage(){
        String hash = new String(new char[40]);
        return new message((char)5,hash,(char)1,"a","z");
    }
}
