package org.wf.service.extension;

import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti.engine.impl.task.TaskDefinition;

/**
 * add specified task listener, actually update the DeploymentManager cache.
 * @author zhurunjia
 *
 */
public class AddTaskListenerCmd implements Command<Object> {

	private TaskListener taskListener;
	private ProcessDefinitionEntity pde;
	private TaskDefinition taskDefition;
	private ProcessEngine processEngine;
	
	
	public AddTaskListenerCmd(TaskListener taskListener, ProcessDefinitionEntity pde,
				TaskDefinition taskDefition, ProcessEngine processEngine) {
		this.taskListener = taskListener;
		this.pde = pde;
		this.taskDefition = taskDefition;
		this.processEngine = processEngine;
	}
	
	@Override
	public Object execute(CommandContext commandContext) {
		//ProcessDefinitionEntityManager pdManager = commandContext.getProcessDefinitionEntityManager();
		ProcessEngineConfigurationImpl config = ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration();
		DeploymentManager dm = config.getDeploymentManager();
		
		Map<String, TaskDefinition> map = pde.getTaskDefinitions();
		if( map == null) return null;
		
		taskDefition.addTaskListener(TaskListener.EVENTNAME_ALL_EVENTS, taskListener);
		map.put(taskDefition.getKey(), taskDefition);
		
		// not thread safe, only used in bean initialization
		dm.getProcessDefinitionCache().add(pde.getId(), pde);
		//pdManager.insert(pde);
		
		return null;
	}

}
