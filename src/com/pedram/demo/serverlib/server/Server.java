package com.pedram.demo.serverlib.server;

import com.pedram.demo.serverlib.MessageTypes;
import com.pedram.demo.serverlib.SimpleTextMessageBody;
import com.pedram.net.BasicSession;
import com.pedram.net.INetMessageProcessor;
import com.pedram.net.io.BasicNetMessage;
import com.pedram.net.services.BasicNetIOManager;
import com.pedram.net.services.IService;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Server implements INetMessageProcessor<MessageTypes>, IService {

    final private int processThreadsCount;
    ExecutorService netMessageProcessorPool;

    BlockingQueue<ProcessTask> processTasks;

    BasicNetIOManager<MessageTypes> netIOManager;


    public void setNetIOManager(BasicNetIOManager<MessageTypes> netIOManager) {
        this.netIOManager = netIOManager;
    }

    public Server(int processThreadsCount) {
        this.processThreadsCount = processThreadsCount;
        netMessageProcessorPool = Executors.newFixedThreadPool(processThreadsCount);
        processTasks = new LinkedBlockingQueue<>();
    }

    @Override
    public void processNetMessage(BasicNetMessage<MessageTypes> msg, BasicSession session) {
        processTasks.add(new ProcessTask(msg, (Session) session));
    }

    @Override
    public void start() throws Exception {
        if(netIOManager == null) {
            throw new NullPointerException("not all services are set properly");
        }
        netIOManager.start();
        for(int i = 0; i < processThreadsCount; i++) {
            netMessageProcessorPool.execute(() -> {
                while (true) {
                    try {
                        ProcessTask task = processTasks.take();
                        BasicNetMessage<MessageTypes> response = handle(task.getMsg());
                        netIOManager.getNetWriter().writeNewNetMessage(task.getSession(), response);
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    private BasicNetMessage<MessageTypes> handle(BasicNetMessage<MessageTypes> msg) {
        switch (msg.getMessageType()) {
            case TEXT_MESSAGE:
                return handleTextMessage((SimpleTextMessageBody) msg);

            default:
                throw new IllegalStateException("Unexpected value: " + msg.getMessageType());
        }
    }

    private BasicNetMessage<MessageTypes> handleTextMessage(SimpleTextMessageBody msg) {
        return new SimpleTextMessageBody("This is a response from server: " + msg.getText()); // echo message back
    }

    @Override
    public void stop() {
        netIOManager.stop();
        netMessageProcessorPool.shutdown();
    }

    private static class ProcessTask {
        BasicNetMessage<MessageTypes> msg;
        Session session;

        public ProcessTask(BasicNetMessage<MessageTypes> msg, Session session) {
            this.msg = msg;
            this.session = session;
        }

        public BasicNetMessage<MessageTypes> getMsg() {
            return msg;
        }

        public Session getSession() {
            return session;
        }
    }
}
