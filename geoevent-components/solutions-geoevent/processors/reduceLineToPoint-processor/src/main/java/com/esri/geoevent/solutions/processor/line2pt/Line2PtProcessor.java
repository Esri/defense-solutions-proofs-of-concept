

package com.esri.geoevent.solutions.processor.line2pt;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.ges.core.ConfigurationException;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.DefaultGeoEventDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManagerException;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;


public class Line2PtProcessor extends GeoEventProcessorBase {

	private String pointType;
	private int processWkid;
	private String outDef;
	private List<FieldDefinition> fds;
	private Boolean createDef = false;
	private GeoEventDefinition ged;
	private GeoEventDefinitionManager manager;
	private Messaging messaging;
	private static final BundleLogger LOGGER = BundleLoggerFactory
			.getLogger(Line2PtProcessor.class);
	public Line2PtProcessor(GeoEventProcessorDefinition definition) throws ComponentException {
		super(definition);
		//spatial = s;
		geoEventMutator = true;
	}

	@Override
	public void afterPropertiesSet() {
		pointType = properties.get("pointType").getValueAsString();
		processWkid = (Integer)properties.get("wkid").getValue();
		outDef = properties.get("outdefname").getValueAsString();
		fds = new ArrayList<FieldDefinition>();
		try {
			//fds.add(new DefaultFieldDefinition("trackId", FieldType.String,
					//"TRACK_ID"));
			fds.add(new DefaultFieldDefinition("LocationTimeStamp", FieldType.Date, "TIMESTAMP"));
			
			//fds.add(new DefaultFieldDefinition("geometry", FieldType.Geometry));

			if ((ged = manager.searchGeoEventDefinition(outDef, definition.getUri().toString())) == null)
			{
				createDef = true;
			}
		}
		catch(ConfigurationException e)
		{
			LOGGER.error(e.getMessage());
		}
		
	}

	@Override
	public synchronized void validate() throws ValidationException {
		// Validation Phase ...
		super.validate();
		
	}

	@Override
	public GeoEvent process(GeoEvent ge) throws Exception {
		
		
		if (createDef) {
			createGeoEventDefinition(ge);
			createDef=false;
		}
		Date timeStart = (Date)ge.getField("TIME_START");
		Date timeEnd = (Date)ge.getField("TIME_END");
		if(timeStart== null)
			return null;
		if(timeEnd==null)
			return null;
		
		MapGeometry mapGeo = ge.getGeometry();
		Geometry geo = mapGeo.getGeometry();
		if(geo.getType()!=Geometry.Type.Polyline)
			return null;
		Polyline polyln = (Polyline)geo;
		Geometry outGeo = null;
		Date ts = null;
		if(pointType.equals("start"))
		{
			ts = (Date)ge.getField("TIME_START");
			outGeo = getStartPoint(polyln);
		}
		else if(pointType.equals("end"))
		{
			ts = (Date)ge.getField("TIME_END");
			outGeo = getEndPoint(polyln);
		}
		else if(pointType.equals("mid"))
		{
			outGeo = getMiddlePoint(mapGeo);
			long midTime = timeStart.getTime() + ((timeEnd.getTime() - timeStart.getTime())/2);
			ts = new Date(midTime);
		}
		MapGeometry outMapGeo = new MapGeometry(outGeo, mapGeo.getSpatialReference());
		GeoEvent msg = createLine2PtGeoevent(ge, outMapGeo, ts);
		
		return msg;
	}
	
	private GeoEvent createLine2PtGeoevent(GeoEvent event, MapGeometry outGeo, Date ts) throws MessagingException, FieldException
	{
		GeoEventCreator creator = messaging.createGeoEventCreator();
		GeoEvent msg = creator.create(outDef, definition.getUri().toString());
		for(FieldDefinition fd: event.getGeoEventDefinition().getFieldDefinitions())
		{
			if(fd.getTags().contains("GEOMETRY"))
			{
				msg.setGeometry(outGeo);
			}
			else
			{
				msg.setField(fd.getName(), event.getField(fd.getName()));
			}
			msg.setField("TIMESTAMP", ts);
	
		}
		return msg;
	}
	
	private void createGeoEventDefinition(GeoEvent event)
	{
		
		GeoEventDefinition eventDef = event.getGeoEventDefinition();
		try {
			ged = eventDef.augment(fds);
		} catch (ConfigurationException e) {
			LOGGER.error(e.getLocalizedMessage());
		}
		ged.setName(outDef);
		ged.setOwner(definition.getUri().toString());
		try {
			manager.addGeoEventDefinition(ged);
		} catch (GeoEventDefinitionManagerException e) {
			LOGGER.error(e.getMessage());
		}
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

	private Point getStartPoint(Polyline polyln)
	{
		int startIndex = polyln.getPathStart(0);
		return polyln.getPoint(startIndex);
	}
	
	private Point getEndPoint(Polyline polyln)
	{
		try {
			int pathCount = polyln.getPathCount();
			int endIndex = polyln.getPathEnd(pathCount - 1);
			return polyln.getPoint(endIndex-1);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw (e);
		}
	}
	
	private Geometry getMiddlePoint(MapGeometry mg) {
		try {
			Unit unit = LinearUnit.create(9001);
			LinearUnit lu = (LinearUnit) unit;
			MapGeometry mapgeo = null;
			Boolean inputProjected = true;
			SpatialReference outSr = null;
			SpatialReference inSr = mg.getSpatialReference();
			if (processWkid != mg.getSpatialReference().getID()) {
				outSr = SpatialReference.create(processWkid);
				Geometry g = GeometryEngine.project(mg.getGeometry(), inSr,
						outSr);
				mapgeo = new MapGeometry(g, outSr);
				inputProjected = false;
			} else {
				mapgeo = mg;
				outSr = mapgeo.getSpatialReference();
			}
			Double midptLen = GeometryEngine.geodesicLength(
					mapgeo.getGeometry(), mapgeo.getSpatialReference(), lu) / 2;
			Point midPt = null;
			Polyline polyln = (Polyline) mapgeo.getGeometry();
			int pathCount = polyln.getPathCount();
			Double currentLen = 0.0;
			for (int i = 0; i < pathCount; ++i) {
				int start = polyln.getPathStart(i);
				int end = polyln.getPathEnd(i);
				int hops = end - start;
				for (int j = start + 1; j < start + hops; ++j) {
					Point startPt = polyln.getPoint(j - 1);
					Point endPt = polyln.getPoint(j);
					Double distance = GeometryEngine.distance(startPt, endPt,
							outSr);
					// currentLn + distance
					if (currentLen + distance >= midptLen) {
						currentLen += distance;
						Double distanceOnSeg = midptLen-(currentLen-distance);
						midPt = findPtOnSegment(startPt, endPt, distanceOnSeg);
						Geometry outGeo = null;
						if (!inputProjected) {
							outGeo = GeometryEngine.project(midPt, outSr, inSr);
						} else {
							outGeo = midPt;
						}
						return outGeo;

					} else {
						currentLen += distance;
					}
				}
				
			}
			return null;
		} catch (Exception e) {
			LOGGER.error(e.getStackTrace().toString());
			LOGGER.error(e.getMessage());
			throw (e);
		}
	}
	
	private Point findPtOnSegment(Point segStart, Point segEnd, Double d)
	{
		Point pt = null;
		Double x1, y1, x2, y2;
		x1 = segStart.getX();
		y1 = segStart.getY();
		x2 = segEnd.getX();
		y2 = segEnd.getY();
		
		Double diffXsquare = Math.pow((x2-x1), 2);
		Double diffYsquare = Math.pow((y2-y1), 2);
		Double x = x1 + d*(x2-x1)/Math.sqrt(diffXsquare+diffYsquare);
		Double y = y1 + d*(y2-y1)/Math.sqrt(diffXsquare + diffYsquare);
		
		pt = new Point(x,y);
		return pt;
	}

	private Point findPtOnSegment(Point segStart, Point segEnd, Double h, Double h2)
	{
		Point pt = null;
		Double x1, y1, x2, y2;
		x1 = segStart.getX();
		y1 = segStart.getY();
		x2 = segEnd.getX();
		y2 = segEnd.getY();
		Double a = null;
		Double o = null;
		Double cosTheta = null;
		Double sinTheta = null;
		Double xMultiplier = null;
		Double yMultiplier = null;
		Double xlen = null;
		Double ylen = null;
		if(x1 > x2 && y1 > y2)
		{
			xMultiplier = 1.0;
			yMultiplier = -1.0;
			a = Math.abs(y2 - y1);
			o = Math.abs(x2 - x1);
			cosTheta = a/h;
			sinTheta = o/h;
			xlen = h2*sinTheta*xMultiplier;
			ylen=h2*cosTheta*yMultiplier;
		}
		else if(x1 < x2 && y1 < y2)
		{
			xMultiplier = -1.0;
			yMultiplier = 1.0;
			a = Math.abs(x2 - x1);
			o = Math.abs(y2 - y1);
			cosTheta = a/h;
			sinTheta = o/h;
			xlen = h2*cosTheta*xMultiplier;
			ylen=h2*sinTheta*yMultiplier;
			
		}
		else if(x1 < x2 && y1 > y2)
		{
			xMultiplier = 1.0;
			yMultiplier = -1.0;
			a = Math.abs(y2 - y1);
			o = Math.abs(x2 - x1);
			cosTheta = a/h;
			sinTheta = o/h;
			xlen = h2*sinTheta*xMultiplier;
			ylen=h2*cosTheta*yMultiplier;
		}
		else if(x1 > x2 && y1 < y2)
		{
			xMultiplier = -1.0;
			yMultiplier = 1.0;
			a = Math.abs(x2 - x1);
			o = Math.abs(y2 - y1);
			cosTheta = a/h;
			sinTheta = o/h;
			xlen = h2*cosTheta*xMultiplier;
			ylen=h2*sinTheta*yMultiplier;
			
		}
		Double newX= segStart.getX() + xlen;
		Double newY =  segStart.getY() + ylen;
		pt = new Point(newX, newY);
		return pt;
	}
	
	public void setManager(GeoEventDefinitionManager manager)
	{
		this.manager = manager;
	}
	
	public void setMessaging(Messaging messaging)
	{
		this.messaging = messaging;
	}
	
}
