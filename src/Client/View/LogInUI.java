package Client.View;

import Client.Controller.ClientManager;
import Shared.Message;
import Shared.User;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class LogInUI extends JFrame {

	private final int width;
	private final int height;
	private JButton btnAvatar;
	private ImageIcon avatar;
	private String imageURL = "./src/Images/logo.png";
	private ClientManager clientManager;
	private JTextField TFUserName;

	public LogInUI(int width, int height, ClientManager clientManager) {
		super("MSN Messenger");
		this.clientManager = clientManager;
		super.setIconImage(new ImageIcon("./src/Images/logo.png").getImage());

		this.setResizable(false);
		this.setSize(width, height);
		JPanel mainPanel = setUpMainPanel();
		setupUserInfo(mainPanel);

		this.setContentPane(mainPanel);
		this.setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.width = width;
		this.height = height;

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
		panel.setSize(new Dimension(400, 900));
		panel.setBackground(Color.decode("#8ecae6"));
		mainPanel.add(panel);

		avatar = new ImageIcon(new ImageIcon(imageURL).getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT));
		btnAvatar = new JButton("");
		btnAvatar.setIcon(avatar);
		btnAvatar.setEnabled(true);
		btnAvatar.setSize(120, 120);
		btnAvatar.setLocation(130, 270);
		btnAvatar.setBorderPainted(false);
		btnAvatar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeAvatar();
			}
		});
		panel.add(btnAvatar);

		TFUserName = new JTextField("Användarnamn");
		TFUserName.setToolTipText("Användarnamn");
		TFUserName.setSize(200,30);
		TFUserName.setLocation(100, 435);
		TFUserName.setBorder(new LineBorder(Color.black,1));
		panel.add(TFUserName);

		JButton btnRegisterAccount = new JButton("Logga In");
		btnRegisterAccount.setEnabled(true);
		btnRegisterAccount.setSize(120, 30);
		btnRegisterAccount.setLocation(140, 485);
		btnRegisterAccount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				requestLogin();
			}
		});
		panel.add(btnRegisterAccount);
	}

	private void requestLogin() {
		User user = new User(TFUserName.getText(), avatar);
		clientManager.setUser(user);

		File contactsFile = new File("./"+user.getUserName()+"_contacts.secret");
		if (contactsFile.exists()) {
			clientManager.getContactsFromDisk();
		}
		clientManager.sendPrivateMessage(new Message("", null, user));
		clientManager.closeLoginPage();
		clientManager.showMainPage();
	}

	public void changeAvatar() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG & PNG Images", "jpg", "png");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(getParent());
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this file: " + chooser.getSelectedFile().getAbsolutePath());
			imageURL = chooser.getSelectedFile().getAbsolutePath();
			updateAvatar();
		}
	}

	public void updateAvatar(){
		avatar = new ImageIcon(new ImageIcon(imageURL).getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT));
		btnAvatar.setIcon(avatar);
	}
}
