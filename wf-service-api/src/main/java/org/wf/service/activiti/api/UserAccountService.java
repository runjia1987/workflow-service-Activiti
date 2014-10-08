package org.wf.service.activiti.api;

import java.util.List;

import org.wf.service.model.UserAccount;

/**
 * user service interface based on activiti indentityService
 * @author zhurunjia
 *
 */
public interface UserAccountService {
	
	/**
	 * by userId
	 * @param userId
	 * @return
	 */
	UserAccount queryByUserId(String userId);
	
	/**
	 * by userName
	 * @param userName
	 * @return
	 */
	UserAccount queryByUserName(String userName);
	
	/**
	 * find the users by a group role
	 * @param roleName = groupId
	 * @return
	 */
	List<UserAccount> queryByGroupRole(String roleName);
	
	/**
	 * synchronzie a user from outside system, and save to workflow DB
	 * <br> update the user in DB if already exists.
	 * @param user
	 */
	void saveUser(UserAccount user);
	
	/**
	 * delete an existing user - group mapping
	 * @param userName
	 * @param roleName
	 */
	void removeUserGroupMapping(String userName, String roleName);

}
