package com.pedram.net;

/**
 * The interface that processes the new arrived data from net
 */
public interface INetDataProcessor {

    <MsgType>
    boolean processNewNetData(Session session);
}
