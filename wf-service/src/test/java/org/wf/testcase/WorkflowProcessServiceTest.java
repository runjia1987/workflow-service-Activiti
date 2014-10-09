package org.wf.testcase;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.wf.service.activiti.api.UserAccountService;
import org.wf.service.activiti.api.WorkflowProcessService;
import org.wf.service.activiti.api.WorkflowTaskService;
import org.wf.service.model.ProcessStatusEntity;
import org.wf.service.model.ProcessStatusEnum;
import org.wf.service.model.UserAccount;
import org.wf.service.model.WorkflowProcess;


@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = false, transactionManager = "transactionManager")
@Transactional
@ContextConfiguration({"classpath:spring/config/applicationContext-all-wf.xml"})
public class WorkflowProcessServiceTest {
	
	@Resource (name = "workflowProcessService")
	private WorkflowProcessService workflowProcessService;
	
	@Resource ( name = "userAccountService")
	private UserAccountService userAccountService;
	
	@Resource ( name = "workflowTaskService")
	private WorkflowTaskService workflowTaskService;
	
	private final String innerProcessId = "123abc";
	
	@Test
	public void testProcessNew(){
		
		WorkflowProcess process = new WorkflowProcess();
		// set fields
		process.setInnerProcessId(innerProcessId);
		process.setAcceptTimeStamp(new Timestamp(new Date().getTime()));
		process.setInitUserName("test_init_user");
		process.setOuterSequenceId("HDSNDA78312GJ");
		process.setProcessDetail("some desc here, a new incoming process.");
		process.setProcessModelKey("request_leave");
		process.setSourceSystem("myApp");
		process.setStatus(ProcessStatusEnum.NEW.name());
		
		String processId = workflowProcessService.processNew(process);
		System.out.println("processId: " + processId);
		
		ProcessStatusEntity pse = workflowProcessService.getProcessStatus(processId);
		System.out.println("1st process status after STARTED: " + pse.toString());
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		//paramMap.put("approved", true);
		paramMap.put("deptLeaderPass", true);
		workflowTaskService.completeTask(pse.getCurrentTaskId(), paramMap, "Jack123");
		
		pse = workflowProcessService.getProcessStatus(processId);
		System.out.println("2nd process status: " + pse.toString());
		
		paramMap.clear();
		paramMap.put("hrPass", false);
		final String anotherUser = "I_am_a_test_user";
		workflowTaskService.assign(pse.getCurrentTaskId(), paramMap, anotherUser);
		workflowTaskService.completeTask(pse.getCurrentTaskId(), paramMap, "I_am_a_test_user");
		
		pse = workflowProcessService.getProcessStatus(processId);
		System.out.println("3rd process status: " + pse.toString());
		paramMap.clear();
		paramMap.put("modifyApplyContentProcessor", true);
		
		/**
		workflowProcessService.suspendProcess(processId);
		System.out.println("process " + processId + " is suspended.");
		
		workflowProcessService.recoverProcess(processId);
		System.out.println("process " + processId + " is recovered.");
		
		workflowProcessService.abortProcess(processId, "I want to stop this process in workflow.");
		System.out.println("process " + processId + " is aborted.");
		*/
	}
	
	@Test
	public void testSyncUser(){
		UserAccount account = new UserAccount();
		account.setUserId("runjia.zhu1");
		account.setUserName("runjia.zhu1s");
		account.setEmail("runjia.zhu@xxx.com");
		account.setRoleName("new_group");
		
		userAccountService.saveUser(account);
		
		System.out.println("saveUser success.");
	}

}
