package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;


/**
 * Interface utilisateur pour le client
 * à lancer en dernier une fois le slave et master lancés
 */
public class IClient {
    private static Client client;

    public static void main(String[] args) throws IOException {
        client = new Client("localhost", 6379);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input;

        while (true) {
            System.out.print("> ");
            input = reader.readLine();

            if (input == null) {
                System.out.println("Au revoir !");
                break;
            }

            String[] parts = input.split("\\s+");
            String command = parts[0];
            String[] arguments = Arrays.copyOfRange(parts, 1, parts.length);

            switch (command.toLowerCase()) {
                case "set":
                    if (arguments.length != 2) {
                        System.out.println("USAGE : SET key value");
                    } else {
                        String key = arguments[0];
                        String value = arguments[1];
                        System.out.println(client.set(key, value));
                    }
                    break;
                case "get":
                    if (arguments.length != 1) {
                        System.out.println("USAGE : GET key");
                    } else {
                        String key = arguments[0];
                        System.out.println(client.get(key));
                    }
                    break;
                case "strlen":
                    if (arguments.length != 1) {
                        System.out.println("USAGE : STRLEN key");
                    } else {
                        String key = arguments[0];
                        System.out.println(client.strlen(key));
                    }
                    break;
                case "append":
                    if (arguments.length != 2) {
                        System.out.println("USAGE : APPEND key value");
                    } else {
                        String key = arguments[0];
                        String value = arguments[1];
                        System.out.println(client.append(key, value));
                    }
                    break;
                case "incr":
                    if (arguments.length != 1) {
                        System.out.println("USAGE : INCR key");
                    } else {
                        String key = arguments[0];
                        System.out.println(client.incr(key));
                    }
                    break;
                case "decr":
                    if (arguments.length != 1) {
                        System.out.println("USAGE : DECR key");
                    } else {
                        String key = arguments[0];
                        System.out.println(client.decr(key));
                    }
                    break;
                case "exists":
                    if (arguments.length != 1) {
                        System.out.println("USAGE : EXISTS key");
                    } else {
                        String key = arguments[0];
                        System.out.println(client.exists(key));
                    }
                    break;
                case "del":
                    if (arguments.length != 1) {
                        System.out.println("USAGE : DEL key");
                    } else {
                        String key = arguments[0];
                        System.out.println(client.del(key));
                    }
                    break;
                case "expire":
                    if (arguments.length != 2) {
                        System.out.println("USAGE : EXPIRE key seconds");
                    } else {
                        String key = arguments[0];
                        int seconds = Integer.parseInt(arguments[1]);
                        System.out.println(client.expire(key, seconds));
                    }
                    break;

                case "subscribe":
                    if (arguments.length != 1) {
                        System.out.println("USAGE : SUBSCRIBE topic");
                    } else {
                        String topic = arguments[0];
                        System.out.println(client.subscribe(topic));
                    }
                    break;

                case "unsubscribe":
                    if (arguments.length != 1) {
                        System.out.println("USAGE : UNSUBSCRIBE topic");
                    } else {
                        String topic = arguments[0];
                        System.out.println(client.unsubscribe(topic));
                    }
                    break;

                case "publish":
                    if (arguments.length != 2) {
                        System.out.println("USAGE : PUBLISH topic content");
                    } else {
                        String topic = arguments[0];
                        String content = arguments[1];
                        System.out.println(client.publish(topic,content));
                    }
                    break;
                case "exit":
                    System.out.println("Au revoir !");
                    client.socket.close();
                    return;
                default:
                    System.out.println("Commande inconnue");
                    break;
            }
        }
    }
}
