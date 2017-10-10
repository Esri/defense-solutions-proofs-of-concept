package com.esri.geoevent.solutions.processor.eventjoiner;

/*
 * #%L
 * FieldGrouperProcessorService.java - fieldgrouper - Esri - 2013
 * org.codehaus.mojo-license-maven-plugin-1.5
 * $Id: update-file-header-config.apt.vm 17764 2012-12-12 10:22:04Z tchemit $
 * $HeadURL: https://svn.codehaus.org/mojo/tags/license-maven-plugin-1.5/src/site/apt/examples/update-file-header-config.apt.vm $
 * %%
 * Copyright (C) 2013 - 2014 Esri
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class EventJoinerProcessorService extends GeoEventProcessorServiceBase {
	private Messaging messaging;
	private GeoEventDefinitionManager manager;
	private static final Log LOG = LogFactory.getLog(EventJoinerProcessorService.class);
	public EventJoinerProcessorService() throws PropertyException {
		definition = new EventJoinerProcessorDefinition();
	}

	public GeoEventProcessor create() throws ComponentException {

		EventJoinerProcessor p = new EventJoinerProcessor(definition);
		p.setMessaging(messaging);
		p.setGeoEventDefinitionManager(manager);
		return p;
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
