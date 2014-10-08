package org.wf.service.model;

/**
 * 业务流程状态
 * @author zhurunjia
 *
 */
public enum ProcessStatusEnum {
	
	/**
	 * 新建|发起, 来源于外部或内部
	 */
	NEW, 
	
	/**
	 * 已启动
	 */
	STARTED,
	
	 /**
	  * 正常完成
	  */
	COMPLETED, 
	
	/**
	 * 已放弃
	 */
	ABOTRED,
	
	/**
	 * 已过期
	 */
	EXPIRED,
	
	/**
	 * 暂停
	 */
	SUSPENDED;
	
	/**
	 * 校验名字
	 * @param name
	 * @return
	 */
	public static boolean isAssignable(String name) {
		try {
			ProcessStatusEnum.valueOf(name);
			return true;
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

}
