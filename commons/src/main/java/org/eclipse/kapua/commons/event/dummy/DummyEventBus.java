/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.commons.event.dummy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.kapua.commons.core.ComponentProvider;
import org.eclipse.kapua.commons.event.EventBus;
import org.eclipse.kapua.commons.event.EventListenerImpl;
import org.eclipse.kapua.locator.inject.ManagedObjectPool;
import org.eclipse.kapua.locator.inject.PoolListener;
import org.eclipse.kapua.locator.inject.Service;
import org.eclipse.kapua.service.event.KapuaEvent;
import org.eclipse.kapua.service.event.KapuaEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 0.3.0
  */
@ComponentProvider
@Service(provides=EventBus.class)
public class DummyEventBus implements EventBus {

    private final static Logger logger = LoggerFactory.getLogger(DummyEventBus.class);

    private Map<Class<?>, Object> availableServices;
    
    private ManagedObjectPool managedObjects;
    private boolean isStarted;
    private PoolListener poolListener;    
    private Map<KapuaEventListener, DummyEventBusConnector> eventConnectors;

    @Inject public DummyEventBus(ManagedObjectPool managedObjects) {
        
        this.availableServices = new HashMap<Class<?>, Object>();
        availableServices.put(EventBus.class, this);
        
        this.managedObjects = managedObjects;
        
        this.isStarted = false;
        this.eventConnectors = new HashMap<KapuaEventListener, DummyEventBusConnector>();
        
        // The plugin wants to be notified when a new object is added to the pool.
        // If the object is an event listener it will then manage to plug it into
        // an event bus connector.
        @SuppressWarnings("resource")
        final DummyEventBus thisEventBus = this;
        poolListener = new PoolListener(){

            @Override
            public void onObjectAdded(Object object) {
                if (KapuaEventListener.class.isAssignableFrom(object.getClass())) {
                    thisEventBus.subscribe((KapuaEventListener) object);
                }
            }
        };
        this.managedObjects.register(poolListener);
       
    }

    @Override
    public void publish(KapuaEvent event) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void subscribe(KapuaEventListener eventListener) {
        logger.info("Adding new event listener {} ...", eventListener.getClass().getName());
        DummyEventBusConnector connector = new DummyEventBusConnector(EventListenerImpl.newInstance(eventListener));
        eventConnectors.put(eventListener, connector);
        if (isStarted()) {
            connector.start();
        }
    }

    @Override
    public KapuaEventListener unsubscribe(KapuaEventListener eventListener) {
        if(eventConnectors.containsKey(eventListener)) {
            eventConnectors.remove(eventListener);
            return eventListener;
        }
        return null;
    }

    @Override
    public boolean isSubscribed(KapuaEventListener eventListener) {
        return eventConnectors.containsKey(eventListener);
    }

    public void start() {
        logger.info("Starting...");
        
        // After a container stop/start sequence the pool may be already populated with objects
        // The event listeners in the pool have to be collected and plugged into an EventConnector
        List<KapuaEventListener> eventListeners = managedObjects.getImplementationsOf(KapuaEventListener.class);
        for (KapuaEventListener listener:eventListeners) {
            subscribe(listener);
        }
        setStarted(true);
        logger.info("Starting...DONE");
    }

    public void stop() {
        logger.info("Stopping...");
        
        Set<KapuaEventListener> listeners = eventConnectors.keySet();
        for(KapuaEventListener listener:listeners) {
            eventConnectors.get(listener).stop();
        }
        setStarted(false);
        logger.info("Stopping...DONE");
    }
    
    private boolean isStarted() {
        return isStarted;
    }

    
    private void setStarted(boolean isStarted) {
        this.isStarted = isStarted;
    }
}
