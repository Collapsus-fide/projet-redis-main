package Server;

import java.util.ArrayList;
import java.util.List;

public class Topic {
    private List<ClientHandler> subscribers;

    public Topic() {
        subscribers = new ArrayList<>();
    }

    public void subscribe(ClientHandler subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(ClientHandler subscriber) {
        subscribers.remove(subscriber);
    }

    public void publish(String message) {
        for (ClientHandler subscriber : subscribers) {
            subscriber.sendMessage(message);
        }
    }
}