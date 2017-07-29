package org.activiti.rest.service.api.history;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.resource.Get;

public class HistoricTaskInstanceVariableCollectionResource extends SecuredResource {
	  @Get
	  public List<RestVariable> getVariables() {
			if (!authenticate()) {
				return null;
			}
			
			String taskId = getAttribute("taskId");
			if (taskId == null) {
				throw new ActivitiIllegalArgumentException("The taskId cannot be null");
			}
			
			HistoricTaskInstance taskObject = (HistoricTaskInstance) ActivitiUtil.getHistoryService().createHistoricTaskInstanceQuery().taskId(taskId).includeProcessVariables().singleResult();
			
			if (taskObject == null) {
				throw new ActivitiObjectNotFoundException("Historic task instance '" + taskId
						+ "' couldn't be found.", HistoricTaskInstance.class);
			}

			Map<String, Object> variableMap = taskObject.getProcessVariables();
			List<RestVariable> result = ((ActivitiRestServicesApplication) getApplication(ActivitiRestServicesApplication.class))
					.getRestResponseFactory().createRestVariables(this, variableMap, taskId, RestResponseFactory.VARIABLE_HISTORY_TASK,
							null);

			return result;
		}
}