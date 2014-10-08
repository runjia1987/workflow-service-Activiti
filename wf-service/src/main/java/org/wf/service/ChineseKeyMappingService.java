package org.wf.service;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;

public class ChineseKeyMappingService implements InitializingBean {
	
	private String sourcesFolder;
	
	private final Map<String, String> chineseKeyMappingMap = new HashMap<String, String>(1 << 8);

	@Override
	public void afterPropertiesSet() throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL url = cl.getResource(sourcesFolder);
		File folder = new File(url.getFile());
		File[] mappings = folder.listFiles();
		
		InputStream ins = null;
		Properties prop = new Properties();
		for (File mapping : mappings) {
			ins = cl.getResourceAsStream(sourcesFolder + "/" + mapping.getName());
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

	public void setSourcesFolder(String sourcesFolder) {
		this.sourcesFolder = sourcesFolder;
	}

}
