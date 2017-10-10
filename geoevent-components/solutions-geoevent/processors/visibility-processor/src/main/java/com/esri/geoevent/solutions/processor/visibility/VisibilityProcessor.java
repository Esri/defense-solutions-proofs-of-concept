package com.esri.geoevent.solutions.processor.visibility;

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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;




import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.ges.core.ConfigurationException;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.core.http.GeoEventHttpClient;
import com.esri.ges.core.http.GeoEventHttpClientService;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManagerException;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;


public class VisibilityProcessor extends GeoEventProcessorBase {
	// private static final Log LOG = LogFactory.getLog(VisibilityProcessor.class);
	
	private static final BundleLogger LOG = BundleLoggerFactory.getLogger(VisibilityProcessor.class);

	public GeoEventDefinitionManager manager;
	private SpatialReference srIn;
	private SpatialReference srBuffer;
	private SpatialReference srOut;
	private String outDefName;
	private String gp;
	private String is;
	private String radiusSource;
	private Boolean isRadiusConstant = false;
	private Double radiusConstant;
	private String radiusEventfld;
	private String radiusUnit;
	private String elevationSource;
	private String elevEventfld;
	private double elevConstant;
	private Boolean isElevConstant=false;
	private String units_elev;
	private int outwkid;
	private int procwkid;
	private Messaging messaging;
	private GeoEventHttpClientService httpClientService;
	
	public VisibilityProcessor(GeoEventProcessorDefinition definition, GeoEventHttpClientService service)
			throws ComponentException {
		super(definition);
		//manager = m;
		//tagMgr=tm;
		this.httpClientService = service;
		geoEventMutator = true;
		
	}
	public void setMessaging(Messaging messaging)
	{
		this.messaging = messaging;
	}
	public void setManager(GeoEventDefinitionManager manager)
	{
		this.manager = manager;
	}
	@Override 
	public void afterPropertiesSet()
	{
		LOG.info("afterPropertiesSet starts.................");

		outDefName = properties.get("outdefname").getValueAsString();
		gp = properties.get("gpservice").getValue().toString();
		is = properties.get("imageservice").getValue().toString();
		radiusSource = properties.get("radiusSource").getValue().toString();
		if(radiusSource.equals("Constant"))
		{
			isRadiusConstant=true;
			radiusConstant = (Double) properties.get("radius").getValue();
		}
		else
		{
			radiusEventfld = properties.get("radiusEvent").getValue().toString();
		}
		radiusUnit = properties.get("units").getValue().toString();
		elevationSource = properties.get("elevationSource").getValue().toString();
		if(elevationSource.equals("Constant"))
		{
			isElevConstant=true;
			elevConstant = (Double)properties.get("elevation").getValue();
		}
		units_elev = properties.get("units_elev").getValue().toString();
		elevEventfld = properties.get("elevationEvent").getValue().toString();
		outwkid = (Integer) properties.get("wkidout").getValue();
		procwkid = (Integer) properties.get("wkidbuffer").getValue();

		LOG.info("afterPropertiesSet ends.................");

	}
	
	@Override
	public synchronized void validate() throws ValidationException
	{
		LOG.info("validate starts.................");

		super.validate();
		try
		{
			//srIn = SpatialReference.create(inwkid);
			srBuffer = SpatialReference.create(procwkid);
			srOut = SpatialReference.create(outwkid);
		}
		catch(Exception e)
		{
			ValidationException ve = new ValidationException("Invalid wkid");
			LOG.error(e.getMessage());
			LOG.error(ve.getMessage());
			throw ve;
		}
		
		LOG.info("validate ends................");

	}
	
	@Override
	public GeoEvent process(GeoEvent ge) throws Exception {
		
		LOG.info("VisibilityProcessor.process starts.................");
				
		double radius;
		if(!ge.getGeoEventDefinition().getTagNames().contains("GEOMETRY"))
		{
			return null;
		}
		srIn=ge.getGeometry().getSpatialReference();
		if(isRadiusConstant)
		{
			radius = radiusConstant;
		}
		else
		{
			
			radius = (Double)ge.getField(radiusEventfld);
		}
		
		double elevation;
		
		if(isElevConstant){
			elevation =elevConstant;
		}
		else
		{
			elevation = (Double)ge.getField(elevEventfld);
		}
		
		LOG.info("Calling ConstructVisibilityRest.................");

		GeoEvent outGeo = ConstructVisibilityRest(ge, gp, is, radius, radiusUnit,  elevation, units_elev, procwkid);
		
		LOG.info("VisibilityProcessor.process ends.................");

		return outGeo;
	}
	
	private GeoEvent ConstructVisibilityRest(GeoEvent ge, String gpservice, String imageservice, double range, String unit,  double elevation, String units_elev, int wkid) throws UnsupportedEncodingException, IOException, ConfigurationException, FieldException, MessagingException, GeoEventDefinitionManagerException 
	{
LOG.info("Starting ConstructVisibilityRest.................");
		UnitConverter uc = new UnitConverter();
		range = uc.Convert(range, unit, srBuffer);
		String procUnitName = srBuffer.getUnit().getName();
		// normalize horizontal and vertical units to z-factor = 1
		// double inElev = elevation;
		if (procUnitName.equals("Meter")) {
			if (units_elev.equals("Feet")) {
				elevation = elevation * 0.3048;
			}
		} else {
			if (units_elev.equals("Meters")) {
				elevation = elevation * 3.28084;
			}
		}
		
		MapGeometry eventGeo = ge.getGeometry();
		Geometry tmpGeo = eventGeo.getGeometry();
		Geometry projectedGeo = null;
		
		if( srIn.getID() != procwkid )
		{
			projectedGeo = GeometryEngine.project(tmpGeo, srIn, srBuffer);
		}
		else
		{
			projectedGeo = tmpGeo;
		}
		Geometry mask = null;
		if(tmpGeo.getType()==Geometry.Type.Polygon)
		{
			mask = projectedGeo;
		}
		else
		{
			mask = GeometryEngine.buffer(projectedGeo, srBuffer, range);
		}
		
		Envelope extent = new Envelope();
		String obs = "";
		if(properties.get("observerSource").getValueAsString().equals("Geoevent"))
		{
			mask.queryEnvelope(extent);
			Double x = extent.getCenterX();
			Double y = extent.getCenterY();

			String cx = ((Double) x).toString();
			String cy = ((Double) y).toString();
			obs = cx + " " + cy;
		}
		else if(properties.get("observerSource").getValueAsString().equals("Field"))
		{
			
			String xeventfld = properties.get("observerXEvent").getValue().toString();
			String[] arr = xeventfld.split(":");
			Double x = (Double)ge.getField(arr[1]);
			String yeventfld = properties.get("observerYEvent").getValue().toString();
			arr = yeventfld.split(":");
			Double y = (Double)ge.getField(arr[1]);
			Point p = new Point(x,y);
			p = (Point)GeometryEngine.project(p, srIn, srBuffer);
			obs = ((Double)p.getX()).toString() + " " + ((Double)p.getY()).toString();
		}
		else
		{
			Double x = (Double)properties.get("observerX").getValue();
			Double y = (Double)properties.get("observerY").getValue();
			Point p = new Point(x,y);
			p = (Point)GeometryEngine.project(p, srIn, srBuffer);
			obs = ((Double)p.getX()).toString() + " " + ((Double)p.getY()).toString();
		}
		
LOG.info("Got Values in ConstructVisibilityRest.................");
			
		String contentType = "application/json";
		GeoEventHttpClient http = httpClientService.createNewClient();
		//HttpClient httpclient = HttpClientBuilder.create().build();
		String observers = URLEncoder.encode(obs, "UTF-8");
		imageservice = URLEncoder.encode(imageservice, "UTF-8");
		String geoJson=GeometryEngine.geometryToJson(srBuffer, mask);
		String jsonGeo = URLEncoder.encode(
				geoJson, "UTF-8");

		String args = "observers=" + observers + "&image_service_url="
				+ imageservice + "&radius=" + ((Double) range).toString()
				+ "&height=" + ((Double) elevation).toString() + "&json_mask="
				+ jsonGeo + "&wkid=" + ((Integer) wkid).toString() + "&f=json";
		String path = gpservice + "/execute?";
		String uri = path + args;
		MapGeometry visible = null;
		MapGeometry nonvisible = null;
		try {
			HttpPost httppost = new HttpPost(uri);
			httppost.setHeader("Accept", contentType);
			CloseableHttpResponse response = http.execute(httppost, GeoEventHttpClient.DEFAULT_TIMEOUT);
			//HttpResponse response = http.execute(httppost);

			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				InputStream instream = entity.getContent();
				try {
					// instream.read();
					BufferedReader br = new BufferedReader(
							new InputStreamReader((instream)));
					String output = "";
					String ln;
					while ((ln = br.readLine()) != null) {
						output += ln;
					}
					
					ObjectMapper mapper = new ObjectMapper();
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (Map<String, Object>)mapper.readValue(output, new TypeReference<HashMap<String, Object>>(){});
					@SuppressWarnings("unchecked")
					ArrayList<Object> resString = (ArrayList<Object>)map.get("results");
					
					@SuppressWarnings("unchecked")
					Map<String, Object> r = (Map<String, Object>)resString.get(0);
					@SuppressWarnings("unchecked")
					Map<String,Object> fset = (Map<String,Object>)r.get("value");
					//String fsetJson = mapper.writeValueAsString(val);
					@SuppressWarnings("unchecked")
					List<HashMap<String, Object>> features = (ArrayList<HashMap<String,  Object>>)fset.get("features");
					for (HashMap<String, Object> feature : features) {

						@SuppressWarnings("unchecked")
						HashMap<String, Object> attributes = (HashMap<String, Object>) feature.get("attributes");
						int code = (Integer) attributes.get(
								"gridcode");
						// com.esri.ges.spatial.Geometry tmpgesVis = null;
						// com.esri.ges.spatial.Geometry tmpgesNonVis =
						// null;
						if (code == 1) {
							@SuppressWarnings("unchecked")
							Map<String, Object> objGeo = (Map<String, Object>) feature.get("geometry");
							Geometry tmpvis = generateGeoFromMap(objGeo);
							Geometry vis = GeometryEngine.project(tmpvis,
									srBuffer, srOut);
							visible = new MapGeometry(vis, srOut);

						} else {
							@SuppressWarnings("unchecked")
							Map<String, Object> objGeo = (Map<String, Object>) feature.get("geometry");
							Geometry tmpnonvis=generateGeoFromMap(objGeo);
							Geometry nonvis = GeometryEngine.project(
									tmpnonvis, srBuffer, srOut);
							nonvisible = new MapGeometry(nonvis, srOut);

						}
					}
					
				} catch (IOException ex) {
					// In case of an IOException the connection will be
					// released
					// back to the connection manager automatically
					throw ex;
				} catch (RuntimeException ex) {
					// In case of an unexpected exception you may want to
					// abort
					// the HTTP request in order to shut down the underlying
					// connection immediately.
					httppost.abort();
					throw ex;
				} finally {
					// Closing the input stream will trigger connection
					// release
					try {
						instream.close();
					} catch (Exception ignore) {
					}
				}
			}
		}
		catch(Exception ex){
			
			LOG.error("Exception in ConstructVisibilityRest: " + ex.getMessage());

			return null;
		}
		GeoEventDefinition geoDef = ge.getGeoEventDefinition();
		ArrayList<FieldDefinition> newFieldDefs = new ArrayList<FieldDefinition>();
		FieldDefinition visFldDef = new DefaultFieldDefinition("visible", FieldType.Geometry, "GEOMETRY_VISIBLE");
		FieldDefinition nonvisFldDef = new DefaultFieldDefinition("nonvisible", FieldType.Geometry, "GEOMETRY_NONVISIBLE");
		newFieldDefs.add(visFldDef);
		newFieldDefs.add(nonvisFldDef);
		GeoEventDefinition edOut;
		Collection<GeoEventDefinition>eventDefs = manager.searchGeoEventDefinitionByName(outDefName);
		Iterator<GeoEventDefinition>eventDefIt = eventDefs.iterator();
		while(eventDefIt.hasNext())
		{
			GeoEventDefinition currentDef = eventDefIt.next();
			manager.deleteGeoEventDefinition(currentDef.getGuid());
		}
		edOut = geoDef.augment(newFieldDefs);
		edOut.setOwner(getId());
		edOut.setName(outDefName);
		manager.addGeoEventDefinition(edOut);

		GeoEventCreator geoEventCreator = messaging.createGeoEventCreator();
		GeoEvent geOut = geoEventCreator.create(edOut.getGuid(), new Object[] {
				ge.getAllFields(), visible, nonvisible });
		geOut.setProperty(GeoEventPropertyName.TYPE, "message");
		geOut.setProperty(GeoEventPropertyName.OWNER_ID, getId());
		geOut.setProperty(GeoEventPropertyName.OWNER_ID, definition.getUri());

		for (Map.Entry<GeoEventPropertyName, Object> property : ge.getProperties())
	        if (!geOut.hasProperty(property.getKey()))
	          geOut.setProperty(property.getKey(), property.getValue());
		//queries.clear();
		//responseMap.clear();
		
LOG.info("Ending ConstructVisibilityRest.................");
		
		return geOut;
	}
	
	
		
	private String ConstructJsonMaskFromGeoEvent(GeoEvent ge) throws IOException
	{
		MapGeometry eventgeo = ge.getGeometry();
		Geometry geo = eventgeo.getGeometry();
		Geometry maskGeo = GeometryEngine.project(geo, srIn, srBuffer);
		return GeometryEngine.geometryToJson(srBuffer, maskGeo);
	}
	
	private Geometry generateGeoFromMap(Map<String, Object> objGeo) throws Exception
	{
		Geometry geo = null;
		if(objGeo.containsKey("rings"))
		{
			@SuppressWarnings("unchecked")
			ArrayList<ArrayList<ArrayList<Object>>> rings= (ArrayList<ArrayList<ArrayList<Object>>>)objGeo.get("rings");
			geo = generatePolygon(rings);
		}
		else if(objGeo.containsKey("paths"))
		{
			@SuppressWarnings("unchecked")
			ArrayList<ArrayList<ArrayList<Object>>> paths= (ArrayList<ArrayList<ArrayList<Object>>>)objGeo.get("paths");
			geo = generatePolyLine(paths);
		}
		else if(objGeo.containsKey("points"))
		{
			
		}
		else
		{
			Double x = Double.valueOf(objGeo.get("x").toString());
			Double y = Double.valueOf(objGeo.get("y").toString());
			if(objGeo.size() > 2)
			{
				Double z = Double.valueOf(objGeo.get("z").toString());
				geo = generate3DPoint(x,y,z);
			}
			else
			{
				geo = generatePoint(x,y);
			}
		}
		return geo;
	}
	
	private Point generatePoint(Double x, Double y)
	{
		Point p = new Point(x, y);
		return p;
	}
	
	private Point generate3DPoint(Double x, Double y, Double z)
	{
		Point p = new Point(x, y, z);
		return p;
	}
	
	private Polyline generatePolyLine(ArrayList<ArrayList<ArrayList<Object>>> paths)
	{
		Polyline polyln = new Polyline();
		for(ArrayList<ArrayList<Object>> path: paths)
		{
			Boolean firstPt = true;
			for(ArrayList<Object> strPt: path)
			{
				Point p = null;
				if(strPt.size() > 2)
				{
					Double x = (Double)strPt.get(0);
					Double y = (Double)strPt.get(1);
					Double z = (Double)strPt.get(2);
					p = generate3DPoint(x,y,z);
				}
				else
				{
					Double x = (Double)strPt.get(0);
					Double y = (Double)strPt.get(1);
					p = generatePoint(x,y);
				}
				if(firstPt)
				{
					polyln.startPath(p);
					firstPt = false;
				}
				else
				{
					polyln.lineTo(p);
				}
			}
		}
		return polyln;
	}
	
	private Polygon generatePolygon(ArrayList<ArrayList<ArrayList<Object>>> paths) throws Exception
	{
		try {
			Polygon polygon = new Polygon();
			for (ArrayList<ArrayList<Object>> path : paths) {
				Boolean firstPt = true;
				for (ArrayList<Object> strPt : path) {
					Point p = null;
					if (strPt.size() > 2) {
						
						//String strY = strPt.get(1);
						//String strZ = strPt.get(2);
						Double x = (Double)strPt.get(0);
						Double y = (Double)strPt.get(1);
						Double z = (Double)strPt.get(2);
						p = generate3DPoint(x, y, z);
					} else {
						Double x = (Double)strPt.get(0);
						Double y = (Double)strPt.get(1);
						p = generatePoint(x, y);
					}
					if (firstPt) {
						polygon.startPath(p);
						firstPt = false;
					} else {
						polygon.lineTo(p);
					}
				}
			}
			polygon.closeAllPaths();
			return polygon;
		} catch (Exception e) {
			throw (e);
		}
	}

}
