

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

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

//import com.esri.ges.spatial.Spatial;

public class BufferProcessor extends GeoEventProcessorBase {
	//Spatial spatial;
	GeoEventDefinitionManager manager;
	private Double radius;
	private Integer bufferwkid;
	private Integer outwkid;
	private String units;
	String bufferEventFld;

	public BufferProcessor(GeoEventProcessorDefinition definition,
			GeoEventDefinitionManager m) throws ComponentException {
		super(definition);
		//spatial = s;
		geoEventMutator = true;
		manager = m;
	}

	@Override
	public void afterPropertiesSet() {
		// Initialization Phase ...
		String radiusSource = properties.get("radiusSource").getValue()
				.toString();
		if (radiusSource.equals("Constant")) {
			radius = (Double) properties.get("radius").getValue();
		} else {
			bufferEventFld = properties.get("radiusEvent").getValue()
					.toString();
		}

		units = properties.get("units").getValue().toString();
		outwkid = (Integer) properties.get("wkidout").getValue();
		bufferwkid = (Integer) properties.get("wkidbuffer").getValue();
	}

	@Override
	public synchronized void validate() throws ValidationException {
		// Validation Phase ...
		super.validate();
		if (bufferEventFld == null) {
			if (radius == null)
				throw new ValidationException("Radius is not specified.");
			if (radius <= 0)
				throw new ValidationException("Radius must be greater than 0.");
		} else if (bufferEventFld.trim().equals("")) {
			if (radius == null)
				throw new ValidationException("Radius is not specified.");
			if (radius <= 0)
				throw new ValidationException("Radius must be greater than 0.");
		}
	}

	@Override
	public GeoEvent process(GeoEvent ge) throws Exception {
		// Operation phase...
		if (radius == null) {
			radius = (Double) ge.getField(bufferEventFld);
			if (radius == null) {
				Exception e = new Exception("Radius is not defined in geoevent");
				throw (e);
			}
		}
		MapGeometry mapGeo = ge.getGeometry();
		Point eventGeo = (Point) mapGeo.getGeometry();
		double x = eventGeo.getX();
		double y = eventGeo.getY();
		int inwkid = mapGeo.getSpatialReference().getID();
		//int inwkid = eventGeo.getSpatialReference().getWkid();
		Geometry buffer = constructBuffer(x, y, radius,
				units, inwkid, bufferwkid, outwkid);
		
		SpatialReference srOut = SpatialReference.create(outwkid);
		MapGeometry outMapGeo = new MapGeometry(buffer, srOut);
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

	private Geometry constructBuffer(double x, double y,
			double radius, String units, int wkidin, int wkidbuffer, int wkidout)
			 {
		Point center = new Point();
		center.setX(x);
		center.setY(y);
		SpatialReference srIn = SpatialReference.create(wkidin);
		SpatialReference srBuffer = SpatialReference.create(wkidbuffer);
		SpatialReference srOut = SpatialReference.create(wkidout);
		UnitConverter uc = new UnitConverter();
		String c_name = uc.findConnonicalName(units);
		int unitout = uc.findWkid(c_name);
		Unit u = LinearUnit.create(unitout);
		Point centerProj = (Point) GeometryEngine.project(center, srIn,
				srBuffer);
		Geometry buffer = GeometryEngine
				.buffer(centerProj, srBuffer, radius, u);
		Geometry bufferout = GeometryEngine.project(buffer, srBuffer, srOut);
		//String json = GeometryEngine.geometryToJson(srOut, bufferout);
		return bufferout;

	}
}
