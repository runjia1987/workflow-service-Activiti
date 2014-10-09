package org.wf.service.extension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.wf.service.activiti.api.NotifyService;
import org.wf.service.activiti.api.UserAccountService;
import org.wf.service.activiti.api.WorkflowProcessService;
import org.wf.service.model.UserAccount;
import org.wf.service.utils.PropertyReader;
import org.wf.service.utils.ServiceConstants;

/**
 * global listener for task notifying, events [ create, assign, complete ]
 * @author zhurunjia
 *
 */
@Component("globalTaskNotificationListener")
public class GlobalTaskNotifyListener implements TaskListener, InitializingBean {

	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = LoggerFactory.getLogger(GlobalTaskNotifyListener.class);

	@Resource(name = "taskService")
	private TaskService taskService;
	
	@Resource(name = "repositoryService")
	private RepositoryService repositoryService;
	
	@Resource( name = "emailNotifyService")
	private NotifyService emailNotifyService;
	
	@Resource(name = "runtimeService")
	private RuntimeService runtimeService;
	
	@Resource( name = "userAccountService")
	private UserAccountService userAccountService;
	
	@Resource( name = "workflowProcessService")
	private WorkflowProcessService workflowProcessService;
	
	@Resource( name = "processEngine")
	private ProcessEngine processEngine;
	
	
	/**
	 * process the fired event.
	 */
	@Override
	public void notify(DelegateTask delegateTask) {
		String event = delegateTask.getEventName();
		String taskId = delegateTask.getId();
		logger.info("notify event receives[" + event + "] for task: " + taskId);
		
		Set<IdentityLink> user_group_set = delegateTask.getCandidates();
		if( user_group_set == null) {
			user_group_set = new HashSet<IdentityLink>(10);
		}
		// add assignee
		if (StringUtils.isNotEmpty(delegateTask.getAssignee())) {
			IdentityLinkEntity ie = new IdentityLinkEntity();
			ie.setUserId(delegateTask.getAssignee() );
			user_group_set.add(ie);
		}
		
		if (user_group_set != null) {
			Iterator<IdentityLink> itr = user_group_set.iterator();
			String[] groupIds = new String[user_group_set.size()];
			String[] userIds = new String[user_group_set.size()];
			int i = 0, j = 0;
			while( itr.hasNext()) {
				IdentityLink link = itr.next();
				if (StringUtils.isNotEmpty(link.getGroupId())) {
					groupIds[i++] = link.getGroupId();	
				}
				if(StringUtils.isNotBlank(link.getUserId())) {
					userIds[j++] = link.getUserId();
				}
			}
			user_group_set.clear();user_group_set = null; // help GC
			
			List<String> accountsList = new ArrayList<String>(10);
			if ( i > 0) {
				// group roles
				int index = 0;
				for (String groupId : groupIds) {
					if ( ++index > i) break;
					List<UserAccount> userList = userAccountService.queryByGroupRole(groupId);
					if(userList != null && userList.size() > 0){
						for (UserAccount ua : userList){
							accountsList.add(ua.getUserId());
						}
					}
				}
			}
			if ( j > 0){
				// user Ids
				int index = 0;
				for (String userId : userIds) {
					if( ++index > j) break;
					accountsList.add(userId);
				}
			}
			groupIds = null;userIds = null;
			
			if (accountsList.size() == 0) {
				String message = "taskId [" + taskId + "] " + delegateTask.getName()
								+ " does not define any user or group";
				if ( EVENTNAME_CREATE.equals(event)) {
					String assignee = PropertyReader.getValue(ServiceConstants.DefaultAssigneeKey);
					logger.info(message + ", assign to default " + assignee);
					delegateTask.setAssignee(assignee);
				} else {
					throw new RuntimeException(message);
				}
			} else {
				logger.info("taskId [" + taskId + "] get users: " + accountsList);
			}
			
			// if complete event happens
			if (EVENTNAME_COMPLETE.equals(event)) {
				Map<String, Object> variables = taskService.getVariables(taskId);
				String currentUserId = (String) variables.get(ServiceConstants.CurrentUserIdKey);
				if ( ! accountsList.contains(currentUserId)) {
					throw new RuntimeException("current user " + currentUserId
							+ " is not allowed to approve this task !!!");
				} else {
					logger.info("taskId [" + taskId + "] is sucessfully completed by user: " + currentUserId);
				}
			}
			
			if (EVENTNAME_ASSIGNMENT.equals(event) || EVENTNAME_COMPLETE.equals(event)) {
				notifyAssignee("", accountsList, null);
			}
		}
	}
	
	/**
	 * notify the assignees
	 * @param taskId
	 */
	private void notifyAssignee(String taskId, List<String> accountsList, Map<String, Object> paramMap) {
		// notify by email or else...
		
	}

	@Override
	/**
	 * add this TaskListener for all UserTask nodes
	 */
	public void afterPropertiesSet() throws Exception {
		RepositoryServiceImpl rsImpl = (RepositoryServiceImpl)repositoryService;
		List<ProcessDefinition> pdList = repositoryService.createProcessDefinitionQuery().list();
		
		logger.info("ProcessDefinition list size [" + pdList.size() + "]");
		
		for (ProcessDefinition pd : pdList) {
			logger.info("start to add taskListener for process: " + pd.getId());
			ProcessDefinitionEntity pde = (ProcessDefinitionEntity) rsImpl.getDeployedProcessDefinition( pd.getId() );
			List<ActivityImpl> aList = pde.getActivities();
			for (ActivityImpl ai : aList) {
				
				String activity_type_ = (String) ai.getProperty(ServiceConstants.ActivityTypeKey);
				// only consider "userTask"
				if (GlobalExecutionListener.ACT_TYPE_userTask.equals(activity_type_)) {
					if (ai.getActivityBehavior() instanceof UserTaskActivityBehavior) {
						logger.info("process " + pd.getId() + ", this activity is a UserTask: " + ai.getId());
						TaskDefinition td = ((UserTaskActivityBehavior) ai.getActivityBehavior()).getTaskDefinition();
						rsImpl.getCommandExecutor().execute(new AddTaskListenerCmd(this, pde, td, processEngine));
					}
				}
			}
		}	
		pdList.clear();
		pdList = null;
		
		logger.info("add global TaskListener for all UserTask nodes, completed.");
	}

}
