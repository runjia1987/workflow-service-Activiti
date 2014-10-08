package org.wf.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ChineseKeyMappingService implements InitializingBean {
	
	private String loadResourcesPattern = "chinese/key/mapping/*.properties";
	
	private final Map<String, String> chineseKeyMappingMap = new HashMap<String, String>(1 << 8);

	@Override
	public void afterPropertiesSet() throws Exception {
		ResourcePatternResolver rpr = new PathMatchingResourcePatternResolver();
		Resource[] resources = rpr.getResources(loadResourcesPattern);
		
		InputStream ins = null;
		Properties prop = new Properties();
		for (Resource resource : resources) {
			ins = resource.getInputStream();
			prop.load(ins);
			
			Set<Entry<Object, Object>> set = prop.entrySet();
			Iterator<Entry<Object, Object>> itr = set.iterator();
			Entry<Object, Object> entry;
			while (itr.hasNext()) {
				entry = itr.next();
				chineseKeyMappingMap.put( (String)entry.getKey(), (String)entry.getValue());
			}
			
			set.clear();set = null;
			prop.clear();prop = null;
			ins.close();ins = null;
		}
	}
	
	public String getMapping(String key) {
		return chineseKeyMappingMap.get(key);
	}

}
