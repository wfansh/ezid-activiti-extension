/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.rest.service.api.history;

import java.util.Date;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.activiti.rest.service.application.ApplicationContextHolder;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import cn.ezid.activiti.extension.callback.CallbackService;

/**
 * @author Tijs Rademakers
 */
public class HistoricTaskInstanceResource extends SecuredResource {

	@Get
	public HistoricTaskInstanceResponse getTaskInstance() {
		if (!authenticate()) {
			return null;
		}
		return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
				.createHistoricTaskInstanceResponse(this, getHistoricTaskInstanceFromRequest());
	}

	@Put
	public HistoricTaskInstanceResponse updateTask(HistoricTaskRequest taskRequest) {
		if (!authenticate()) {
			return null;
		}

		if (taskRequest == null) {
			throw new ResourceException(new Status(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(),
					"A request body was expected when updating the task.", null, null));
		}

		String taskId = getAttribute("taskId");
		if (taskId == null) {
			throw new ActivitiIllegalArgumentException("The taskId cannot be null");
		}

		HistoricTaskInstanceEntity taskInstanceEntity = (HistoricTaskInstanceEntity) ActivitiUtil.getHistoryService()
				.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		
		if (taskInstanceEntity == null) {
			throw new ActivitiObjectNotFoundException("Could not find a task instance with id '" + taskId + "'.",
					HistoricTaskInstance.class);
		}
		
		taskInstanceEntity.setDescription(taskRequest.getDescription());

		ProcessEngineConfigurationImpl configurationImpl = ApplicationContextHolder.getProcessEngineConfiguration();
		DbSqlSession dbSession = (DbSqlSession) configurationImpl.getDbSqlSessionFactory().openSession();
		dbSession.update("updateHistoricTaskInstance", taskInstanceEntity);
		dbSession.commit();
		dbSession.close();

		return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
				.createHistoricTaskInstanceResponse(this, taskInstanceEntity);
	}
	
	@Delete
	public void deleteTaskInstance() {
		if (!authenticate()) {
			return;
		}

		String taskId = getAttribute("taskId");
		if (taskId == null) {
			throw new ActivitiIllegalArgumentException("The taskId cannot be null");
		}

		ActivitiUtil.getHistoryService().deleteHistoricTaskInstance(taskId);
	}

	@Post
	public void executeTaskAction(HistoricTaskActionRequest actionRequest) {
		if (!authenticate()) {
			return;
		}

		if (actionRequest == null) {
			throw new ResourceException(new Status(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(),
					"A request body was expected when executing a task action.", null, null));
		}

		HistoricTaskInstanceEntity taskInstanceEntity = (HistoricTaskInstanceEntity) getHistoricTaskInstanceFromRequest();
		if (HistoricTaskActionRequest.ACTION_COMPLETE.equals(actionRequest.getAction())) {
			ProcessEngineConfigurationImpl configurationImpl = ApplicationContextHolder.getProcessEngineConfiguration();
			DbSqlSession dbSession = (DbSqlSession) configurationImpl.getDbSqlSessionFactory().openSession();
			taskInstanceEntity.setEndTime(new Date());
			dbSession.update("updateHistoricTaskInstance", taskInstanceEntity);
			dbSession.commit();
			dbSession.close();
			
			/**
			 *  Invoke callback when completing historic task instance
			 */
			HistoricVariableInstance callback = ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(taskInstanceEntity.getProcessInstanceId()).variableName(CallbackService.IN_VAR_CERT_CALLBACK).singleResult();
			if (callback != null) {
				ApplicationContextHolder.getApplicationContext().getBean(CallbackService.class)
					.callback(callback.getValue().toString(), taskInstanceEntity.getProcessInstanceId());
			}
		} else {
			throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
		}
	}
	
	protected HistoricTaskInstance getHistoricTaskInstanceFromRequest() {
		String taskId = getAttribute("taskId");
		if (taskId == null) {
			throw new ActivitiIllegalArgumentException("The taskId cannot be null");
		}

		HistoricTaskInstance taskInstance = ActivitiUtil.getHistoryService().createHistoricTaskInstanceQuery()
				.taskId(taskId).singleResult();
		if (taskInstance == null) {
			throw new ActivitiObjectNotFoundException("Could not find a task instance with id '" + taskId + "'.",
					HistoricTaskInstance.class);
		}
		return taskInstance;
	}
}
