package com.pedram.net.services;

import com.pedram.net.Session;
import com.pedram.net.io.MessageBody;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The NetWriter service class
 * Most of the class must be implemented by user later based on different methods of writing and sending responses to users
 */
public abstract class AbstractNetWriter implements IService {

    protected ExecutorService writeThreadPool;


    public AbstractNetWriter(int writeThreadsCount) {
        writeThreadPool = Executors.newFixedThreadPool(writeThreadsCount);
    }


    public abstract <MsgType> void writeNewNetMessage(Session session, MessageBody<MsgType> messageBody);

}
