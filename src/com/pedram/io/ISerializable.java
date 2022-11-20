package com.pedram.io;

import java.nio.ByteBuffer;

public interface ISerializable {
    ISerializable deserialize(ByteBuffer buffer);

    void serialize(ByteBuffer buffer);
}
