/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.authentication.jpa;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.kapua.commons.core.ComponentProvider;
import org.eclipse.kapua.commons.jpa.AbstractEntityManagerFactory;
import org.eclipse.kapua.locator.inject.Service;

/**
 * Entity manager factory for the authentication module.
 *
 * @since 1.0
 *
 */
@ComponentProvider
@Service(provides=AuthenticationEntityManagerFactory.class)
public class AuthenticationEntityManagerFactoryImpl extends AbstractEntityManagerFactory implements AuthenticationEntityManagerFactory {

    private static final String PERSISTENCE_UNIT_NAME = "kapua-authentication";
    private static final String DATASOURCE_NAME = "kapua-dbpool";
    private static final Map<String, String> UNIQUE_CONSTRAINTS = new HashMap<>();

//    private static AuthenticationEntityManagerFactory instance = new AuthenticationEntityManagerFactory();

    /**
     * Constructs a new entity manager factory and configure it to use the authentication persistence unit.
     */
    public AuthenticationEntityManagerFactoryImpl() {
        super(PERSISTENCE_UNIT_NAME,
                DATASOURCE_NAME,
                UNIQUE_CONSTRAINTS);
    }

    /**
     * Return the {@link EntityManager} singleton instance
     * 
     * @return
     * @throws KapuaException
     */
//    public static EntityManager getEntityManager()
//            throws KapuaException {
//        return instance.createEntityManager();
//    }

    /**
     * Return the {@link EntityManager} singleton instance
     * 
     * @return
     */
//    public static AuthenticationEntityManagerFactory getInstance() {
//        return instance;
//    }

}
