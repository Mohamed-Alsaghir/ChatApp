package Shared;

import javax.swing.*;
import java.io.Serializable;

public class User implements Serializable {
    private String userName;
    private ImageIcon profilePicture;

    public User(String username, ImageIcon profilePicture) {
        this.userName = username;
        this.profilePicture = profilePicture;
    }

    public int hashCode() {
        return userName.hashCode();
    }

    public boolean equals(Object obj) {
        if(obj!=null && obj instanceof User)
            return userName.equals(((User)obj).getUserName());
        return false;
    }

    public String getUserName() {
        return userName;
    }

    public ImageIcon getProfilePicture() {
        return profilePicture;
    }

}

