package com.pedram.demo.serverlib;

public enum MessageTypes {
    TEXT_MESSAGE ((byte) 1);

    private final byte num;
    MessageTypes(byte b) {
        this.num = b;
    }

    static byte get(MessageTypes type) {
        return type.num;
    }

    static MessageTypes get(int num) throws Exception {
        switch (num) {
            case 1:
                return MessageTypes.TEXT_MESSAGE;
            default:
                throw new Exception(String.format("%s doesn't represent a message type", num));
        }
    }
}
