package org.wf.service.utils;


import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * start to create database table schemas according to db.properties configuration.
 * @author zhurunjia
 *
 */
public class CreateDbTables {

	public static void main(String[] args) {
		ApplicationContext ac = null;
		try {
			ac = new ClassPathXmlApplicationContext("classpath:spring/config/applicationContext.xml");
			System.out.println("context created.");
			
			ProcessEngineConfiguration pec = ac.getBean("processEngineConfiguration", ProcessEngineConfiguration.class);
			pec.setDatabaseSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE).buildProcessEngine();
		
			pec.buildProcessEngine();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			// created
			System.out.println("schema created.");
		
			if(ac != null) 
				((ConfigurableBeanFactory)ac.getAutowireCapableBeanFactory()).destroySingletons();
		}
		
	}

}
