

package com.esri.geoevent.solutions.processor.polygon2pt;

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

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;


public class Polygon2PtProcessor extends GeoEventProcessorBase {


	public Polygon2PtProcessor(GeoEventProcessorDefinition definition) throws ComponentException {
		super(definition);
		//spatial = s;
		geoEventMutator = true;
	}

	@Override
	public void afterPropertiesSet() {
		
	}

	@Override
	public synchronized void validate() throws ValidationException {
		// Validation Phase ...
		super.validate();
		
	}

	@Override
	public GeoEvent process(GeoEvent ge) throws Exception {
		
		MapGeometry mapGeo = ge.getGeometry();
		Geometry geo = mapGeo.getGeometry();
		if(geo.getType()!=Geometry.Type.Polygon)
			return null;
		Point center = GeometryUtility.CenterOfMass((Polygon)geo);
		MapGeometry outMapGeo = new MapGeometry(center, mapGeo.getSpatialReference());
		ge.setGeometry(outMapGeo);
		return ge;
	}

	@Override
	public void shutdown() {
		// Destruction Phase
		super.shutdown();
	}

	@Override
	public boolean isGeoEventMutator() {
		return true;
	}
	
	public void onServiceStart() {
		// Service Start Phase
	}
	
	public void onServiceStop() {
		// Service Stop Phase
	}

	
}
