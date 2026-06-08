package com.priveat.app.data.model;

public class ChatMessage {
    private final String message;
    private final boolean fromUser;

    public ChatMessage(String message, boolean fromUser) {
        this.message = message;
        this.fromUser = fromUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFromUser() {
        return fromUser;
    }
}
