package org.jackJew.wf.service.testcase;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@Transactional
@ContextConfiguration({"classpath:spring/config/applicationContext.xml"})
public class StartProcessTest001 {
	
	@Resource( name = "runtimeService")
	private RuntimeService runtimeService;
	
	@Resource( name = "repositoryService")
	private RepositoryService repositoryService;
	
	private String resourceName
				= "D:\\workspace\\wf-service\\src\\main\\resources\\deployments\\Test001.bpmn";
	
	@Test
	public void testStart() throws Exception{
		repositoryService.createDeployment().name("Test001").addInputStream(
					resourceName, new FileInputStream(resourceName)).deploy();
		
		Map<String, Object> variables = new HashMap<String, Object>();
		ProcessInstance pi = 
				runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
		
		System.out.println("processInstanceId: " + pi.getProcessInstanceId());
	}

}
