package com.esri.geoevent.solutions.processor.addxyz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

public class AddXYZProcessor extends GeoEventProcessorBase {
	
	GeoEventDefinitionManager manager;
	Messaging messaging;
	private static final Log LOG = LogFactory
			.getLog(AddXYZProcessor.class);
	private String invalidGeo = "com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.ERROR_INVALID_GEOMETRY";
	private String geoFldName;
	private String gedName;
	private String xfield;
	private String yfield;
	private String zfield;
	public AddXYZProcessor(GeoEventProcessorDefinition definition,  GeoEventDefinitionManager mgr, Messaging mes)
			throws ComponentException {
		super(definition);

		manager = mgr;
		messaging = mes;
	}
	
	@Override
	public void afterPropertiesSet()
	{
		geoFldName = properties.get("geofield").getValueAsString();
		gedName = properties.get("gedName").getValueAsString();
		xfield = properties.get("xfield").getValueAsString();
		yfield = properties.get("yfield").getValueAsString();
		zfield = properties.get("zfield").getValueAsString();
	}

	public GeoEvent process(GeoEvent evt) throws Exception {
		try
		{
			GeoEventDefinition geDef = evt.getGeoEventDefinition();
			GeoEventDefinition outDef = null;
			Collection<GeoEventDefinition> defs = manager.searchGeoEventDefinitionByName(gedName);
			if(defs.size() == 0)
			{
				List<FieldDefinition> newPropertyDefs = new ArrayList<FieldDefinition>();
				FieldDefinition xfDef = new DefaultFieldDefinition(xfield, FieldType.Double);
				FieldDefinition yfDef = new DefaultFieldDefinition(yfield, FieldType.Double);
				FieldDefinition zfDef = new DefaultFieldDefinition(zfield, FieldType.Double);
				newPropertyDefs.add(xfDef);
				newPropertyDefs.add(yfDef);
				newPropertyDefs.add(zfDef);
				
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
			MapGeometry mapGeo = evt.getGeometry();
			Geometry geo = mapGeo.getGeometry();
			if(!(geo instanceof Point))
			{
				IOException e = new IOException();
				LOG.error(invalidGeo);
				throw(e);
			}
			Point pt = (Point)geo;
			newEvent.setField(xfield, pt.getX());
			newEvent.setField(yfield, pt.getY());
			if(zfield != null && !zfield.equals(""))
			{
				if(pt.getZ() != Double.NaN)
				{
					newEvent.setField(zfield, pt.getZ());
				}
				else
				{
					newEvent.setField(zfield, 0.0);
				}
			}
			List<FieldDefinition> fldDefs = geDef.getFieldDefinitions();
			for(FieldDefinition fd: fldDefs)
			{
				String name = fd.getName();
				newEvent.setField(name, evt.getField(name));
			}
			return newEvent;
		}
		catch(Exception e)
		{
			LOG.error(e.getMessage());
			throw(e);
		}
	}
}
