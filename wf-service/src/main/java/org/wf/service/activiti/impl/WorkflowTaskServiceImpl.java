package org.wf.service.activiti.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wf.service.activiti.api.WorkflowTaskService;
import org.wf.service.extension.DynamicTaskCmd;
import org.wf.service.utils.ServiceConstants;

@Transactional
@Service("workflowTaskService")
public class WorkflowTaskServiceImpl implements WorkflowTaskService {

	private static final long serialVersionUID = 1L;

	private final static Logger logger = LoggerFactory
			.getLogger(WorkflowTaskServiceImpl.class);

	@Resource(name = "taskService")
	private TaskService taskService;

	@Resource(name = "runtimeService")
	private RuntimeService runtimeService;

	@Resource(name = "repositoryService")
	private RepositoryService repositoryService;
	
	@Resource( name = "historyService")
	private HistoryService historyService;

	@Override
	public void completeTask(String taskId, Map<String, Object> paramMap,
			String userId) {
		paramMap.put(ServiceConstants.CurrentUserIdKey, userId); // set the current userId

		taskService.setVariables(taskId, paramMap);
		taskService.complete(taskId);
	}

	@Override
	public void rejectTask(String taskId, Map<String, Object> paramMap,
			String userId) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		String executionId = task.getExecutionId();
		if (executionId == null) return;

		ExecutionEntity execution = (ExecutionEntity) runtimeService
				.createExecutionQuery().executionId(executionId).singleResult();
		String currentActivitiId = execution.getActivityId(), previousActivityId = null;
		
		// order by activity startTime asc
		List<HistoricActivityInstance> haiList = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(task.getProcessInstanceId())
				.orderByHistoricActivityInstanceStartTime().asc().list();
		
		for ( int i = 0; i < haiList.size(); i++) {
			if (haiList.get(i).getActivityId() == currentActivitiId && i > 0) {
				previousActivityId = haiList.get(i - 1).getActivityId();
				break;
			}
		}
		String rejectReason = userId + " rejects: "+ paramMap.get(ServiceConstants.RejectTaskReason);
		
		if (previousActivityId != null) {
			((TaskServiceImpl) taskService).getCommandExecutor().execute(
					new DynamicTaskCmd(taskService, execution, previousActivityId, rejectReason));
		}

	}

	@Override
	public void assign(String taskId, Map<String, Object> paramMap,
			String userId) {
		try {
			taskService.setAssignee(taskId, userId);
			taskService.setVariables(taskId, paramMap);

			logger.info("assign task " + taskId + " to user " + userId
					+ " is completed.");
		} catch (ActivitiTaskAlreadyClaimedException ae) {
			logger.info("assign[taskId: " + taskId + ", userId: " + userId
					+ "] fails, because ActivitiTaskAlreadyClaimedException: "
					+ ae);
			;
		}
	}

	public static void main(String[] args) {
		new WorkflowTaskServiceImpl().completeTask("123",
				new HashMap<String, Object>(6), "Jack");
	}

}
