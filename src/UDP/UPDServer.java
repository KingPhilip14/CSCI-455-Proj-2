package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
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
                System.out.println("Client with port " + port  + " sent new packages\n");

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

                manageClientMenuInput(selection);

                // If the user exits immediately, don't perform anymore logic
                if(selection == 5)
                {
                    return;
                }

                // Continue using the program if the client's input equals 1
                boolean continueUse = continueProgram();

                while(continueUse)
                {
                    System.out.printf("Client at port %d is continuing use of the server\n", PORT);

                    selection = prompt("\nWhat else would you like to do?");
                    manageClientMenuInput(selection);

                    continueUse = continueProgram();
                }
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
                System.out.printf("Sent a message to client with port %d. Message came from prompt() method\n",
                        PORT);

                return Integer.parseInt(receiveClientInput());
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            // If -1 is returned, something went wrong
            return -1;
        }

        private String receiveClientInput()
        {
            while(true)
            {
                // Server program doesn't proceed without this. Not completely sure why...
                System.out.print("");

                if(!messageQueue.isEmpty())
                {
                    DatagramPacket receivedPacket = messageQueue.poll();

                    return new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                }
            }
        }

        private void processClientMessage(DatagramPacket packet)
        {
            messageQueue.add(packet);
        }

        private void manageClientMenuInput(int clientSelection)
        {
            switch(clientSelection)
            {
                case 1:
                    handleEventCreation();
                    break;
                case 2:
                    contributeToEvent();
                    break;
                case 3:
                    viewCurrentEvents();
                    break;
                case 4:
                    viewPastEvents();
                    break;
                default:
                    sendMessageToClient("Connection with the server is being severed...");
                    Thread.currentThread().interrupt();
                    System.out.printf("\nCommunications with client at port %d ended.\n", PORT);
                    break;
            }
        }

        private void contributeToEvent()
        {
            System.out.printf("\nClient at port %d is trying to contribute to an new event.", PORT);

            viewCurrentEvents();

            // If there are no current events, simply exit this method to proceed with the program
            if(currentEvents.isEmpty())
            {
                return;
            }

            int index;

            try
            {
                sendMessageToClient("Which event would you like to contribute to?\n" +
                        "Please provide one of the numbers for the listed events > ");

                System.out.printf("Sending count of current events to client at port %d. Number of events: %d\n",
                        PORT, currentEvents.size());

                // Since there are events to contribute to, send information to the client to select their choice
                byte[] outputData = String.valueOf(currentEvents.size()).getBytes();
                outputPacket = new DatagramPacket(outputData, outputData.length, this.ADDRESS, this.PORT);
                clientSocket.send(outputPacket);

                // Get the client's choice
                index = Integer.parseInt(receiveClientInput());
            }
            catch(IOException e)
            {
                System.out.println("Something went wrong sending the size of the currentEvents ArrayList to the " +
                        "client. Aborting...");
                return;
            }

            LOCK.lock();
            Event selectedEvent = currentEvents.get(--index);
            LOCK.unlock();

            double contribution;

            // Ask how much money the user would like to contribute
            sendMessageToClient("How much money would you like to contribute? > ");
            contribution = Double.parseDouble(receiveClientInput());

            // Add the money to the event and print its updated information
            LOCK.lock();
            selectedEvent.addMoney(contribution);
            LOCK.unlock();

            DecimalFormat df = new DecimalFormat("#.00");
            String formattedContribution = df.format(contribution);

            String message = String.format("\nYou contributed %s to %s." +
                    "\nHere is the event's updated information:" +
                    "\nIndex:\t\t\t\t\t\t%d" +
                    "\n%s" +
                    "\n", formattedContribution, selectedEvent.getName(), index + 1, selectedEvent);

            sendMessageToClient(message);

            System.out.println("A client successfully donated to an event.");
        }

        private void handleEventCreation()
        {
            System.out.printf("\nClient at port %d is trying to create a new event.", PORT);

            // Get the event details from the client
            String eventName = createEventName();
            System.out.printf("Successfully received event name from client at port %d\n", PORT);

            double goal = Double.parseDouble(createEventGoal());
            System.out.printf("Successfully received event goal from client at port %d\n", PORT);

            String deadline = createEventDeadline();
            System.out.printf("Successfully received event deadline from client at port %d\n", PORT);

            Event newEvent = new Event(eventName, goal, deadline);
            addCurrentEvent(newEvent);

            System.out.printf("\nA new event was successfully received from client at port %d!\n" +
                    "The event was added to the list of current events." +
                    "\nCurrent event list size: %d" +
                    "\nEvent information:" +
                    "\n%s\n", PORT, currentEvents.size(), newEvent.toString());

            // Send a confirmation message to the client
            sendMessageToClient(String.format("\nYou successfully created an event with the following information:\n" +
                    "%s\n", newEvent.toString()));
        }

        private void addCurrentEvent(Event event)
        {
            LOCK.lock();
            try
            {
                currentEvents.add(event);
                Collections.sort(currentEvents);

                System.out.println();
            }
            finally
            {
                LOCK.unlock();
            }
        }

        private String createEventName()
        {
            try
            {
                byte[] outputData = "\nProvide the name of the event you want to create > ".getBytes();
                outputPacket = new DatagramPacket(outputData, outputData.length, this.ADDRESS, this.PORT);
                clientSocket.send(outputPacket);

                return receiveClientInput();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            // Default event name
            return "Default Event Name #" + currentEvents.size();
        }

        private String createEventGoal()
        {
            try
            {
                byte[] outputData = "Provide the amount of money you'd like to raise (e.g., 100.00) > ".getBytes();
                outputPacket = new DatagramPacket(outputData, outputData.length, this.ADDRESS, this.PORT);
                clientSocket.send(outputPacket);

                return receiveClientInput();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            // Default amount to raise
            return "100.00";
        }

        private String createEventDeadline()
        {
            try
            {
                byte[] outputData = String.format("What day would you like the event to end?\n" +
                        "Please note that every event will conclude at %s\n" +
                        "Provide the input as yyyy-mm-dd > ", Event.CONCLUDING_TIME).getBytes();
                outputPacket = new DatagramPacket(outputData, outputData.length, this.ADDRESS, this.PORT);
                clientSocket.send(outputPacket);

                return receiveClientInput();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            // Default deadline
            return "2100-01-01";
        }

        private void sendMessageToClient(String message)
        {
            try
            {
                byte[] outputData = message.getBytes();
                outputPacket = new DatagramPacket(outputData, outputData.length, this.ADDRESS, this.PORT);
                clientSocket.send(outputPacket);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        /**
         * Prints all current events to the client.
         */
        private void viewCurrentEvents()
        {
            LOCK.lock();
            try
            {
                System.out.println("A client is trying to view the current events list.");

                System.out.println("Updating all events to give client most recent updates.");
                updateEvents();

                if(!currentEvents.isEmpty())
                {
                    printEvents(currentEvents, "current");
                    System.out.println("Printed current events list to a client.");
                }
                else
                {
                    sendMessageToClient("\nThere are no current events. You should create one!\n");
                    System.out.println("Told a client to create an event since the current events list is empty.");
                }
            }
            finally
            {
                LOCK.unlock();
            }
        }

        /**
         * Prints all past events to the client.
         */
        private void viewPastEvents()
        {
            LOCK.lock();
            try
            {
                System.out.println("A client is trying to view the past events list.");

                System.out.println("Updating all events to give client most recent updates.");
                updateEvents();

                if(!pastEvents.isEmpty())
                {
                    printEvents(pastEvents, "past");
                    System.out.println("Printed past events list to a client.");
                }
                else
                {
                    sendMessageToClient("\nThere are no past events. You should create one so it's eventually added!\n");
                    System.out.println("Told a client to create an event since the past events list is empty");
                }
            }
            finally
            {
                LOCK.unlock();
            }
        }

        private void updateEvents()
        {
            for(int i = 0; i < currentEvents.size(); i++)
            {
                Event event = currentEvents.get(i);

                // try to set the event's concluded state to true if it's deadline has been met
                event.updateConcludedState();

                if(event.getHasConcluded())
                {
                    currentEvents.remove(event);
                    pastEvents.add(event);
                    System.out.printf("Event \"%s\" has concluded. It was moved from the current events list to the " +
                            "past events list.\n", event.getName());
                }
            }
        }

        /**
         * Prints the info for every event in a given list to the client.
         */
        private void printEvents(ArrayList<Event> events, String descriptor)
        {
            StringBuilder message = new StringBuilder("\nHere are all " + descriptor + " events: \n");

            for(int i = 0; i < events.size(); i++)
            {
                Event event = events.get(i);

                message.append(String.format("Index:\t\t\t\t\t\t%d" +
                        "\n%s" +
                        "\n", i + 1, event.toString()));
            }

            sendMessageToClient(message.toString());
        }

        /**
         * Asks if the client would like to continue using the program.
         * @return true or false representing if the client would continue using the program
         */
        public boolean continueProgram()
        {
            System.out.println("Asking a client if they want to keep using the server");
            sendMessageToClient("\nWould you like to continue using the program?\n" +
                    "1) Yes\n" +
                    "2) No\n" +
                    "> ");

            boolean willContinue = Integer.parseInt(receiveClientInput()) == 1;

            System.out.println("Client is trying to continue using the program: " + willContinue);

            // Continue using the program if the client's input equals 1
            return willContinue;
        }
    }
}
