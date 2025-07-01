package Shared;

import javax.swing.*;
import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    String content;
    ImageIcon attachment;
    User sender;
    User receiver;
    List<User> groupOfReceivers;
    boolean userIsLeaving;


    public Message(String content, ImageIcon attachment, User sender){
        this.content = content;
        this.attachment = attachment;
        this.sender = sender;
        this.userIsLeaving = false;
    }
    public String getContent() {
        return content;
    }

    public ImageIcon getAttachment() {
        return attachment;
    }

    public User getSender() {
        return sender;
    }

    public List<User> getGroupOfReceivers() {
        return groupOfReceivers;
    }

    public void setGroupOfReceivers(List<User> groupOfReceivers) {
        this.groupOfReceivers = groupOfReceivers;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }
}
