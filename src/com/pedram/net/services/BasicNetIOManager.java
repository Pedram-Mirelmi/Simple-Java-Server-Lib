package com.pedram.net.services;

import com.pedram.demo.serverlib.server.SessionCreator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An abstract net io handler that has two separate services for reading and writing
 * We use dependency injection in constructor for these two services
 * Another method that needs implementing is sendRespond. you may just call write on the socket in a loop until there's no remaining.
 * Or you can create a senderService and add it.
 * Extending BasicSession class and overriding createSession is also recommended
 */

public class BasicNetIOManager<MsgType> implements IService {

    protected final String ip;
    protected final int port;
    protected AbstractNetReader netReader;
    protected AbstractNetWriter<MsgType> netWriter;

    protected SessionCreator sessionCreator;

    private final ExecutorService acceptor;
    /**
     * Services are not initialized in this constructor. So pay attention to set them later
     * @param ip the ip of the acceptor socket
     * @param port the port of the acceptor socket
     */
    public BasicNetIOManager(String ip, int port) {
        this(null, null, null, ip, port);
    }

    /**
     * @param ip   the ip address to bind the listening socket to it
     * @param port the port that the listening socket will bind to it
     * @param netWriter the netWriter service
     * @param netReader the netReader service
     */
    public BasicNetIOManager(SessionCreator sessionCreator, AbstractNetReader netReader, AbstractNetWriter<MsgType> netWriter, String ip, int port) {
        this.acceptor = Executors.newSingleThreadExecutor();
        this.netReader = netReader;
        this.netWriter = netWriter;
        this.ip = ip;
        this.port = port;
    }

    public void setNetReader(AbstractNetReader netReader) {
        this.netReader = netReader;
    }

    public void setNetWriter(AbstractNetWriter<MsgType> netWriter) {
        this.netWriter = netWriter;
    }

    public void setSessionCreator(SessionCreator sessionCreator) {
        this.sessionCreator = sessionCreator;
    }

    public AbstractNetWriter<MsgType> getNetWriter() {
        return netWriter;
    }

    @Override
    public void start() throws Exception {
        if(netWriter == null || netReader == null || sessionCreator == null)
            throw new RuntimeException("Not All Services has set properly");
        try {
            acceptor.execute(new AsyncNetAcceptor());
            netReader.start();
            netWriter.start();
        } catch (IOException e) {
            System.out.println("Something went wrong with starting the service: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        acceptor.shutdown();
        netReader.stop();
        netWriter.stop();
    }

    /**
     * An implementation of Runnable class that will be run in a separate to accept new connections nonblocking
     */
    protected class AsyncNetAcceptor implements Runnable {

        protected Selector acceptorSelector;
        protected ServerSocketChannel listeningSocket;

        public AsyncNetAcceptor() throws IOException {
            acceptorSelector = Selector.open();
            listeningSocket = ServerSocketChannel.open();
            listeningSocket.configureBlocking(false);
            listeningSocket.bind(new InetSocketAddress(ip, port));
            listeningSocket.register(acceptorSelector, SelectionKey.OP_ACCEPT);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (acceptorSelector.select() <= 0)
                        continue;
                    Iterator<SelectionKey> selectedKeysItter = acceptorSelector.selectedKeys().iterator();
                    while (selectedKeysItter.hasNext()) {
                        SelectionKey key = selectedKeysItter.next();
                        if (key.isAcceptable()) {
                            netReader.registerNewConnection(sessionCreator.createSession((SocketChannel) key.channel()));
                        } else {
                            throw new IllegalStateException("Unknown state of key");
                        }
                        selectedKeysItter.remove();
                    }

                } catch (Exception e) {
                    System.out.println("Something went wrong with accepting a client: " + e.getMessage());
                }
            }
        }
    }
}
