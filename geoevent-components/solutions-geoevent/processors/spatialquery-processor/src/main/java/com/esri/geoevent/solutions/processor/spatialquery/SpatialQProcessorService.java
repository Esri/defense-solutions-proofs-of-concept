package com.esri.geoevent.solutions.processor.spatialquery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.http.GeoEventHttpClientService;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class SpatialQProcessorService extends GeoEventProcessorServiceBase
{
	public GeoEventDefinitionManager manager;
	public ArcGISServerConnectionManager connectionManager;
	public Messaging messaging;
	public GeoEventHttpClientService httpClientService;
	private static final Log LOG = LogFactory
			.getLog(SpatialQProcessorService.class);

	public SpatialQProcessorService()
	{
		definition = new SpatialQProcessorDefinition();
	}
	
	

	@Override
	public GeoEventProcessor create() throws ComponentException
	{
		SpatialQProcessor processor = new SpatialQProcessor(definition);
		processor.setMessaging(messaging);
		processor.setManager(manager);
		processor.setConnectionManager(connectionManager);
		processor.setHttpClentService(httpClientService);
		return processor;
	}

	public void setManager(GeoEventDefinitionManager m)
	{
		manager = m;
	}
	
	public void setConnectionManager(ArcGISServerConnectionManager cm)
	{
		connectionManager = cm;
	}
	
	public void setMessaging(Messaging m)
	{
		messaging = m;
	}
	
	public void setHttpClientService(GeoEventHttpClientService service)
	{
		httpClientService = service;
	}
}
