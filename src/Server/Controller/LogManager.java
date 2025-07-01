package Server.Controller;

import Server.View.LogUI;
import Shared.Message;
import Shared.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogManager {
    private final String logFilePath = "src/Server/Logs/traffic.log";

    public LogManager() {
        Path path = Paths.get(logFilePath);

        if (Files.exists(path)) {
            System.out.println("The file exists.");
        } else {
            System.out.println("The file does not exist.");
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LogUI log = new LogUI(this);
    }

    public void registerServerStartUp() {
        String formattedDateTime = getCurrentLocalDateTime();
        try {
            FileWriter fw = new FileWriter(logFilePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(formattedDateTime + " - Server has started.");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerUserLogIn(User user) {
        String formattedDateTime = getCurrentLocalDateTime();
        try {
            FileWriter fw = new FileWriter(logFilePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(formattedDateTime + " - Username: " + user.getUserName() + " has logged in.");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerUserLogOut(User user) {
        String formattedDateTime = getCurrentLocalDateTime();
        try {
            FileWriter fw = new FileWriter(logFilePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(formattedDateTime + " - Username: " + user.getUserName() + " has logged out.");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerSentMessage(Message message) {
        String formattedDateTime = getCurrentLocalDateTime();
        try {
            FileWriter fw = new FileWriter(logFilePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(formattedDateTime + " - " + message.getSender().getUserName() + " sent '" + message.getContent() + "' to " + message.getReceiver().getUserName() + ".");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerSavedMessage(Message message) {
        String formattedDateTime = getCurrentLocalDateTime();
        try {
            FileWriter fw = new FileWriter(logFilePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(formattedDateTime + " - Message from '" + message.getSender().getUserName() + "' to '" + message.getReceiver().getUserName() + "' was saved because the receiver was offline. Content: '" + message.getContent() + "'.");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getCurrentLocalDateTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    public List<String> readLogs() {
        List<String> tmpLogData = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(logFilePath);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)
        ) {
            String logLine;
            while ((logLine = br.readLine()) != null) { // This will advance to the next line
                tmpLogData.add(logLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmpLogData;
    }


    public List<String> retrievePeriodicalLogs(LocalTime from, LocalTime to) {
        List<String> output = new ArrayList<>();
        for (String line : readLogs()) {

            String regex = "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) - (.*)";

            Pattern pattern = Pattern.compile(regex);

            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                String dateTimeString = matcher.group(1);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);
                LocalTime time = dateTime.toLocalTime();
                if (time.isAfter(from) && time.isBefore(to)) {
                    output.add(line);
                }
                String text = matcher.group(2);


            } else {
                System.out.println("Could not parse the date and time from the logs.");
            }
        }
        return output;
    }
}