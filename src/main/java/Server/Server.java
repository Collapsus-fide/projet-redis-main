package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Logger;


public class Server {


    Map<String, String> store = new HashMap<>();
    protected static final String CRLF = "\r\n";
    Logger logger = Logger.getLogger(Server.class.getName());
    private Socket slaveSocket = null; // socket de connexion au serveur slave
    OutputStream slaveOut = null; // outputstream pour envoyer les requetes au slave
    private ServerSocket serverSocket; // socket principal du serveur permettant d'ecouter un port
    private Map<String, Topic> topics = new HashMap<>(); // liste des sujets créés par les utilisateurs et auxquels ils peuvent s'abonner
    ;

    Map<String, KeyExpiration> expirationMap = new HashMap<>();


    /**
     *
     * @param PORT
     * @param SLAVE_PORT
     * @throws IOException
     * Lancement du serveur
     */
    public void start(int PORT,int SLAVE_PORT) throws IOException {
        serverSocket = new ServerSocket(PORT); // socket pour le serveur principal

        //gestion de la connexion au serveur esclave
        if (SLAVE_PORT != -1) {
            slaveSocket = new Socket("localhost", SLAVE_PORT);
            slaveOut = slaveSocket.getOutputStream();

        }


        //Connexion-deconnexion
        System.out.println("Redis server listening on port " + PORT + "...");

        //attente de connexion de clients
        while (true) {
            //si une demande de connexion  est recu sur le port d'écoute alors appel de la fonction handleClients
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            handleClient(clientSocket);
        }
    }


    /**
     *
     * @param clientSocket
     * @throws IOException
     *
     * Création d'un nouveau thread pour gérer le client
     */
    private void handleClient(Socket clientSocket) throws IOException {
        Thread clientThread = new Thread(new ClientHandler(this, clientSocket));
        clientThread.start();
    }

    /**
     *
     * @param topicName
     * @return
     * Créé ou récupère un sujet
     */
    public Topic getOrCreateTopic(String topicName) {
        Topic topic = topics.get(topicName);
        if (topic == null) {
            topic = new Topic();
            topics.put(topicName, topic);
        }
        return topic;
    }

    public Topic getTopic(String topicName) {
        return topics.get(topicName);
    }
}