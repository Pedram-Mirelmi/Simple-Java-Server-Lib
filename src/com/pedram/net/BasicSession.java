package com.pedram.net;
import java.nio.channels.SocketChannel;

/**
 * A Class that holds any useful information corresponding to a connection.
 * @implNote It's highly recommended that user extends this BasicSession(and implement ISessionCreator accordingly) for further uses
 */
public class BasicSession {
    protected SocketChannel socket;

    public BasicSession(SocketChannel socket) {
        this.socket = socket;
    }

    public SocketChannel getSocket() {
        return socket;
    }
}
