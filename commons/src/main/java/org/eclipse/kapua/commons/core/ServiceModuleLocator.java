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
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.commons.core;

public class ServiceModuleLocator {

    private static ServiceModuleProvider serviceModuleProvider;

    private ServiceModuleLocator() {}

    public static void setModuleProvider(ServiceModuleProvider aServiceModuleProvider) {
        serviceModuleProvider = aServiceModuleProvider;
    }

    public static ServiceModuleProvider getModuleProvider() {
        return serviceModuleProvider;
    }

 }
