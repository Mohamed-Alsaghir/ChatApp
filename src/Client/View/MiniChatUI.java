package Client.View;


import Client.Controller.ClientManager;
import Shared.Message;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MiniChatUI extends JFrame {
	private List<String> usersToChatWith;
	private ClientManager clientManager;
	private JTextArea txtAreaMyCurrentMessage;
	private String imageURL = "./src/Images/logo.png";
	private ImageIcon attachment;

	public MiniChatUI(List<String> usersToChatWith, ClientManager clientManager) {
		super("Grupp meddelande");
		super.setIconImage(new ImageIcon("./src/Images/logo.png").getImage());
		this.usersToChatWith = usersToChatWith;
		this.clientManager = clientManager;
		this.setResizable(false);
		this.setSize(400, 200);
		JPanel mainPanel = setUpMainPanel();
		setupUserInfo(mainPanel);

		this.setContentPane(mainPanel);
		this.setVisible(true);
	}

	private JPanel setUpMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setLocation(0,0);
		panel.setBackground(Color.gray);
		return panel;
	}

	private void setupUserInfo(JPanel mainPanel) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setLocation(0, 0);
		panel.setSize(new Dimension(400, 200));
		panel.setBackground(Color.decode("#8ecae6"));
		mainPanel.add(panel);

		JButton btnSendMyMessage = new JButton("Skicka");
		btnSendMyMessage.setEnabled(true);
		btnSendMyMessage.setSize(120, 30);
		btnSendMyMessage.setLocation(260, 130);
		btnSendMyMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clientManager.sendGroupMessage(txtAreaMyCurrentMessage.getText(), attachment, usersToChatWith);
				attachment = null;
				txtAreaMyCurrentMessage.setText(""); // Clear the input field
			}
		});

		panel.add(btnSendMyMessage);

		JButton btnMyMessageAttachment = new JButton("Bifoga bild");
		btnMyMessageAttachment.setEnabled(true);
		btnMyMessageAttachment.setSize(120, 30);
		btnMyMessageAttachment.setLocation(140, 130);
		btnMyMessageAttachment.addActionListener(e -> addAttachment());
		panel.add(btnMyMessageAttachment);

		txtAreaMyCurrentMessage = new JTextArea(5, 20);
		txtAreaMyCurrentMessage.setEditable(true);
		JScrollPane scrollPaneMyMessage = new JScrollPane(txtAreaMyCurrentMessage);
		scrollPaneMyMessage.setLocation(20,20);
		scrollPaneMyMessage.setSize(360,100);
		panel.add(scrollPaneMyMessage);

	}
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
}