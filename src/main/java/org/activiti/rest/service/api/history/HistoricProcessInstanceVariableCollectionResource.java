package org.activiti.rest.service.api.history;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.resource.Get;

public class HistoricProcessInstanceVariableCollectionResource extends SecuredResource {
	
	@Get
	public List<RestVariable> getVariables() {
		if (!authenticate()) {
			return null;
		}
		
		String processInstanceId = getAttribute("processInstanceId");
		if (processInstanceId == null) {
			throw new ActivitiIllegalArgumentException("The processInstanceId cannot be null");
		}
		
		HistoricProcessInstance processObject = (HistoricProcessInstance) ActivitiUtil.getHistoryService()
				.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).includeProcessVariables()
				.singleResult();

		if (processObject == null) {
			throw new ActivitiObjectNotFoundException("Historic process instance '" + processInstanceId
					+ "' couldn't be found.", HistoricProcessInstanceEntity.class);
		}

		Map<String, Object> variableMap = processObject.getProcessVariables();
		List<RestVariable> result = ((ActivitiRestServicesApplication) getApplication(ActivitiRestServicesApplication.class))
				.getRestResponseFactory().createRestVariables(this, variableMap, processInstanceId, RestResponseFactory.VARIABLE_HISTORY_PROCESS,
						null);

		return result;
	}
}
