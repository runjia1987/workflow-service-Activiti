package org.wf.dao.activiti.impl;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.core.Ordered;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.ibatis.sqlmap.client.SqlMapClient;

public class BaseDaoSupport extends SqlMapClientDaoSupport {

	/**
	 * will be processed by CommonAnnotationBeanPostProcessor
	 */
	@Resource(name = "sqlMapClient")
	private SqlMapClient sqlMapClient;
	
	/**
	 * JSR-250 @PostConstruct has a lower priority than JSR-250 @Resource,
	 * <br>
	 * InitDestroyAnnotationBeanPostProcessor (Ordered.LOWEST_PRECEDENCE) vs.
	 *  CommonAnnotationBeanPostProcessor (Ordered.LOWEST_PRECEDENCE - 3)
	 */
	@PostConstruct
	public void setSuper(){
		super.setSqlMapClient(sqlMapClient);
	}
	
}
