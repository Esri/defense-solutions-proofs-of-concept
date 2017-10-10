package com.esri.geoevent.solutions.processor.trackidle;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class TrackIdleProcessorService extends GeoEventProcessorServiceBase
{
  private Messaging messaging;
  private GeoEventDefinitionManager manager;
  public TrackIdleProcessorService()
  {
    definition = new TrackIdleProcessorDefinition();
  }

  @Override
  public GeoEventProcessor create() throws ComponentException
  {
    TrackIdleProcessor detector = new TrackIdleProcessor(definition);
    detector.setMessaging(messaging);
    detector.setManager(manager);
    return detector;
  }

  public void setMessaging(Messaging messaging)
  {
    this.messaging = messaging;
  }
  
  public void setManager(GeoEventDefinitionManager manager)
  {
	  this.manager = manager;
  }
}