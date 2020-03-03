package Message;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class message {

    public static String ourTeamName = "Shenhav&Shauli Code Cracking Inc1";

    private String teamName;
    private char type;
    private String hash;
    private char originalLength;
    private String start;
    private String end;
    private InetAddress ip;
    private int port;


    public message(char type, String hash, char originalLength, String start, String end){
        this.teamName = ourTeamName;
        this.type = type;
        this.hash = hash;
        this.originalLength = originalLength;
        this.start = start;
        this.end = end;
        this.ip = null;
        this.port = 0;
    }

    public message(byte[] data, InetAddress ip, int port){
        this.teamName = new String(data,0,32);
        this.type = (char) data[32];
        this.hash = new String(data,32+1,40);
        this.originalLength = (char) data[32+1+40];
        this.start = new String(data,32 + 1 + 40 + 1,originalLength);
        this.end = new String(data,32 + 1 + 40 + 1+256,originalLength);
        this.ip = ip;
        this.port = port;
    }

    public DatagramPacket getPacket(){
        int length  = 32 + 1 + 40 + 1 + 256 + 256;
        byte[] bytes = new byte[length];
        int offset = 0;
        bytes = copyStringToArray(bytes, teamName, offset);
        offset += 32;
        bytes[offset] = (byte)type;
        offset ++;
        bytes = copyStringToArray(bytes, hash, offset);
        offset += 40;
        bytes[offset] = (byte)originalLength;
        offset ++;
        String newStart = start+(new String(new byte[256-originalLength]));
        bytes = copyStringToArray(bytes, newStart, offset);
        offset += 256;
        String newEnd = end+(new String(new byte[256-originalLength]));
        bytes = copyStringToArray(bytes, newEnd, offset);
        return new DatagramPacket(bytes, length);
    }

    private byte[] copyStringToArray(byte[] bytes, String input, int offset){
        byte[] inputArr =  input.getBytes();
        int length = inputArr.length;
        for(int i = 0; i < length; i++)
            bytes[i+offset] = inputArr[i];
        return bytes;
    }


    public String getTeamName() {
        return teamName;
    }

    public char getType() {
        return type;
    }

    public String getHash() {
        return hash;
    }

    public char getOriginalLength() {
        return originalLength;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public InetAddress getIp(){
        return ip;
    }

    public void setIp(InetAddress ip){
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
