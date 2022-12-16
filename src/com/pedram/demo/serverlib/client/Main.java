package com.pedram.demo.serverlib.client;

import com.pedram.demo.serverlib.MessageHeader;
import com.pedram.demo.serverlib.MessageTypes;
import com.pedram.demo.serverlib.SimpleTextMessageBody;
import com.pedram.net.io.BasicNetMessage;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("127.0.0.1", 60000);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter a String");
            String line = scanner.nextLine();
            BasicNetMessage<MessageTypes> msg = new SimpleTextMessageBody(line);
            ByteBuffer buffer = ByteBuffer.allocate(MessageHeader.getMessageHeaderSize() + msg.calculateNeededSizeForThis());
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            msg.getHeader().serialize(buffer);
            msg.serialize(buffer);
            buffer.flip();
            socket.getOutputStream().write(buffer.array());

            ByteBuffer headerBuffer = ByteBuffer.wrap(socket.getInputStream().readNBytes(5));
            headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
            MessageHeader header = new MessageHeader();
            header.deserialize(headerBuffer);

            ByteBuffer bodyBuffer = ByteBuffer.wrap(socket.getInputStream().readNBytes(header.getMessageSize()));

            switch (header.getMessageType()) {
                case TEXT_MESSAGE:
                    SimpleTextMessageBody response = new SimpleTextMessageBody();
                    response.deserialize(bodyBuffer);
                    System.out.println("Response: " + response.getText());
                    break;
                default:
                    throw new IllegalStateException("Unknown message type");

            }
        }

    }

}
