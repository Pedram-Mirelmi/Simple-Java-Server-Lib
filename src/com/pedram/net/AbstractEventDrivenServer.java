package com.pedram.net;

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
public abstract class AbstractEventDrivenServer implements IService {

    LinkedList<IService> services;

    AbstractNetIOManager netIOManager;

    public AbstractEventDrivenServer(AbstractNetIOManager netIOManager) {
        this.netIOManager = netIOManager;
        this.services.add(netIOManager);
    }

    public AbstractEventDrivenServer() {
    }

    public void setNetIOManager(AbstractNetIOManager netIOManager) {
        services.remove(this.netIOManager);
        this.netIOManager = netIOManager;
        services.add(netIOManager);
    }

    public void start() {
        for (IService service : services)
            service.start();
    }


    abstract <T>
    void handleNewMessage(MessageBody<T> msg, Session session);
}
