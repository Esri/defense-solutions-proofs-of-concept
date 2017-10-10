package com.esri.geoevent.solutions.processor.timewindowsort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.EventDestination;
import com.esri.ges.messaging.EventUpdatable;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.GeoEventProducer;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

public class TimeWindowSortProcessor extends GeoEventProcessorBase implements
		Runnable, GeoEventProducer, EventUpdatable {
	private static final Log LOG = LogFactory
			.getLog(TimeWindowSortProcessor.class);
	private boolean monitoring = false;
	private boolean running = false;
	private Integer interval;
	private String sortfield;
	//private SortedMap<Object, ArrayList<GeoEvent>> sorted = new TreeMap<Object, ArrayList<GeoEvent>>();
	@SuppressWarnings("rawtypes")
	private HashMap cache;
	private GeoEventCreator geoEventCreator;
	private GeoEventDefinitionManager manager;
	private Messaging messaging;
	private GeoEventProducer geoEventProducer;
	private long timestamp;
	private Thread t;
	private FieldType sortedFieldType;
	public TimeWindowSortProcessor(GeoEventProcessorDefinition definition)
			throws ComponentException {
		super(definition);
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
		interval = (Integer) properties.get("interval").getValue();
		sortfield = properties.get("orderby").getValueAsString();
		timestamp = System.currentTimeMillis();
		String type = properties.get("expectedType").getValueAsString();
		if(type.equals("string"))
		{
			cache = new HashMap<String, ArrayList<GeoEvent>>();
			sortedFieldType = FieldType.String;
		}
		if(type.equals("int"))
		{
			cache = new HashMap<Integer, ArrayList<GeoEvent>>();
			sortedFieldType = FieldType.Integer;
		}
		if(type.equals("long"))
		{
			cache = new HashMap<Long, ArrayList<GeoEvent>>();
			sortedFieldType = FieldType.Long;
		}
		if(type.equals("short"))
		{
			cache = new HashMap<Short, ArrayList<GeoEvent>>();
			sortedFieldType = FieldType.Short;
		}
		if(type.equals("double"))
		{
			cache = new HashMap<Double, ArrayList<GeoEvent>>();
			sortedFieldType = FieldType.Double;
		}
		if(type.equals("Float"))
		{
			cache = new HashMap<Float, ArrayList<GeoEvent>>();
			sortedFieldType = FieldType.Float;
		}
		if(type.equals("date"))
		{
			cache = new HashMap<Long, ArrayList<GeoEvent>>();
			sortedFieldType = FieldType.Date;
		}
		this.running = true;

	}

	@SuppressWarnings("unchecked")
	public GeoEvent process(GeoEvent evt) throws Exception {

			GeoEventDefinition ged = evt.getGeoEventDefinition();
			FieldDefinition fd = ged.getFieldDefinition(sortfield);
			if(fd == null)
				return null;
			Object val = null;
			FieldType type = fd.getType();
			if(type != sortedFieldType)
			{
				return null;
			}
			if(sortedFieldType == FieldType.Date)
			{
				Date d = (Date)evt.getField(sortfield);
				val = (Long)d.getTime();
			}
			else
			{
				val = evt.getField(sortfield);
			}
			if (cache.containsKey(val)) {
				ArrayList<GeoEvent> evtList = (ArrayList<GeoEvent>) cache.get(val);
				evtList.add(evt);
				cache.put(val, evtList);
			} else {
				ArrayList<GeoEvent> evtList = new ArrayList<GeoEvent>();
				evtList.add(evt);
				cache.put(val, evtList);
			}

		return null;
	}

	@Override
	public synchronized void validate() throws ValidationException {
		// Validation Phase ...
		super.validate();

	}
	
	@Override
	public void onServiceStart()
	{
		startMonitoring();
	}
	
	@Override
	public void onServiceStop()
	{
		stopMonitoring();
	}

	@Override
	public void shutdown() {
		cache.clear();
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

	public void setManager(GeoEventDefinitionManager manager) {
		this.manager = manager;
	}

	public void setMessaging(Messaging messaging) {
		this.messaging = messaging;
		this.geoEventCreator = messaging.createGeoEventCreator();
	}

	private void startMonitoring() {
		try {
			this.monitoring=true;
			t = new Thread(this);
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void stopMonitoring()
	{
		this.monitoring=false;
		cache.clear();
		t.interrupt();

	}

	@Override
	public void run() {
		while (this.monitoring) {
			try {
				if (running) {
					long now = System.currentTimeMillis();
					long testInterval = now - this.timestamp;
					if (testInterval>=this.interval) {
						timestamp = now;
						flush();
					}
				}
			} catch (Exception e) {

			}
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void flush() throws MessagingException, InterruptedException
	{
		HashMap cacheCopy = null;

		ArrayList sorted = null;
		if(sortedFieldType==FieldType.String)
		{
			HashMap<String, ArrayList<GeoEvent>> tmpCache = cache;
			cacheCopy = tmpCache;
			cache = new HashMap<String, ArrayList<GeoEvent>>();
			sorted = new ArrayList<String>(cacheCopy.keySet());
		}
		else if(sortedFieldType==FieldType.Integer)
		{
			HashMap<Integer, ArrayList<GeoEvent>> tmpCache = cache;
			cacheCopy = tmpCache;
			cache = new HashMap<Integer, ArrayList<GeoEvent>>();
			sorted = new ArrayList<Integer>(cacheCopy.keySet());
		}
		else if(sortedFieldType==FieldType.Long)
		{
			HashMap<Long, ArrayList<GeoEvent>> tmpCache = cache;
			cacheCopy = tmpCache;
			cache = new HashMap<Long, ArrayList<GeoEvent>>();
			sorted = new ArrayList<Long>(cacheCopy.keySet());
		}
		else if(sortedFieldType==FieldType.Short)
		{
			HashMap<Long, ArrayList<GeoEvent>> tmpCache = cache;
			cacheCopy = tmpCache;
			cache = new HashMap<Short, ArrayList<GeoEvent>>();
			sorted = new ArrayList<Short>(cacheCopy.keySet());
		}
		else if(sortedFieldType==FieldType.Double)
		{
			HashMap<Double, ArrayList<GeoEvent>> tmpCache = cache;
			cacheCopy = tmpCache;
			cache = new HashMap<Double, ArrayList<GeoEvent>>();
			sorted = new ArrayList<Double>(cacheCopy.keySet());
		}
		else if(sortedFieldType==FieldType.Float)
		{
			HashMap<Float, ArrayList<GeoEvent>> tmpCache = cache;
			cacheCopy = tmpCache;
			cache = new HashMap<Float, ArrayList<GeoEvent>>();
			sorted = new ArrayList<Float>(cacheCopy.keySet());
		}
		else if(sortedFieldType==FieldType.Date)
		{
			HashMap<Long, ArrayList<GeoEvent>> tmpCache = cache;
			cacheCopy = tmpCache;
			cache = new HashMap<Long, ArrayList<GeoEvent>>();
			sorted = new ArrayList<Long>(cacheCopy.keySet());
		}
		Collections.sort(sorted);
		for(Object k: sorted)
		{
			List<GeoEvent> list = (List<GeoEvent>) cacheCopy.get(k);
			for (GeoEvent msg : list) {
				msg.setProperty(GeoEventPropertyName.TYPE, "event");
				msg.setProperty(GeoEventPropertyName.OWNER_ID, getId());
				msg.setProperty(GeoEventPropertyName.OWNER_URI,
						definition.getUri());
				send(msg);
				t.wait(250);
			}
		}
		cacheCopy.clear();
		cacheCopy = null;
	}

}
	



