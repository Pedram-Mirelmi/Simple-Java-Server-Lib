package com.pedram.demo.serverlib.server;

import com.pedram.demo.serverlib.MessageHeader;
import com.pedram.net.BasicSession;
import com.pedram.net.io.BasicNetMessage;
import com.pedram.net.services.AbstractNetWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetWriter<MsgType> extends AbstractNetWriter<MsgType> {
    private final int writeThreadsCount;

    private final BlockingQueue<WriteTask<MsgType>> writeTasks;
    public NetWriter(int writeThreadsCount) {
        super(writeThreadsCount);
        this.writeThreadsCount = writeThreadsCount;
        this.writeTasks = new LinkedBlockingQueue<>();
    }

    @Override
    public void writeNewNetMessage(BasicSession session, BasicNetMessage<MsgType> msg) {
        writeTasks.add(new WriteTask<>(session, msg));
    }

    @Override
    public void start() {
        for(int i = 0; i < this.writeThreadsCount; i++) {
            writeThreadPool.execute(() -> {
                while (true) {
                    try {
                        WriteTask<MsgType> writeTask = writeTasks.take();
                        ByteBuffer msgBuffer = ByteBuffer.allocate(MessageHeader.getMessageHeaderSize()
                                                                    + writeTask.msgBody.calculateNeededSizeForThis());
                        msgBuffer.order(ByteOrder.LITTLE_ENDIAN);
                        writeTask.msgBody.getHeader().serialize(msgBuffer);
                        writeTask.msgBody.serialize(msgBuffer);
                        msgBuffer.flip();
                        writeTask.session.getSocket().write(msgBuffer);
                    } catch (InterruptedException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    @Override
    public void stop() {
        writeThreadPool.shutdown();
    }

    private static class WriteTask<MsgType> {
        private final BasicSession session;
        private final BasicNetMessage<MsgType> msgBody;

        public WriteTask(BasicSession session, BasicNetMessage<MsgType> msgBody)
        {
            this.session = session;
            this.msgBody = msgBody;
        }

    }
}
