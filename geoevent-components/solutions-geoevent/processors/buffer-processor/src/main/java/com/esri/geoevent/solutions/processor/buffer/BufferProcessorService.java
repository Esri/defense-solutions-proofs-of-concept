package com.esri.geoevent.solutions.processor.buffer;

/*
 * #%L
 * Esri :: AGES :: Solutions :: Processor :: Geometry
 * $Id:$
 * $HeadURL:$
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
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class BufferProcessorService extends GeoEventProcessorServiceBase {
	private static final Log LOG = LogFactory
			.getLog(BufferProcessorService.class);
	public GeoEventDefinitionManager manager;
	public BufferProcessorService() throws PropertyException {
		definition = new BufferProcessorDefinition();
	}

	@Override
	public GeoEventProcessor create() {
		try {
			return new BufferProcessor(definition, manager);
		} catch (ComponentException e) {
			LOG.error("Geometry processor");
			LOG.error(e.getMessage());
			LOG.error(e.getStackTrace());
			return null;
		} catch (Exception e) {
			LOG.error("Geometry processor");
			LOG.error(e.getMessage());
			LOG.error(e.getStackTrace());
			return null;
		}
	}
	
	
	public void setManager(GeoEventDefinitionManager m)
	{
		manager = m;
	}
	
	public void start() {
		try {
			BufferProcessorDefinition bDef = (BufferProcessorDefinition) definition;
			bDef.setManager(manager);
		} catch (Exception e) {
			LOG.error("Geometry processor");
			LOG.error(e.getMessage());
			LOG.error(e.getStackTrace());
		}
	}

}
