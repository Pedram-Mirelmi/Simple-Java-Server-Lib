package com.pedram.net.services;

import com.pedram.net.BasicSession;
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

    /**
     * @param readThreadsCount number of all threads that's recommended that this service and all its reading subservices will create and use(preferably in thread-pools
     * @throws IOException calling another constructor may throw an IOException
     */
    public AbstractNetReader(int readThreadsCount) throws IOException {
        readThreadPool = Executors.newFixedThreadPool(readThreadsCount);
        readSelectorPool = new SelectorPool(readThreadsCount);
    }

    /**
     * The overridden method from IService interface that assign tasks to the thread-pool
     */
    @Override
    public void start() throws NullPointerException {
        try {
            for (SelectorPool.SelectorWithChannelCount selector : readSelectorPool.getSelectors()) {
                readThreadPool.execute(new AsyncNetReader(selector.getSelector()));
            }
        } catch (Exception e) {
            System.out.println("Something went wrong with starting net reader: " + e.getMessage());
        }
    }


    /**
     * The Method with which we distribute the new connections to several selectors
     *
     * @param session the session of the new connection
     * @throws IOException registering may throw IOException
     */
    public void registerNewConnection(@NotNull BasicSession session) throws Exception {
        SocketChannel clientSocket = session.getSocket();
        clientSocket.configureBlocking(false);

        // Here we distribute the sockets and hand it in to the selector with the least number of channels registered it
        Selector nextSelector = readSelectorPool.getNextSelector();
        SelectionKey key = clientSocket.register(nextSelector, SelectionKey.OP_READ);
        nextSelector.wakeup();
        key.attach(session);
        readSelectorPool.updateSelectorState(nextSelector);
    }


    protected abstract void handleNewRead(@NotNull BasicSession session) throws Exception;

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
                        if (key.isReadable()) {
                            handleNewRead((BasicSession) key.attachment());
                        } else {
                            throw new IllegalStateException("Unknown state of key");
                        }
                        selectedKeysItter.remove();
                    }
                }
            } catch (Exception e) {
                    System.out.println("Something went wrong with reading: " + e.getMessage());
            }
        }
    }

}
