package com.pedram.net;

import java.nio.channels.SelectionKey;

public interface ISessionCreator {
    Session createSession(SelectionKey key);
}
