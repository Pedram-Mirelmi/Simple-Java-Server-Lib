package com.pedram.net;

import com.pedram.net.io.MessageBody;

public interface INetMessageProcessor {
    
    public abstract <MsgType> void processNewNetMessage(MessageBody<MsgType> msg, Session session);
}

