package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient
{
    private static final int SERVER_PORT = 9876;

    public static void main(String args[]) throws Exception
    {
        // Create and send an initial message to the server to initiate communication
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        String initialMessage = String.format("Client from port %d started and ready!", socket.getLocalPort());

        byte[] sendData = initialMessage.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
        socket.send(sendPacket);

        // Start main function here -------------------------------------------------------------------
        int mainMenuSelection = menuSelection(socket, address, 1, 5);

        mainMenuLogic(mainMenuSelection, socket, address);

        // Ask if the user would like to continue using the program
        boolean continueUse = menuSelection(socket, address, 1, 2) == 1;

        while(continueUse)
        {
            mainMenuSelection = menuSelection(socket, address, 1, 5);

            mainMenuLogic(mainMenuSelection, socket, address);

            // Ask if the user would like to continue using the program
            continueUse = menuSelection(socket, address, 1, 2) == 1;
        }
    }

    private static void mainMenuLogic(int selection, DatagramSocket socket, InetAddress address)
    {
        switch(selection)
        {
            case 1:
                createEvent(socket, address);
                break;
            case 2:
                contributeToEvent(socket, address);
                break;
            case 3:
            case 4:
                printServerMessage(socket);
                break;
            default:
                // Exit immediately if the user asks to
                System.out.println("\nThank you for using GoFundMe V2 (simplified edition).\n" +
                        "We hope to see you again!");
                System.exit(0);
                break;
        }
    }

    private static void contributeToEvent(DatagramSocket socket, InetAddress address)
    {
        // Receive a message saying there are no events or a list of all current events
        printServerMessage(socket);
        byte[] receiveData = new byte[4];
        String maxNumStr = "";
        int maxNum = 0;

        // need to receive the amount of current events here; if 0, return
        try
        {
            // Receive the max number of events to contribute to
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            maxNumStr = new String(receivePacket.getData(), 0, receivePacket.getLength());
            maxNum = Integer.parseInt(maxNumStr);

            // exit the method if there are no events to contribute to
            if(maxNum == 0)
            {
                return;
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        // Receive a message on which message to contribute to
        printServerMessage(socket);

        // Select the number from the menu and send it to the server
        menuSelection(socket, address, 1, maxNum);

        // Receive a message asking how much money to donate
        printServerMessage(socket);

        // Send the requested money amount to the server
        sendDataToServer(socket, address, 1);

        // Receive a message confirming the contribution to the event
        printServerMessage(socket);
    }

    private static int menuSelection(DatagramSocket socket, InetAddress address, int min, int max)
    {
        try
        {
            // Read in the main menu from the server
            printServerMessage(socket);

            // Convert user integer input to bytes
            int selection = Utils.menuSelection(min, max);

            byte[] sendData = String.valueOf(selection).getBytes();

            // Send menu selection to server
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
            socket.send(sendPacket);

            return selection;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        // If -1 is returned,something went wrong
        return -1;
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

    private static void sendDataToServer(DatagramSocket socket, InetAddress address, int mode)
    {
        try
        {
            String data;

            // validate the data depending on the mode given; 1 = validateMoney, 2 = validateDeadline
            switch(mode)
            {
                case 1:
                    double amount = Utils.validateMoney();
                    data = String.valueOf(amount);
                    break;
                case 2:
                    data = Utils.validateDeadline();
                    break;
                default:
                    data = new Scanner(System.in).nextLine();
                    break;
            }

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
        sendDataToServer(socket, address, 0);
    }

    private static void createEventGoal(DatagramSocket socket, InetAddress address)
    {
        // Receive the prompt to make the event goal
        printServerMessage(socket);

        // Send event goal
        sendDataToServer(socket, address, 1);
    }

    private static void createEventDeadline(DatagramSocket socket, InetAddress address)
    {
        // Receive the prompt to make the event deadline
        printServerMessage(socket);

        // Send event deadline
        sendDataToServer(socket, address, 2);
    }
}
