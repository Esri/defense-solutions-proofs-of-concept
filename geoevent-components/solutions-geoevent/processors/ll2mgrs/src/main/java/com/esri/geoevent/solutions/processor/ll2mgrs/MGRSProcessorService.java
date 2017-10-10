package com.esri.geoevent.solutions.processor.ll2mgrs;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class MGRSProcessorService extends GeoEventProcessorServiceBase {
	private GeoEventDefinitionManager manager;
	public Messaging messaging;
	public MGRSProcessorService()
	{
			definition = new MGRSProcessorDefinition();
	}
	public GeoEventProcessor create() throws ComponentException {
		MGRSProcessor processor =  new MGRSProcessor(definition);
		processor.setManager(manager);
		processor.setMessaging(messaging);
		return processor;
	}
	
	public void setManager(GeoEventDefinitionManager manager)
	{
		this.manager =  manager;
	}
	
	public void setMessaging(Messaging messaging)
	{
		this.messaging = messaging;
	}
	


}
