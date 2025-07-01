package Client.View;


import Client.Controller.ClientManager;
import Shared.Message;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * The ChatUI class provides a user interface for a chat session. It enables users to
 * send messages, view incoming messages, and attach image files. This class extends JFrame,
 * providing a visual representation of the chat window.
 *
 * This UI component is primarily intended for private messaging between two users.
 * It provides features like appending new messages, handling image attachments,
 * and managing user chat session interactions.
 */
public class ChatUI extends JFrame {
	private String userToChatWith;
	private ClientManager clientManager;
	private JTextArea txtAreaMyCurrentMessage;
	private String imageURL = "./src/Images/logo.png";
	private ImageIcon attachment;
	private JTextArea txtAreaMessages;
	private JLabel attachmentReceived;


	/**
	 * Constructs a new ChatUI for the given user and initializes the UI components.
	 *
	 * @param userToChatWith The username of the user to chat with.
	 * @param clientManager The manager handling the client-side operations.
	 */
	public ChatUI(String userToChatWith, ClientManager clientManager) {
		super("Konversation: "+clientManager.getUser().getUserName() + " med " + userToChatWith);
		super.setIconImage(new ImageIcon("./src/Images/logo.png").getImage());
		this.userToChatWith = userToChatWith;
		this.clientManager = clientManager;
		this.setResizable(false);
		this.setSize(1200, 800);
		JPanel mainPanel = setUpMainPanel();
		setupUserInfo(mainPanel);
		this.setContentPane(mainPanel);
		this.setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				clientManager.removeFromOpenChats(ChatUI.this);
			}
		});
	}

	/**
	 * Sets up the main panel for the chat UI.
	 *
	 * @return The configured main JPanel.
	 */
	private JPanel setUpMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setLocation(0,0);
		panel.setBackground(Color.gray);
		return panel;
	}

	/**
	 * Sets up the user info and chat components in the main panel.
	 *
	 * @param mainPanel The main panel where the components are added.
	 */
	private void setupUserInfo(JPanel mainPanel) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setLocation(0, 0);
		panel.setSize(new Dimension(1200, 800));
		panel.setBackground(Color.decode("#8ecae6"));
		mainPanel.add(panel);


		ImageIcon imageIcon = new ImageIcon(new ImageIcon("./src/Images/imageIcon.png").getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT));

		attachmentReceived = new JLabel("");
		attachmentReceived.setIcon(imageIcon);
		attachmentReceived.setEnabled(true);
		attachmentReceived.setLocation(875, 50);
		attachmentReceived.setSize(120, 120);
		panel.add(attachmentReceived);

		JButton btnSendMyMessage = new JButton("Skicka");
		btnSendMyMessage.setEnabled(true);
		btnSendMyMessage.setSize(120, 120);
		btnSendMyMessage.setLocation(875, 500);
		btnSendMyMessage.addActionListener(e -> {
			clientManager.sendPrivateMessage(txtAreaMyCurrentMessage.getText(), attachment, userToChatWith);
			attachment = null;
			appendTxtAreaMessages(clientManager.getUser().getUserName(), txtAreaMyCurrentMessage.getText());
			txtAreaMyCurrentMessage.setText(""); // Clear the input field
		});

		panel.add(btnSendMyMessage);

		JButton btnMyMessageAttachment = new JButton("Bifoga bild");
		btnMyMessageAttachment.setEnabled(true);
		btnMyMessageAttachment.setSize(120, 30);
		btnMyMessageAttachment.setLocation(875, 620);
		btnMyMessageAttachment.addActionListener(e -> addAttachment());
		panel.add(btnMyMessageAttachment);

		txtAreaMessages = new JTextArea(5, 20);
		txtAreaMessages.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(txtAreaMessages);
		scrollPane.setLocation(50,50);
		scrollPane.setSize(800,400);
		panel.add(scrollPane);

		txtAreaMyCurrentMessage = new JTextArea(5, 20);
		txtAreaMyCurrentMessage.setEditable(true);
		JScrollPane scrollPaneMyMessage = new JScrollPane(txtAreaMyCurrentMessage);
		scrollPaneMyMessage.setLocation(50,470);
		scrollPaneMyMessage.setSize(800,200);
		panel.add(scrollPaneMyMessage);

	}

	/**
	 * Opens a file chooser dialog for the user to select an image as an attachment.
	 * This method updates the image URL and attachment based on the user's choice.
	 */
	public void addAttachment() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG & PNG Images", "jpg", "png");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(getParent());
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this file: " + chooser.getSelectedFile().getAbsolutePath());
			imageURL = chooser.getSelectedFile().getAbsolutePath();
			attachment = new ImageIcon(new ImageIcon(imageURL).getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT));
		}
	}

	/**
	 * Appends a message to the chat UI's text area.
	 *
	 * @param username The username of the user who sent the message.
	 * @param content The content of the message to append.
	 */
	public void appendTxtAreaMessages(String username, String content) {
		if (!txtAreaMessages.getText().isEmpty()) {
			txtAreaMessages.append("\n");
		}
		this.txtAreaMessages.append(username + ": "+ content);
	}

	/**
	 * Sets the image icon for the chat UI.
	 *
	 * @param imageIcon The icon image to set.
	 */
	public void setImageIcon(ImageIcon imageIcon) {
		if(imageIcon != null){
			attachmentReceived.setIcon(imageIcon);
		}
	}

	public String getUserToChatWith() {
		return userToChatWith;
	}

}