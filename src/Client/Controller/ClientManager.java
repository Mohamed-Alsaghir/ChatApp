package Client.Controller;

import Client.View.ChatUI;
import Client.View.LogInUI;
import Client.View.MainUI;
import Shared.*;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Manages the client's functionalities including sending/receiving messages and managing the UI.
 */
public class ClientManager{
    private String ipAddress;
    private int port;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Message message;
    private List<User> nonContacts;
    private List<User> contacts;
    private User user;
    private List<ChatUI> openChats;

    private LogInUI logInUI;
    private MainUI mainUI;

    /**
     * Constructor that sets up the client connection parameters.
     *
     * @param ipAddress The IP address to connect to.
     * @param port      The port number to connect on.
     */
    public ClientManager(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
        nonContacts = new ArrayList<>();
        contacts = new ArrayList<>();
        openChats = new ArrayList<>();
        connect();
    }

    /**
     * Sends a private message to another user.
     *
     * @param message The message object to send.
     */
    public void sendPrivateMessage(Message message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a private message with content, attachment and a specified receiver.
     *
     * @param content    The content of the message.
     * @param attachment The attachment associated with the message.
     * @param receiver   The username of the receiving user.
     */
    public void sendPrivateMessage(String content, ImageIcon attachment, String receiver) {
        boolean userFound = false;
        try {
            for (User user : nonContacts) {
                if (user.getUserName().equals(receiver)) {
                    System.out.println("message sending is working");
                    Message message = new Message(content, attachment, this.user);
                    message.setReceiver(user);
                    oos.writeObject(message);
                    oos.flush();
                    userFound = true;
                    break;
                }
            }

            // Check in Contacts
            if(!userFound){
                for (User user : contacts) {
                    if (user.getUserName().equals(receiver)) {
                        Message message = new Message(content, attachment, this.user);
                        message.setReceiver(user);
                        oos.writeObject(message);
                        oos.flush();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a group message to multiple users.
     *
     * @param content    The content of the message.
     * @param attachment The attachment associated with the message.
     * @param receivers  The list of usernames who should receive the message.
     */
    public void sendGroupMessage(String content, ImageIcon attachment, List<String> receivers) {
        try {
            // Identify the users who are in the receivers list
            Set<User> usersToSendTo = new HashSet<>();

            for (String receiver : receivers) {
                // Check in onlineUsers
                for (User user : nonContacts) {
                    if (user.getUserName().equals(receiver)) {
                        usersToSendTo.add(user);
                        break; // Break once a match is found
                    }
                }

                // Check in Contacts
                for (User user : contacts) {
                    if (user.getUserName().equals(receiver)) {
                        usersToSendTo.add(user);
                        break; // Break once a match is found
                    }
                }
            }

            ArrayList<User> usersList = new ArrayList<>(usersToSendTo);
            Message message = new Message(content, attachment, user);
            message.setGroupOfReceivers(usersList);
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Connects to the server using the provided IP address and port.
     */
    private void connect() {
        try {
            socket = new Socket(ipAddress, port);
            showLogInPage();
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                while (true) {
                    Object obj = ois.readObject();
                    if (obj instanceof Message) {
                        message = (Message) obj;
                        String sender = message.getSender().getUserName();
                        boolean chatFound = false;
                        for(ChatUI chat : openChats){
                            if(chat.getUserToChatWith().equals(sender)){
                                refreshChat(chat);
                                chatFound = true;
                                break;
                            }
                        }
                        if(!chatFound){
                            ChatUI popUpChat = new ChatUI(sender, this);
                            refreshChat(popUpChat);
                            addToOpenChats(popUpChat);
                        }
                    } else if (obj instanceof OnlineUsersList) {
                        OnlineUsersList onlineUsersList = (OnlineUsersList) obj;
                        this.nonContacts = onlineUsersList.getOnlineUsers();
                        while (mainUI == null) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        mainUI.refreshUserList(nonContacts, contacts);
                    }
                }
            } catch (IOException e) {
                exit();
            } catch (ClassNotFoundException e) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Refreshes the chat UI with the latest message.
     *
     * @param chat The chat UI to be refreshed.
     */
    private void refreshChat(ChatUI chat) {
        chat.appendTxtAreaMessages(message.getSender().getUserName(), message.getContent());
        chat.setImageIcon(message.getAttachment());
    }

    /**
     * Handles the logic to close the client connection properly.
     */
    private void exit() {
        if(!socket.isClosed()) {
            try {
                try (ObjectOutputStream contactsOOS = new ObjectOutputStream(new FileOutputStream("./"+user.getUserName()+"_contacts.secret"))) {
                    contactsOOS.writeObject(contacts);
                }
                oos.close();
                ois.close();
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Displays the login page for the client.
     */
    public void showLogInPage() {
        logInUI = new LogInUI(400,900,this);
    }

    /**
     * Closes the login page UI.
     */
    public void closeLoginPage() {
        logInUI.setVisible(false);
    }

    /**
     * Displays the main chat page for the client.
     */
    public void showMainPage() {
        mainUI = new MainUI(400,900, user.getUserName(), user.getProfilePicture(), nonContacts, contacts, this);
        mainUI.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
    }

    /**
     * Adds a specified user to the client's contact list.
     *
     * @param username The username of the user to add.
     */
    public synchronized void addToContacts(String username) {
        boolean isAlreadyInContacts = false;

        // Check if the user is already in contacts
        for (User user : contacts) {
            if (user.getUserName().equals(username)) {
                isAlreadyInContacts = true;
                break;
            }
        }

        if (!isAlreadyInContacts) {
            // If not in contacts, find the user in nonContacts and add to contacts
            Iterator<User> iterator = nonContacts.iterator();
            while (iterator.hasNext()) {
                User user = iterator.next();
                if (user.getUserName().equals(username)) {
                    contacts.add(user);
                    break;
                }
            }
        }

        mainUI.refreshUserList(nonContacts, contacts);
    }

    /**
     * Retrieves the client's contact list from disk.
     */
    public void getContactsFromDisk() {
        try (ObjectInputStream contactsOIS = new ObjectInputStream(new FileInputStream("./"+user.getUserName()+"_contacts.secret"))) {
            // Read the object from file
            Object obj = contactsOIS.readObject();
            if (obj instanceof List) {
                contacts = (List<User>) obj;
            } else {
                System.err.println("Error: Unexpected content in contacts file.");
            }
        } catch (IOException e) {
            System.err.println("Error reading contacts from file: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Error: User class not found while reading from contacts file.");
        }
    }

    /**
     * Adds a chat to the list of open chat windows.
     *
     * @param chatUI The chat window to add.
     */
    public synchronized void addToOpenChats(ChatUI chatUI) {
        openChats.add(chatUI);
        System.out.println("adding to openchats success");
    }

    /**
     * Removes a chat from the list of open chat windows.
     *
     * @param chatUI The chat window to remove.
     */
    public synchronized void removeFromOpenChats(ChatUI chatUI) {
        openChats.remove(chatUI);
        System.out.println("removing to openchats success");
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

