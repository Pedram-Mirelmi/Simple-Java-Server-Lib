package com.pedram.net.io;

import com.pedram.io.ISerializable;

/**
 * @param <MsgType> An enum representing the type of each net message
 */
public abstract class MessageBody<MsgType> implements ISerializable {
    /**
     * calculates the size of the message's body when serialized
     * may be dependent on any non-primitive types
     *
     * @return the size in bytes needed to serialize (body only)
     */
    public abstract int calculateBodySize();

    /**
     * @return the type of the message
     * for example a login, register, new [x], ...
     * type of the message will help with deserializing later
     */
    public abstract MsgType getMessageType();
}
