package org.wf.service.model;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

/**
 * workflow business process model
 * @author zhurunjia
 *
 */
public class WorkflowProcess implements ConstraintsValidator, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * outside sequence id(application_name + random_number), 必须
	 */
	private String outerSequenceId;
	
	/**
	 * source system key, 必须
	 */
	private String sourceSystem = "self";
	
	/**
	 *  目标工作流程的标识key, 必须
	 *  <br> = processDefinitionKey in activiti
	 * 
	 */
	private String processModelKey;
	
	/**
	 * workflow 业务标识id(内部) = ProcessInstanceId
	 */
	private String innerProcessId;
	
	/**
	 * incoming timstamp, 必须
	 */
	private Timestamp acceptTimeStamp;
	
	/**
	 * 业务流程状态
	 */
	private String status = ProcessStatusEnum.NEW.name();
	
	/**
	 * 发起的用户
	 */
	private UserAccount initUserAccount;
	
	/**
	 * 发起的用户名
	 */
	private String initUserName;
	
	/**
	 * 任务明细信息, 必须
	 */
	private String processDetail;
	
	/**
	 * 任务的最大过期天数， 默认30d
	 */
	private Short maxExpirationDays = 30;
	
	public WorkflowProcess(){
		if (StringUtils.isEmpty(initUserName) && initUserAccount != null) {
			initUserName = initUserAccount.getUserName();
		}
	}
	
	public String toString() {
		return this.outerSequenceId + "," + sourceSystem + "," + processModelKey 
						+ "," + initUserName + "," + processDetail;
	}

	@Override
	public boolean validate() {
		return StringUtils.isNotEmpty(outerSequenceId)
				&& acceptTimeStamp != null
				&& StringUtils.isNotEmpty(processModelKey)
				&& StringUtils.isNotEmpty(sourceSystem)
				&& status != null && ProcessStatusEnum.isAssignable(status)
				&& StringUtils.isNotEmpty(processDetail);
	}

	public Timestamp getAcceptTimeStamp() {
		return acceptTimeStamp;
	}

	public void setAcceptTimeStamp(Timestamp acceptTimeStamp) {
		this.acceptTimeStamp = acceptTimeStamp;
	}

	public String getInnerProcessId() {
		return innerProcessId;
	}

	public void setInnerProcessId(String innerProcessId) {
		this.innerProcessId = innerProcessId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getProcessDetail() {
		return processDetail;
	}

	public void setProcessDetail(String processDetail) {
		this.processDetail = processDetail;
	}

	public Short getMaxExpirationDays() {
		return maxExpirationDays;
	}

	public void setMaxExpirationDays(Short maxExpirationDays) {
		this.maxExpirationDays = maxExpirationDays;
	}

	public String getProcessModelKey() {
		return processModelKey;
	}

	public void setProcessModelKey(String processModelKey) {
		this.processModelKey = processModelKey;
	}

	public String getSourceSystem() {
		return sourceSystem;
	}

	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}

	public String getInitUserName() {
		return initUserName;
	}

	public void setInitUserName(String initUserName) {
		this.initUserName = initUserName;
	}

	public String getOuterSequenceId() {
		return outerSequenceId;
	}

	public void setOuterSequenceId(String outerSequenceId) {
		this.outerSequenceId = outerSequenceId;
	}

	public UserAccount getInitUserAccount() {
		return initUserAccount;
	}

	public void setInitUserAccount(UserAccount initUserAccount) {
		this.initUserAccount = initUserAccount;
	}
	
}
