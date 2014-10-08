package org.wf.service.extension;

import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * add specified execution listener, actually update the DeploymentManager cache.
 * @author zhurunjia
 *
 */
public class AddExecutionListenerCmd implements Command<Object> {
	
	private ExecutionListener executionListener;
	private String processDefinitionId;
	private ProcessEngine processEngine;
	
	private final static Logger logger = LoggerFactory.getLogger(AddExecutionListenerCmd.class);
	
	public AddExecutionListenerCmd(ExecutionListener listener, String processDefinitionId,
				ProcessEngine processEngine) {
		this.executionListener = listener;
		this.processDefinitionId = processDefinitionId;
		this.processEngine = processEngine;
	}

	@Override
	public Object execute(CommandContext commandContext) {
		RepositoryServiceImpl rsImpl = (RepositoryServiceImpl) processEngine.getRepositoryService();
		ProcessDefinitionEntity pde = (ProcessDefinitionEntity) rsImpl.getDeployedProcessDefinition(processDefinitionId);
		
		ProcessEngineConfigurationImpl config = ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration();
		DeploymentManager dm = config.getDeploymentManager();
		
		List<ActivityImpl> list = pde.getActivities();
		if( list == null) return null;
		
		logger.info("start to add executionListener for proccess: " + processDefinitionId);
		for (ActivityImpl impl : list) {
			logger.info("activity: " + impl.getId() + " addExecutionListener OK.");
			impl.addExecutionListener(ExecutionListener.EVENTNAME_END, executionListener);
		}
		
		// not thread safe, only used in bean initialization
		dm.getProcessDefinitionCache().add(processDefinitionId, pde);
		
		return null;
	}

}
