package Server.View;

import Server.Controller.LogManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogUI extends JFrame{
    private JComboBox<String> fromComboBox;
    private JComboBox<String> toComboBox;
    private JButton submitButton;
    private JTextArea logsTextArea;
    private LogManager logManager;

    public LogUI(LogManager logM) {
        logManager = logM;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Server Log");
        setSize(620, 560);
        setLayout(new FlowLayout());

        fromComboBox = new JComboBox<>(generateHourStrings());
        toComboBox = new JComboBox<>(generateHourStrings());
        submitButton = new JButton("Display Logs");
        logsTextArea = new JTextArea(30, 50);


        submitButton.addActionListener(e -> {
            saveLog();
        });

        add(new JLabel("From:"));
        add(fromComboBox);
        add(new JLabel("To:"));
        add(toComboBox);
        add(submitButton);
        add(logsTextArea); // Add the text area

        setVisible(true);
    }

    private String[] generateHourStrings() {
        String[] hours = new String[24];
        DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (int hour = 0; hour < 24; hour++) {
            hours[hour] = LocalTime.of(hour, 0).format(hourFormatter);
        }

        return hours;
    }

    public void saveLog(){
        logsTextArea.setText("");
        String selectedFromHour = (String) fromComboBox.getSelectedItem();
        String selectedToHour = (String) toComboBox.getSelectedItem();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime localFrom = LocalTime.parse(selectedFromHour,formatter);
        LocalTime localTo = LocalTime.parse(selectedToHour,formatter);
        List<String> logs = logManager.retrievePeriodicalLogs(localFrom,localTo);
        for (String line: logs) {
            logsTextArea.append(line + "\n");
        }
        System.out.println("From: " + selectedFromHour + " To: " + selectedToHour);
    }
}
