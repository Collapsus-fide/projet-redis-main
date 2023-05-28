package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static Server.Server.CRLF;

public class ClientHandler implements Runnable {

    private Server server;
    private Socket clientSocket;
    private PrintWriter out;
    private List<String> subscribedTopics; // liste des topics que ce client suit



    public ClientHandler(Server server,Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        this.subscribedTopics = new ArrayList<>();
    }


        @Override
    public void run() {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String command;
        while (true) {
            try {
                if ((command = in.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            int commandLength = 0;
            if (command.charAt(0) == '*') {
                commandLength = Character.getNumericValue(command.charAt(1));


            }
            if (commandLength < 2) {
                out.print("-ERR wrong number of arguments" + CRLF);
                out.flush();
                continue;
            }
            List<String> parts = new ArrayList<>();
            for (int i=0; i< commandLength*2; i++){
                try {
                    parts.add(in.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
            switch (parts.get(1).toUpperCase()) {

                case "SET":
                    if (parts.size() < 6) {
                        out.print("-ERR wrong number of arguments for 'SET' command" + CRLF);
                        out.flush();
                        continue;
                    }
                    server.logger.info("key : "+ parts.get(3)+" with value "+ parts.get(5)+" set");
                    server.store.put(parts.get(3), parts.get(5));
                    out.print("+OK" + CRLF);
                    out.flush();
                    //Envoyer la mise à jour au serveur esclave
                    updateSlave(commandLength,parts);
                    break;

                case "GET":
                    String result = server.store.get(parts.get(3));
                    if (result == null) {
                        out.print("$-1" + CRLF);
                    } else {
                        out.print("$" + result.length() + CRLF);
                        out.print(result + CRLF);
                    }
                    out.flush();
                    break;

                case "STRLEN":
                    result = server.store.get(parts.get(3));
                    if (result == null) {
                        out.print(":0" + CRLF);
                    } else {
                        out.print(":" + result.length() + CRLF);
                    }
                    out.flush();
                    break;

                case "APPEND":
                    if (parts.size() < 5) {
                        out.print("-ERR wrong number of arguments for 'APPEND' command" + CRLF);
                        out.flush();
                        continue;
                    }

                    String oldValue = server.store.get(parts.get(3));
                    if (oldValue == null) {
                        server.store.put(parts.get(3), parts.get(5));
                        out.print(":" + parts.get(5).length() + CRLF);
                    } else {
                        server.store.put(parts.get(3), oldValue + parts.get(5));
                        out.print(":" + (oldValue.length() + parts.get(5).length()) + CRLF);
                    }
                    out.flush();
                    //Envoyer la mise à jour au serveur esclave
                    updateSlave(commandLength,parts);
                    break;

                case "INCR":
                    String valincr = server.store.get(parts.get(3));
                    if (valincr == null) {
                        valincr = "0";
                    }
                    try {
                        long lVal = Long.parseLong(valincr);
                        lVal++;
                        server.store.put(parts.get(3), Long.toString(lVal));
                        updateSlave(commandLength,parts);
                        out.print(":" + lVal + CRLF);
                    } catch (NumberFormatException e) {
                        out.print("-ERR value is not an integer or out of range" + CRLF);
                    }
                    out.flush();
                    break;

                case "DECR":
                    String val = server.store.get(parts.get(3));
                    if (val == null) {
                        val = "0";
                    }
                    try {
                        long lVal = Long.parseLong(val);
                        lVal--;
                        server.store.put(parts.get(3), Long.toString(lVal));
                        updateSlave(commandLength,parts);
                        out.print(":" + lVal + CRLF);
                    } catch (NumberFormatException e) {
                        out.print("-ERR value is not an integer or out of range" + CRLF);
                    }
                    out.flush();
                    break;

                case "EXISTS":
                    if (server.store.containsKey(parts.get(3))) {
                        out.print(":1" + CRLF);
                    } else {
                        out.print(":0" + CRLF);
                    }
                    out.flush();
                    break;

                case "DEL":
                    int deleted = 0;
                    for (int i = 1; i < parts.size(); i++) {
                        if (server.store.remove(parts.get(i)) != null) {
                            updateSlave(commandLength,parts);
                            deleted++;
                        }
                    }
                    out.print(":" + deleted + CRLF);
                    out.flush();
                    break;

                case "EXPIRE":
                    if (parts.size() < 5) {
                        out.print("-ERR wrong number of arguments for 'EXPIRE' command" + CRLF);
                        out.flush();
                        continue;
                    }
                    try {
                        int seconds = Integer.parseInt(parts.get(5));
                        long expirationTime = System.currentTimeMillis() + (seconds * 1000L);
                        KeyExpiration keyExpiration = new KeyExpiration(parts.get(3), expirationTime);
                        server.expirationMap.put(parts.get(3), keyExpiration);
                    } catch (NumberFormatException e) {
                        out.print("-ERR value is not a valid integer" + CRLF);
                        out.flush();
                        continue;
                    }
                    updateSlave(commandLength,parts);
                    out.print(":1" + CRLF);
                    out.flush();
                    break;

                case "SUBSCRIBE":
                    if (parts.size() < 4) {
                        out.print("-ERR wrong number of arguments for 'SUBSCRIBE' command" + CRLF);
                        out.flush();
                        continue;
                    }
                    String topicName = parts.get(3);
                    Topic topic = server.getOrCreateTopic(topicName);
                    topic.subscribe(this);
                    subscribedTopics.add(topicName);
                    out.print("+OK" + CRLF);
                    out.flush();
                    break;

                case "UNSUBSCRIBE":
                    if (parts.size() < 4) {
                        out.print("-ERR wrong number of arguments for 'UNSUBSCRIBE' command" + CRLF);
                        out.flush();
                        continue;
                    }
                    topicName = parts.get(3);
                    topic = server.getTopic(topicName);
                    if (topic != null) {
                        topic.unsubscribe(this);
                        subscribedTopics.remove(topicName);
                        out.print("+OK" + CRLF);
                    } else {
                        out.print("-ERR topic does not exist" + CRLF);
                    }
                    out.flush();
                    break;

                case "PUBLISH":
                    if (parts.size() < 5) {
                        out.print("-ERR wrong number of arguments for 'PUBLISH' command" + CRLF);
                        out.flush();
                        continue;
                    }
                    topicName = parts.get(3);
                    String message = parts.get(5);
                    topic = server.getTopic(topicName);
                    if (topic != null) {
                        topic.publish(message);
                        out.print("+OK" + CRLF);
                    } else {
                        out.print("-ERR topic does not exist" + CRLF);
                    }
                    out.flush();
                    break;

                default:
                    out.print("-ERR unknown command '" + parts.get(3) + "'" + CRLF);
                    out.flush();
                    break;
            }
        }

        try {
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        out.close();
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     *
     * @param message
     * envoie le message sur un publish a été réalisé et que ce client est abonné au sujet.
     */
    public void sendMessage(String message) {
        out.print(message + CRLF);
        out.flush();
    }


    private void updateSlave(int length, List<String> parts) {
        // Si un serveur esclave est connecté, envoyer les mises à jour
        if (server.slaveOut != null) {
            server.logger.info("Mise à jour du slave...");
            StringBuilder request = new StringBuilder("*" + length + "\r\n");
            for (String part : parts) {
                request.append(part).append("\r\n");
            }
            try {
                server.slaveOut.write(request.toString().getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            server.logger.info("Mise à jour du slave terminée");
        }
    }
}
