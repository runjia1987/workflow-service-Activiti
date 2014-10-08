package org.activiti.designer.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
@ContextConfiguration(value = {"classpath:spring/config/applicationContext-all-wf.xml"})
public class StartProcess001Test {

	private String filename = "/Users/zhurunjia/Desktop/workspace/wf-service/target/classes/deployments/request_leave.bpmn";

	@Resource(name = "runtimeService")
	private RuntimeService runtimeService;
	
	@Resource(name = "repositoryService")
	private RepositoryService repositoryService;

	@Test
	public void startProcess() throws Exception {
		Thread.sleep(3000);
		
		//repositoryService.createDeployment().addInputStream(filename, new FileInputStream(filename)).deploy();
		
		Map<String, Object> variableMap = new HashMap<String, Object>();
		String[] array = {"Jack1", "Jack2"};
		List<?> list = Arrays.asList(array);	
		variableMap.put("bpm_assignees", list);
		
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("activitiParallelReview", variableMap);
		
		System.out.println("processInstanceId: " + processInstance.getId() + ", processDefinitionId: "
					+ processInstance.getProcessDefinitionId());
	}
}