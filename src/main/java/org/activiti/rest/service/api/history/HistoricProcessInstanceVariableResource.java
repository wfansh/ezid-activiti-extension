package org.activiti.rest.service.api.history;

import java.io.IOException;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.activiti.rest.service.application.ApplicationContextHolder;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

public class HistoricProcessInstanceVariableResource extends SecuredResource {
	
	@Get
	public RestVariable getVariable() {
		if (authenticate() == false)
			return null;

		String processInstanceId = getAttribute("processInstanceId");
		if (processInstanceId == null) {
			throw new ActivitiIllegalArgumentException("The processInstanceId cannot be null");
		}

		String variableName = getAttribute("variableName");
		if (variableName == null) {
			throw new ActivitiIllegalArgumentException("The variableName cannot be null");
		}

		HistoricProcessInstance processObject = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
				.processInstanceId(processInstanceId).includeProcessVariables().singleResult();

		if (processObject == null) {
			throw new ActivitiObjectNotFoundException("Historic process instance '" + processInstanceId
					+ "' couldn't be found.", HistoricProcessInstanceEntity.class);
		}

		Object value = processObject.getProcessVariables().get(variableName);

		if (value == null) {
			throw new ActivitiObjectNotFoundException("Historic process instance '" + processInstanceId
					+ "' variable value for " + variableName + " couldn't be found.", VariableInstanceEntity.class);
		} else {
			return ((ActivitiRestServicesApplication) getApplication(ActivitiRestServicesApplication.class))
					.getRestResponseFactory().createRestVariable(this, variableName, value, null, processInstanceId,
							RestResponseFactory.VARIABLE_HISTORY_PROCESS, false);
		}
	}

	@Put
	public RestVariable updateVariable(Representation representation) {
		if (authenticate() == false)
			return null;

		String processInstanceId = getAttribute("processInstanceId");
		if (processInstanceId == null) {
			throw new ActivitiIllegalArgumentException("The processInstanceId cannot be null");
		}

		String variableName = getAttribute("variableName");
		if (variableName == null) {
			throw new ActivitiIllegalArgumentException("The variableName cannot be null");
		}

		HistoricVariableInstanceEntity hisVariableEntity = (HistoricVariableInstanceEntity)ActivitiUtil.getHistoryService()
				.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).variableName(variableName)
				.singleResult();

		if (hisVariableEntity == null) {
			throw new ActivitiObjectNotFoundException("Historic variable instance '" + processInstanceId + " "
					+ variableName + "' couldn't be found.", HistoricVariableInstance.class);
		}

		try {
			RestVariable restVariable = getConverterService().toObject(representation, RestVariable.class, this);
			
			if (restVariable == null) {
				throw new ResourceException(new Status(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(),
						"Invalid body was supplied", null, null));
			}
			if (!restVariable.getName().equals(variableName)) {
				throw new ActivitiIllegalArgumentException(
						"Variable name in the body should be equal to the name used in the requested URL.");
			}

			ProcessEngineConfigurationImpl configurationImpl = ApplicationContextHolder.getProcessEngineConfiguration();
		    
			Object value = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
					.getVariableValue(restVariable);
			VariableType type = configurationImpl.getVariableTypes().findVariableType(value);
			
			HistoricUtils.copyVariableValues(hisVariableEntity, VariableInstanceEntity.create(variableName, type, value), configurationImpl.getClock().getCurrentTime());

			DbSqlSession dbSession = (DbSqlSession)configurationImpl.getDbSqlSessionFactory().openSession();
			dbSession.update("updateHistoricVariableInstance", hisVariableEntity);
			dbSession.commit();
			dbSession.close();
			
			return restVariable;
		} catch (IOException ioe) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ioe);
		}
	}
	
}
