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
package org.eclipse.kapua.service.device.management.registry.operation.notification;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.service.KapuaEntityService;

public interface DeviceManagementOperationNotificationRegistryService
        extends KapuaEntityService<DeviceManagementOperationNotification, DeviceManagementOperationNotificationCreator> {

    /**
     * Returns the {@link DeviceManagementOperationNotificationListResult} with elements matching the provided query.
     *
     * @param query The {@link DeviceManagementOperationNotificationQuery} used to filter results.
     * @return The {@link DeviceManagementOperationNotificationListResult} with elements matching the query parameter.
     * @throws KapuaException
     * @since 1.0.0
     */
    @Override
    public DeviceManagementOperationNotificationListResult query(KapuaQuery<DeviceManagementOperationNotification> query) throws KapuaException;

}
