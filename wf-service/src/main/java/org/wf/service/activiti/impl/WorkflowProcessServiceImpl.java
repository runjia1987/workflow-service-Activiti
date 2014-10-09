package org.wf.service.activiti.impl;

import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wf.dao.activiti.WorkflowProcessDao;
import org.wf.service.activiti.api.UserAccountService;
import org.wf.service.activiti.api.WorkflowProcessService;
import org.wf.service.extension.DiagramPictureDrawer;
import org.wf.service.model.ProcessStatusEntity;
import org.wf.service.model.ProcessStatusEnum;
import org.wf.service.model.UserAccount;
import org.wf.service.model.WorkflowProcess;

@Transactional
@Service("workflowProcessService")
public class WorkflowProcessServiceImpl implements WorkflowProcessService {

	private static final Logger logger = LoggerFactory
			.getLogger(WorkflowProcessServiceImpl.class);

	@Resource(name = "workflowProcessDao")
	private WorkflowProcessDao workflowProcessDao;

	@Resource(name = "runtimeService")
	private RuntimeService runtimeService;
	
	@Resource( name = "userAccountService")
	private UserAccountService userAccountService;
	
	@Resource( name = "historyService")
	private HistoryService historyService;
	
	@Resource( name = "taskService")
	private TaskService taskService;
	
	@Resource( name = "diagramPictureDrawer")
	private DiagramPictureDrawer diagramPictureDrawer;

	@Override
	public String processNew(WorkflowProcess process) {
		logger.info("processNew receives[" + process.toString() + "].");

		if ( ! process.validate()) {
			logger.error("workflowProcess [" + process + "] validation fails.");

		} else {
			ProcessInstance pi = runtimeService.startProcessInstanceByKey(process.getProcessModelKey());

			String processInstanceId = pi.getProcessInstanceId();

			// insert into custom table, wf_process
			process.setInnerProcessId(processInstanceId);
			process.setStatus(ProcessStatusEnum.STARTED.name());
			workflowProcessDao.saveProcess(process);
			
			// save the specfied user info
			UserAccount account = null;
			if (process.getInitUserAccount() != null) {
				account = process.getInitUserAccount();
			} else if ( StringUtils.isNotEmpty(process.getInitUserName())) {
				account = new UserAccount();
				account.setUserName(process.getInitUserName());
			}
			userAccountService.saveUser(account);
			
			logger.info("processNew return processInstanceId[" + processInstanceId + "]");
			return processInstanceId;
			
		}
		return null;
	}

	@Override
	public void notifyWhenCompleted(String innerProcessId) {

		WorkflowProcess process = null;
		// query the process from DB dao
		process = workflowProcessDao.queryByProcessId(innerProcessId);
		
		if (process != null) {
			// populate the userAccount field
			process.setInitUserAccount(userAccountService.queryByUserName(process.getInitUserName()));
			// UPDATE status to completed.
			process.setStatus(ProcessStatusEnum.COMPLETED.name());
			workflowProcessDao.updateProcess(process);
			logger.info("set process " + innerProcessId + " to status COMPLETED.");
			UserAccount userAccount = process.getInitUserAccount();
			if (userAccount != null ){
				// notify this init user by email or else..
				// TODO	
			}
			
			// send message to outside
			
		} else {
			logger.warn("Could not find any process for innerProcessId: "
					+ innerProcessId);
		}
	}

	@Override
	public void suspendProcess(String innerProcessId) {
		// first query DB
		WorkflowProcess process = workflowProcessDao.queryByProcessId(innerProcessId);
		process.setStatus(ProcessStatusEnum.SUSPENDED.name());
		workflowProcessDao.updateProcess(process);
		
		// later
		runtimeService.suspendProcessInstanceById(innerProcessId);
	}

	@Override
	public ProcessStatusEntity getProcessStatus(String innerProcessId) {
		WorkflowProcess process = workflowProcessDao.queryByProcessId(innerProcessId);
		
		ProcessStatusEntity pse = new ProcessStatusEntity();
		pse.setStatus(process.getStatus());
		pse.setInnerProcessId(innerProcessId);
		
		Task task = taskService.createTaskQuery().processInstanceId(
						innerProcessId).singleResult();
		pse.setCurrentTaskId(task.getId());
		
		if ( ProcessStatusEnum.STARTED.name().equals(pse.getStatus()) 
				// || ProcessStatusEnum.COMPLETED.name().equals(pse.getStatus())
				|| ProcessStatusEnum.SUSPENDED.name().equals(pse.getStatus()) ) {
			String diagrampath = diagramPictureDrawer.drawDiagram(innerProcessId);
			pse.setDiagramPicturePath(diagrampath);
		}
		return pse;
	}

	@Override
	public void recoverProcess(String innerProcessId) {
		// first re-activate the process
		runtimeService.activateProcessInstanceById(innerProcessId);
		
		// later update to DB
		WorkflowProcess process = workflowProcessDao.queryByProcessId(innerProcessId);
		process.setStatus(ProcessStatusEnum.STARTED.name());
		workflowProcessDao.updateProcess(process);
	}

	@Override
	public void abortProcess(String innerProcessId, String abortReason) {
		// later update to DB
		WorkflowProcess process = workflowProcessDao.queryByProcessId(innerProcessId);
		process.setStatus(ProcessStatusEnum.ABOTRED.name());
		workflowProcessDao.updateProcess(process);
		
		// saved to act_hi_procinst table.
		runtimeService.deleteProcessInstance(innerProcessId, abortReason);
		
		// delete all related history
		historyService.deleteHistoricProcessInstance(innerProcessId);
	}

}
