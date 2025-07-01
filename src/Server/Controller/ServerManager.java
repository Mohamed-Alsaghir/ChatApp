package Server.Controller;

import Shared.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * The ServerManager class is responsible for managing the server-side operations
 * of a chat application. It listens for incoming client connections, manages active connections,
 * updates online user lists, and handles messages intended for offline users. Each connected
 * client is associated with a ConnectionHandler thread which manages its communication.
 */
public class ServerManager {
    private ConnectionHandler connectionHandler;
    private Socket client;
    private List<ConnectionHandler> connections;
    private LogManager logger;
    private LinkedHashMap<String, List<Message>> savedMessagesForOfflineUsers;
    private int PORT = 9999;

    /**
     * Constructor for the ServerManager class. Initializes necessary fields and establishes the server connection.
     */
    public ServerManager() {
        connections = new ArrayList<>();
        savedMessagesForOfflineUsers = new LinkedHashMap<>();
        logger = new LogManager();
        connect();
        logger.registerServerStartUp();
    }

    /**
     * Establishes a server connection and listens for incoming client connections.
     * When a client connects, it spawns a new ConnectionHandler thread to manage that client's communication.
     */
    private void connect() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while(true) {
                try {
                    client = serverSocket.accept();
                    System.out.println("Connection Successful!");

                    connectionHandler = new ConnectionHandler(client);

                    connectionHandler.start();
                    System.out.println(connections.size());

                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new ConnectionHandler to the list of active connections and updates the list of online users.
     *
     * @param connectionHandler The ConnectionHandler to be added.
     */
    private synchronized void addConnection(ConnectionHandler connectionHandler) {
        connections.add(connectionHandler);
        updateOnlineUsers();
        System.out.println("Connection added successfully!");
    }

    /**
     * Removes a ConnectionHandler from the list of active connections and updates the list of online users.
     *
     * @param connectionHandler The ConnectionHandler to be removed.
     */
    private synchronized void removeConnection(ConnectionHandler connectionHandler) {
        connections.remove(connectionHandler);
        updateOnlineUsers();
    }

    /**
     * Updates and broadcasts the list of online users to all connected clients.
     */
    private void updateOnlineUsers() {
        List<User> onlineUsers = new ArrayList<>();
        for(ConnectionHandler connection : connections){
            onlineUsers.add(connection.user);
        }
        for(ConnectionHandler connection : connections){
            connection.sendMessage(new OnlineUsersList(onlineUsers));
        }
    }


    /**
     * The ConnectionHandler class represents a thread dedicated to handle communication for a
     * specific client. It manages message routing, checking for saved messages, and gracefully
     * shutting down a client connection. Each instance of this class is tied to a specific client.
     */
    public class ConnectionHandler extends Thread {
        private Socket client;
        private Message message;
        private User user;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;

        /**
         * Constructor for the ConnectionHandler class.
         *
         * @param client The client socket associated with this handler.
         */
        public ConnectionHandler(Socket client) {
            this.client = client;
            try {
                oos = new ObjectOutputStream(client.getOutputStream());
                ois = new ObjectInputStream(client.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * The main run method of the ConnectionHandler thread. Handles incoming messages and routes them accordingly.
         */
        public void run() {
            saveClient();
            try {
                while (true) {
                    message = (Message) ois.readObject();
                    if (message.getGroupOfReceivers() == null) {
                        boolean receiverOnline = false;

                        for (ConnectionHandler connection : connections) {
                            if (message.getReceiver().getUserName().equals(connection.user.getUserName())) {
                                connection.sendMessage(message);
                                logger.registerSentMessage(message);
                                receiverOnline = true;
                                break;
                            }
                        }

                        if (!receiverOnline) {
                            saveOfflineMessage(message);
                            logger.registerSavedMessage(message);
                        }
                    } else {
                        List<User> offlineReceivers = new ArrayList<>();

                        for (User groupMember : message.getGroupOfReceivers()) {
                            boolean isOnline = false;
                            for (ConnectionHandler connection : connections) {
                                if (groupMember.getUserName().equals(connection.user.getUserName())) {
                                    message.setReceiver(groupMember);
                                    connection.sendMessage(message);
                                    logger.registerSentMessage(message);
                                    isOnline = true;
                                    break;
                                }
                            }

                            if (!isOnline) {
                                offlineReceivers.add(groupMember);
                            }
                        }

                        if (!offlineReceivers.isEmpty()) {
                            for(User user : offlineReceivers){
                                message.setReceiver(user);
                                saveOfflineMessage(message);  // Save the message for each offline user in the group
                                logger.registerSavedMessage(message);  // Log that the message was saved
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                shutdown();
            }
        }

        /**
         * Saves client's details, retrieves and sends any saved messages for the client.
         */
        private void saveClient() {
            try {
                message = (Message) ois.readObject();
                user = message.getSender();
                System.out.println("read object for client " + user.getUserName() + " successfully!");
                addConnection(this);

                List<Message> missedMessages = checkForSavedMessages(user.getUserName());
                Iterator<Message> iterator = missedMessages.iterator();
                while (iterator.hasNext()) {
                    Message missedMessage = iterator.next();
                    System.out.println("There are " + missedMessages.size() + " unsent messages for " + missedMessage.getReceiver());
                    sendMessage(missedMessage);
                    iterator.remove();  // Safely remove the current message from the list
                }
                removeSavedMessages(user.getUserName());

                logger.registerUserLogIn(user);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /**
         * Shuts down the client connection and cleans up resources.
         */
        public void shutdown(){
            System.out.println(user.getUserName() + " has logged out.");
            try {
                ois.close();
                oos.close();
                if(!client.isClosed())
                    client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            removeConnection(this);
            System.out.println("closed connection removed!");
            logger.registerUserLogOut(user);
        }

        /**
         * Sends a message object to the client associated with this ConnectionHandler.
         *
         * @param obj The message object to be sent.
         */
        public synchronized void sendMessage(Object obj) {
            try {
                oos.writeObject(obj);
                oos.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    /**
     * Removes saved messages for a specific user from the saved messages map.
     *
     * @param userName The username of the user whose messages are to be removed.
     */
    private void removeSavedMessages(String userName) {
        savedMessagesForOfflineUsers.remove(userName);
    }

    /**
     * Checks for saved messages associated with a specific receiver.
     *
     * @param receiver The username of the user whose messages are to be retrieved.
     * @return A list of messages saved for the specified receiver.
     */
    private List<Message> checkForSavedMessages(String receiver) {
        List<Message> output = new ArrayList<>();
        savedMessagesForOfflineUsers.forEach((username,messages) -> {
            if (receiver.equals(username)) {
                output.addAll(messages);
            }
        });
        return output;
    }

    /**
     * Saves a message intended for an offline user.
     *
     * @param message The message to be saved.
     */
    private void saveOfflineMessage(Message message) {
        savedMessagesForOfflineUsers.computeIfAbsent(message.getReceiver().getUserName(), k -> new ArrayList<>()).add(message);
    }
}