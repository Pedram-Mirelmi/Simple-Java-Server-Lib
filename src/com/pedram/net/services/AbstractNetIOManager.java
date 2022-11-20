package com.pedram.net.services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * An abstract net io handler that has two separate services for reading and writing
 * We use dependency injection in constructor for these two services
 * <p>
 * Another method that needs implementing is sendRespond. you may just call write on the socket in a loop until there's no remaining.
 * Or you can create a senderService and add it.
 * <p>
 * Extending Session class and overriding createSession is also recommended
 */

public abstract class AbstractNetIOManager implements IService {

    protected final String ip;
    protected final int port;
    protected AbstractNetReader netReader;
    protected AbstractNetWriter netWriter;

    /**
     * @param ip   the ip address to bind the listening socket to it
     * @param port the port that the listening socket will bind to it
     * @throws IOException may be thrown while creating selectors and sockets.
     */
    public AbstractNetIOManager(AbstractNetReader netReader, AbstractNetWriter netWriter, String ip, int port) {
        this.netReader = netReader;
        this.netWriter = netWriter;
        this.ip = ip;
        this.port = port;
    }

    public void setNetReader(AbstractNetReader netReader) {
        this.netReader = netReader;
    }

    public void setNetWriter(AbstractNetWriter netWriter) {
        this.netWriter = netWriter;
    }

    @Override
    public void start() {
        try {
            new Thread(new AsyncNetAcceptor()).start();
            netReader.start();
            netWriter.start();
        } catch (IOException e) {
            System.out.println("Something went wrong with starting the service");
        }
    }

    /**
     * An implementation of Runnable class that will be run in a separate to accept new connections nonblocking
     */
    protected class AsyncNetAcceptor implements Runnable {

        Selector acceptorSelector;
        ServerSocketChannel listeningSocket;

        public AsyncNetAcceptor() throws IOException {
            acceptorSelector = Selector.open();
            listeningSocket = ServerSocketChannel.open();
            listeningSocket.configureBlocking(false);
            listeningSocket.bind(new InetSocketAddress(ip, port));
            listeningSocket.register(acceptorSelector, SelectionKey.OP_ACCEPT);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (acceptorSelector.select() <= 0)
                        continue;
                    Iterator<SelectionKey> selectedKeysItter = acceptorSelector.selectedKeys().iterator();
                    while (selectedKeysItter.hasNext()) {
                        SelectionKey key = selectedKeysItter.next();
                        selectedKeysItter.remove();
                        if (key.isAcceptable()) {
                            netReader.registerNewConnection(listeningSocket.accept());
                        } else {
                            throw new IllegalStateException("Unknown state of key");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Something went wrong with accepting a client");
            }
        }
    }

    /**
     * An implementation of Runnable that will be delivered to a thread-pool to read from multiple sockets nonblocking
     */

}
