package com.pedram.net;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractNetReader implements IService {

    INetDataProcessor dataProcessor;

    ExecutorService readThreadPool;

    SelectorPool readSelectorPool;

    public void setDataProcessor(INetDataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    public void setSessionCreator(ISessionCreator sessionCreator) {
        this.sessionCreator = sessionCreator;
    }

    ISessionCreator sessionCreator;

    /**
     * @param readThreadsCount number of all threads that's recommended that this service and all its reading subservices will create and use(preferably in thread-pools
     * @throws IOException calling another constructor may throw an IOException
     */
    public AbstractNetReader(int readThreadsCount) throws IOException {
        this(null, null, readThreadsCount);
    }

    /**
     * @param sessionCreator   the interface with which we create sessions
     * @param dataProcessor    the interface to which we deliver each new data arrived
     * @param readThreadsCount number of all threads that's recommended that this service and all its reading subservices will create and use(preferably in thread-pools
     * @throws IOException Creating a SelectorPool may throw an IOException
     */
    public AbstractNetReader(INetDataProcessor dataProcessor, ISessionCreator sessionCreator, int readThreadsCount) throws IOException {
        this.dataProcessor = dataProcessor;
        this.sessionCreator = sessionCreator;
        readThreadPool = Executors.newFixedThreadPool(readThreadsCount);
        readSelectorPool = new SelectorPool(readThreadsCount);
    }


    /**
     * The Method with which we distribute the new connections to several selectors
     *
     * @param newClient the socket of the new connection
     * @throws IOException registering may throw IOException
     */
    public void registerNewConnection(@NotNull SocketChannel newClient) throws IOException {
        newClient.configureBlocking(false);

        // Here we distribute the sockets and hand it in to the selector with the least number of channels registered it
        SelectorPool.SelectorWithChannelCount selector = readSelectorPool.getSelectors().poll();
        newClient.register(selector.getSelector(), SelectionKey.OP_READ);
    }


    /**
     * The overridden method from IService interface that assign tasks to the thread-pool
     */
    @Override
    public void start() {
        try {
            for (SelectorPool.SelectorWithChannelCount selector : readSelectorPool.getSelectors()) {
                readThreadPool.execute(new AsyncNetReader(selector.getSelector()));
            }
        } catch (Exception e) {
            System.out.println("Something went wrong with starting net reader: " + e.getMessage());
        }
    }

    /**
     * An implementation of Runnable that will be delivered to the thread-pool
     * <p>
     * Each instance has its own selector that selects some channels each time and then let the dataProcessor to process the data
     */
    private class AsyncNetReader implements Runnable {

        Selector readerSelector;

        public AsyncNetReader(Selector selector) {
            this.readerSelector = selector;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (readerSelector.select() <= 0)
                        continue;
                    Iterator<SelectionKey> selectedKeysItter = readerSelector.selectedKeys().iterator();
                    while (selectedKeysItter.hasNext()) {
                        SelectionKey key = selectedKeysItter.next();
                        selectedKeysItter.remove();
                        if (key.isReadable()) {
                            if (key.attachment() == null) {
                                key.attach(sessionCreator.createSession(key));
                            }
                            if (!dataProcessor.processNewNetData(key)) {
                                readSelectorPool.updateSelectorState(key.selector());
                            }
                        } else {
                            throw new IllegalStateException("Unknown state of key");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Something went wrong with reading: " + e.getMessage());
            }
        }
    }

}
