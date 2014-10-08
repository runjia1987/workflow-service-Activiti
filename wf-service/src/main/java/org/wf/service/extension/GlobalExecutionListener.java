package org.wf.service.extension;

import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.wf.service.activiti.api.WorkflowProcessService;

/**
 * activity execution listener, events [ start, take, end ]
 * @author zhurunjia
 *
 */
@Component("globalExecutionListener")
public class GlobalExecutionListener implements ExecutionListener, InitializingBean {

	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = LoggerFactory.getLogger(GlobalExecutionListener.class);
	
	@Resource(name = "repositoryService")
	private RepositoryService repositoryService;
	
	@Resource( name = "workflowProcessService")
	private WorkflowProcessService workflowProcessService;
	
	@Resource( name = "processEngine")
	private ProcessEngine processEngine;
	
	public final static String ACT_TYPE_endEvent = "endEvent";
	public final static String ACT_TYPE_exclusiveGateway = "exclusiveGateway";
	public final static String ACT_TYPE_userTask = "userTask";
	public final static String ACT_TYPE_serviceTask = "serviceTask";
	

	@Override
	public void notify(DelegateExecution delegateExecution) throws Exception {
		String event = delegateExecution.getEventName();
		logger.info("notify event receives[" + event + "]");
		
		String currentActivityId = delegateExecution.getCurrentActivityId();
		String processDefinitionId = delegateExecution.getProcessDefinitionId();
		
		if(EVENTNAME_END.equals(event)) {
			// check if this is the last activity node
			if (checkIfEndActivity(currentActivityId, processDefinitionId)) {
				// notify the init user
				logger.info("currentActivityId " + currentActivityId
							+ " is last activity of process " + processDefinitionId);
				workflowProcessService.notifyWhenCompleted(delegateExecution.getProcessInstanceId());
			}
		}
	}
	
	private boolean checkIfEndActivity(String activityId, String processDefinitionId){
		RepositoryServiceImpl rsImpl = (RepositoryServiceImpl) repositoryService;
		ProcessDefinitionEntity pde = (ProcessDefinitionEntity)
							rsImpl.getDeployedProcessDefinition(processDefinitionId);
		
		List<ActivityImpl> list = pde.getActivities();
		
		if(list == null) return false;
		
		for (ActivityImpl ai : list) {
			String activity_type_ = (String) ai.getProperty("type");
			logger.info("activity_type_: " + activity_type_);
			
			if (ai.getId().equals(activityId) && ACT_TYPE_endEvent.equals(activity_type_)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		RepositoryServiceImpl rsImpl = (RepositoryServiceImpl)repositoryService;
		List<ProcessDefinition> pdList = repositoryService.createProcessDefinitionQuery().list();
		
		logger.info("ProcessDefinition list size [" + pdList.size() + "]");
		
		for (ProcessDefinition pd : pdList) {
			rsImpl.getCommandExecutor().execute(new AddExecutionListenerCmd(this, pd.getId(), processEngine));
		}
		
		pdList.clear();
		pdList = null;
		logger.info("add global ExecutionListener for all Activity nodes, completed.");
	}

}
