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
package org.eclipse.kapua.app.console.module.job.client.schedule;

import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.TimeField;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.eclipse.kapua.app.console.module.api.client.messages.ValidationMessages;
import org.eclipse.kapua.app.console.module.api.client.ui.dialog.entity.EntityAddEditDialog;
import org.eclipse.kapua.app.console.module.api.client.ui.panel.FormPanel;
import org.eclipse.kapua.app.console.module.api.client.util.ConsoleInfo;
import org.eclipse.kapua.app.console.module.api.client.util.DialogUtils;
import org.eclipse.kapua.app.console.module.api.shared.model.GwtSession;
import org.eclipse.kapua.app.console.module.job.client.messages.ConsoleJobMessages;
import org.eclipse.kapua.app.console.module.job.shared.model.job.GwtTrigger;
import org.eclipse.kapua.app.console.module.job.shared.model.job.GwtTriggerCreator;
import org.eclipse.kapua.app.console.module.job.shared.model.job.GwtTriggerProperty;
import org.eclipse.kapua.app.console.module.job.shared.service.GwtTriggerService;
import org.eclipse.kapua.app.console.module.job.shared.service.GwtTriggerServiceAsync;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JobScheduleAddDialog extends EntityAddEditDialog {

    protected static final ConsoleJobMessages JOB_MSGS = GWT.create(ConsoleJobMessages.class);
    private static final ValidationMessages VAL_MSGS = GWT.create(ValidationMessages.class);
    private static final GwtTriggerServiceAsync TRIGGER_SERVICE = GWT.create(GwtTriggerService.class);
    private static final String KAPUA_ID_CLASS_NAME = "org.eclipse.kapua.model.id.KapuaId";

    private final String jobId;
    protected final TextField<String> triggerName;
    protected final DateField startsOn;
    protected final TimeField startsOnTime;
    protected final DateField endsOn;
    protected final TimeField endsOnTime;
    protected final NumberField retryInterval;
    protected final TextField<String> cronExpression;

    public JobScheduleAddDialog(GwtSession currentSession, String jobId) {
        super(currentSession);
        this.jobId = jobId;

        triggerName = new TextField<String>();
        startsOn = new DateField();
        startsOnTime = new TimeField();
        endsOn = new DateField();
        endsOnTime = new TimeField();
        retryInterval = new NumberField();
        cronExpression = new TextField<String>();

        DialogUtils.resizeDialog(this, 600, 500);
    }

    @Override
    public void createBody() {
        FormPanel mainPanel = new FormPanel(150);

        triggerName.setAllowBlank(false);
        triggerName.setFieldLabel(JOB_MSGS.dialogAddScheduleScheduleNameLabel());
        mainPanel.add(triggerName);

        startsOn.setFieldLabel(JOB_MSGS.dialogAddScheduleStartsOnLabel());
        startsOn.setFormatValue(true);
        startsOn.setAllowBlank(false);
        startsOn.getPropertyEditor().setFormat(DateTimeFormat.getFormat("dd/MM/yyyy"));
        mainPanel.add(startsOn);

        startsOnTime.setAllowBlank(false);
        startsOnTime.setFieldLabel(JOB_MSGS.dialogAddScheduleStartsOnTimeLabel());
        startsOnTime.setFormat(DateTimeFormat.getFormat("HH:mm"));
        mainPanel.add(startsOnTime);

        endsOn.setFieldLabel(JOB_MSGS.dialogAddScheduleEndsOnLabel());
        endsOn.setFormatValue(true);
        endsOn.getPropertyEditor().setFormat(DateTimeFormat.getFormat("dd/MM/yyyy"));
        mainPanel.add(endsOn);

        endsOnTime.setFieldLabel(JOB_MSGS.dialogAddScheduleEndsOnTimeLabel());
        endsOnTime.setFormat(DateTimeFormat.getFormat("HH:mm"));
        mainPanel.add(endsOnTime);

        retryInterval.setFieldLabel(JOB_MSGS.dialogAddScheduleRetryIntervalLabel());
        retryInterval.setAllowDecimals(false);
        retryInterval.setAllowNegative(false);
        mainPanel.add(retryInterval);

        cronExpression.setFieldLabel(JOB_MSGS.dialogAddScheduleCronScheduleLabel());
        mainPanel.add(cronExpression);

        bodyPanel.add(mainPanel);
    }

    @Override
    protected void preSubmit() {
        if (triggerName.getValue() == null) {
            triggerName.markInvalid(VAL_MSGS.nameRequiredMsg());
            return;
        }

        if (endsOn.getValue() == null && endsOnTime.getValue() != null) {
            endsOn.markInvalid(VAL_MSGS.endTimeWithoutEndDate());
        }

        if (endsOn.getValue() != null) {
            if (endsOnTime.getValue() == null) {
                endsOnTime.markInvalid(VAL_MSGS.endDateWithoutEndTime());
                return;
            } else {
                if (endsOn.getValue().before(startsOn.getValue())) {
                    endsOn.markInvalid(VAL_MSGS.endsOnDateEarlierThanStartsOn());
                    return;
                } else if (endsOn.getValue().equals(startsOn.getValue()) && endsOnTime.getValue().getDate().before(startsOnTime.getValue().getDate())) {
                    endsOnTime.markInvalid(VAL_MSGS.endsOnTimeEarlierThanStartsOn());
                    return;
                }
            }
        }

        if (cronExpression.getValue() == null && retryInterval.getValue() == null) {
            cronExpression.markInvalid(VAL_MSGS.retryIntervalOrCronRequired());
            retryInterval.markInvalid(VAL_MSGS.retryIntervalOrCronRequired());
            return;
        } else if (retryInterval.getValue() != null) {
            super.preSubmit();
        } else if (cronExpression.getValue() != null) {
            TRIGGER_SERVICE.validateCronExpression(cronExpression.getValue(), new AsyncCallback<Boolean>() {

                @Override
                public void onFailure(Throwable caught) {
                    ConsoleInfo.display(MSGS.popupError(), JOB_MSGS.unableToValidateCronExpression(caught.getLocalizedMessage()));
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        JobScheduleAddDialog.super.preSubmit();
                    } else {
                        cronExpression.markInvalid(VAL_MSGS.invalidCronExpression());
                    }
                }
            });
        } else {
            cronExpression.markInvalid(VAL_MSGS.cronExpressionRequired());
        }
    }

    @Override
    public void submit() {
        GwtTriggerCreator gwtTriggerCreator = new GwtTriggerCreator();

        gwtTriggerCreator.setScopeId(currentSession.getSelectedAccountId());

        gwtTriggerCreator.setTriggerName(triggerName.getValue());
        Date startsOnDate = startsOn.getValue();
        startsOnDate.setTime(startsOnDate.getTime() + (3600 * 1000 * startsOnTime.getValue().getHour()) + 60 * 1000 * startsOnTime.getValue().getMinutes());
        gwtTriggerCreator.setStartsOn(startsOnDate);
        Date endsOnDate = endsOn.getValue();
        endsOnDate.setTime(endsOnDate.getTime() + (3600 * 1000 * endsOnTime.getValue().getHour()) + 60 * 1000 * endsOnTime.getValue().getMinutes());
        gwtTriggerCreator.setEndsOn(endsOnDate);
        gwtTriggerCreator.setRetryInterval(retryInterval.getValue() != null ? retryInterval.getValue().longValue() : null);
        gwtTriggerCreator.setCronScheduling(cronExpression.getValue());
        List<GwtTriggerProperty> gwtTriggerPropertyList = new ArrayList<GwtTriggerProperty>();
        gwtTriggerPropertyList.add(new GwtTriggerProperty("scopeId", KAPUA_ID_CLASS_NAME, currentSession.getSelectedAccountId()));
        gwtTriggerPropertyList.add(new GwtTriggerProperty("jobId", KAPUA_ID_CLASS_NAME, jobId));
        gwtTriggerCreator.setTriggerProperties(gwtTriggerPropertyList);

        TRIGGER_SERVICE.create(xsrfToken, gwtTriggerCreator, new AsyncCallback<GwtTrigger>() {

            @Override
            public void onSuccess(GwtTrigger arg0) {
                exitStatus = true;
                exitMessage = JOB_MSGS.dialogAddScheduleConfirmation();
                hide();
            }

            @Override
            public void onFailure(Throwable cause) {
                unmask();

                submitButton.enable();
                cancelButton.enable();
                status.hide();

                exitStatus = false;
                exitMessage = JOB_MSGS.dialogAddScheduleError(cause.getLocalizedMessage());

                hide();
            }
        });
    }

    @Override
    public String getHeaderMessage() {
        return JOB_MSGS.dialogAddScheduleHeader();
    }

    @Override
    public String getInfoMessage() {
        return JOB_MSGS.dialogAddScheduleInfo();
    }
}
