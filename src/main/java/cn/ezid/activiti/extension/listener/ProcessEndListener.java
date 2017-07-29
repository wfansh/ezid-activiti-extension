package cn.ezid.activiti.extension.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.rest.service.application.ApplicationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ezid.activiti.extension.callback.CallbackService;

public class ProcessEndListener implements ExecutionListener {
	private static final long serialVersionUID = 8109672351806260461L;

	private static final Logger log = LoggerFactory.getLogger(ProcessEndListener.class); 
	
	@Override
	public void notify(DelegateExecution execution) throws Exception {
		log.info("Invoke process end listener");
		
		Object callback = execution.getVariable(CallbackService.IN_VAR_CERT_CALLBACK);
		log.info("Call back URL {} for process instance {}.", callback, execution.getProcessInstanceId());
		
		if (callback == null || !String.class.isInstance(callback)) {
			return;
		}
		
		String processInstatnceId = execution.getProcessInstanceId(); 

		ApplicationContextHolder.getApplicationContext().getBean(CallbackService.class)
				.callback(callback.toString(), processInstatnceId);
	}
}
