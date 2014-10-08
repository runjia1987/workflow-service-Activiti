package org.wf.service.extension;

import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.task.Task;

/**
 * dynamic task command
 * @author zhurunjia
 *
 */
public class DynamicTaskCmd implements Command<Object> {
	
	private TaskService taskService;
	
	private ExecutionEntity executionEntity;
	
	private String activityId;
	
	private String reason;
	
	/**
	 * @param taskService
	 * @param executionEntity
	 * @param activityId	the target activityId
	 * @param reason the reason
	 */
	public DynamicTaskCmd(TaskService taskService, ExecutionEntity executionEntity, String activityId, String reason) {
		this.taskService = taskService;
		this.executionEntity = executionEntity;
		this.activityId = activityId;
		this.reason = reason;
	} 

	@Override
	public Object execute(CommandContext commandContext) {
		List<Task> taskList = taskService.createTaskQuery().executionId(executionEntity.getId()).list();
		
		for (Task task : taskList) {  
			taskService.deleteTask(task.getId(), reason);
        }
		
        ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();
        ActivityImpl activity = processDefinition.findActivity(activityId);  
        executionEntity.executeActivity(activity);
        
        return null;  
    }

}
