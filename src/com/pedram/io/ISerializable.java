package com.pedram.io;

import java.nio.ByteBuffer;

public interface ISerializable {
    void deserialize(ByteBuffer buffer) throws Exception;

    void serialize(ByteBuffer buffer);

    int calculateNeededSizeForThis();
}
