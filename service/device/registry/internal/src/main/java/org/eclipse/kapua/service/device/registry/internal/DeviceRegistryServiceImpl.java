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
package org.eclipse.kapua.service.device.registry.internal;

import org.eclipse.kapua.KapuaEntityNotFoundException;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.KapuaIllegalArgumentException;
import org.eclipse.kapua.commons.configuration.AbstractKapuaConfigurableResourceLimitedService;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicate;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.model.query.predicate.KapuaPredicate;
import org.eclipse.kapua.service.authorization.domain.Domain;
import org.eclipse.kapua.service.device.registry.Device;
import org.eclipse.kapua.service.device.registry.DeviceCreator;
import org.eclipse.kapua.service.device.registry.DeviceFactory;
import org.eclipse.kapua.service.device.registry.DeviceListResult;
import org.eclipse.kapua.service.device.registry.DevicePredicates;
import org.eclipse.kapua.service.device.registry.DeviceQuery;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.eclipse.kapua.service.device.registry.common.DeviceValidation;
import org.eclipse.kapua.service.event.KapuaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DeviceRegistryService} implementation.
 *
 * @since 1.0.0
 */
@KapuaProvider
public class DeviceRegistryServiceImpl extends AbstractKapuaConfigurableResourceLimitedService<Device, DeviceCreator, DeviceRegistryService, DeviceListResult, DeviceQuery, DeviceFactory>
        implements DeviceRegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRegistryServiceImpl.class);

    private static final Domain DEVICE_DOMAIN = new DeviceDomain();
    // Constructors

    /**
     * Constructor
     *
     * @param deviceEntityManagerFactory
     */
    public DeviceRegistryServiceImpl(DeviceEntityManagerFactory deviceEntityManagerFactory) {
        super(DeviceRegistryService.class.getName(), DEVICE_DOMAIN, deviceEntityManagerFactory, DeviceRegistryService.class, DeviceFactory.class);
    }

    /**
     * Constructor
     */
    public DeviceRegistryServiceImpl() {
        this(DeviceEntityManagerFactory.instance());
    }

    // Operations implementation
    @Override
    public Device create(DeviceCreator deviceCreator) throws KapuaException {
        DeviceValidation.validateCreatePreconditions(deviceCreator);
        if (allowedChildEntities(deviceCreator.getScopeId()) <= 0) {
            // TODO Check exception type to be catched by the broker
            throw new KapuaIllegalArgumentException("scopeId", "max devices reached");
        }
        return entityManagerSession.onTransactedInsert(entityManager -> DeviceDAO.create(entityManager, deviceCreator));
    }

    @Override
    public Device update(Device device) throws KapuaException {
        DeviceValidation.validateUpdatePreconditions(device);

        return entityManagerSession.onTransactedResult(entityManager -> {
            Device currentDevice = DeviceDAO.find(entityManager, device.getId());
            if (currentDevice == null) {
                throw new KapuaEntityNotFoundException(Device.TYPE, device.getId());
            }

            currentDevice.setStatus(device.getStatus());
            currentDevice.setDisplayName(device.getDisplayName());
            currentDevice.setGroupId(device.getGroupId());
            currentDevice.setSerialNumber(device.getSerialNumber());
            currentDevice.setModelId(device.getModelId());
            currentDevice.setImei(device.getImei());
            currentDevice.setImsi(device.getImsi());
            currentDevice.setIccid(device.getIccid());
            currentDevice.setBiosVersion(device.getBiosVersion());
            currentDevice.setFirmwareVersion(device.getFirmwareVersion());
            currentDevice.setOsVersion(device.getOsVersion());
            currentDevice.setJvmVersion(device.getJvmVersion());
            currentDevice.setOsgiFrameworkVersion(device.getOsgiFrameworkVersion());
            currentDevice.setApplicationFrameworkVersion(device.getApplicationFrameworkVersion());
            currentDevice.setApplicationIdentifiers(device.getApplicationIdentifiers());
            currentDevice.setAcceptEncoding(device.getAcceptEncoding());
            currentDevice.setCustomAttribute1(device.getCustomAttribute1());
            currentDevice.setCustomAttribute2(device.getCustomAttribute2());
            currentDevice.setCustomAttribute3(device.getCustomAttribute3());
            currentDevice.setCustomAttribute4(device.getCustomAttribute4());
            currentDevice.setCustomAttribute5(device.getCustomAttribute5());

            currentDevice.setConnectionId(device.getConnectionId());
            currentDevice.setLastEventId(device.getLastEventId());

            currentDevice.setTagIds(device.getTagIds());
            // Update
            return DeviceDAO.update(entityManager, currentDevice);
        });
    }

    @Override
    public Device find(KapuaId scopeId, KapuaId entityId) throws KapuaException {
        DeviceValidation.validateFindPreconditions(scopeId, entityId);
        return entityManagerSession.onResult(entityManager -> DeviceDAO.find(entityManager, entityId));
    }

    @Override
    public DeviceListResult query(KapuaQuery<Device> query) throws KapuaException {
        DeviceValidation.validateQueryPreconditions(query);
        return entityManagerSession.onResult(entityManager -> DeviceDAO.query(entityManager, query));
    }

    @Override
    public long count(KapuaQuery<Device> query) throws KapuaException {
        DeviceValidation.validateCountPreconditions(query);
        return entityManagerSession.onResult(entityManager -> DeviceDAO.count(entityManager, query));
    }

    @Override
    public void delete(KapuaId scopeId, KapuaId deviceId) throws KapuaException {
        DeviceValidation.validateDeletePreconditions(scopeId, deviceId);
        entityManagerSession.onTransactedAction(entityManager -> DeviceDAO.delete(entityManager, deviceId));
    }

    @Override
    public Device findByClientId(KapuaId scopeId, String clientId) throws KapuaException {
        DeviceValidation.validateFindByClientIdPreconditions(scopeId, clientId);

        DeviceQueryImpl query = new DeviceQueryImpl(scopeId);
        KapuaPredicate predicate = new AttributePredicate<>(DevicePredicates.CLIENT_ID, clientId);
        query.setPredicate(predicate);

        //
        // Query and parse result
        Device device = null;
        DeviceListResult result = query(query);
        if (!result.isEmpty()) {
            device = result.getFirstItem();
        }

        return device;
    }

    @Override
    public void onKapuaEvent(KapuaEvent kapuaEvent) throws KapuaException {
        if (kapuaEvent == null) {
            //service bus error. Throw some exception?
        }
        LOGGER.info("DeviceRegistryService: received kapua event from {}, operation {}", kapuaEvent.getService(), kapuaEvent.getOperation());
        if ("group".equals(kapuaEvent.getService()) && "delete".equals(kapuaEvent.getOperation())) {
            deleteDeviceByGroupId(kapuaEvent.getScopeId(), kapuaEvent.getEntityId());
        }
        else if ("account".equals(kapuaEvent.getService()) && "delete".equals(kapuaEvent.getOperation())) {
            deleteDeviceByAccountId(kapuaEvent.getScopeId(), kapuaEvent.getEntityId());
        }
    }

    private void deleteDeviceByGroupId(KapuaId scopeId, KapuaId groupId) throws KapuaException {
        KapuaLocator locator = KapuaLocator.getInstance();
        DeviceFactory deviceFactory = locator.getFactory(DeviceFactory.class);

        DeviceQuery query = deviceFactory.newQuery(scopeId);
        query.setPredicate(new AttributePredicate<>(DevicePredicates.GROUP_ID, groupId));

        DeviceListResult devicesToDelete = query(query);

        for (Device d : devicesToDelete.getItems()) {
            d.setGroupId(null);
            update(d);
        }
    }

    private void deleteDeviceByAccountId(KapuaId scopeId, KapuaId accountId) throws KapuaException {
        KapuaLocator locator = KapuaLocator.getInstance();
        DeviceFactory deviceFactory = locator.getFactory(DeviceFactory.class);

        DeviceQuery query = deviceFactory.newQuery(accountId);

        DeviceListResult devicesToDelete = query(query);

        for (Device d : devicesToDelete.getItems()) {
            delete(d.getScopeId(), d.getId());
        }
    }

}
