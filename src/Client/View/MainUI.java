package Client.View;

import Client.Controller.ClientManager;
import Shared.User;


import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Represents the main user interface of the MSN Messenger application.
 * This UI allows the user to view their contacts, initiate chats, and manage their profile.
 */
public class MainUI extends JFrame {
	private int width;
	private int height;
	private String username;
	private ImageIcon profilePicture;
	private ClientManager clientManager;
	private JList nonContactsList;
	private JList contactsList;
	private List<Integer> currentUsersSelection;
	private List<String> usersToSendTo;
	private Boolean isGroupChatMode;


	/**
	 * Creates the main user interface of the messenger.
	 *
	 * @param width         The width of the window.
	 * @param height        The height of the window.
	 * @param userName      The name of the current user.
	 * @param profilePicture The profile picture of the current user.
	 * @param onlineUsers   List of online users.
	 * @param contacts      List of contacts of the current user.
	 * @param clientManager The manager handling client-side operations.
	 */
	public MainUI(int width, int height, String userName, ImageIcon profilePicture, List<User> onlineUsers, List<User> contacts, ClientManager clientManager) {
		super("MSN Messenger");
		super.setIconImage(new ImageIcon("./src/Images/logo.png").getImage());
		this.username = userName;
		this.profilePicture = profilePicture;
		this.clientManager = clientManager;
		this.usersToSendTo = new ArrayList<>();
		isGroupChatMode = false;

		this.setResizable(false);
		this.setSize(width, height);
		JPanel mainPanel = setUpMainPanel();
		setupUserInfo(mainPanel);
		setUpAddContactButton(mainPanel);
		setupChatButton(mainPanel);
		setupFriendsSection(onlineUsers, contacts, mainPanel);

		this.setContentPane(mainPanel);

		this.setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.width = width;
		this.height = height;
	}

	/**
	 * Sets up the primary main panel of the UI.
	 *
	 * @return The main JPanel configured.
	 */
	private JPanel setUpMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setLocation(0,0);
		panel.setBackground(Color.gray);
		return panel;
	}

	/**
	 * Sets up the user information section on the main UI.
	 *
	 * @param mainPanel The main panel where the user information components will be added.
	 */
	private void setupUserInfo(JPanel mainPanel) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setLocation(0,0);
		panel.setSize(new Dimension(500,120));
		panel.setBackground(Color.decode("#8ecae6"));
		mainPanel.add(panel);

		JLabel userNameLabel = new JLabel(username);
		userNameLabel.setLocation(120, 50);
		userNameLabel.setSize(200, 20);
		panel.add(userNameLabel);

		ImageIcon avatar = new ImageIcon(profilePicture.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT));
		JLabel lblIcon = new JLabel(avatar);
		lblIcon.setSize(100,100);
		lblIcon.setLocation(10,10);
		lblIcon.setBackground(Color.gray);
		lblIcon.setForeground(Color.gray);
		panel.add(lblIcon);
	}

	/**
	 * Sets up the section displaying online users and friends.
	 *
	 * @param onlineUsers List of online users.
	 * @param contacts    List of contacts of the current user.
	 * @param mainPanel   The main panel where these components will be added.
	 */
	private void setupFriendsSection(List<User> onlineUsers, List<User> contacts, JPanel mainPanel) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setLocation(0, 120);
		panel.setSize(new Dimension(500, 780));
		panel.setBackground(Color.decode("#219ebc"));
		mainPanel.add(panel);

		nonContactsList = new JList();
		nonContactsList.setCellRenderer(new ImageListCellRenderer());
		nonContactsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		nonContactsList.setLayoutOrientation(JList.VERTICAL);
		nonContactsList.setFixedCellHeight(46);

		JScrollPane nonContactsScrollPane = new JScrollPane(nonContactsList);
		nonContactsScrollPane.setMinimumSize(new Dimension(150, 50));
		nonContactsScrollPane.setLocation(10, 10);
		nonContactsScrollPane.setSize(380, 360); // Adjusted size
		panel.add(nonContactsScrollPane);

		contactsList = new JList();
		contactsList.setCellRenderer(new ImageListCellRenderer());
		contactsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		contactsList.setLayoutOrientation(JList.VERTICAL);
		contactsList.setFixedCellHeight(46);

		ListSelectionListener listSelectionListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int[] selectedIndices = nonContactsList.getSelectedIndices();

				if (selectedIndices.length == 1) {
					isGroupChatMode = false;
				} else {
					isGroupChatMode = true;
				}
			}
		};
		nonContactsList.addListSelectionListener(listSelectionListener);

		JMenuItem groupMenuItem = new JMenuItem("Start a Group Chat");
		groupMenuItem.addActionListener(e -> {
			for (int i = 0; i < currentUsersSelection.size(); i++) {
				// Get the selected panels
				Object[] selectedPanels = nonContactsList.getSelectedValues();

				// Extract the usernames and open the ChatUI
				for (Object selectedPanel : selectedPanels) {
					JPanel selPanel = (JPanel) selectedPanel;
					for (Component component : selPanel.getComponents()) {
						if (component instanceof JLabel) {
							JLabel label = (JLabel) component;
							String username = label.getText(); // Extracting the text
							usersToSendTo.add(username); // Adding the username to the list
						}
					}
				}
			}
		});
		usersToSendTo = new ArrayList<>();
		JScrollPane ContactsScrollPane = new JScrollPane(contactsList); // Fixed here
		ContactsScrollPane.setMinimumSize(new Dimension(150, 50));
		ContactsScrollPane.setLocation(10, 369); // Adjusted location
		ContactsScrollPane.setSize(380, 350); // Adjusted size
		panel.add(ContactsScrollPane);

		refreshUserList(onlineUsers, contacts);
	}

	/**
	 * Sets up the button to initiate chats.
	 *
	 * @param mainPanel The main panel where this button will be added.
	 */
	private void setupChatButton(JPanel mainPanel) {
		JButton chatButton = new JButton("Chat");
		chatButton.addActionListener(e -> {
			Set<String> usersToChatWith = new HashSet<>(); // Using a set to avoid duplicates

			// Helper function to extract users from selected panels
			Consumer<Object[]> extractUsers = (selectedPanels) -> {
				for (Object selectedPanel : selectedPanels) {
					JPanel panel = (JPanel) selectedPanel;
					for (Component component : panel.getComponents()) {
						if (component instanceof JLabel) {
							JLabel label = (JLabel) component;
							String username = label.getText();
							usersToChatWith.add(username);
						}
					}
				}
			};

			// Extract users from both lists
			extractUsers.accept(nonContactsList.getSelectedValues());
			extractUsers.accept(contactsList.getSelectedValues());

			// Take action based on the number of unique users selected
			if (usersToChatWith.size() == 1) {
				setUpPrivateChat(usersToChatWith.iterator().next()); // As there's only one user, get it
			} else {
				setUpGroupChat(new ArrayList<>(usersToChatWith)); // Convert set to list and pass to the group chat setup
			}
		});

		chatButton.setLocation(310, 840); // Set location as per your layout
		chatButton.setSize(80, 30);
		mainPanel.add(chatButton);
	}


	/**
	 * Sets up a private chat UI with a specified user.
	 *
	 * @param username The username of the user to chat with.
	 */
	private void setUpPrivateChat(String username) {
		ChatUI privateChat = new ChatUI(username, clientManager);
		clientManager.addToOpenChats(privateChat);
	}

	/**
	 * Sets up a group chat UI with specified users.
	 *
	 * @param usersToSendTo List of usernames to include in the group chat.
	 */
	private void setUpGroupChat(List<String> usersToSendTo) {
		MiniChatUI groupChat = new MiniChatUI(usersToSendTo, clientManager);
	}

	/**
	 * Refreshes the list of online users and contacts displayed in the UI.
	 *
	 * @param onlineUsers Updated list of online users.
	 * @param contacts    Updated list of contacts.
	 */
	public void refreshUserList(List<User> onlineUsers, List<User> contacts) {
		ArrayList<JPanel> panels = new ArrayList<>();

		if (onlineUsers != null) {
			for (User user : onlineUsers) {
				String userName = user.getUserName();
				if (!userName.equals(this.username)) {
					ImageIcon imageIcon = new ImageIcon(user.getProfilePicture().getImage().getScaledInstance(50, 40, Image.SCALE_DEFAULT));
					JLabel jLabel = new JLabel(userName, imageIcon, JLabel.LEFT);
					JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
					jPanel.add(jLabel);
					panels.add(jPanel);
				}
			}
		}
		JPanel[] jPanels = panels.toArray(new JPanel[0]);
		nonContactsList.setListData(jPanels);

		panels = new ArrayList<>();

		if (contacts != null) {
			for (User user : contacts) {
				String userName = user.getUserName();
				if (!userName.equals(this.username)) {
					ImageIcon imageIcon = new ImageIcon(user.getProfilePicture().getImage().getScaledInstance(50, 40, Image.SCALE_DEFAULT));
					JLabel jLabel = new JLabel(userName, imageIcon, JLabel.LEFT);
					JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
					jPanel.add(jLabel);
					panels.add(jPanel);
				}
			}
		}

		jPanels = panels.toArray(new JPanel[0]);
		contactsList.setListData(jPanels);
	}

	/**
	 * Sets up the "Add Contact" button on the main UI.
	 *
	 * @param mainPanel The main panel where this button will be added.
	 */
	public void setUpAddContactButton(JPanel mainPanel){
		JButton addContact = new JButton("Add Contact");
		addContact.addActionListener(e -> {
			Object[] userList = nonContactsList.getSelectedValues();


			for (Object selectedPanel : userList) {
				JPanel panel = (JPanel) selectedPanel;
				for (Component component : panel.getComponents()) {
					if (component instanceof JLabel) {
						JLabel label = (JLabel) component;
						String username = label.getText(); // Extracting the text
						clientManager.addToContacts(username);
					}
				}
			}

		});
		addContact.setLocation(10, 840); // Set location as per your layout
		addContact.setSize(120, 30);
		mainPanel.add(addContact); // Don't forget to add the button to the mainPanel
	}

}
