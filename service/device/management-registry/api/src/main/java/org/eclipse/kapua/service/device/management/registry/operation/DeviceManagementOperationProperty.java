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
package org.eclipse.kapua.service.device.management.registry.operation;

public interface DeviceManagementOperationProperty {

    public String getName();

    public void setName(String name);

    public String getPropertyType();

    public void setPropertyType(String propertyType);

    public String getPropertyValue();

    public void setPropertyValue(String propertyValue);
}