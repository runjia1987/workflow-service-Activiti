package org.wf.dao.activiti.impl;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.ibatis.sqlmap.client.SqlMapClient;

public class BaseDaoSupport extends SqlMapClientDaoSupport {

	/**
	 * will be processed by CommonAnnotationBeanPostProcessor
	 */
	@Resource(name = "sqlMapClient")
	private SqlMapClient sqlMapClient;
	
	/**
	 * @PostConstruct has lower priority compared with JSR-250 @Resource
	 */
	@PostConstruct
	public void setSuper(){
		super.setSqlMapClient(sqlMapClient);
	}
	
}
