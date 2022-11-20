package com.pedram.net.services;

import com.pedram.net.Session;
import com.pedram.net.io.MessageBody;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractNetWriter implements IService {

    protected ExecutorService writeThreadPool;


    public AbstractNetWriter(int writeThreadsCount) {
        writeThreadPool = Executors.newFixedThreadPool(writeThreadsCount);
    }


    public abstract <MsgType> void write(Session session, MessageBody<MsgType> messageBody);

}
