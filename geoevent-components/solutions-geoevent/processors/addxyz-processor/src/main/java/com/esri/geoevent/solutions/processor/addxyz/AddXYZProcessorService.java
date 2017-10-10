package com.esri.geoevent.solutions.processor.addxyz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class AddXYZProcessorService extends GeoEventProcessorServiceBase {

	GeoEventDefinitionManager manager;
	Messaging messaging;
	private static final Log LOG = LogFactory
			.getLog(AddXYZProcessorService.class);
	public AddXYZProcessorService() throws PropertyException {
		definition = new AddXYZProcessorDefinition();
	}

	public GeoEventProcessor create() throws ComponentException {
		// TODO Auto-generated method stub
		return new AddXYZProcessor(definition, manager, messaging);
	}
	
	
	public void setManager(GeoEventDefinitionManager m)
	{
		manager = m;
	}
	
	public void setMessaging(Messaging m)
	{
		messaging = m;
	}

}
