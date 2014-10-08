package org.wf.service.model;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * status entity for workflow process
 * @author zhurunjia
 *
 */
public class ProcessStatusEntity implements ConstraintsValidator,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String currentTaskId;
	
	private String innerProcessId;
	
	private String diagramPicturePath;
	
	/**
	 * 业务流程状态
	 */
	private String status = ProcessStatusEnum.STARTED.name();

	@Override
	public boolean validate() {
		return StringUtils.isNotEmpty(innerProcessId) && ProcessStatusEnum.isAssignable(status);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder(1 << 8);
		builder.append("currentTaskId: ").append(currentTaskId).append(", processId: ")
				.append(innerProcessId).append(", diagramPath: ").append(diagramPicturePath)
				.append(", status: ").append(status);
		
		return builder.toString();
	}

	public String getInnerProcessId() {
		return innerProcessId;
	}

	public void setInnerProcessId(String innerProcessId) {
		this.innerProcessId = innerProcessId;
	}

	public String getDiagramPicturePath() {
		return diagramPicturePath;
	}

	public void setDiagramPicturePath(String diagramPicturePath) {
		this.diagramPicturePath = diagramPicturePath;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCurrentTaskId() {
		return currentTaskId;
	}

	public void setCurrentTaskId(String currentTaskId) {
		this.currentTaskId = currentTaskId;
	}

}
