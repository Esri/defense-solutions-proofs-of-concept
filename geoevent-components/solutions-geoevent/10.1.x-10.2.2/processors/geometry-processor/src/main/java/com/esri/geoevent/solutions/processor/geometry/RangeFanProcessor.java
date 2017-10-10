package com.esri.geoevent.solutions.processor.geometry;

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


import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;

import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import com.esri.ges.spatial.GeometryException;
import com.esri.ges.spatial.Spatial;
import com.esri.core.geometry.Geometry;


public class RangeFanProcessor extends GeoEventProcessorBase {
	private static final Log LOG = LogFactory.getLog(RangeFanProcessor.class);
	public Spatial spatial;
	public GeoEventDefinitionManager manager;
	//public TagManager tagMgr;
	private SpatialReference srIn;
	private SpatialReference srBuffer;
	private SpatialReference srOut;

	public RangeFanProcessor(GeoEventProcessorDefinition definition, Spatial s, GeoEventDefinitionManager m)
			throws ComponentException {
		super(definition);
		spatial = s;
		manager = m;
		//tagMgr=tm;
		geoEventMutator= true;
	}
	
	@Override
	public GeoEvent process(GeoEvent ge) throws Exception {
		double range;
		String rangeSource = properties.get("rangeSource").getValue().toString();
		if(rangeSource.equals("Constant"))
		{
			range = (Double) properties.get("range").getValue();
		}
		else
		{
			String eventfld = properties.get("rangeEvent").getValue().toString();
			String[] arr = eventfld.split(":");
			range = (Double)ge.getField(arr[1]);
		}
		String unit = properties.get("units").getValue().toString();
		
		double bearing;
		String bearingSource = properties.get("bearingSource").getValue().toString();
		if(bearingSource.equals("Constant"))
		{
			bearing = (Double) properties.get("bearing").getValue();
		}
		else
		{
			String eventfld = properties.get("bearingEvent").getValue().toString();
			String[] arr = eventfld.split(":");
			bearing = (Double)ge.getField(arr[1]);
		}
		
		double traversal;
		String traversalSource = properties.get("traversalSource").getValue().toString();
		if(traversalSource.equals("Constant"))
		{
			traversal = (Double) properties.get("traversal").getValue();
		}
		else
		{
			String eventfld = properties.get("traversalEvent").getValue().toString();
			String[] arr = eventfld.split(":");
			traversal = (Double)ge.getField(arr[1]);
		}
		
		
		int inwkid = (Integer) properties.get("wkidin").getValue();
		int outwkid = (Integer) properties.get("wkidout").getValue();
		int bufferwkid = (Integer) properties.get("wkidbuffer").getValue();
		srIn = SpatialReference.create(inwkid);
		srBuffer = SpatialReference.create(bufferwkid);
		srOut = SpatialReference.create(outwkid);
		
		com.esri.ges.spatial.Point eventGeo = (com.esri.ges.spatial.Point) ge.getGeometry();
		double x = eventGeo.getX();
		double y = eventGeo.getY();
		Geometry fan = constructRangeFan(x, y, range, unit, bearing, traversal);
		Geometry fanout = GeometryEngine.project(fan, srBuffer, srOut);
		String json = GeometryEngine.geometryToJson(srOut, fanout);
		com.esri.ges.spatial.Geometry outfan = spatial.fromJson(json);
		ge.setGeometry(outfan);
		return ge;
	}

	private Geometry constructRangeFan(double x, double y, double range,
			String unit, double bearing, double traversal)
			throws GeometryException {
		Polygon fan = new Polygon();
		Point center = new Point();
		center.setX(x);
		center.setY(y);
		// SpatialReference srIn = SpatialReference.create(wkidin);
		// SpatialReference srBuffer = SpatialReference.create(wkidbuffer);
		// SpatialReference srOut = SpatialReference.create(wkidout);
		Point centerProj = (Point) GeometryEngine.project(center, srIn,
				srBuffer);

		double centerX = centerProj.getX();
		double centerY = centerProj.getY();
		bearing = GeometryUtility.Geo2Arithmetic(bearing);
		double leftAngle = bearing - (traversal / 2);
		double rightAngle = bearing + (traversal / 2);
		int count = (int) Math.round(Math.abs(leftAngle - rightAngle));
		fan.startPath(centerProj);
		UnitConverter uc = new UnitConverter();
		range = uc.Convert(range, unit, srBuffer);
		for (int i = 0; i < count; ++i) {
			double d = Math.toRadians(leftAngle + i);
			double arcX = centerX + (range * Math.cos(d));
			double arcY = centerY + (range * Math.sin(d));
			Point arcPt = new Point(arcX, arcY);
			// arcPt = (Point) GeometryEngine.project(arcPt, srBuffer, srOut);
			fan.lineTo(arcPt);
		}
		fan.closeAllPaths();
		return fan;
	}
}
