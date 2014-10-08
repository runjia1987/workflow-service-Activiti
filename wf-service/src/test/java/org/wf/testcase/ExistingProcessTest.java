package org.wf.testcase;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.wf.service.activiti.api.WorkflowProcessService;
import org.wf.service.model.ProcessStatusEntity;

@ContextConfiguration(locations = {"classpath:spring/config/applicationContext-all-wf.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class ExistingProcessTest {
	
	@Resource (name = "workflowProcessService")
	private WorkflowProcessService workflowProcessService;
	
	@Test
	public void testExisting(){
		ProcessStatusEntity pse = workflowProcessService.getProcessStatus("1502");
		System.out.println("get existing process status: " + pse.toString());
	}

}
