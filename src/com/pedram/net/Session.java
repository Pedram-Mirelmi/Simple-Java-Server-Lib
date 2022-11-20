package com.pedram.net;

import java.nio.channels.SelectionKey;

/**
 * A Class that holds any useful information corresponding to a connection.
 * @implNote It's highly recommended that user extends this Session(and implement ISessionCreator accordingly) for further uses
 */
public class Session {
    SelectionKey selectionKey;

    public Session(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }
}
