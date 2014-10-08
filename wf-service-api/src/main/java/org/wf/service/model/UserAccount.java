package org.wf.service.model;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * User account model
 * @author zhurunjia
 *
 */
public class UserAccount implements ConstraintsValidator, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 内部用户id, = userName, 简化处理
	 */
	private String userId;
	
	/**
	 * 用户名称，必须
	 */
	private String userName;
	
	/**
	 * 用户email，必须
	 */
	private String email;
	
	/**
	 * 角色名称，必须, default = 'default'
	 */
	private String roleName = "default";
	
	public UserAccount(){
	}
	
	public String toString() {
		return this.userName + "," + this.roleName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	@Override
	public boolean validate() {
		this.userId = userName;
		return StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(roleName) && StringUtils.isNotEmpty(email);
	}

}
