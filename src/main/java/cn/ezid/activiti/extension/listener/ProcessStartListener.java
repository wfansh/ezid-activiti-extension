package cn.ezid.activiti.extension.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProcessStartListener implements ExecutionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2915736495579036176L;

	private static final Logger log = LoggerFactory.getLogger(ProcessEndListener.class); 
		
	@Override
	public void notify(DelegateExecution execution) throws Exception {
		log.info("invoke process start listener");
	}

}
