package com.pedram.demo.serverlib;

import com.pedram.net.io.BasicMessageHeader;

import java.nio.ByteBuffer;

public class MessageHeader extends BasicMessageHeader<MessageTypes> {
    public static int getMessageHeaderSize() {
        return 4 + 1;
    }

    public MessageHeader() {
        super();
    }

    public MessageHeader(int messageSize, MessageTypes messageType) {
        super(messageSize, messageType);
    }

    @Override
    public void deserialize(ByteBuffer buffer) throws Exception {
        messageType = MessageTypes.get(buffer.get());
        messageSize = (int)((long) buffer.getInt() & 0xffffffffL);
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.put(MessageTypes.get(messageType));
        buffer.putInt((int)(messageSize & 0xffffffffL));
    }

    @Override
    public int calculateNeededSizeForThis() {
        return getMessageHeaderSize();
    }
}
