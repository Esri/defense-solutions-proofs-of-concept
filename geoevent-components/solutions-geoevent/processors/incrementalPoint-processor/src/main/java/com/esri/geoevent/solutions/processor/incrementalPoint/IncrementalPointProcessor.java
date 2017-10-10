

package com.esri.geoevent.solutions.processor.incrementalPoint;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
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
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManagerException;
import com.esri.ges.messaging.EventDestination;
import com.esri.ges.messaging.EventUpdatable;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.GeoEventProducer;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;


public class IncrementalPointProcessor extends GeoEventProcessorBase implements
GeoEventProducer, EventUpdatable {
	class IncrementPoint
	{
		private Point point;
		Integer nextVertexIndex;
		IncrementPoint(Point p, Integer i)
		{
			this.point = p;
			this.nextVertexIndex=i;
		}
		
		public Point getPoint()
		{
			return this.point;
		}
		
		public Integer getNextVertexIndex()
		{
			return this.nextVertexIndex;
		}
	}
	
	private int processWkid;
	private String outDef;
	private List<FieldDefinition> fds;
	private Boolean createDef = false;
	private GeoEventDefinition ged;
	private GeoEventCreator geoEventCreator;
	private GeoEventDefinitionManager manager;
	private Messaging messaging;
	private GeoEventProducer geoEventProducer;
	private SpatialReference processSr;
	private SpatialReference outSr;
	private String intervalType;
	private Double distInterval;
	private long timeInterval;
	private Boolean usingTime;
	private Boolean usingVertex;
	private static final BundleLogger LOGGER = BundleLoggerFactory
			.getLogger(IncrementalPointProcessor.class);
	public IncrementalPointProcessor(GeoEventProcessorDefinition definition) throws ComponentException {
		super(definition);
		//spatial = s;
		geoEventMutator = true;
	}
	@Override
	public void send(GeoEvent geoEvent) throws MessagingException {
		if (geoEventProducer != null && geoEvent != null)
			geoEventProducer.send(geoEvent);
	}
	@Override
	public void setId(String id) {
		super.setId(id);
		geoEventProducer = messaging
				.createGeoEventProducer(new EventDestination(id + ":event"));
	}
	@Override
	public void afterPropertiesSet() {
		intervalType = properties.get("intervalType").getValueAsString();
		if(intervalType.equals("time"))
		{
			timeInterval = (long)properties.get("timeinterval").getValue();
			usingTime = true;
			usingVertex=false;
		}
		else if(intervalType.equals("distance"))
		{
			distInterval = (Double)properties.get("distanceinterval").getValue();
			usingTime = false;
			usingVertex=false;
		}
		else if (intervalType.equals("vertex"))
		{
			usingTime=false;
			usingVertex=true;
		}

		processWkid = (Integer)properties.get("wkid").getValue();
		outDef = properties.get("outdefname").getValueAsString();
		fds = new ArrayList<FieldDefinition>();
		processSr = SpatialReference.create(processWkid);
		try {
			fds.add(new DefaultFieldDefinition("locationtimestamp", FieldType.Date, "TIMESTAMP"));
			fds.add(new DefaultFieldDefinition("timefromstart", FieldType.Double, "TIME_FROM_START"));
			fds.add(new DefaultFieldDefinition("distanceonline", FieldType.Double, "DISTANCE_ON_LINE"));
			Collection<GeoEventDefinition>eventDefs = manager.searchGeoEventDefinitionByName(outDef);
			Iterator<GeoEventDefinition>eventDefIt = eventDefs.iterator();
			while(eventDefIt.hasNext())
			{
				GeoEventDefinition currentDef = eventDefIt.next();
				manager.deleteGeoEventDefinition(currentDef.getGuid());
			}
			createDef = true;
		}
		
		catch(ConfigurationException e)
		{
			LOGGER.error(e.getMessage());
		} catch (GeoEventDefinitionManagerException e) {
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
		if(ge.getGeometry().getGeometry().getType()!=Geometry.Type.Polyline)
			return null;
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
		long start = timeStart.getTime();
		long end = timeEnd.getTime();
		if(start >= end)
			return null;
		MapGeometry mg = ge.getGeometry();

		
		MapGeometry mapGeo = null;
		Boolean projected=false;
		SpatialReference inSr = mg.getSpatialReference();
		if (processWkid != mg.getSpatialReference().getID()) {
			projected=true;
			processSr = SpatialReference.create(processWkid);
			Geometry g = GeometryEngine.project(mg.getGeometry(), inSr,
					processSr);
			mapGeo = new MapGeometry(g, processSr);
			outSr = inSr;
		} else {
			mapGeo = mg;
			outSr = mapGeo.getSpatialReference();
		}
		
		Geometry geo = mapGeo.getGeometry();
		Polyline polyln = (Polyline)geo;
		Point startPt = polyln.getPoint(0);
		Unit unit = LinearUnit.create(9001);
		LinearUnit lu = (LinearUnit) unit;
		double distTotal = GeometryEngine.geodesicLength(polyln, processSr, lu);
		if(usingVertex)
		{
			processVertices(ge, polyln, distTotal, start, end, lu, projected);
		}
		else
		{
			processIncrements(ge, polyln, startPt, distTotal, start, end, projected);
		}
		/*int numHops = 0;
		long timeTotal = end - start;
		if(usingTime)
		{
			distInterval = distTotal/(timeTotal/timeInterval);
			numHops = (int) Math.floor(distTotal/distInterval);
		}
		else
		{
			numHops = (int) Math.floor(distTotal/distInterval);
			timeInterval = timeTotal/numHops;
		}
		
		Integer ptIndex = 0;
		GeoEvent msg = null;
		//Geometry outGeo = null;
		//Geometry projGeo = null;
		for(int i = 0; i < numHops; ++i)
		{
			IncrementPoint ip = this.getNextPoint(polyln, startPt, ptIndex, distInterval);
			Geometry outGeo = null;
			Geometry projGeo = ip.getPoint();
			if(projected)
			{
				outGeo = GeometryEngine.project(projGeo, processSr, outSr);
			}
			else
			{
				outGeo=projGeo;
			}
			MapGeometry outMapGeo = new MapGeometry(outGeo, mapGeo.getSpatialReference());
			msg = createIncrementalPointGeoevent(ge, outMapGeo, timeStart, i);
			send(msg);
			startPt = (Point)projGeo;
			ptIndex = ip.getNextVertexIndex();
		}*/
		
		return null;
	}
	
	private void processIncrements(GeoEvent ge, Polyline polyln, Point startPt, double distTotal, long start, long end, Boolean projected) throws MessagingException, FieldException
	{
		int numHops = 0;
		long timeTotal = end - start;
		Date timeStart = new Date(start);
		if(usingTime)
		{
			distInterval = distTotal/(timeTotal/timeInterval);
			numHops = (int) Math.floor(distTotal/distInterval);
		}
		else
		{
			numHops = (int) Math.floor(distTotal/distInterval);
			timeInterval = timeTotal/numHops;
		}
		
		Integer ptIndex = 0;
		GeoEvent msg = null;
		//Geometry outGeo = null;
		//Geometry projGeo = null;
		for(int i = 0; i < numHops; ++i)
		{
			IncrementPoint ip = this.getNextPoint(polyln, startPt, ptIndex, distInterval);
			Geometry outGeo = null;
			Geometry projGeo = ip.getPoint();
			if(projected)
			{
				outGeo = GeometryEngine.project(projGeo, processSr, outSr);
			}
			else
			{
				outGeo=projGeo;
			}
			MapGeometry outMapGeo = new MapGeometry(outGeo, outSr);
			msg = createIncrementalPointGeoevent(ge, outMapGeo, timeStart, i);
			send(msg);
			startPt = (Point)projGeo;
			ptIndex = ip.getNextVertexIndex();
		}
	}
	
	private void processVertices(GeoEvent ge, Polyline polyln, double distTotal, long start, long end, LinearUnit lu, Boolean projected) throws MessagingException, FieldException
	{
		int count = polyln.getPointCount();
		double currentDist = 0;
		long currentTime = start;
		long totalTime = end - start;
		Geometry outGeo = null;
		Point projGeo = null;
		Point lastPoint = null;
		for(int i = 0; i < count; ++i)
		{
			projGeo = polyln.getPoint(i);
			
			if(i!=0)
			{
				Polyline seg = new Polyline();
				seg.startPath(lastPoint);
				seg.lineTo(projGeo);
				double segDist = GeometryEngine.geodesicLength(seg, processSr, lu);
				currentDist += segDist;
				double percent = currentDist/distTotal;
				currentTime = (long) Math.floor((start + (totalTime*percent)));
				
			}
			if(projected)
			{
				outGeo = GeometryEngine.project(projGeo, processSr, outSr);
			}
			else
			{
				outGeo=projGeo;
			}
			MapGeometry outMapGeo = new MapGeometry(outGeo, outSr);
			double minutesFromStart = (currentTime - start)/60000;
			GeoEvent msg = createVertexGeoevent(ge, outMapGeo, currentDist, currentTime, minutesFromStart, i);
			send(msg);
			lastPoint = projGeo;
		}
	}
	
	private GeoEvent createVertexGeoevent(GeoEvent event, MapGeometry outGeo, double dist, long time, double timeFromStartMinutes, Integer increment) throws MessagingException, FieldException
	{
		Date ts = new Date(time);
		Double distOnLine = dist;
		GeoEvent msg = geoEventCreator.create(outDef, definition.getUri().toString());
		for(FieldDefinition fd: event.getGeoEventDefinition().getFieldDefinitions())
		{
			
			if(fd.getTags().contains("GEOMETRY"))
			{
				msg.setGeometry(outGeo);
			}
			else if(fd.getTags().contains("TRACK_ID"))
			{
				String trackid = event.getTrackId() + "_" + increment.toString();
				msg.setField("TRACK_ID", trackid);
			}
			else
			{
				msg.setField(fd.getName(), event.getField(fd.getName()));
			}
		}
		msg.setField("TIMESTAMP", ts);
		msg.setField("TIME_FROM_START", timeFromStartMinutes);
		msg.setField("DISTANCE_ON_LINE", distOnLine);
		msg.setProperty(GeoEventPropertyName.TYPE, "event");
		msg.setProperty(GeoEventPropertyName.OWNER_ID, getId());
		msg.setProperty(GeoEventPropertyName.OWNER_URI,
				definition.getUri());
		return msg;
	}
	
	private GeoEvent createIncrementalPointGeoevent(GeoEvent event, MapGeometry outGeo, Date timestart, Integer increment) throws MessagingException, FieldException
	{
		long multiplier = increment+1;
		long timeFromStart = timeInterval*multiplier;
		long incrementTime = timestart.getTime() + (timeFromStart);
		double timeFromStartMinutes = timeFromStart/60000.0;
		Double distOnLine = distInterval*multiplier;
		Date ts = new Date(incrementTime);
		GeoEvent msg = geoEventCreator.create(outDef, definition.getUri().toString());
		for(FieldDefinition fd: event.getGeoEventDefinition().getFieldDefinitions())
		{
			
			if(fd.getTags().contains("GEOMETRY"))
			{
				msg.setGeometry(outGeo);
			}
			else if(fd.getTags().contains("TRACK_ID"))
			{
				String trackid = event.getTrackId() + "_" + increment.toString();
				msg.setField("TRACK_ID", trackid);
			}
			else
			{
				msg.setField(fd.getName(), event.getField(fd.getName()));
			}
		}
		msg.setField("TIMESTAMP", ts);
		msg.setField("TIME_FROM_START", timeFromStartMinutes);
		msg.setField("DISTANCE_ON_LINE", distOnLine);
		msg.setProperty(GeoEventPropertyName.TYPE, "event");
		msg.setProperty(GeoEventPropertyName.OWNER_ID, getId());
		msg.setProperty(GeoEventPropertyName.OWNER_URI,
				definition.getUri());
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
	
	@Override
	public EventDestination getEventDestination() {
		return (geoEventProducer != null) ? geoEventProducer
				.getEventDestination() : null;
	}

	@Override
	public List<EventDestination> getEventDestinations() {
		return (geoEventProducer != null) ? Arrays.asList(geoEventProducer
				.getEventDestination()) : new ArrayList<EventDestination>();
	}

	@Override
	public void disconnect() {
		if (geoEventProducer != null)
			geoEventProducer.disconnect();
	}

	@Override
	public boolean isConnected() {
		return (geoEventProducer != null) ? geoEventProducer.isConnected()
				: false;
	}

	@Override
	public String getStatusDetails() {
		return (geoEventProducer != null) ? geoEventProducer.getStatusDetails()
				: "";
	}

	@Override
	public void setup() throws MessagingException {
		;
	}

	@Override
	public void init() throws MessagingException {
		;
	}

	@Override
	public void update(Observable o, Object arg) {
		;
	}
	@Override
	public void onServiceStart() {
		// Service Start Phase
	}
	@Override
	public void onServiceStop() {
		// Service Stop Phase
	}
	
	private IncrementPoint getNextPoint(Polyline polyln, Point startPt, Integer i, Double dist)
	{
		Point startVertex = polyln.getPoint(i);
		Double currentDist = GeometryEngine.distance(startPt, startVertex, processSr);
		Point segStart = null;
		Point segEnd = null;
		Boolean multipleVertices = true;
		if(currentDist > dist)
		{
			segStart = startPt;
			segEnd = startVertex;
			multipleVertices = false;
		}
		while(currentDist < dist)
		{
			Point start = polyln.getPoint(i);
			Point end = polyln.getPoint(i+1);
			currentDist += GeometryEngine.distance(start, end, processSr);
			++i;
		}
		if(multipleVertices)
		{
			segStart = polyln.getPoint(i-1);
			segEnd = polyln.getPoint(i);
		}
		Double segLen = GeometryEngine.distance(segStart, segEnd, processSr);
		Double distOver = currentDist - dist;
		Double distOnSeg = segLen - distOver;
		Point p = findPtOnSegment(segStart, segEnd, distOnSeg);
		IncrementPoint ip = new IncrementPoint(p, i);
		return ip;
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
	
	public void setManager(GeoEventDefinitionManager manager)
	{
		this.manager = manager;
	}
	
	public void setMessaging(Messaging messaging)
	{
		this.messaging = messaging;
		this.geoEventCreator = messaging.createGeoEventCreator();
	}
	
}


