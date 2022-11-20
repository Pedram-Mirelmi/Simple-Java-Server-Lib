package com.pedram.net.services;

import com.pedram.net.INetMessageProcessor;
import com.pedram.net.Session;
import com.pedram.net.io.MessageBody;

import java.util.LinkedList;

/**
 * An abstract Event-driven server open for extension like adding services(database, authentication, ...).
 * <p>
 * For simplest scenario, the user needs to implement the start() method that starts all the services.
 *
 * @implNote It's highly recommended that user adds their own services
 * as member variables while extending and also
 * override start method and call super.start() at the end.
 * @implNote It's also recommended that services use a thread safe queue for communication and use
 * producer-consumer model. when a new net message arrives it should be delivered to handleNewMessage method
 */
public abstract class AbstractEventDrivenServer implements IService, INetMessageProcessor {

    protected LinkedList<IService> services;

    protected AbstractNetIOManager netIOManager;

    public AbstractEventDrivenServer(AbstractNetIOManager netIOManager) {
        this.netIOManager = netIOManager;
        this.services.add(netIOManager);
    }

    public AbstractEventDrivenServer() {} // default constructor so that other services will be added later after construction

    public void setNetIOManager(AbstractNetIOManager netIOManager) {
        services.remove(this.netIOManager);
        this.netIOManager = netIOManager;
        services.add(netIOManager);
    }

    @Override
    public void start() {
        for (IService service : services)
            service.start();
    }


    /**
     * The overridden method from INetMessageProcessor. you can implement this and pass this class as an INetMessageManager
     * and make main things happen in the center
     * @param msg
     * @param session
     * @param <MsgType>
     */
    @Override
    public abstract <MsgType> void processNewNetMessage(MessageBody<MsgType> msg, Session session);
}
