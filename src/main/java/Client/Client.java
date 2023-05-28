package Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;


/**
 * classe du client rédis permettant d'envoyer les requetes au serveur redis.
 *
 */
public class Client {

    Socket socket; // socket se connectant au serveur redis.
    private InputStream inputStream; //permet de récupérer et lire les réponses du serveur à travers le socket
    private OutputStream outputStream;// envoie les commandes sous forme de string via le socket
    Logger logger = Logger.getLogger(Client.class.getName());


    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    /**
     *
     * @param key nom de la clé à créer ou modifier
     * @param value valeur de la clé
     * @return
     * @throws IOException
     * créé une clé et y associe une valeur
     *
     */
    public String set(String key, String value) throws IOException {

        String request = "*3\r\n$3\r\nSET\r\n$" + key.getBytes().length + "\r\n" + key + "\r\n$" + value.getBytes().length + "\r\n" + value + "\r\n";
        //chaque partie de la commande est séparée par \r\n (correspond à un retour à la ligne)
        //1ère partie "*3" correspond au nombre de parametre (ici 3)
        //2eme partie "$3" correspond à la longueur du 1er parametre
        //3eme partie "SET" est le premier parametre, il correspond toujours au nom de la commande
        //4eme partie key.getBytes().length longueur du 2eme parametre
        //5eme partie key 2eme parametre (ici le nom de la clé à set.
        //6eme partie value.getBytes().length longueur du 2eme parametre
        //7eme partie value (valeur à enregistrer dans la clé.
        outputStream.write(request.getBytes());
        return readLine();
    }

    /**
     *
     * @param key
     * @return
     * @throws IOException
     * renvoie la valeur d'une clé
     */
    public String get(String key) throws IOException {
        String request = "*2\r\n$3\r\nGET\r\n$" + key.getBytes().length + "\r\n" + key + "\r\n";
        outputStream.write(request.getBytes());
        return readResponse();
    }

    /**
     *
     * @param key
     * @return
     * @throws IOException
     *
     * renvoie la longueur de la valeur d'une clé.
     */
    public String strlen(String key) throws IOException {
        String request = "*2\r\n$6\r\nSTRLEN\r\n$" + key.getBytes().length + "\r\n" + key + "\r\n";
        outputStream.write(request.getBytes());
        return readLine();
    }

    /**
     *
     * @param key
     * @return
     * @throws IOException
     *
     * permet de s'abonner à un sujet en particulier afin de recevoir toutes les nouvelles postées sur celui-ci
     */
    public String subscribe(String key) throws IOException {
        String request = "*2\r\n$9\r\nsubscribe\r\n$" + key.getBytes().length + "\r\n" + key + "\r\n";
        outputStream.write(request.getBytes());
        return readLine();
    }

    /**
     *
     * @param key
     * @return
     * @throws IOException
     *      * permet de se désabonner d'un sujet en particulier afin de ne plus recevoir les nouvelles postées sur celui-ci
     */
    public String unsubscribe(String key) throws IOException {
        String request = "*2\r\n$11\r\nunsubscribe\r\n$" + key.getBytes().length + "\r\n" + key + "\r\n";
        outputStream.write(request.getBytes());
        return readLine();
    }

    /**
     *
     * @param key
     * @param value
     * @return
     * @throws IOException
     * publie une nouvelle sur le sujet indiqué
     */
    public String publish(String key,String value) throws IOException {
        String request = "*3\r\n$7\r\npublish\r\n$" + key.getBytes().length + "\r\n" + key + "\r\n"+ "\r\n$" + value.getBytes().length + "\r\n" + value + "\r\n";
        outputStream.write(request.getBytes());
        return readLine();
    }

    /**
     *
     * @param key
     * @param value
     * @return
     * @throws IOException
     * ajoute la chaine de caractère indiqué à la fin de la valeur d'une clé.
     */
    public String append(String key, String value) throws IOException {
        String request = "*3\r\n$6\r\nAPPEND\r\n$" + key.getBytes().length + "\r\n" + key + "\r\n$" + value.getBytes().length + "\r\n" + value + "\r\n";
        outputStream.write(request.getBytes());
        return readLine();
    }

    /**
     *
     * @param key
     * @return
     * @throws IOException
     * Si la valeur de la clé indiqué est un int alors l'incrémente de 1.
     */
    public String incr(String key) throws IOException {
        String request = "*2\r\n$4\r\nINCR\r\n$" + key.getBytes().length + "\r\n" + key + "\r\n";
        outputStream.write(request.getBytes());
        return readLine();
    }

    /**
     *
     * @param key
     * @return
     * @throws IOException
     *
     * Si la valeur de la clé indiqué est un int alors la décrémente de 1.
     */
    public String decr(String key) throws IOException {
        String request = "*2\r\n$4\r\nDECR\r\n$" + key.getBytes().length + "\r\n" + key + "\r\n";
        outputStream.write(request.getBytes());
        return readLine();
    }

    /**
     *
     * @param key
     * @return
     * @throws IOException
     *
     * Verifie si une clé existe bien
     */
    public String exists(String key) throws IOException {
        String request = "*2\r\n$6\r\nEXISTS\r\n$" + key.getBytes().length + "\r\n" + key + "\r\n";
        outputStream.write(request.getBytes());
        return readLine();
    }

    /**
     *
     * @param key
     * @return
     * @throws IOException
     * supprime une clé
     */
    public String del(String key) throws IOException {
        String request = "*2\r\n$3\r\nDEL\r\n$" + key.getBytes().length + "\r\n" + key + "\r\n";
        outputStream.write(request.getBytes());
        return readLine();
    }


    /**
     *
     * @param key
     * @param seconds
     * @return
     * @throws IOException
     * Ajoute un temps d'expiration à une clé
     */
    public String expire(String key, int seconds) throws IOException {
        String request = "*3\r\n$6\r\nEXPIRE\r\n$" + key.getBytes().length + "\r\n" + key + "\r\n$" + Integer.toString(seconds).getBytes().length + "\r\n" + Integer.toString(seconds) + "\r\n";
        outputStream.write(request.getBytes());
        return readLine();
    }

    public String readLine() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        int b;
        while ((b = inputStream.read()) != -1) {
            buf.put((byte) b);
            if (b == '\r') {
                buf.put((byte) inputStream.read());
                break;
            }
        }
        buf.flip();
        return StandardCharsets.UTF_8.decode(buf).toString().trim();
    }

    /**
     *
     * @return
     * @throws IOException
     * Lis la réponse du serveur
     */
    public String readResponse() throws IOException {
        char line =readLine().charAt(1);
        int length = Character.getNumericValue(line);
        if (length == -1) {
            return null;
        }
        byte[] data = new byte[length];
        int dataRead = inputStream.read(data);
        if (dataRead != length) {
            throw new RuntimeException("Erreur de lecture de la réponse");
        }
        if (inputStream.read() != '\r' || inputStream.read() != '\n') {
            throw new RuntimeException("Mauvais format de réponse");
        }
        return new String(data, StandardCharsets.UTF_8);
    }
    public int readInteger() throws IOException {
        String line = readLine();
        return Integer.parseInt(line);
    }
}