package org.wf.service.extension;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.wf.service.utils.PropertyReader;
import org.wf.service.utils.ServiceConstants;


/**
 * draw diagram picture with highlighted activity node
 * @author zhurunjia
 *
 */
@Component("diagramPictureDrawer")
public class DiagramPictureDrawer {
	
	private final static Logger logger = LoggerFactory.getLogger(DiagramPictureDrawer.class);
	
	@Resource( name ="repositoryService")
	private RepositoryService repositoryService;
	
	@Resource( name = "runtimeService")
	private RuntimeService runtimeService;
	
	@Resource( name = "historyService")
	private HistoryService historyService;
	
	@Resource( name = "processEngine")
	private ProcessEngine processEngine;
	
	@Resource( name = "diagramPictureCache")
	private DiagramPictureCache diagramPictureCache;
	
	private String storePicturePath;
	
	private volatile boolean setDiagramCanvas = false;
	
	@PostConstruct
	public void init(){
		storePicturePath = PropertyReader.getValue(ServiceConstants.StorePicturePath);
	}
	
	/**
	 * return the diagram picture path
	 * @param processInstanceId = innerProcessId
	 * @return
	 */
	public String drawDiagram(String processInstanceId) {
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
					.processInstanceId(processInstanceId).singleResult();
		// get activity id list from history with this processInstanceId
		List<HistoricActivityInstance> haiList = historyService.createHistoricActivityInstanceQuery()
						.processInstanceId(processInstanceId)
						.orderByHistoricActivityInstanceStartTime().asc().list();
		if( haiList == null || haiList.size() == 0) {
			return null;
		}
		// get last activity id
		String activityId = haiList.get(haiList.size() - 1).getActivityId();
		String cacheKey = processInstanceId + "#" + activityId;
		
		final String path = diagramPictureCache.get(cacheKey);
		if ( StringUtils.isNotEmpty(path) ) {
			logger.info("found in diagram cache: " + cacheKey);
			return path;
		}
		
		logger.info("not found in diagramPictureCache, try to fetch from runtime services.");
		List<String> highlightedHistoryActivities = new ArrayList<String>();
		for (HistoricActivityInstance hai : haiList) {
			highlightedHistoryActivities.add(hai.getActivityId());
		}
		logger.info("highlightedHistoryActivities: " + highlightedHistoryActivities);
		
		// get highlighted flow id list from active running
		RepositoryServiceImpl rsImpl = (RepositoryServiceImpl) repositoryService;
		final String processDefinitionId = pi.getProcessDefinitionId();
		ProcessDefinitionEntity pde = (ProcessDefinitionEntity) rsImpl.getDeployedProcessDefinition(processDefinitionId);
		List<String> activeHightlightedFlows = findHighLightedFlows(pde, haiList);
		
		BpmnModel bm = repositoryService.getBpmnModel(processDefinitionId);
		// for Chinese font
		Context.setProcessEngineConfiguration(
						((ProcessEngineImpl)processEngine ).getProcessEngineConfiguration());
		
		logger.info("start to draw and save to local server...");
		setDiagramCanvas();
		InputStream ins = ProcessDiagramGenerator.generateDiagram ( bm, "png",  
	             highlightedHistoryActivities,
	             activeHightlightedFlows);
		
		// save the picture file
		String pictureFileName = storePicturePath + File.separator + randomFileName() + ".png";
		if (savePicture(ins, pictureFileName)) {
			logger.info("draw diagram completed and save to cache: " + pictureFileName);
			diagramPictureCache.put(cacheKey, pictureFileName);
		}
		return pictureFileName;
	}
	
	public void setDiagramCanvas(){
		if ( ! setDiagramCanvas) {
			try {
				Class<?> cl = Class.forName("org.activiti.engine.impl.bpmn.diagram.ProcessDiagramCanvas",
							true, Thread.currentThread().getContextClassLoader());
				// highlight the label color
				Field field = cl.getDeclaredField("LABEL_COLOR");
				field.setAccessible(true);
				Color LABEL_COLOR = (Color) field.get(null);
				LABEL_COLOR = Color.BLACK;
				field.set(null, LABEL_COLOR);
				
			} catch (Exception e) {
				logger.error("", e);
			}
			setDiagramCanvas = true;
		}
	}
	
	/**
	 * find highlighted flow id list from active running
	 */
	public List<String> findHighLightedFlows(ProcessDefinitionEntity processDefinitionEntity,
						List<HistoricActivityInstance> haiList ) {
		List<String> activeHightlightedFlows = new ArrayList<String>();
		
		List<ActivityImpl> startTimeNodes = new ArrayList<ActivityImpl>();
		for (int i = 0; i < haiList.size() - 1; i++) {
			ActivityImpl activity =  processDefinitionEntity.findActivity( haiList.get(i).getActivityId() );
			ActivityImpl tempActivity = processDefinitionEntity.findActivity(
							haiList.get(i + 1).getActivityId() );
			startTimeNodes.add(tempActivity);
			
			int j = i + 1;
			if (j < haiList.size() - 1) {
				HistoricActivityInstance hai_1 = haiList.get(j), hai_2 = haiList.get(j + 1);
				
				if (hai_1.getStartTime().equals(hai_2.getStartTime()) ) {
					ActivityImpl tempActivity2 = processDefinitionEntity.findActivity(hai_2.getActivityId());
					startTimeNodes.add(tempActivity2);
				}
			}
			
			List<PvmTransition> ptList = activity.getOutgoingTransitions();
			for (PvmTransition pt : ptList)  {
				ActivityImpl pvmActivity = (ActivityImpl) pt.getDestination();
				if  (startTimeNodes.contains(pvmActivity)) {
					activeHightlightedFlows.add(pt.getId());
				}
			}
			startTimeNodes.clear();
		}
		logger.info("findHighLightedFlows return highlighted flows: " + activeHightlightedFlows);
		return activeHightlightedFlows;
	}
	
	
	private boolean savePicture(InputStream ins, String pictureFileName) {
		OutputStream ous = null;
		try {
			ous = new FileOutputStream(pictureFileName);
			int reads = 0;
			byte[] buffer = new byte[1 << 9];
			while ( (reads = ins.read(buffer)) > 0) {
				ous.write(buffer, 0, reads);
			}
			return true;
			
		} catch(Exception e) {
			String message = "savePicture failure " + (e != null ? e.getMessage() : "");
			throw new RuntimeException(message);
		} finally {
			if ( ins != null)
				try {
					ins.close();
					ous.flush();
					ous.close();
				} catch (Exception e) { }
		}
	}
	
	private String randomFileName() {
		return UUID.randomUUID().toString();
	}
	
}
