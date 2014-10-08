package org.wf.service.activiti.impl;

import java.util.ArrayList;

import org.activiti.engine.identity.Group;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.wf.service.activiti.api.UserAccountService;
import org.wf.service.model.UserAccount;

@Transactional
@Service("userAccountService")
public class UserAccountServiceImpl implements UserAccountService {

	private static final Logger logger = LoggerFactory
			.getLogger(UserAccountServiceImpl.class);

	@Resource(name = "identityService")
	private IdentityService identityService;

	@Override
	@Transactional(readOnly = true)
	public UserAccount queryByUserId(String userId) {
		UserQuery query = identityService.createUserQuery().userId(userId);
		List<User> users = query.list();
		
		if (users != null && users.size() > 0) {
			return convert(users.get(0));
		} 
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public UserAccount queryByUserName(String userName) {
		UserQuery query = identityService.createUserQuery().userFirstName(userName);
		List<User> users = query.list();

		if (users != null && users.size() > 0) {
			return convert(users.get(0));
		} 
		return null;
	}

	private UserAccount convert(User activiti_user) {
		UserAccount account = new UserAccount();
		account.setUserId(activiti_user.getFirstName());
		account.setEmail(activiti_user.getEmail());
		account.setUserName(activiti_user.getFirstName());

		return account;
	}

	@Override
	@Transactional
	public void saveUser(UserAccount user) {
		// save the group/role
		String groupName = user.getRoleName();
		if ( ! StringUtils.isEmpty(groupName)) {
			List<?> existedGroups = identityService.createGroupQuery().groupName(user.getRoleName()).list();
			if (existedGroups == null || existedGroups.size() == 0) {
				org.activiti.engine.identity.Group group = identityService
						.newGroup(user.getRoleName());
				group.setName(groupName);
				group.setId(groupName);
				// for group, name is same as id
				identityService.saveGroup(group);
			}
		}

		// save user to DB
		UserEntity activiti_user = new UserEntity();
		activiti_user.setEmail(user.getEmail());
		activiti_user.setFirstName(user.getUserName()); // firstName
		activiti_user.setId(user.getUserName());
		// check if already existed
		List<?> existedUser = identityService.createUserQuery().userId(user.getUserName()).list();
		if( existedUser == null || existedUser.size() == 0) {
			identityService.saveUser(activiti_user);
		} else {
			identityService.deleteUser(activiti_user.getId());
			identityService.saveUser(activiti_user);
		}

		// create a user - group mapping
		identityService.deleteMembership(user.getUserName(), groupName);
		identityService.createMembership(user.getUserName(), groupName);
		
	}

	@Override
	public void removeUserGroupMapping(String userName, String roleName) {
		// delete a user - group mapping
		try {
			// get userId
			String userId = queryByUserName(userName).getUserId();
			
			// roleName is same as roleId
			identityService.deleteMembership(userId, roleName);
		} catch (RuntimeException re) {
			re.printStackTrace();
			logger.warn("createMembership fail, possibly already exists.");
		}

	}

	@Override
	@Transactional(readOnly = true)
	public List<UserAccount> queryByGroupRole(String roleName) {
		List<User> activiti_user_list = identityService.createUserQuery().memberOfGroup(roleName).list();
		
		if (activiti_user_list != null && activiti_user_list.size() > 0) {
			List<UserAccount> users = new ArrayList<UserAccount>(activiti_user_list.size());
			for ( int i = 0; i < activiti_user_list.size(); i++){
				users.add(convert(activiti_user_list.get(i)));
			}
			
			activiti_user_list.clear();
			return Collections.unmodifiableList(users);
		} else {
			logger.error("queryByGroupRole get no user for group role[" + roleName + "]");
			return null;
		}
	}
}
