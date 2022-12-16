package com.pedram.demo.serverlib;

import com.pedram.net.io.BasicNetMessage;

import java.nio.ByteBuffer;

public class SimpleTextMessageBody extends BasicNetMessage<MessageTypes> {
    private String text;

    public String getText() {
        return text;
    }

    public SimpleTextMessageBody() {}

    public SimpleTextMessageBody(String text) {
        this.text = text;
        setHeaderAutomatically();
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        this.text = new String(buffer.array(), buffer.position(), buffer.remaining());
        this.setHeaderAutomatically();
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.put(text.getBytes());
    }

    @Override
    public int calculateNeededSizeForThis() {
        return text.length();
    }

    @Override
    public void setHeaderAutomatically() {
        this.header = new MessageHeader(text.length(), MessageTypes.TEXT_MESSAGE);
    }

    @Override
    public MessageTypes getMessageType() {
        return MessageTypes.TEXT_MESSAGE;
    }
}
