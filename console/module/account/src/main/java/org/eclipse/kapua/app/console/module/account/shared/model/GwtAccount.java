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
package org.eclipse.kapua.app.console.module.account.shared.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.eclipse.kapua.app.console.module.api.shared.model.GwtUpdatableEntityModel;

public class GwtAccount extends GwtUpdatableEntityModel implements Serializable {

    private static final long serialVersionUID = -5999185569672904770L;

    //
    // Defines value for service plan
    public static final int ADMIN_ACCOUNT_ID = 1;
    public static final int SERVICE_PLAN_UNLIMITED = -1;
    public static final int SERVICE_PLAN_DISABLED = 0;

    public enum GwtAccountStatus implements IsSerializable {
        BEING_PROVISIONED, ENABLED, DISABLED, BEING_DELETED;

        GwtAccountStatus() {
        }
    }

    public enum GwtAccountProvisioningStatus implements IsSerializable {
        WAITING_TO_START, IN_PROGRESS, FAILED, COMPLETED;

        GwtAccountProvisioningStatus() {
        }
    }

    public enum GwtAccountDataIndexBy implements IsSerializable {
        SERVER_TIMESTAMP, DEVICE_TIMESTAMP;

        GwtAccountDataIndexBy() {
        }
    }

    public enum GwtAccountMetricsIndexBy implements IsSerializable {
        TIMESTAMP, VALUE, NONE;

        GwtAccountMetricsIndexBy() {
        }
    }

    private String brokerUrl;
    private GwtOrganization gwtOrganization;

    public GwtAccount() {
        super();
    }

    public String getName() {
        return (String) get("name");
    }

    public String getUnescapedName() {
        return (String) getUnescaped("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getPassword() {
        return (String) get("password");
    }

    public void setPassword(String password) {
        set("password", password);
    }

    public GwtOrganization getGwtOrganization() {
        return gwtOrganization;
    }

    public void setGwtOrganization(GwtOrganization gwtOrganization) {
        this.gwtOrganization = gwtOrganization;
    }

    public void setParentAccountId(String parentAccountId) {
        set("parentAccountId", parentAccountId);
    }

    public String getParentAccountId() {
        return (String) get("parentAccountId");
    }

}
