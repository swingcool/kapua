/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.broker.core.setting;

import org.eclipse.kapua.broker.core.plugin.ConnectorDescriptor;
import org.eclipse.kapua.commons.setting.SettingKey;

/**
 * Broker settings
 */
public enum BrokerSettingKey implements SettingKey {
    /**
     * Allow disabling the default connector descriptor
     */
    DISABLE_DEFAULT_CONNECTOR_DESCRIPTOR("broker.connector.descriptor.default.disable"),
    /**
     * A URI to a configuration file for providing additional {@link ConnectorDescriptor} configurations
     */
    CONFIGURATION_URI("broker.connector.descriptor.configuration.uri"),
    /**
     * Broker IP resolver implementation (if not evaluated, the default resolver will be used).
     */

    BROKER_IP_RESOLVER_CLASS_NAME("broker.ip_resolver_class_name"),
    /**
     * Broker IP used by the default resolver.
     */
    BROKER_IP("broker.ip"),
	 /**
     * Broker name (used also for the vm connector name)
     */
    BROKER_NAME("broker.name");
    private String key;

    private BrokerSettingKey(String key) {
        this.key = key;
    }

    @Override
    public String key() {
        return key;
    }
}
