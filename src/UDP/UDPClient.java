package UDP;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class UDPClient
{
    private static final int SERVER_PORT = 9876;
//    private static final int CLIENT_PORT = 6789;

    public static void main(String args[]) throws Exception
    {
        // Send an initial message to the server to initiate communication
        DatagramSocket socket = new DatagramSocket();
        String initialMessage = String.format("Client from port %d started and ready!", socket.getLocalPort());

        InetAddress address = InetAddress.getByName("localhost");

        byte[] sendData = initialMessage.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
        socket.send(sendPacket);

        // Read in the main menu from the server
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
        System.out.print(receivedMessage);

        // Convert user integer input to bytes
        int selection = Utils.menuSelection(1, 5);
        System.out.println("You selected: " + selection);

        sendData = String.valueOf(selection).getBytes();

        // Send menu selection to server
        sendPacket = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
        socket.send(sendPacket);

//        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
//
//        DatagramSocket clientSocket = new DatagramSocket();
//
//        InetAddress IPAddress = InetAddress.getByName("localhost");
//
//        byte[] sendData;
//        byte[] receiveData = new byte[1024];
//
//        System.out.println("The UDP client is on. Please enter your input:");
//
//        String sentence = inFromUser.readLine();
//        sendData = sentence.getBytes();
//
//        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 6789);
//
//        clientSocket.send(sendPacket);
//
//        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//
//        clientSocket.receive(receivePacket);
//
//        String modifiedSentence = new String(receivePacket.getData());
//
//        System.out.println("FROM SERVER:" + modifiedSentence);
//        clientSocket.close();
    }
}
