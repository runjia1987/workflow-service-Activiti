package org.wf.testcase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class TestJavaURL {

	@Test
	public void runURL() throws IOException{
		ResourcePatternResolver rpr = new PathMatchingResourcePatternResolver();
		Resource[] rArray = rpr.getResources("deployments/*.bpmn");
		for (Resource r : rArray) {
			System.out.println(r.getURL());
			
			System.out.println(r.getURL().getPath());
		}
		
		// the below is not good to retrieve reources from jar
		/**
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL parentUrl = cl.getResource("deployments/");
		File[] resources = new File(parentUrl.getFile()).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".xml"))
					return true;
				return false;
			}
		});
		for(File f : resources) {
			InputStream ins = null;
			try {
				ins = new FileInputStream(f);
				ins.close();
				
				System.out.println(f.getName());
				
			} catch (Exception e){
				e.printStackTrace();
			}
		}**/
	}
	
}
