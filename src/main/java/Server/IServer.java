package Server;

import java.io.IOException;

public class IServer {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start(6379,6380);
    }
}
