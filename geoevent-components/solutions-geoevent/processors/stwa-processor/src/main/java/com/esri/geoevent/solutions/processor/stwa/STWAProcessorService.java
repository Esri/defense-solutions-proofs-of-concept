package com.esri.geoevent.solutions.processor.stwa;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class STWAProcessorService extends GeoEventProcessorServiceBase{
	
	GeoEventDefinitionManager manager;
	Messaging messaging;
	public STWAProcessorService() throws PropertyException{
		definition = new STWAProcessorDefinition();
	}

	public GeoEventProcessor create() throws ComponentException {
		STWAProcessor twsProcessor = new STWAProcessor(definition);
		twsProcessor.setManager(manager);
		twsProcessor.setMessaging(messaging);
		return twsProcessor;	
	}
	
	public void setmanager(GeoEventDefinitionManager manager)
	{
		this.manager=manager;
	}
	
	public void setMessaging(Messaging messaging)
	{
		this.messaging = messaging;
	}

}
