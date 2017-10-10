package com.esri.geoevent.solutions.processor.ll2mgrs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.ges.core.ConfigurationException;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import com.esri.sde.sdk.pe.engine.PeGeogcs;
import com.esri.sde.sdk.pe.engine.PeNotationMgrs;
import com.esri.sde.sdk.pe.factory.PeFactory;

public class MGRSProcessor extends GeoEventProcessorBase{
	private Integer accuracy;
	private String newdef;
	private String geofld;
	private List<FieldDefinition> fds;
	private GeoEventDefinitionManager manager;
	private Messaging messaging;
	
	public MGRSProcessor(GeoEventProcessorDefinition definition)
			throws ComponentException {
		super(definition);
		// TODO Auto-generated constructor stub
	}

	@Override
	public GeoEvent process(GeoEvent evt) throws Exception {
		MapGeometry mapGeo = (MapGeometry) evt.getField(geofld);
		Geometry geo = mapGeo.getGeometry();
		int wkid = mapGeo.getSpatialReference().getID();
		if(wkid != 4326)
		{
			return null;
		}
		if(geo.getType() != Geometry.Type.Point)
		{
			return null;
		}
		Point pt = (Point)geo;
		double[] coords = {pt.getX(), pt.getY()};
		PeGeogcs pegeocs = PeFactory.geogcs(4326);
		String[] mgrsvals = new String[1];
		PeNotationMgrs.geog_to_mgrs(pegeocs, 1, coords, accuracy, false, mgrsvals);
		String mgrs = mgrsvals[0];
		//LL ll = new LL(pt.getX(), pt.getY());
		//ll.setAccuracy(accuracy);
		
		//MGRS2LatLongConverter converter = new MGRS2LatLongConverter();
		//String mgrs = converter.LL2MRGS(ll);
		GeoEventDefinition edOut;
		GeoEventDefinition geoDef = evt.getGeoEventDefinition();
		if((edOut=manager.searchGeoEventDefinition(newdef, getId()))==null)
		{
			edOut = geoDef.augment(fds);
			edOut.setOwner(getId());
			edOut.setName(newdef);
			manager.addGeoEventDefinition(edOut);
		}
		GeoEventCreator  geoEventCreator = messaging.createGeoEventCreator();
		GeoEvent geOut = geoEventCreator.create(edOut.getGuid(), new Object[] {
			evt.getAllFields(), mgrs });
		geOut.setProperty(GeoEventPropertyName.TYPE, "message");
		geOut.setProperty(GeoEventPropertyName.OWNER_ID, getId());
		geOut.setProperty(GeoEventPropertyName.OWNER_ID, definition.getUri());
		return geOut;
	}
	
	@Override 
	public void afterPropertiesSet()
	{
		geofld = properties.get("geofld").getValueAsString();
		newdef = properties.get("eventdef").getValueAsString();
		accuracy = (Integer)properties.get("accuracy").getValue();
		try {
			FieldDefinition fd = new DefaultFieldDefinition("mgrs", FieldType.String);
			fds = new ArrayList<FieldDefinition>();
			fds.add(fd);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	//getters setters
	
	
	public void setManager(GeoEventDefinitionManager manager)
	{
		this.manager =  manager;
	}
	
	public void setMessaging(Messaging messaging)
	{
		this.messaging = messaging;
	}

}
