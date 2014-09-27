package org.jackJew.wf.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CreateDataBaseSchema {
	
	public static void main(String[] args){
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/config/applicationContext.xml");
		
		System.out.println("success");
	}

}
