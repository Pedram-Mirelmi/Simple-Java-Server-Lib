package com.pedram.demo.serverlib.server;

import com.pedram.demo.serverlib.MessageHeader;
import com.pedram.demo.serverlib.MessageTypes;
import com.pedram.demo.serverlib.SimpleTextMessageBody;
import com.pedram.net.BasicSession;
import com.pedram.net.io.BasicNetMessage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public class Session extends BasicSession {
    ByteBuffer headerInBuffer;
    ByteBuffer bodyInBuffer;

    MessageHeader tempHeader;
    boolean isHeaderRead = false;
    boolean isBodyRead = false;

    public Session(SocketChannel socket) {
        super(socket);
        headerInBuffer = ByteBuffer.allocate(MessageHeader.getMessageHeaderSize());
        headerInBuffer.order(ByteOrder.LITTLE_ENDIAN);
        tempHeader = new MessageHeader();
    }

    public ByteBuffer getHeaderInBuffer() {
        return headerInBuffer;
    }

    public ByteBuffer getBodyInBuffer() {
        return bodyInBuffer;
    }

    public boolean isHeaderRead() {
        return isHeaderRead;
    }

    public boolean isBodyRead() {
        return isBodyRead;
    }

    public void deserializeHeader() throws Exception {
        headerInBuffer.flip();
        tempHeader.deserialize(headerInBuffer);
    }

    public BasicNetMessage<MessageTypes> deserializeMessage() throws Exception {
        switch (tempHeader.getMessageType()) {
            case TEXT_MESSAGE:
                SimpleTextMessageBody msg = new SimpleTextMessageBody();
                bodyInBuffer.flip();
                msg.deserialize(bodyInBuffer);
                return msg;
            default:
                throw new Exception("Unknown type of message found");
        }
    }

    public void resetBuffers() {
        headerInBuffer.clear();
        bodyInBuffer = null;
        isHeaderRead = false;
        isBodyRead = false;
    }

    void tryReadHeader() throws Exception {
        getSocket().read(getHeaderInBuffer());
        if(headerInBuffer.remaining() == 0) {
            isHeaderRead = true;
            deserializeHeader();
            bodyInBuffer = ByteBuffer.allocate(tempHeader.getMessageSize());
            bodyInBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    void tryReadBody() throws Exception {
        getSocket().read(getBodyInBuffer());
        isBodyRead = bodyInBuffer.remaining() == 0;
    }
}
