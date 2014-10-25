package org.wf.service.extension;

import java.util.LinkedHashMap;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * a local cache for diagram picitures
 * @author zhurunjia
 *
 */
@Component("diagramPictureCache")
public class DiagramPictureCache extends LinkedHashMap<String, String> {
	
	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = LoggerFactory.getLogger(DiagramPictureCache.class);
	
	private final int cacheSize = 40;
	
	/**
	 * LRU style, accessOrder = true
	 */
	public DiagramPictureCache() {
		super(100, .75f, true);
	}

	@Override
	protected boolean removeEldestEntry(
			java.util.Map.Entry<String, String> eldest) {
		if (cacheSize < super.size()) {
			logger.info("cache number exceeds the cacheSize, will LRU removeEldestEntry. ");
			return true;
		} else {
			return false;
		}
	}
	
	@PreDestroy
	public void destroy(){
		this.clear();
	}

}
