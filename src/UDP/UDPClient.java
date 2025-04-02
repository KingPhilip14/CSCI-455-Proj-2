package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient
{
    private static final int SERVER_PORT = 9876;
//    private static final int CLIENT_PORT = 6789;

    public static void main(String args[]) throws Exception
    {
        // Create and send an initial message to the server to initiate communication
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        String initialMessage = String.format("Client from port %d started and ready!", socket.getLocalPort());

        byte[] sendData = initialMessage.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
        socket.send(sendPacket);

        // Read in the main menu from the server
        printServerMessage(socket);

        // Convert user integer input to bytes
        int selection = Utils.menuSelection(1, 5);

        sendData = String.valueOf(selection).getBytes();

        // Send menu selection to server
        sendPacket = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
        socket.send(sendPacket);

        // Creating an event logic
        createEvent(socket, address);
    }

    private static void printServerMessage(DatagramSocket socket)
    {
        try
        {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.print(receivedMessage);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void sendDataToServer(DatagramSocket socket, InetAddress address)
    {
        try
        {
            String data = new Scanner(System.in).nextLine();
            byte[] sendData = data.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
            socket.send(sendPacket);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void createEvent(DatagramSocket socket, InetAddress address)
    {
        byte[] receiveData = new byte[1024];
        createEventName(socket, address);
        createEventGoal(socket, address);
        createEventDeadline(socket, address);
        printServerMessage(socket);
    }

    private static void createEventName(DatagramSocket socket, InetAddress address)
    {
        // Receive the prompt to make the event name
        printServerMessage(socket);

        // Send event name
        sendDataToServer(socket, address);
    }

    private static void createEventGoal(DatagramSocket socket, InetAddress address)
    {
        // Receive the prompt to make the event goal
        printServerMessage(socket);

        // Send event goal
        sendDataToServer(socket, address);
    }

    private static void createEventDeadline(DatagramSocket socket, InetAddress address)
    {
        // Receive the prompt to make the event deadline
        printServerMessage(socket);

        // Send event deadline
        sendDataToServer(socket, address);
    }
}
