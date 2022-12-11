package com.pedram.net;

import com.pedram.net.io.BasicNetMessage;
import com.pedram.net.services.IService;

public interface INetMessageProcessor<MsgType> extends IService {
    
    void processNetMessage(BasicNetMessage<MsgType> msg, BasicSession session);
}

