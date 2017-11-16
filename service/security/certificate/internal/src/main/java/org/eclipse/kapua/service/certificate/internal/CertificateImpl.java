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
package org.eclipse.kapua.service.certificate.internal;

import org.eclipse.kapua.commons.model.AbstractKapuaUpdatableEntity;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.certificate.Certificate;
import org.eclipse.kapua.service.certificate.CertificateStatus;

import java.util.Date;

public class CertificateImpl extends AbstractKapuaUpdatableEntity implements Certificate {

    private String certificate;
    private String family;
    private String privateKey;

    public CertificateImpl() {
    }

    public CertificateImpl(KapuaId scopeId) {
        super(scopeId);
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCertificate() {
        return certificate;
    }

    @Override
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    @Override
    public String getFamily() {
        return family;
    }

    @Override
    public void setFamily(String family) {
        this.family = family;
    }

    @Override
    public String getPrivateKey() {
        return privateKey;
    }

    @Override
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public Integer getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setVersion(Integer version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSerial() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSerial(String serial) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAlgorithm() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAlgorithm(String algorithm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSubject(String subject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getIssuer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIssuer(String issuer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getNotBefore() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNotBefore(Date notBefore) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getNotAfter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNotAfter(Date notAfter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CertificateStatus getStatus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatus(CertificateStatus status) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getDigest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDigest(byte[] digest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean getDefault() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDefault(Boolean isDefault) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean getCa() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCa(Boolean isCa) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KapuaId getCaId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCaId(KapuaId caId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPassword() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPassword(String password) {
        throw new UnsupportedOperationException();
    }
}
