/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.commons.event.bus.jms;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.KapuaRuntimeException;
import org.eclipse.kapua.commons.event.EventScope;
import org.eclipse.kapua.commons.event.bus.EventBusMarshaler;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.security.KapuaSession;
import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.eclipse.kapua.commons.setting.system.SystemSettingKey;
import org.eclipse.kapua.service.event.KapuaEvent;
import org.eclipse.kapua.service.event.KapuaEventBus;
import org.eclipse.kapua.service.event.KapuaEventBusException;
import org.eclipse.kapua.service.event.KapuaEventBusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * JMS event bus implementation
 * 
 * @since 1.0
 */
public class JMSEventBus implements KapuaEventBus, ExceptionListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(JMSEventBus.class);

    private final static int PRODUCER_POOL_MIN_SIZE = SystemSetting.getInstance().getInt(SystemSettingKey.EVENT_BUS_PRODUCER_POOL_MIN_SIZE);
    private final static int PRODUCER_POOL_MAX_SIZE = SystemSetting.getInstance().getInt(SystemSettingKey.EVENT_BUS_PRODUCER_POOL_MAX_SIZE);
    private final static int PRODUCER_POOL_BORROW_WAIT = SystemSetting.getInstance().getInt(SystemSettingKey.EVENT_BUS_PRODUCER_POOL_BORROW_WAIT_MAX);
    private final static int PRODUCER_POOL_EVICTION_INTERVAL = SystemSetting.getInstance().getInt(SystemSettingKey.EVENT_BUS_PRODUCER_EVICTION_INTERVAL);
    private final static int CONSUMER_POOL_SIZE = SystemSetting.getInstance().getInt(SystemSettingKey.EVENT_BUS_CONSUMER_POOL_SIZE);
    private final static String MESSAGE_SERIALIZER = SystemSetting.getInstance().getString(SystemSettingKey.EVENT_BUS_MESSAGE_SERIALIZER);

    private List<Subscription> subscriptionList = new ArrayList<>();
    private EventBusJMSConnectionBridge eventBusJMSConnectionBridge;
    private EventBusMarshaler eventBusMarshaler;

    /**
     * Default constructor
     * 
     * @throws KapuaEventBusException
     * @throws JMSException
     */
    public JMSEventBus() throws KapuaEventBusException, JMSException {
        eventBusJMSConnectionBridge = new EventBusJMSConnectionBridge(this);
    }

    /**
     * Start the event bus
     * 
     * @throws KapuaEventBusException
     */
    public void start() throws KapuaEventBusException {
        try {
            //initialize event bus marshaler
            Class<?> messageSerializerClazz = Class.forName(MESSAGE_SERIALIZER);
            if (EventBusMarshaler.class.isAssignableFrom(messageSerializerClazz)) {
                eventBusMarshaler = (EventBusMarshaler) messageSerializerClazz.newInstance();
            }
            else {
                throw new KapuaEventBusException(String.format("Wrong message serializer Object type ('%s')!", messageSerializerClazz));
            }

            eventBusJMSConnectionBridge.start();
        } catch (JMSException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new KapuaEventBusException(e);
        }
    }

    @Override
    public void publish(String address, KapuaEvent kapuaEvent) 
            throws KapuaEventBusException {
        eventBusJMSConnectionBridge.publish(address, kapuaEvent);
    }

    @Override
    public synchronized void subscribe(String address, String name, final KapuaEventBusListener kapuaEventListener)
            throws KapuaEventBusException {
        try {
            Subscription subscription = new Subscription(address, name, kapuaEventListener);
            subscriptionList.add(subscription);
            eventBusJMSConnectionBridge.subscribe(subscription);
        } catch (KapuaEventBusException e) {
            throw new KapuaEventBusException(e);
        }
    }

    private final void setSession(KapuaEvent kapuaEvent) {
        KapuaSession.createFrom(kapuaEvent.getScopeId(), kapuaEvent.getUserId());
    }

    /**
     * Stop the event bus
     * 
     * @throws KapuaEventBusException
     */
    public void stop() throws KapuaEventBusException {
        eventBusJMSConnectionBridge.stop();
    }

    @Override
    public void onException(JMSException e) {
        LOGGER.error("Connection to the event bus thrown exception: {}", e.getMessage(), e);
        int i = 1;
        while (true) {
            LOGGER.info("EventBus restarting attempt... {}", i);
            try {
                restart();
                LOGGER.info("EventBus restarting attempt... {} DONE", i);
                LOGGER.info("EventBus connection RESTORED");
                break;
            } catch (KapuaEventBusException | JMSException e1) {
                LOGGER.error("Cannot start new event bus connection... try again...", e1);
                waitBeforeRetry();
            }
            i++;
        }
    }

    private void waitBeforeRetry() {
        // wait a bit
        try {
            Thread.sleep(2000);// TODO move to configuration
        } catch (InterruptedException e) {
            LOGGER.error("Wait for connect interrupted!", e);
        }
    }

    private void restart() throws KapuaEventBusException, JMSException {
        // restart the event bus connection bridge with a new instance
        // so no synchronization is needed
        EventBusJMSConnectionBridge instanceToCleanUp = null;
        try {
            EventBusJMSConnectionBridge newInstance = new EventBusJMSConnectionBridge(this);
            newInstance.start();
            // restore subscriptions
            for (Subscription subscription : subscriptionList) {
                newInstance.subscribe(subscription);
            }
            instanceToCleanUp = eventBusJMSConnectionBridge;
            eventBusJMSConnectionBridge = newInstance;
        } catch (Throwable t) {
            throw t;
        }
        finally {
            try {
                if (instanceToCleanUp != null) {
                    LOGGER.info("Cleanup old JMSConnectionBridge instance...");
                    instanceToCleanUp.stop();
                }
                else {
                    LOGGER.warn("Null old JMSConnectionBridge instance. no cleanup will be done...");
                }
            } catch (KapuaEventBusException e) {
                LOGGER.error("Cannot destroy old event bus connection: {}", e.getMessage(), e);
            } finally {
                instanceToCleanUp = null;
            }
        }
    }

    private class EventBusJMSConnectionBridge {

        private Connection jmsConnection;
        private Map<String, SenderPool> senders = new HashMap<>();

        public EventBusJMSConnectionBridge(ExceptionListener exceptionListener) throws JMSException {
            String eventbusUrl = SystemSetting.getInstance().getString(SystemSettingKey.EVENT_BUS_URL);
            String eventbusUsername = SystemSetting.getInstance().getString(SystemSettingKey.EVENT_BUS_USERNAME);
            String eventbusPassword = SystemSetting.getInstance().getString(SystemSettingKey.EVENT_BUS_PASSWORD);
            ConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(eventbusUrl);

            jmsConnection = jmsConnectionFactory.createConnection(eventbusUsername, eventbusPassword);
            jmsConnection.setExceptionListener(exceptionListener);
        }

        void start() throws JMSException {
            jmsConnection.start();
        }

        void stop() throws KapuaEventBusException {
            try {
                if (jmsConnection != null) {
                    jmsConnection.close();
                }
            } catch (JMSException e) {
                throw new KapuaEventBusException(e);
            }
            finally {
                jmsConnection = null;
            }

            // iterate over all possibles entries
            Iterator<String> senderIterator = senders.keySet().iterator();
            while (senderIterator.hasNext()) {
                SenderPool senderPool = senders.get(senderIterator.next());
                senderPool.close();
                senderPool.clear();
                // borrowed object will be returned to the pool soon (since the connection is gone bad) and then destroyed by the pool (since the pool is stopped)
            }
        }

        void publish(String address, KapuaEvent kapuaEvent)
                throws KapuaEventBusException {
            if (address != null && address.trim().length() > 0) {
                SenderPool senderPool = senders.get(address);
                Sender sender = null;
                try {
                    if (senderPool == null) {
                        synchronized (SenderPool.class) {
                            senderPool = senders.get(address);
                            if (senderPool == null) {
                                senderPool = new SenderPool(new PooledSenderFactory(address));
                                senders.put(address, senderPool);
                            }
                        }
                    }
                    sender = senderPool.borrowObject();
                    sender.sendMessage(kapuaEvent);
                } catch (Exception e) {
                    throw new KapuaEventBusException(e);
                } finally {
                    if (sender != null) {
                        senderPool.returnObject(sender);
                    }
                }
            } else {
                LOGGER.warn("Discarded event publish since the publish address is empty!");
            }
        }

        synchronized void subscribe(Subscription subscription)
                throws KapuaEventBusException {
            try {
                String subscriptionStr = String.format("events.%s", subscription.getAddress());
                // create a bunch of sessions to allow parallel event processing
                for (int i = 0; i < CONSUMER_POOL_SIZE; i++) {
                    final Session jmsSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    Topic jmsTopic = jmsSession.createTopic(subscriptionStr);
                    MessageConsumer jmsConsumer = jmsSession.createSharedDurableConsumer(jmsTopic, subscription.getName());
                    jmsConsumer.setMessageListener(new MessageListener() {

                        @Override
                        public void onMessage(Message message) {
                            try {
                                if (message instanceof TextMessage) {
                                    TextMessage textMessage = (TextMessage) message;
                                    final KapuaEvent kapuaEvent = eventBusMarshaler.unmarshal(textMessage);
                                    setSession(kapuaEvent);
                                    KapuaSecurityUtils.doPrivileged(() -> {
                                        try {
                                            // restore event context
                                            EventScope.set(kapuaEvent);
                                            subscription.getKapuaEventListener().onKapuaEvent(kapuaEvent);
                                        } finally {
                                            EventScope.end();
                                        }
                                    });

                                } else {
                                    LOGGER.error("Discarding wrong event message type '{}'", message != null ? message.getClass() : "null");
                                }
                            } catch (Throwable t) {
                                LOGGER.error(t.getMessage(), t);
                                // throwing the exception to prevent the message acknowledging (https://docs.oracle.com/javaee/7/api/javax/jms/Session.html#AUTO_ACKNOWLEDGE)
                                throw KapuaRuntimeException.internalError(t);
                            }
                        }
                    });
                }
            } catch (JMSException e) {
                throw new KapuaEventBusException(e);
            }
        }

        private class Sender {

            // TODO manage the session/producer in a stronger way (if the client disconnects due to a network error the connection will not be restored)
            private Session jmsSession;
            private MessageProducer jmsProducer;

            public Sender(Connection jmsConnection, String address) throws JMSException {
                address = String.format("events.%s", address);
                jmsSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic jmsTopic = jmsSession.createTopic(address);
                jmsProducer = jmsSession.createProducer(jmsTopic);
            }

            public void sendMessage(KapuaEvent kapuaEvent) throws Exception {
                try {
                    TextMessage message = jmsSession.createTextMessage();
                    // Serialize outgoing kapua event based on platform configuration
                    eventBusMarshaler.marshal(message, kapuaEvent);
                    jmsProducer.send(message);
                } catch (JMSException | KapuaException e) {
                    LOGGER.error("Message publish interrupted: {}", e.getMessage());
                    throw e;
                }
            }

            public void close() {
                try {
                    jmsSession.close();
                } catch (JMSException e) {
                    LOGGER.warn("Cannot close the Sender connection!", e);
                }
            }

        }

        private class PooledSenderFactory extends BasePooledObjectFactory<Sender> {

            private String address;

            public PooledSenderFactory(String address) {
                this.address = address;
            }

            @Override
            public Sender create()
                    throws Exception {
                try {
                    return new Sender(jmsConnection, address);
                } catch (JMSException e) {
                    throw new KapuaEventBusException(e);
                }
            }

            @Override
            public PooledObject<Sender> wrap(Sender sender) {
                return new DefaultPooledObject<>(sender);
            }

            @Override
            public void destroyObject(PooledObject<Sender> pooledSender)
                    throws Exception {
                Sender sender = pooledSender.getObject();
                if (sender != null) {
                    sender.close();
                }
                super.destroyObject(pooledSender);
            }

        }

        private class SenderPool extends GenericObjectPool<Sender> {

            public SenderPool(PooledSenderFactory factory) {
                super(factory);

                GenericObjectPoolConfig senderPoolConfig = new GenericObjectPoolConfig();
                senderPoolConfig.setMinIdle(PRODUCER_POOL_MIN_SIZE);
                senderPoolConfig.setMaxIdle(PRODUCER_POOL_MAX_SIZE);
                senderPoolConfig.setMaxTotal(PRODUCER_POOL_MAX_SIZE);
                senderPoolConfig.setMaxWaitMillis(PRODUCER_POOL_BORROW_WAIT);
                senderPoolConfig.setTestOnReturn(true);
                senderPoolConfig.setTestOnBorrow(true);
                senderPoolConfig.setTestWhileIdle(false);
                senderPoolConfig.setBlockWhenExhausted(true);
                senderPoolConfig.setTimeBetweenEvictionRunsMillis(PRODUCER_POOL_EVICTION_INTERVAL);
                setConfig(senderPoolConfig);
            }

        }

    }

    private class Subscription {

        String name;
        String address;
        KapuaEventBusListener kapuaEventListener;

        public Subscription(String address, String name, KapuaEventBusListener kapuaEventListener) {
            this.name = name;
            this.address = address;
            this.kapuaEventListener = kapuaEventListener;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public KapuaEventBusListener getKapuaEventListener() {
            return kapuaEventListener;
        }

    }

}
