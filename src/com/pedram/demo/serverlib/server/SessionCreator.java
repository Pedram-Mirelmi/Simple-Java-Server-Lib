package com.pedram.demo.serverlib.server;

import com.pedram.net.ISessionCreator;
import com.pedram.net.BasicSession;

import java.nio.channels.SocketChannel;

public class SessionCreator implements ISessionCreator {
    @Override
    public BasicSession createSession(SocketChannel socket) {
        return new Session(socket);
    }
}
