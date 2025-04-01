package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class UPDServer
{
    private static final int SERVER_PORT = 9876;

    // create ArrayLists to keep track of current and previous fundraisers
    private static ArrayList<Event> currentEvents = new ArrayList<>();
    private static ArrayList<Event> pastEvents = new ArrayList<>();

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
        byte[] sendData;

        while (true)
        {
            DatagramPacket clientInputPacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(clientInputPacket);

            InetAddress address = clientInputPacket.getAddress();
            int port = clientInputPacket.getPort();

            // Create a new thread to associate with the client's sent packet and start it
            ClientManager clientThread = new ClientManager(clientSocket, address, port);
            new Thread(clientThread).start();

            System.out.printf("A new client packet connected! A new thread was created for it." +
                    "\nTotal clients visited: %d" +
                    "\nNew client's address: %s:%s\n" +
                    "\n", ++clientCount, address, port);

//            String sentence = new String(clientInputPacket.getData(), 0, clientInputPacket.getLength());
//
//            InetAddress IPAddress = clientInputPacket.getAddress();
//
//            int port = clientInputPacket.getPort();
//
//            String capitalizedSentence = sentence.toUpperCase();
//
//            sendData = capitalizedSentence.getBytes();
//
//            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
//
//            server.send(sendPacket);
        }
    }

    private static class ClientManager implements Runnable
    {
        private DatagramSocket clientSocket;
        private DatagramPacket clientInputPacket;
        private DatagramPacket outputPacket;
        private byte[] inputData = new byte[1024];
        private byte[] outputData;
        private final InetAddress ADDRESS;
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
                int selection = prompt("Welcome to GoFundMe (simplified edition v2). What would you like to do?");

                System.out.println("Selection: " + selection);
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

                outputData = mainPrompt.getBytes();
                outputPacket = new DatagramPacket(outputData, outputData.length, this.ADDRESS, this.PORT);
                clientSocket.send(outputPacket);
                System.out.printf("Sent a message to a client. Message \"%s\"\n came from prompt() method\n",
                        mainPrompt);

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
            try
            {
                // Make a buffer for getting the user's input
                inputData = new byte[4];
                clientInputPacket = new DatagramPacket(inputData, inputData.length);
                clientSocket.receive(clientInputPacket);

                // Convert the user's input from bytes to an int
                ByteBuffer buffer = ByteBuffer.wrap(inputData);
                return buffer.getInt();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            // If -1 is returned, something went wrong
            return -1;
        }
    }
}
