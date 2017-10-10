package com.esri.geoevent.solutions.processor.bearing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Segment;
import com.esri.core.geometry.SpatialReference;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.Tag;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;


public class BearingProcessor extends GeoEventProcessorBase {
	Messaging messaging;
	GeoEventDefinitionManager manager;
	private String oGeoFldName;
	private String dGeoFldName;
	private String oXFldName;
	private String oYFldName;
	private String dXFldName;
	private String dYFldName;
	private Boolean oUseGeo = false;
	private Boolean dUseGeo = false;
	private String bfldName;
	private String gedName;
	private Boolean createGeo = false;
	private Integer wkid;
	private static final Log LOG = LogFactory
			.getLog(BearingProcessor.class);
	public BearingProcessor(GeoEventProcessorDefinition definition)
			throws ComponentException {
		super(definition);
	}
	

	public void setMessaging(Messaging m)
	{
		messaging = m;
	}
	
	public void setGDManager(GeoEventDefinitionManager m)
	{
		manager = m;
	}
	
	@Override
	public GeoEvent process(GeoEvent ge) throws Exception {
		Double ox = null;
		Double oy = null;
		Double dx = null;
		Double dy = null;
		if(oUseGeo)
		{
			MapGeometry mg = (MapGeometry)ge.getField(oGeoFldName);
			Geometry o = mg.getGeometry();
			if(o==null)
			{
				Exception e = new Exception();
				LOG.error("error retrieving origin geometry");
				throw(e);
			}
			if(o instanceof Point)
			{
				ox = ((Point)o).getX();
				oy = ((Point)o).getY();
			}
			else
			{
				Exception e = new IOException();
				LOG.error("Invalid Origin geometry");
				throw(e);
			}
		}
		else
		{
			ox = (Double)ge.getField(oXFldName);
			oy = (Double)ge.getField(oYFldName);
			if(ox == null){
				Exception e = new Exception();
				LOG.error("error retrieving origin's x value");
				throw(e);
			}
			if(oy == null)
			{
				Exception e = new Exception();
				LOG.error("error retrieving origin's y value");
				throw(e);
			}			
		}
		if(dUseGeo)
		{
			MapGeometry mg = (MapGeometry)ge.getField(dGeoFldName);
			Geometry d=mg.getGeometry();
			if(d==null)
			{
				Exception e = new Exception();
				LOG.error("error retrieving destination geometry");
				throw(e);
			}
			if(d instanceof Point)
			{
				dx = ((Point)d).getX();
				dy = ((Point)d).getY();
			}
			else
			{
				Exception e = new IOException();
				LOG.error("Invalid Destination geometry");
				throw(e);
			}
		}
		else
		{
			dx = (Double)ge.getField(dXFldName);
			dy = (Double)ge.getField(dYFldName);
			if(dx == null){
				Exception e = new Exception();
				LOG.error("error retrieving destinations's x value");
				throw(e);
			}
			if(dy == null)
			{
				Exception e = new Exception();
				LOG.error("error retrieving origin's y value");
				throw(e);
			}			
		}
		Double b = generateBearing(ox,oy,dx,dy);
		GeoEventDefinition geDef = ge.getGeoEventDefinition();
		List<FieldDefinition> fdefs = geDef.getFieldDefinitions();
		Boolean hasGeo = false;
		for(FieldDefinition fldDef: fdefs)
		{
			List<String> tags = fldDef.getTags();
			for (String tag:tags)
			{
				if(tag.equals("GEOMETRY"))
				{
					hasGeo = true;
				}
			}
		}
		GeoEventDefinition outDef = null;
		Collection<GeoEventDefinition> defs = manager.searchGeoEventDefinitionByName(gedName);
		if(defs.size() == 0)
		{
			List<FieldDefinition> newPropertyDefs = new ArrayList<FieldDefinition>();
			FieldDefinition bfDef = new DefaultFieldDefinition(bfldName, FieldType.Double);
			
			newPropertyDefs.add(bfDef);
			if(!hasGeo && createGeo)
			{
				FieldDefinition geoFld = new DefaultFieldDefinition("geometry", FieldType.Geometry, "GEOMETRY");
				newPropertyDefs.add(geoFld);
			}
			outDef = geDef.augment(newPropertyDefs);
			outDef.setOwner(geDef.getOwner());
			outDef.setName(gedName);
			manager.addGeoEventDefinition(outDef);
		}
		else
		{
			outDef = (GeoEventDefinition) defs.toArray()[0];
		}
		GeoEventCreator creator = messaging.createGeoEventCreator();
		GeoEvent newEvent = creator.create(outDef.getGuid());
		newEvent.setField(bfldName, b);
		
		
		for(FieldDefinition fDef:fdefs)
		{
			String name = fDef.getName();
			Object val = ge.getField(name);
			
			newEvent.setField(name, val);
		}
		
		if(createGeo)
		{
			SpatialReference sr;
			SpatialReference srout = null;
			Boolean projectGeo = false;
			if(!hasGeo)
			{
				sr = SpatialReference.create(wkid);
			}
			else
			{
				sr = ge.getGeometry().getSpatialReference();
				if(sr.getID() != wkid)
				{
					projectGeo = true;
					srout = SpatialReference.create(wkid);
				}
			}
			MapGeometry mapGeo = null;
			if(projectGeo)
			{
				mapGeo = GenerateGeometry(ox,oy,dx,dy,sr,srout);
			}
			else
			{
				mapGeo = GenerateGeometry(ox,oy,dx,dy,sr);
			}
			newEvent.setGeometry(mapGeo);
		}
		return newEvent;
	}
	
	private MapGeometry GenerateGeometry(Double ox, Double oy, Double dx, Double dy, SpatialReference sr)
	{
		Point origin = new Point();
		Point destination = new Point();
		origin.setXY(ox, oy);
		destination.setXY(dx, dy);
		Polyline ln = new Polyline();
		ln.startPath(origin);
		ln.lineTo(destination);
		MapGeometry mapGeo = new MapGeometry(ln, sr);
		return mapGeo;
		
	}
	
	private MapGeometry GenerateGeometry(Double ox, Double oy, Double dx, Double dy, SpatialReference srin, SpatialReference srout)
	{
		Point origin = new Point();
		Point destination = new Point();
		origin.setXY(ox, oy);
		destination.setXY(dx, dy);
		Polyline ln = new Polyline();
		ln.startPath(origin);
		ln.lineTo(destination);
		MapGeometry tmp_mapGeo = new MapGeometry(ln, srin);
		Geometry projected = GeometryEngine.project(tmp_mapGeo.getGeometry(), srin, srout);
		MapGeometry mapGeo = new MapGeometry(projected, srout);
		return mapGeo;
		
	}
	
	public Double generateBearing(Double ox, Double oy, Double dx, Double dy)
	{
		Double bearing = 0.0;
		Double lon1 = Math.toRadians(ox);
		Double lat1 = Math.toRadians(oy);
		Double lon2 = Math.toRadians(dx);
		Double lat2 = Math.toRadians(dy);
		Double dLon = lon2 - lon1;
		Double y = Math.sin(dLon) * Math.cos(lat2);
		Double x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
		Double bRad = Math.atan2(y, x);
		Double tmpB = Math.toDegrees(bRad);
		//bearing =360.0-(tmpB+270.0)%360.0;
		bearing = tmpB;
		return bearing;
	}
	@Override
	public boolean isGeoEventMutator() {
		return true;
	}
	
	@Override
	public void shutdown() {
		// Destruction Phase
		super.shutdown();
	}
	
	@Override
	public synchronized void validate() throws ValidationException {
		try
		{
			super.validate();
		}
		catch(Exception e)
		{
			LOG.error(e.getMessage());
		}
	}
	
	@Override
	public void afterPropertiesSet() {
		try{
			String osrc = properties.get("osrc").getValueAsString();
			// if(osrc.equals("Geometry"))
			oXFldName = properties.get("oxFld").getValueAsString();
			oYFldName = properties.get("oyFld").getValueAsString();
			oGeoFldName = properties.get("oGeoFld").getValueAsString();
			if (osrc.equals("geo")) {
				oUseGeo = true;
				//oGeoFldName = properties.get("oGeoFld").getValueAsString();

			} else {
				//oXFldName = properties.get("oxFld").getValueAsString();
				//oYFldName = properties.get("oyFld").getValueAsString();
			}

			String dsrc = properties.get("dsrc").getValueAsString();
			dXFldName = properties.get("dxFld").getValueAsString();
			dYFldName = properties.get("dyFld").getValueAsString();
			dGeoFldName = properties.get("dGeoFld").getValueAsString();
			if (dsrc.equals("geo")) {
				dUseGeo = true;
				//dGeoFldName = properties.get("dGeoFld").getValueAsString();

			} else {
				//dXFldName = properties.get("dxFld").getValueAsString();
				//dYFldName = properties.get("dyFld").getValueAsString();
			}
			bfldName = properties.get("newfld").getValueAsString();
			gedName = properties.get("newdef").getValueAsString();
			createGeo = (Boolean) properties.get("generateGeo").getValue();
			wkid = (Integer) properties.get("wkidout").getValue();
		}
		catch(Exception e)
		{
			LOG.error(e.getMessage());
		}
	}
	
}
