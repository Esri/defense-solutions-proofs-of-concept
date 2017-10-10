package com.esri.geoevent.solutions.processor.bearing;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class BearingProcessorService extends GeoEventProcessorServiceBase {
	Messaging messaging;
	GeoEventDefinitionManager manager;
	public BearingProcessorService() throws PropertyException {
		definition = new BearingProcessorDefinition();
	}

	@Override
	public GeoEventProcessor create() throws ComponentException {
		BearingProcessor bp =  new BearingProcessor(definition);
		bp.setMessaging(messaging);
		bp.setGDManager(manager);
		return bp;
		
	}
	
	
	public void setMessaging(Messaging m)
	{
		messaging = m;
	}
	
	public void setManager(GeoEventDefinitionManager m)
	{
		manager = m;
	}

}
