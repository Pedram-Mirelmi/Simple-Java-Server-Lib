package com.pedram.net.services;


import com.pedram.net.ISessionCreator;
import com.pedram.net.SelectorPool;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public abstract class AbstractNetReader implements IService {

    protected ExecutorService readThreadPool;
    protected SelectorPool readSelectorPool;

    protected ISessionCreator sessionCreator;

    /**
     * @param readThreadsCount number of all threads that's recommended that this service and all its reading subservices will create and use(preferably in thread-pools
     * @throws IOException calling another constructor may throw an IOException
     */
    public AbstractNetReader(int readThreadsCount) throws IOException {
        this(null, readThreadsCount);
    }

    /**
     * @param sessionCreator   the interface with which we create sessions
     * @param readThreadsCount number of all threads that's recommended that this service and all its reading subservices will create and use(preferably in thread-pools
     * @throws IOException Creating a SelectorPool may throw an IOException
     */
    public AbstractNetReader(ISessionCreator sessionCreator, int readThreadsCount) throws IOException {
        this.sessionCreator = sessionCreator;
        readThreadPool = Executors.newFixedThreadPool(readThreadsCount);
        readSelectorPool = new SelectorPool(readThreadsCount);
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

    public void setSessionCreator(ISessionCreator sessionCreator) {
        this.sessionCreator = sessionCreator;
    }


    /**
     * The Method with which we distribute the new connections to several selectors
     *
     * @param newClient the socket of the new connection
     * @throws IOException registering may throw IOException
     */
    public void registerNewConnection(@NotNull SocketChannel newClient) throws Exception {
        newClient.configureBlocking(false);

        // Here we distribute the sockets and hand it in to the selector with the least number of channels registered it
        Selector nextSelector = readSelectorPool.getNextSelector();
        newClient.register(nextSelector, SelectionKey.OP_READ);
        readSelectorPool.updateSelectorState(nextSelector);
    }


    protected abstract void handleNewRead(@NotNull SelectionKey key) throws Exception;

    /**
     * An implementation of Runnable that will be delivered to the thread-pool
     * <p>
     * Each instance has its own selector that selects some channels each time and then let the dataProcessor to process the data
     */
    protected class AsyncNetReader implements Runnable {

        protected Selector readSelector;

        public AsyncNetReader(Selector selector) {
            this.readSelector = selector;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (readSelector.select() <= 0)
                        continue;
                    Iterator<SelectionKey> selectedKeysItter = readSelector.selectedKeys().iterator();
                    while (selectedKeysItter.hasNext()) {
                        SelectionKey key = selectedKeysItter.next();
                        selectedKeysItter.remove();
                        if (key.isReadable()) {
                            if (key.attachment() == null) {
                                key.attach(sessionCreator.createSession(key));
                            }
                            handleNewRead(key);
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
