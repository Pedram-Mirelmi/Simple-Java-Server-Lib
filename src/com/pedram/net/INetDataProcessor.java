package com.pedram.net;

import java.nio.channels.SelectionKey;

/**
 * The interface that processes the new arrived data from net
 */
public interface INetDataProcessor {

    <MsgType>
    boolean processNewNetData(SelectionKey key);
}
