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
package org.eclipse.kapua.commons.event.service.internal;

import javax.inject.Inject;

import org.eclipse.kapua.KapuaEntityNotFoundException;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.event.service.api.Event;
import org.eclipse.kapua.commons.event.service.api.KapuaEventCreator;
import org.eclipse.kapua.commons.event.service.api.KapuaEventListResult;
import org.eclipse.kapua.commons.event.service.api.KapuaEventStoreService;
import org.eclipse.kapua.commons.jpa.EntityManagerFactory;
import org.eclipse.kapua.commons.service.internal.AbstractKapuaService;
import org.eclipse.kapua.commons.util.ArgumentValidator;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.domain.Domain;
import org.eclipse.kapua.service.authorization.permission.Actions;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.event.RaiseKapuaEvent;

/**
 * {@link KapuaEventStoreService} implementation.
 *
 * @since 1.0.0
 */
public class KapuaEventStoreServiceImpl extends AbstractKapuaService implements KapuaEventStoreService {

    private static final Domain KAPUA_EVENT_DOMAIN = new KapuaEventDomain();
    private static final KapuaLocator LOCATOR = KapuaLocator.getInstance();
    private static final AuthorizationService AUTHORIZATION_SERVICE = LOCATOR.getService(AuthorizationService.class);
    private static final PermissionFactory PERMISSION_FACTORY = LOCATOR.getFactory(PermissionFactory.class);

    /**
     * Constructor.
     * 
     * @since 1.0.0
     */
    @Inject
    public KapuaEventStoreServiceImpl(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    @Override
    @RaiseKapuaEvent
    public Event create(KapuaEventCreator kapuaEventCreator)
            throws KapuaException {

        throw new UnsupportedOperationException();
    }

    @Override
    @RaiseKapuaEvent
    public Event update(Event kapuaEvent)
            throws KapuaException {
        //
        // Validation of the fields
        ArgumentValidator.notNull(kapuaEvent.getId(), "kapuaEvent.id");

        //
        // Check Access
        AUTHORIZATION_SERVICE.checkPermission(PERMISSION_FACTORY.newPermission(KAPUA_EVENT_DOMAIN, Actions.write, kapuaEvent.getScopeId()));

        //
        // Do update
        return entityManagerSession.onTransactedResult(em -> {
            Event oldKapuaEvent = KapuaEventStoreDAO.find(em, kapuaEvent.getId());
            if (oldKapuaEvent == null) {
                throw new KapuaEntityNotFoundException(Event.TYPE, kapuaEvent.getId());
            }

            // Update
            return KapuaEventStoreDAO.update(em, kapuaEvent);
        });
    }

    @Override
    @RaiseKapuaEvent
    public void delete(KapuaId scopeId, KapuaId kapuaEventId)
            throws KapuaException {

        //
        // Validation of the fields
        ArgumentValidator.notNull(kapuaEventId, "scopeId");
        ArgumentValidator.notNull(scopeId, "kapuaEventId");

        //
        // Check Access
        Actions action = Actions.write;
        AUTHORIZATION_SERVICE.checkPermission(PERMISSION_FACTORY.newPermission(KAPUA_EVENT_DOMAIN, action, scopeId));

        //
        // Do delete
        entityManagerSession.onTransactedAction(em -> {
            KapuaEventStoreDAO.delete(em, kapuaEventId);
        });
    }

    @Override
    public Event find(KapuaId scopeId, KapuaId kapuaEventId)
            throws KapuaException {
        //
        // Validation of the fields
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(kapuaEventId, "kapuaEventId");

        //
        // Check Access
        AUTHORIZATION_SERVICE.checkPermission(PERMISSION_FACTORY.newPermission(KAPUA_EVENT_DOMAIN, Actions.read, scopeId));

        //
        // Make sure kapuaEvent exists
        return findById(kapuaEventId);
    }

    @Override
    public Event find(KapuaId kapuaEventId)
            throws KapuaException {
        //
        // Validation of the fields
        ArgumentValidator.notNull(kapuaEventId, "kapuaEventId");

        //
        // Check Access
        AUTHORIZATION_SERVICE.checkPermission(PERMISSION_FACTORY.newPermission(KAPUA_EVENT_DOMAIN, Actions.read, kapuaEventId));

        //
        // Make sure kapuaEvent exists
        return findById(kapuaEventId);
    }

    @Override
    public KapuaEventListResult query(KapuaQuery<Event> query)
            throws KapuaException {
        ArgumentValidator.notNull(query, "query");

        //
        // Check Access
        AUTHORIZATION_SERVICE.checkPermission(PERMISSION_FACTORY.newPermission(KAPUA_EVENT_DOMAIN, Actions.read, query.getScopeId()));

        return entityManagerSession.onResult(em -> KapuaEventStoreDAO.query(em, query));
    }

    @Override
    public long count(KapuaQuery<Event> query)
            throws KapuaException {
        ArgumentValidator.notNull(query, "query");
        ArgumentValidator.notNull(query.getScopeId(), "query.scopeId");

        //
        // Check Access
        AUTHORIZATION_SERVICE.checkPermission(PERMISSION_FACTORY.newPermission(KAPUA_EVENT_DOMAIN, Actions.read, query.getScopeId()));

        return entityManagerSession.onResult(em -> KapuaEventStoreDAO.count(em, query));
    }

    /**
     * Find an {@link Event} without authorization checks.
     *
     * @param kapuaEventId
     * @return
     * @throws KapuaException
     * 
     * @since 1.0.0
     */
    private Event findById(KapuaId kapuaEventId)
            throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(kapuaEventId, "kapuaEventId");

        return entityManagerSession.onResult(em -> KapuaEventStoreDAO.find(em, kapuaEventId));
    }

}
