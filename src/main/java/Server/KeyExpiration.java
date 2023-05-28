package Server;

public class KeyExpiration {
    private String key;
    private long expirationTime;

    public KeyExpiration(String key, long expirationTime) {
        this.key = key;
        this.expirationTime = expirationTime;
    }

    public String getKey() {
        return key;
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}
