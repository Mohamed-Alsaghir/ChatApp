package Shared;

import java.io.Serializable;
import java.util.List;

public class OnlineUsersList implements Serializable {
    List<User> onlineUsers;

    public OnlineUsersList(List<User> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    public List<User> getOnlineUsers() {
        return onlineUsers;
    }
}
