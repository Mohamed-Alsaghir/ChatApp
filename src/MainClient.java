import Client.Controller.ClientManager;


public class MainClient {
    public static void main(String[] args) {
        ClientManager clientManager = new ClientManager("127.0.0.1", 9999);
    }
}