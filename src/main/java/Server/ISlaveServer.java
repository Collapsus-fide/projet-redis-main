package Server;

import java.io.IOException;

public class ISlaveServer {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start(6380,-1);
    }
}
