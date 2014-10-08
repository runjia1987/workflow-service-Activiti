package org.wf.service.activiti.api;

import java.util.Map;

/**
 * workflow task service
 * @author zhurunjia
 *
 */
public interface WorkflowTaskService {
	
	/**
	 * the assignee set the current task as passed and completed
	 * @param taskId
	 * @param paramMap
	 * @param userId
	 */
	void completeTask(String taskId, Map<String, Object> paramMap, String userId);
	
	/**
	 * reject the current taskId, will return back get the previous task node。
	 * <br> not implemented.
	 * @param taskId
	 * @param paramMap provide the "reason" in paramMap
	 */
	void rejectTask(String taskId, Map<String, Object> paramMap, String userId);
	
	/**
	 * assign the task to specified user, with details
	 * @param taskId
	 * @param paramMap
	 * @param userId
	 */
	void assign(String taskId, Map<String, Object> paramMap, String userId);

}
