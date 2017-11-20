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

import org.eclipse.kapua.commons.event.service.api.ServiceEvent;
import org.eclipse.kapua.commons.event.service.api.ServiceEventListResult;
import org.eclipse.kapua.commons.model.query.KapuaListResultImpl;

/**
 * KapuaEvent list result implementation.
 *
 * @since 1.0
 */
public class ServiceEventListResultImpl extends KapuaListResultImpl<ServiceEvent> implements ServiceEventListResult {

    private static final long serialVersionUID = -5118004898345748297L;
}