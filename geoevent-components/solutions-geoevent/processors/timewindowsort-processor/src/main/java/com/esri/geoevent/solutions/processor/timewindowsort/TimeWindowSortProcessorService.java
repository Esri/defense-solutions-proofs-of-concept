package com.esri.geoevent.solutions.processor.timewindowsort;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class TimeWindowSortProcessorService extends GeoEventProcessorServiceBase{
	
	GeoEventDefinitionManager manager;
	Messaging messaging;
	public TimeWindowSortProcessorService() throws PropertyException{
		definition = new TimeWindowSortProcessorDefinition();
	}

	public GeoEventProcessor create() throws ComponentException {
		TimeWindowSortProcessor twsProcessor = new TimeWindowSortProcessor(definition);
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
