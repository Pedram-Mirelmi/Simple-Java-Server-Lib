package com.pedram.net;

import java.nio.channels.SelectionKey;

public class Session {
    SelectionKey selectionKey;

    public Session(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }
}

