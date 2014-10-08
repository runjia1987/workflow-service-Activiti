package org.wf.service.utils;

import javax.annotation.Resource;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * scan expired process, set them to status: ProcessStatusEnum.EXPIRED
 * @author zhurunjia
 *
 */
@Component
public class ScanExpiredProcessJob implements DisposableBean {
	
	@Resource(name = "runtimeService")
	private RuntimeService runtimeService;

	@Override
	public void destroy() throws Exception {
		// innerProcessId
		//runtimeService.suspendProcessInstanceById("");
		
		//ProcessInstance pi = runtimeService.startProcessInstanceByKey("definition key", "business ID");
		//pi.getActivityId();
	}

}
