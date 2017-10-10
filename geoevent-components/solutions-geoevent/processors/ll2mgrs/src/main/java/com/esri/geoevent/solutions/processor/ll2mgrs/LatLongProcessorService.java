package com.esri.geoevent.solutions.processor.ll2mgrs;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class LatLongProcessorService  extends GeoEventProcessorServiceBase {
	private GeoEventDefinitionManager manager;
	public Messaging messaging;

	public LatLongProcessorService() throws PropertyException
	{
		definition=new LatLongProcessorDefinition();
	}

	@Override
	public GeoEventProcessor create() throws ComponentException {
		LatLongProcessor processor = new LatLongProcessor(definition);
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