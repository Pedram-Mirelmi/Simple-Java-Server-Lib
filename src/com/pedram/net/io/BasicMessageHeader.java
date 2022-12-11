package com.pedram.net.io;

import com.pedram.io.ISerializable;



public abstract class BasicMessageHeader<MsgType> implements ISerializable {

    protected int messageSize;
    protected MsgType messageType;

    public BasicMessageHeader() {}

    public BasicMessageHeader(int messageSize, MsgType messageType) {
        this.messageSize = messageSize;
        this.messageType = messageType;
    }

    public MsgType getMessageType() {
        return messageType;
    }

    public int getMessageSize() {
        return messageSize;
    }
}
