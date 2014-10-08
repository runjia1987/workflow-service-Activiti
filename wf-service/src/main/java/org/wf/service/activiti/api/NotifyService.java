package org.wf.service.activiti.api;

import java.util.Map;

public interface NotifyService {
	
	void notify(String recipient, Map<String, Object> paramMap);

}
