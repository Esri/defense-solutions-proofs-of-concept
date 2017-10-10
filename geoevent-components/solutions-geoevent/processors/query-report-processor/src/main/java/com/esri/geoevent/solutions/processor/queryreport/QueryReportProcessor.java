package com.esri.geoevent.solutions.processor.queryreport;


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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Segment;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnection;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerType;
import com.esri.ges.manager.datastore.agsconnection.Field;
import com.esri.ges.manager.datastore.agsconnection.Layer;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;


public class QueryReportProcessor extends GeoEventProcessorBase {
	//private enum SortType {Name, Distance};
	//public SortType currentSort = SortType.Name;
	private static final Log LOG = LogFactory.getLog(QueryReportProcessor.class);
	private Tokenizer tokenizer = new Tokenizer();
	private Map<String, String> eventTokenMap = new HashMap<String, String>();
	public GeoEventDefinitionManager manager;
	public ArcGISServerConnectionManager connectionManager;
	public Messaging messaging;
	private SpatialReference srIn;
	private SpatialReference srBuffer;
	private SpatialReference srOut;
	private double radius;
	private String units;
	private int inwkid;
	private int outwkid;
	private int bufferwkid;
	private String geoSrc;
	private Boolean useCentroid;
	private String eventfld;
	private Boolean useTimeStamp;
	private String file;
	private String host;
	String outDefName;
	String connName;
	private ArcGISServerConnection conn;
	private String folder;
	private String service;
	private String lyrName;
	private Layer layer;
	private String layerId;
	private String field;
	//Field[] fields;
	private Boolean calcDist;
	private String wc;
	private String lyrHeaderCfg;
	private String distToken="";
	private Boolean sortByDist;
	private String distUnits="";
	//String token;
	private String sortField;
	private String itemConfig;
	private String title;
	private String header;
	private com.esri.core.geometry.Geometry inGeometry;
	private GeoEvent currentEvent = null;
	private String ts;
	private String time;
	private String endpoint=null;
	private String token;
	public QueryReportProcessor(GeoEventProcessorDefinition definition, GeoEventDefinitionManager m, ArcGISServerConnectionManager cm, Messaging msg)
			throws ComponentException {
		super(definition);
		manager = m;
		connectionManager = cm;
		messaging = msg;
		geoEventMutator= true;
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
	public void afterPropertiesSet()
	{
		radius = (Double)properties.get("radius").getValue();
		units = properties.get("units").getValue().toString();
		//inwkid = (Integer) properties.get("wkidin").getValue();
		outwkid = (Integer) properties.get("wkidout").getValue();
		bufferwkid = (Integer) properties.get("wkidbuffer").getValue();
		geoSrc = properties.get("geosrc").getValueAsString();
		useCentroid = (Boolean)properties.get("usecentroid").getValue();
		eventfld = properties.get("geoeventdef").getValue().toString();
		useTimeStamp = (Boolean)properties.get("usetimestamp").getValue();
		
		DateTime dt = DateTime.now();
		ts = ((Integer) dt.getYear()).toString()
				+ ((Integer) dt.getMonthOfYear()).toString()
				+ ((Integer) dt.getDayOfMonth()).toString()
				+ ((Integer) dt.getHourOfDay()).toString()
				+ ((Integer) dt.getMinuteOfHour()).toString()
				+ ((Integer) dt.getSecondOfMinute()).toString();
		time = ((Integer) dt.getYear()).toString()
				+"/" + ((Integer) dt.getMonthOfYear()).toString()
				+"/"+ ((Integer) dt.getDayOfMonth()).toString()
				+" "+ ((Integer) dt.getHourOfDay()).toString()
				+ ":"+((Integer) dt.getMinuteOfHour()).toString()
				+ ":"+((Integer) dt.getSecondOfMinute()).toString();
		file = properties.get("filename").getValueAsString() + ts + ".html";
		host = properties.get("host").getValueAsString();
		outDefName = properties.get("gedname").getValueAsString();
		connName = properties.get("connection").getValueAsString();
		folder = properties.get("folder").getValueAsString();
		service = properties.get("service").getValueAsString();
		lyrName = properties.get("layer").getValueAsString();
		try
		{
			conn = connectionManager.getArcGISServerConnection(connName);
		}
		catch(Exception e)
		{
			LOG.error(e.getMessage());
			ValidationException ve = new ValidationException("Unable to make connection to ArcGIS Server");
			LOG.error(ve.getMessage());
			try {
				throw ve;
			} catch (ValidationException e1) {

				e1.printStackTrace();
			}
		}
		layer =conn.getLayer(folder, service, lyrName, ArcGISServerType.FeatureServer);
		layerId = ((Integer)layer.getId()).toString();
		field = properties.get("field").getValueAsString();
		sortField = properties.get("sortfield").getValueAsString();
		if(!properties.get("endpoint").getValueAsString().isEmpty())
		{
			endpoint=properties.get("endpoint").getValueAsString();
		}
		try {
			token = conn.getDecryptedToken();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//fields = conn.getFields(folder, service, layer.getId(), ArcGISServerType.FeatureServer);
		calcDist = (Boolean)properties.get("calcDistance").getValue();
		wc = properties.get("wc").getValueAsString();
		lyrHeaderCfg = properties.get("lyrheader").getValueAsString();
		sortByDist=false;
		if(calcDist)
		{
			sortByDist = (Boolean)properties.get("sortdist").getValue();
			distToken="${distance.value}";
			distUnits=properties.get("dist_units").getValueAsString();
		}
		//token = properties.get("field-token").getValueAsString();
		itemConfig = properties.get("item-config").getValueAsString();
		title = properties.get("title").getValueAsString();
	    Object objHeader = properties.get("header");
	    header = null;
	    if(objHeader != null)
	       {
	    	   header = properties.get("header").getValueAsString();
	       }
	}
	
	@Override
	public synchronized void validate() throws ValidationException
	{
		if(radius <= 0)
		{
			ValidationException ve = new ValidationException("Radius cannot be less than or equal to 0");
			LOG.error(ve.getMessage());
			throw ve;
		}
		
		try
		{
			srBuffer = SpatialReference.create(bufferwkid);
		}
		catch(Exception e)
		{
			LOG.error(e.getMessage());
			ValidationException ve = new ValidationException("Invalid wkid");
			LOG.error(ve.getMessage());
			throw ve;
		}
		try
		{
			srOut = SpatialReference.create(outwkid);
		}
		catch(Exception e)
		{
			LOG.error(e.getMessage());
			ValidationException ve = new ValidationException("Invalid wkid");
			LOG.error(ve.getMessage());
			throw ve;
		}
		
		
	}
	
	@Override
	public GeoEvent process(GeoEvent ge) throws Exception {
		//CreateQueryMap();
		if(!ge.getGeoEventDefinition().getTagNames().contains("GEOMETRY"))
		{
			return null;
		}
		srIn=ge.getGeometry().getSpatialReference();
		inwkid = srIn.getID();
		currentEvent = ge;
		List<FieldDefinition> fldDefs = ge.getGeoEventDefinition().getFieldDefinitions();
		for(FieldDefinition fd: fldDefs)
		{
			if(fd.getType() != FieldType.Geometry && fd.getType() != FieldType.Group)
			{
				String n = fd.getName();
				String tk = tokenizer.tokenize("geoevent."+n);
				eventTokenMap.put(tk, n);
			}
			
		}
		ArrayList<Object> queries = CreateQueries();
		

		MapGeometry geo = ge.getGeometry();
		MapGeometry inGeo = null;
		if(geoSrc.equals("Buffer"))
		{
			
			Geometry rtGeo = constructGeometry(geo);
			if(useCentroid)
			{
				if(geo.getGeometry().getType() == Geometry.Type.Point)
				{
					inGeometry = rtGeo;
				}
				else
				{
					Envelope env = new Envelope();
					rtGeo.queryEnvelope(env);
					inGeometry = env.getCenter();
				}
			}
			else
			{
				inGeometry = constructGeometry(geo);
			}
			
			Unit u = queryUnit(units);
			inGeo = constructBuffer(geo.getGeometry(),radius,u);
		}
		else if(geoSrc.equals("Event_Definition"))
		{
			String geostr = (String)ge.getField(eventfld);
			MapGeometry g = constructGeometryFromString(geostr);
			Geometry polyGeo= constructGeometry(g);
			if(useCentroid)
			{
				Envelope env = new Envelope();
				polyGeo.queryEnvelope(env);
				inGeometry = env.getCenter();
			}
			else
			{
				inGeometry = polyGeo;
			}
			com.esri.core.geometry.Geometry projGeo = GeometryEngine.project(polyGeo, srBuffer, srOut);
			inGeo = new MapGeometry(projGeo, srOut);
			//String json = GeometryEngine.geometryToJson(srOut, projGeo);
		}
		else
		{
			
			Geometry polyGeo = constructGeometry(geo);
			if(useCentroid)
			{
				Envelope env = new Envelope();
				polyGeo.queryEnvelope(env);
				inGeometry = env.getCenter();
			}
			else
			{
				inGeometry = polyGeo;
			}
			com.esri.core.geometry.Geometry projGeo = GeometryEngine.project(polyGeo, srBuffer, srOut);
			//String json = GeometryEngine.geometryToJson(srOut, projGeo);
			inGeo = new MapGeometry(projGeo, srOut);
			
		}
		Geometry newGeo = inGeo.getGeometry();
		String jsonGeo = GeometryEngine.geometryToJson(srOut.getID(), newGeo);
		String geotype = GeometryUtility.parseGeometryType(newGeo.getType());
		HashMap<String, Object> responseMap = ExecuteRestQueries(jsonGeo, geotype, queries);
		String timestamp = "";
		if(useTimeStamp)
	       {
			timestamp = time;
	       }

		ParseResponses(timestamp, file, responseMap);
		
		if(host.contains("http://"))
		{
			host.replace("http://", "");
		}
		if (host.contains(":6180")) {
			host.replace(":6180", "");
		}
		String url = "http://" + host + ":6180/geoevent/assets/reports/" + file;
		GeoEventDefinition geoDef = ge.getGeoEventDefinition();
		
		GeoEventDefinition edOut;
		if((edOut=manager.searchGeoEventDefinition(outDefName, getId()))==null)
		{
			List<FieldDefinition> fds = Arrays
					.asList(((FieldDefinition) new DefaultFieldDefinition(
							"url", FieldType.String)));
			edOut = geoDef.augment(fds);
			edOut.setOwner(getId());
			edOut.setName(outDefName);
			manager.addGeoEventDefinition(edOut);
		}
		GeoEventCreator geoEventCreator = messaging.createGeoEventCreator();
		GeoEvent geOut = geoEventCreator.create(edOut.getGuid(), new Object[] {
				ge.getAllFields(), url });
		geOut.setProperty(GeoEventPropertyName.TYPE, "message");
		geOut.setProperty(GeoEventPropertyName.OWNER_ID, getId());
		geOut.setProperty(GeoEventPropertyName.OWNER_ID, definition.getUri());

		for (Map.Entry<GeoEventPropertyName, Object> property : ge.getProperties())
	        if (!geOut.hasProperty(property.getKey()))
	          geOut.setProperty(property.getKey(), property.getValue());
		//queries.clear();
		//responseMap.clear();
		return geOut;

	}
	private Unit queryUnit(String units)
	{
		UnitConverter uc = new UnitConverter();
		String cn = uc.findConnonicalName(units);
		int unitout = uc.findWkid(cn);
		Unit  u = LinearUnit.create(unitout);
		return u;
	}
	private Geometry constructGeometry(MapGeometry geo) throws Exception
	{
		try{
			
			Geometry geoIn= geo.getGeometry();
			return GeometryEngine.project(geoIn, srIn, srBuffer);
		}
		catch(Exception e)
		{
			LOG.error(e.getMessage());
			LOG.error(e.getStackTrace());
			throw(e);
		}
	}
	
	private MapGeometry constructGeometryFromString(String geoString)
	{
		String[] pairs = geoString.split(" ");
		
		Polygon polygon = new Polygon();
		Boolean firstit = true;
		for(String coords: pairs)
		{
			
			String[] tuple = coords.split(",");
			Double x = Double.parseDouble(tuple[0]);
			Double y = Double.parseDouble(tuple[1]);
			Point p = new Point(x,y);
			Double z = Double.NaN;
			if (tuple.length>2)
			{
				z = Double.parseDouble(tuple[2]);
				p.setZ(z);
			}
			if(firstit)
			{
				polygon.startPath(p);
				firstit=false;
			}
			else
			{
				polygon.lineTo(p);
			}
		}
		polygon.closeAllPaths();
		MapGeometry mapgeo = new MapGeometry(polygon, srOut);
		return mapgeo;
	}
	
	
	private MapGeometry constructBuffer(Geometry geo, double radius, Unit u) throws JsonParseException, IOException
	{
		
		Polygon buffer = GeometryEngine.buffer(inGeometry, srBuffer, radius, u);
		Geometry bufferout = GeometryEngine.project(buffer, srBuffer, srOut);
		MapGeometry mapGeo = new MapGeometry(bufferout, srOut);
		return mapGeo;
		//String json = GeometryEngine.geometryToJson(srOut, bufferout);
		//return spatial.fromJson(json);
		
	}
	
	public ArrayList<Object> CreateQueries()
	{
		ArrayList<Object>queries = new ArrayList<Object>();
		URL url = conn.getUrl();
		String curPath=null;
		if (endpoint != null)
		{
			curPath = endpoint;
		}
		else
		{
			String baseUrl = url.getProtocol() +"://"+ url.getHost() + ":" + url.getPort()
				+ url.getPath() + "rest/services/";
		  	curPath = baseUrl + "/" + folder + "/" + service + "/FeatureServer/" + layerId;
		}
		String restpath = curPath + "/query?";
		HashMap<String, Object> query = new HashMap<String, Object>();
		HashMap<String, String> fieldMap = new HashMap<String, String>();

		String fldsString = field;
		String[] fieldArray = fldsString.split(",");
		for(String f: fieldArray)
		{
			String tk = tokenizer.tokenize(f);
			fieldMap.put(f, tk);
		}
		
		query.put("restpath", restpath);
		query.put("path", curPath);
		query.put("whereclause", wc);
		query.put("fields", fldsString );
		//query.put("outfields", fields);
		query.put("tokenMap", fieldMap);
		query.put("headerconfig", lyrHeaderCfg);
		query.put("usingdist", calcDist);
		query.put("sortbydist", sortByDist);
		query.put("distunits", distUnits);
		query.put("disttoken", distToken);
		query.put("itemconfig", itemConfig);
		query.put("layer", layer.getName());
		UUID uid = UUID.randomUUID();
		query.put("id", uid);
		queries.add(query);
		return queries;
	}
	
	private HashMap<String, Object> ExecuteRestQueries(String jsonGeometry, String geoType, ArrayList<Object> queries)
			throws UnsupportedEncodingException {
		String contentType = "application/json";
		HttpClient httpclient = HttpClientBuilder.create().build();
		HashMap<String, Object>responseMap = new HashMap<String, Object>();
		for (int i = 0; i < queries.size(); ++i) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> query = (HashMap<String, Object>) queries
					.get(i);
			String path = (String) query.get("restpath");
			String wc = URLEncoder.encode((String) query.get("whereclause"),
					"UTF-8");
			String geo = URLEncoder.encode(jsonGeometry, "UTF-8");
			String fields = (String) query.get("fields");
			@SuppressWarnings("unchecked")
			HashMap<String, String> tokenMap = (HashMap<String, String>) query
					.get("tokenMap");
			String itemConfig = (String) query.get("itemconfig");
			//
			String args = "where="
					+ wc
					+ "&objectIds=&time=&geometry="
					+ geo
					+ "&geometryType="
					+ geoType
					+ "&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=*"
					//+ fields
					+ "&returnGeometry=true&maxAllowableOffset=&geometryPrecision=&outSR=&gdbVersion=&returnDistinctValues=false&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&f=json";
			if(token != null)
			{
				args += "&token=" + token;
			}
			String uri = path + args;
			try {
				HttpPost httppost = new HttpPost(uri);
				httppost.setHeader("Accept", contentType);
				HttpResponse response = httpclient.execute(httppost);

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
						//JsonFactory jf = new JsonFactory();
						//JsonParser jp = jf.createJsonParser(output);
						//FeatureSet fset = FeatureSet.fromJson(jp);
						Map<String, Object> map = new HashMap<String, Object>();
						ObjectMapper mapper = new ObjectMapper();
						map = mapper.readValue(output,  new TypeReference<HashMap<String, Object>>(){});
						HashMap<String, Object> tuple = new HashMap<String, Object>();
						String lyr = (String)query.get("layer");
						String lyrheadercfg = (String)query.get("headerconfig");
						Boolean calcdist = (Boolean)query.get("usingdist");
						Boolean sortByDist = (Boolean)query.get("sortbydist");
						String distToken = (String)query.get("disttoken");
						String distUnits = (String)query.get("distunits");
						String id = query.get("id").toString();
						tuple.put("fset",  map);
						tuple.put("tokenmap", tokenMap);
						tuple.put("config", itemConfig);
						tuple.put("layer", lyr);
						tuple.put("lyrheader", lyrheadercfg);
						tuple.put("sortbydist", sortByDist);
						tuple.put("calcdist", calcdist);
						tuple.put("distunits", distUnits);
						tuple.put("disttoken", distToken);
						responseMap.put(id, tuple);
						
						
					
					
					} catch (IOException ex) {
						// In case of an IOException the connection will be
						// released
						// back to the connection manager automatically
						LOG.error(ex);
						throw ex;
					} catch (RuntimeException ex) {
						// In case of an unexpected exception you may want to
						// abort
						// the HTTP request in order to shut down the underlying
						// connection immediately.
						LOG.error(ex);
						httppost.abort();
						throw ex;
					}catch(Exception ex){
						
						LOG.error(ex);
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
				

			} catch (Exception ex) {
				LOG.error(ex);
				ex.printStackTrace();
			}
			
		}
		return responseMap;
	}
	
	private Geometry generateGeoFromMap(Map<String, Object> objGeo)
	{
		Geometry geo = null;
		if(objGeo.containsKey("rings"))
		{
			ArrayList<ArrayList<ArrayList<String>>> rings= (ArrayList<ArrayList<ArrayList<String>>>)objGeo.get("rings");
			geo = generatePolygon(rings);
		}
		else if(objGeo.containsKey("paths"))
		{
			ArrayList<ArrayList<ArrayList<String>>> paths= (ArrayList<ArrayList<ArrayList<String>>>)objGeo.get("paths");
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
	
	private Polyline generatePolyLine(ArrayList<ArrayList<ArrayList<String>>> paths)
	{
		Polyline polyln = new Polyline();
		for(ArrayList<ArrayList<String>> path: paths)
		{
			Boolean firstPt = true;
			for(ArrayList<String> strPt: path)
			{
				Point p = null;
				if(strPt.size() > 2)
				{
					Double x = Double.valueOf(strPt.get(0));
					Double y = Double.valueOf(strPt.get(1));
					Double z = Double.valueOf(strPt.get(2));
					p = generate3DPoint(x,y,z);
				}
				else
				{
					Double x = Double.valueOf(strPt.get(0));
					Double y = Double.valueOf(strPt.get(1));
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
	
	private Polygon generatePolygon(ArrayList<ArrayList<ArrayList<String>>> paths)
	{
		Polygon polygon = new Polygon();
		for(ArrayList<ArrayList<String>> path: paths)
		{
			Boolean firstPt = true;
			for(ArrayList<String> strPt: path)
			{
				Point p = null;
				if(strPt.size() > 2)
				{
					Double x = Double.valueOf(strPt.get(0));
					Double y = Double.valueOf(strPt.get(1));
					Double z = Double.valueOf(strPt.get(2));
					p = generate3DPoint(x,y,z);
				}
				else
				{
					Double x = Double.valueOf(strPt.get(0));
					Double y = Double.valueOf(strPt.get(1));
					p = generatePoint(x,y);
				}
				if(firstPt)
				{
					polygon.startPath(p);
					firstPt = false;
				}
				else
				{
					polygon.lineTo(p);
				}
			}
		}
		polygon.closeAllPaths();
		return polygon;
	}
	
	private String GetDistAsString(Map<String, Object> objGeo, SpatialReference inputSr, String units) throws JsonParseException, IOException
	{
		Geometry geo = generateGeoFromMap(objGeo);
		com.esri.core.geometry.Geometry curGeo;
		
		if(!inputSr.equals(srBuffer))
		{
			curGeo = GeometryEngine.project(geo, inputSr, srBuffer);
		}
		else
		{
			curGeo=geo;
		}
		double tmpDist = GeometryEngine.distance(inGeometry, curGeo, srBuffer);
		UnitConverter uc = new UnitConverter();
		int inUnitWkid = uc.findWkid(srBuffer.getUnit().getName());
		String cn = uc.findConnonicalName(units);
		int outUnitWkid = uc.findWkid(cn);
		double dist;
		if(inUnitWkid!=outUnitWkid)
		{
			dist = uc.Convert(tmpDist, inUnitWkid, outUnitWkid);
		}
		else
		{
			dist=tmpDist;
		}
		
		DecimalFormat df = new DecimalFormat("#.00");
		return df.format(dist);
	}
	
	private void ParseResponses(String timestamp, String file, HashMap<String, Object>responseMap) throws JsonParseException, IOException
	{
		Map<String, List<String>> nameSortMap = new HashMap<String, List<String>>();
		Map<Double, List<String>> distSortMap = new HashMap<Double, List<String>>();
		ArrayList<String> nSortList = new ArrayList<String>();
		ArrayList<Double> dSortList = new ArrayList<Double>();
		Set<String> keys = responseMap.keySet();
		Iterator<String> it = keys.iterator();
		String body = "";
		while(it.hasNext())
		{
			String k = it.next();
			@SuppressWarnings("unchecked")
			HashMap<String, Object> response = (HashMap<String, Object>) responseMap.get(k);
			@SuppressWarnings("unchecked")
			Map<String, Object> fset = (HashMap<String,Object>)response.get("fset");
			String cfg = (String)response.get("config");
			@SuppressWarnings("unchecked")
			HashMap<String,Object>tokenmap=(HashMap<String, Object>)response.get("tokenmap");
			String layer = (String)response.get("layer");
			//String featuresAsString = fset.get("features").toString();
			List<HashMap<String, Object>> features = (ArrayList<HashMap<String,  Object>>)fset.get("features");
			ObjectMapper om = new ObjectMapper();
			//List<Object> features = new ArrayList<Object>();
			//Graphic[] features = fset.getGraphics();
			//List<String> features = om.readValue(featuresAsString, TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
			String lyrHeader = (String)response.get("lyrheader");
			if(!lyrHeader.isEmpty())
			{
				lyrHeader = "<b>"+lyrHeader+"</b>";
			}
			String items = "";
			
			
			items += "<b>"+layer.toUpperCase() + ": </b>";
			
			Boolean usingDist = (Boolean)response.get("calcdist");
			Boolean sortByDist = (Boolean)response.get("sortbydist");
			String distUnits = (String)response.get("distunits");
			String distToken = (String)response.get("disttoken");
			Map<String, Object> srmap = (HashMap<String, Object>)fset.get("spatialReference");;
			Integer wkid = (Integer)srmap.get("wkid");
			//SpatialReference fsetSr = fset.getSpatialReference();
			SpatialReference fsetSr = SpatialReference.create(wkid);
			for (int i=0; i<features.size();++i)
			{
				HashMap<String, Object> f = features.get(i);
				Map<String, Object> att = (Map<String, Object>) f.get("attributes");
				Map<String, Object> objGeo = (Map<String, Object>)f.get("geometry");
				//String geoString = f.get("geometry").toString();
				Set<String> fields = tokenmap.keySet();
				Iterator<String> itFields = fields.iterator();
				String item = cfg;
				Double distVal = null;
				if(usingDist)
				{
					String d = this.GetDistAsString(objGeo, fsetSr, distUnits);
					if(sortByDist)
					{
						distVal = Double.valueOf(d);
					}
					item = item.replace(distToken, d);
				}
				String attVal=null;
				String sortAttVal=null;
				while(itFields.hasNext())
				{
					String fldname = itFields.next();
					String token = (String) tokenmap.get(fldname);
					attVal = att.get(fldname).toString();
					if(!sortByDist)
					{
						if(fldname.equals(sortField))
						{
							sortAttVal = attVal;
						}
					}
					item = item.replace(token, attVal);
					
				}
				if(!sortByDist)
				{
					if(!nameSortMap.containsKey(sortAttVal))
					{
						List<String> l = new ArrayList<String>();
						l.add(item);
						nameSortMap.put(sortAttVal, l);
						nSortList.add(sortAttVal);
					}
					else
					{
						List<String> l = nameSortMap.get(sortAttVal);
						l.add(item);
						Collections.sort(l);
					}
				}
				else
				{
					if(!distSortMap.containsKey(distVal))
					{
						List<String> l = new ArrayList<String>();
						l.add(item);
						distSortMap.put(distVal, l);
						dSortList.add(distVal);
					}
					else
					{
						List<String> l = distSortMap.get(distVal);
						l.add(item);
						Collections.sort(l);
					}
				}
			}
			String sortedItems = "";
			if(!sortByDist)
			{
				Collections.sort(nSortList);
				sortedItems = ConstructSortedItemsByName(nameSortMap, nSortList);
			}
			else
			{
				Collections.sort(dSortList);
				sortedItems = ConstructSortedItemsByDistance(distSortMap, dSortList);
			}
			//String sortedItems = ConstructSortedItems(sortByDist);
			items = sortedItems;
			body += lyrHeader+"<br>"+items;
		}
		String content = "";
		   //File file = new File("C:/Dev/Java/DefenseSolution/defense-geometry-processor/src/main/resources/ReportTemplate.html"); //for ex foo.txt
		   try {
			   //String name = this.getClass().getName();
		       InputStream is = this.getClass().getClassLoader().getResourceAsStream("ReportTemplate.html");
		       //FileInputStream is = new FileInputStream(file);
			   BufferedReader br = new BufferedReader(new InputStreamReader(is));
		       String ln;
		       while ((ln = br.readLine()) != null) {
					content += ln;
				}
		       
		       if(!timestamp.isEmpty())
		       {
		    	   String tsToken = "${timestamp.value}";
		    	   title = title.replace(tsToken, timestamp);
		    	   if(header != null)
		    	   {
		    		   header = header.replace(tsToken, timestamp);
		    	   }
		    	   body = body.replace(tsToken,  timestamp);
		       }
		       content = content.replace("${TITLE}", "<h1>" +title+"</h1>");
		       content = content.replace("${HEADING}", "<h2>" +header+"</h2>");
		       content = content.replace("${BODY}", body);
		       br.close();
		       Set<String> eventTokens = eventTokenMap.keySet();
		       Iterator<String> eventIt = eventTokens.iterator();
		       while(eventIt.hasNext())
		       {
		    	   String et = eventIt.next();
		    	   String fn = eventTokenMap.get(et);
		    	   String val = currentEvent.getField(fn).toString();
		    	   content = content.replace(et, val);
		       }
		       File dir = new File("assets/reports");
		       if(!dir.exists())
		       {
		    	   dir.mkdir();
		       }
		       String filename = "assets/reports/" + file;
		      
		       File outfile = new File(filename);
		       
		       FileOutputStream fos = new FileOutputStream(outfile);
		       OutputStreamWriter osw = new OutputStreamWriter(fos);    
		       Writer w = new BufferedWriter(osw);
		       w.write(content);
		       w.close();
		   } catch (IOException e) {
		       e.printStackTrace();
		   }
		   
	}
	
	private String ConstructSortedItemsByName(
			Map<String, List<String>> nameSortMap, ArrayList<String> nSortList) {
		String items = "";

		Boolean first = true;
		for (String n : nSortList) {
			List<String> l = nameSortMap.get(n);
			for (String item : l) {
				if (first) {
					items += item;
					first = false;
				} else {
					items += "<br>" + item;
				}
			}
		}

		return items;
	}

	private String ConstructSortedItemsByDistance(
			Map<Double, List<String>> distSortMap, ArrayList<Double> dSortList) {
		String items = "";
		Boolean first = true;
		for (Double d : dSortList) {
			List<String> l = distSortMap.get(d);
			for (String item : l) {
				if (first) {
					items += item;
					first = false;
				} else {
					items += "<br>" + item;
				}
			}
		}
		return items;
	}

}
