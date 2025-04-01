package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class UPDServer
{
    private static final int SERVER_PORT = 9876;

    // create ArrayLists to keep track of current and previous fundraisers
    private static ArrayList<Event> currentEvents = new ArrayList<>();
    private static ArrayList<Event> pastEvents = new ArrayList<>();
    private static final Map<Integer, ClientManager> clientThreads = new HashMap<>();

    public static void main(String args[]) throws Exception
    {
        DatagramSocket clientSocket = new DatagramSocket(SERVER_PORT);

        // Allow for addresses to be reused to prevent errors
        clientSocket.setReuseAddress(true);

        System.out.println("The GoFundMe UDP server is on!\n" +
                "-----------------------------------------------------------\n");

        // An integer to keep track of how many clients connected to the server
        int clientCount = 0;

        // may need to be createdin the while loop
        byte[] receiveData = new byte[1024];

        while (true)
        {
            DatagramPacket clientInputPacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(clientInputPacket);
            String message = new String(clientInputPacket.getData(), 0, clientInputPacket.getLength());
            System.out.println(message);

            InetAddress address = clientInputPacket.getAddress();
            int port = clientInputPacket.getPort();

            // Try to get the client from the HashMap
            ClientManager clientThread = clientThreads.get(port);

            // If client doesn't exist, make a new thread for it and add it to the HashMap
            if(clientThread == null)
            {
                clientThread = new ClientManager(clientSocket, address, port);
                clientThreads.put(port, clientThread);
                new Thread(clientThread).start();

                System.out.printf("A new client packet connected! A new thread was created for it." +
                    "\nTotal clients visited: %d" +
                    "\nNew client's address: %s:%s\n" +
                    "\n", ++clientCount, address, port);
            }
            else
            {
                System.out.println("Client with port " + port  + " sent new packages");

                // Otherwise, send the necessary information to the existing client
                clientThread.processClientMessage(clientInputPacket);
            }
        }
    }

    private static class ClientManager implements Runnable
    {
        private DatagramSocket clientSocket;
        private DatagramPacket clientInputPacket;
        private DatagramPacket outputPacket;
        private final InetAddress ADDRESS;
        private final Queue<DatagramPacket> messageQueue = new LinkedList<>();
        private final int PORT;
        private static final ReentrantLock LOCK = new ReentrantLock();

        private ClientManager(DatagramSocket clientSocket, InetAddress address, int port)
        {
            this.clientSocket = clientSocket;
            this.ADDRESS = address;
            this.PORT = port;
        }

        @Override
        public void run()
        {
            try
            {
                int selection = prompt("Welcome to GoFundMe 2.0 (simplified edition).\nWhat would you like to do?");
                System.out.println("Client selection: " + selection);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        private int prompt(String prompt)
        {
            try
            {
                String mainPrompt = prompt +
                        "\n1) Create an event" +
                        "\n2) Contribute to an event" +
                        "\n3) View all current events" +
                        "\n4) View all past events" +
                        "\n5) Exit" +
                        "\n> ";

                byte[] outputData = mainPrompt.getBytes();
                outputPacket = new DatagramPacket(outputData, outputData.length, this.ADDRESS, this.PORT);
                clientSocket.send(outputPacket);
                System.out.printf("Sent a message to a client. Message \"%s\" came from prompt() method\n",
                        mainPrompt);

                System.out.println("entering receiveClientInput");
                return receiveClientInput();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            // If -1 is returned, something went wrong
            return -1;
        }

        private int receiveClientInput()
        {
            while(true)
            {
                // Server program doesn't proceed without this. Not completely sure why...
                System.out.print("");

                if(!messageQueue.isEmpty())
                {
                    DatagramPacket receivedPacket = messageQueue.poll();

                    ByteBuffer buffer = ByteBuffer.wrap(receivedPacket.getData());
                    int selection = buffer.getInt();
                    System.out.println("Received number from client: " + selection);

                    // Convert the user's input from bytes to an int
                    return selection;
                }
            }
        }

        private void processClientMessage(DatagramPacket packet)
        {
            messageQueue.add(packet);
        }
    }
}
