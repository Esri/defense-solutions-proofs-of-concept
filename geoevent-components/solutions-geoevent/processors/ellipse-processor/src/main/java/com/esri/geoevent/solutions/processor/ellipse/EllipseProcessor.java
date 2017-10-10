package com.esri.geoevent.solutions.processor.ellipse;

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


import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

public class EllipseProcessor extends GeoEventProcessorBase {
	private static HashMap<String, Integer> wkidLookup = new HashMap<String, Integer>();
	private static final Log LOG = LogFactory.getLog(EllipseProcessor.class);
	private Double majorAxisRadius = Double.NaN;
	private String majorAxisField;
	private Double minorAxisRadius = Double.NaN;
	private String minorAxisField;
	private Double rotation = Double.NaN;
	private String rotationField;
	private String units;
	private Integer inwkid;
	private Integer outwkid;
	private Integer procwkid;
	private String majAxisSource;
	private String minAxisSource;
	private String rotSource;
	
	public EllipseProcessor(GeoEventProcessorDefinition definition)
			throws ComponentException {
		super(definition);
		wkidLookup.put("METER", 9001);
		wkidLookup.put("KILOMETER", 9036);
		wkidLookup.put("FOOT_US", 9003);
		wkidLookup.put("MILE_US", 9035);
		wkidLookup.put("NAUTICAL_MILE", 9030);
		geoEventMutator= true;
	}
	
	@Override
	public void afterPropertiesSet()
	{
		majAxisSource = properties.get("majorAxisSource").getValue().toString();
		if(majAxisSource.equals("Constant"))
		{
			majorAxisRadius = (Double) properties.get("majorAxisRadius").getValue();
		}
		else
		{
			majorAxisField =properties.get("majorAxisEvent").getValueAsString();
		}
		minAxisSource = properties.get("minorAxisSource").getValue().toString();
		if(minAxisSource.equals("Constant"))
		{
			minorAxisRadius = (Double) properties.get("minorAxisRadius").getValue();
		}
		else
		{
			minorAxisField = properties.get("minorAxisEvent").getValueAsString();
		}
		
		rotSource = properties.get("rotationSource").getValue().toString();
		if(rotSource.equals("Constant"))
		{
			rotation = (Double) properties.get("rotation").getValue();
		}
		else
		{
			rotationField=properties.get("rotationEvent").getValueAsString();
		}
		units = properties.get("units").getValue().toString();
		//inwkid = (Integer) properties.get("wkidin").getValue();
		outwkid = (Integer) properties.get("wkidout").getValue();
		procwkid = (Integer) properties.get("wkidbuffer").getValue();
	}

	@Override
	public GeoEvent process(GeoEvent ge) throws Exception {
		
		if(!ge.getGeoEventDefinition().getTagNames().contains("GEOMETRY"))
		{
			return null;
		}
		inwkid = ge.getGeometry().getSpatialReference().getID();
		if(majAxisSource.equals("Event"))
		{
			majorAxisRadius = (Double)ge.getField(majorAxisField);
		}
		if(minAxisSource.equals("Event"))
		{
			minorAxisRadius = (Double)ge.getField(minorAxisField);
		}
		if(rotSource.equals("Event"))
		{
			rotation=(Double)ge.getField(rotationField);
		}
		
		MapGeometry mapGeo = ge.getGeometry();
		Geometry geo = mapGeo.getGeometry();
		if(!(geo instanceof Point))
		{
			return null;
		}
		Point eventGeo = (Point)geo;
		double x = eventGeo.getX();
		double y = eventGeo.getY();
		double rdeg = GeometryUtility.Geo2Arithmetic(rotation);
		double r = Math.toRadians(rdeg);
		MapGeometry ellipse = constructEllipse(x, y, majorAxisRadius, minorAxisRadius, r, inwkid, procwkid, outwkid);
		ge.setGeometry(ellipse);
		return ge;
	}
	
	private MapGeometry constructEllipse(double x, double y, double majorAxis, double minorAxis, double rotation, int wkidin, int wkidbuffer, int wkidout)
	{
		Point center = new Point();
		center.setX(x);
		center.setY(y);
		SpatialReference srIn = SpatialReference.create(wkidin);
		SpatialReference srBuffer = SpatialReference.create(wkidbuffer);
		SpatialReference srOut = SpatialReference.create(wkidout);
		UnitConverter uc = new UnitConverter();
		majorAxis = uc.Convert(majorAxis, units, srBuffer);
		minorAxis = uc.Convert(minorAxis, units, srBuffer);
		Point centerProj = (Point) GeometryEngine.project(center, srIn, srBuffer);
		GeometryUtility geoutil = new GeometryUtility();
		Polygon ellipse = geoutil.GenerateEllipse(centerProj, majorAxis, minorAxis, rotation);
		Geometry ellipseOut = GeometryEngine.project(ellipse, srBuffer, srOut);
		MapGeometry mapGeo = new MapGeometry(ellipseOut, srOut);
		return mapGeo;
	}

}
