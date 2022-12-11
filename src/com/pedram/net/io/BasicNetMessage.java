package com.pedram.net.io;

import com.pedram.io.ISerializable;

/**
 * @param <MsgType> An enum representing the type of each net message
 */
public abstract class BasicNetMessage<MsgType> implements ISerializable {
    protected BasicMessageHeader<MsgType> header;

    public BasicMessageHeader<MsgType> getHeader() {
        return header;
    }

    public abstract void setHeaderAutomatically();

    /**
     * @return the type of the message
     * for example a login, register, new [x], ...
     * type of the message will help with deserializing later
     */
    public abstract MsgType getMessageType();
}
