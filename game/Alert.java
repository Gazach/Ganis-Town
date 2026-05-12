package game;

public enum Alert {
    NEED_WORKER("Need workers!"),
    NOT_ENOUGH_WORKERS("Not enough worker!"),
    NEED_ROAD("Need road access!");

    private final String message;

    Alert(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
