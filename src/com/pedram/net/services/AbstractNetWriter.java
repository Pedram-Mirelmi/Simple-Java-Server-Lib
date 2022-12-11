package com.pedram.net.services;

import com.pedram.net.BasicSession;
import com.pedram.net.io.BasicNetMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The NetWriter service class
 * Most of the class must be implemented by user later based on different methods of writing and sending responses to users
 */
public abstract class AbstractNetWriter<MsgType> implements IService {

    final protected ExecutorService writeThreadPool;


    public AbstractNetWriter(int writeThreadsCount) {
        writeThreadPool = Executors.newFixedThreadPool(writeThreadsCount);
    }


    public abstract void writeNewNetMessage(BasicSession basicSession, BasicNetMessage<MsgType> basicNetMessage);
}
