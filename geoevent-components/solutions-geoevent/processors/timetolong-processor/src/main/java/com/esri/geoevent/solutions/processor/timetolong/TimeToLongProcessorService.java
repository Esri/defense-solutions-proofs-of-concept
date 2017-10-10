package com.esri.geoevent.solutions.processor.timetolong;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class TimeToLongProcessorService extends GeoEventProcessorServiceBase{
	private GeoEventDefinitionManager manager;
	public Messaging messaging;
	
	public TimeToLongProcessorService() throws PropertyException
	{
		definition=new TimeToLongDefinition();
	}
	@Override
	public GeoEventProcessor create() throws ComponentException {
		TimeToLongProcessor t2lproc = new TimeToLongProcessor(definition);
		t2lproc.setManager(manager);
		t2lproc.setMessaging(messaging);
		return t2lproc;
	}
	
	public void setMessaging(Messaging messaging)
	{
		this.messaging=messaging;
	}
	
	public void setManager(GeoEventDefinitionManager manager)
	{
		this.manager = manager;
	}

}
