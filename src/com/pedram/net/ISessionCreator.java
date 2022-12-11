package com.pedram.net;
import java.nio.channels.SocketChannel;

public interface ISessionCreator {
    BasicSession createSession(SocketChannel socket);
}
