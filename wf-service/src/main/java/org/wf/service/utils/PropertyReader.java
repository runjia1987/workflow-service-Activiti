package org.wf.service.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public class PropertyReader {
	
	private final static String[] propertyFiles = { "service/config/system.properties", };
	
	private final static Properties resolvedProperties = new Properties();
	
	private final static String charset = "UTF-8";
	
	private PropertyReader() { }

	static {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		for (String file : propertyFiles) {
			if( StringUtils.isEmpty(file)) continue;
			
			readAndResolve(file, cl);
		}
	}
	
	public static String getValue(String key){
		return resolvedProperties.getProperty(key);
	}
	
	public static String getValue(String key, String sourcePropertyFile){
		boolean ifExists = Arrays.asList(propertyFiles).contains(sourcePropertyFile);
		
		if( ! ifExists) {
			readAndResolve(sourcePropertyFile, Thread.currentThread().getContextClassLoader());
		}
		
		return getValue(key);
	}
	
	/**
	 * read and resolve property file
	 * @param fileName
	 * @param cl
	 */
	private static void readAndResolve(String fileName, ClassLoader cl) {
		InputStream ins = null;
		InputStreamReader insr = null;
		try {
			ins = cl.getResourceAsStream(fileName);
			insr = new InputStreamReader(ins, charset);
			resolvedProperties.load(insr);
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (ins != null) {
				try {
					insr.close();
					ins.close();
				} catch (IOException e) { }
			}
		}
	}
	
	public static void main(String[] args) {
		System.out.println( PropertyReader.getValue("chineseFont", "service/config/system.properties"));
	}
	
}
