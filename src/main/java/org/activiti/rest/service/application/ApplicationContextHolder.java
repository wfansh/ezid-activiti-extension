package org.activiti.rest.service.application;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextHolder implements ApplicationContextAware {

	private static ApplicationContext applicationContext;
	
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	public static ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
		return applicationContext.getBean(ProcessEngineConfigurationImpl.class);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		ApplicationContextHolder.applicationContext = applicationContext;
	}
}
