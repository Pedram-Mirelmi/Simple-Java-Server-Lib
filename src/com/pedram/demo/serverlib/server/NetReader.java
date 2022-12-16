package com.pedram.demo.serverlib.server;

import com.pedram.net.BasicSession;
import com.pedram.net.INetMessageProcessor;
import com.pedram.net.services.AbstractNetReader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class NetReader extends AbstractNetReader {
    protected INetMessageProcessor<com.pedram.demo.serverlib.MessageTypes> messageProcessor;

    public void setMessageProcessor(INetMessageProcessor<com.pedram.demo.serverlib.MessageTypes> messageProcessor) {
        this.messageProcessor = messageProcessor;
    }
    public NetReader(int readThreadsCount) throws IOException {
        super(readThreadsCount);
    }

    @Override
    protected void handleNewRead(@NotNull BasicSession basicSession) throws Exception {
        Session session = (Session) basicSession;
        if(!session.isHeaderRead()) {
            session.tryReadHeader();
            if(session.isHeaderRead()) {
                session.tryReadBody();
            }
        }
        if(!session.isBodyRead()) {
            session.tryReadBody();
        }
        if(session.isBodyRead()) {
            messageProcessor.processNetMessage(session.deserializeMessage(), session);
            session.resetBuffers();
        }

    }

    @Override
    public void stop() {
        readThreadPool.shutdown();
    }
}
