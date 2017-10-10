package com.esri.geoevent.solutions.processor.stwa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.ConfigurationException;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.DefaultGeoEventDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.core.validation.ValidationException;
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

public class STWAProcessor extends GeoEventProcessorBase implements
		Runnable, GeoEventProducer, EventUpdatable {
	private static final Log LOG = LogFactory
			.getLog(STWAProcessor.class);
	private boolean monitoring = false;
	private boolean running = false;
	private Integer interval;
	private String aggregateFld;
	private GeoEventDefinition gedout = null;
	private HashMap<Long, Double> cache = new HashMap<Long, Double>();
	private GeoEventCreator geoEventCreator;
	private GeoEventDefinitionManager manager;
	private Messaging messaging;
	private GeoEventProducer geoEventProducer;
	//private Double sum = Double.NaN;
	//private Double mean = Double.NaN;;
	//private Double variance = Double.NaN;
	//private Double sd = Double.NaN;
	private long timestamp;
	private Thread t;
	private ArrayList<FieldType>typelist = new ArrayList<FieldType>();
	//private List<FieldDefinition> appendFields;
	public STWAProcessor(GeoEventProcessorDefinition definition)
			throws ComponentException {
		super(definition);
		typelist.add(FieldType.Integer);
		typelist.add(FieldType.Short);
		typelist.add(FieldType.Long);
		typelist.add(FieldType.Float);
		typelist.add(FieldType.Double);
		
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
		try {
			aggregateFld = properties.get("aggregate").getValueAsString();
			interval = (Integer) properties.get("interval").getValue();
			ArrayList<FieldDefinition>fields = new ArrayList<FieldDefinition>();
			this.running = true;
			
			String countFld = aggregateFld + "_count";
			FieldDefinition fdCount = new DefaultFieldDefinition(countFld,
					FieldType.Integer, "AGGREGATE_COUNT");
			
			String sumFld = aggregateFld + "_sum";
			FieldDefinition fdSum = new DefaultFieldDefinition(sumFld,
					FieldType.Double, "AGGREGATE_SUM");
			
			String meanFld = aggregateFld + "_mean";
			FieldDefinition fdMean = new DefaultFieldDefinition(meanFld,
					FieldType.Double, "AGGREGATE_MEAN");
			
			String sdFld = aggregateFld + "_stddev";
			FieldDefinition fdsd = new DefaultFieldDefinition(sdFld,
					FieldType.Double, "AGGREGATE_STANDARD_DEVIATION");
			
			String sdVar = aggregateFld + "_var";
			FieldDefinition fdvar = new DefaultFieldDefinition(sdVar,
					FieldType.Double, "AGGREGATE_VARIANCE");
			
			String sdts = "timestamp";
			FieldDefinition fdts = new DefaultFieldDefinition(sdts,
					FieldType.Date, "TIMESTAMP");
			
			fields.add(fdCount);
			fields.add(fdSum);
			fields.add(fdMean);
			fields.add(fdvar);
			fields.add(fdsd);
			fields.add(fdts);
			
			String outgedname = "aggregate_" + aggregateFld;
			Collection<GeoEventDefinition> gedColl = manager.searchGeoEventDefinitionByName(outgedname);
			if(gedColl.isEmpty())
			{
				gedout = new DefaultGeoEventDefinition();
				gedout.setFieldDefinitions(fields);
				gedout.setOwner(getId());
				gedout.setName(outgedname);
				manager.addGeoEventDefinition(gedout);
			}
			else
			{
				gedout = gedColl.iterator().next();
			}
		} catch (ConfigurationException e) {
			LOG.error(e.getMessage());
		} catch (GeoEventDefinitionManagerException e) {
			LOG.error(e.getMessage());
		}
	}

	public GeoEvent process(GeoEvent evt) throws Exception {

		GeoEventDefinition ged = evt.getGeoEventDefinition();
		FieldDefinition fd = ged.getFieldDefinition(aggregateFld);
		if (fd == null)
			return null;
		FieldType type = fd.getType();
		if (!typelist.contains(type)) {
			return null;
		}

		Double val = null;
		if (type == FieldType.Double) {
			val = (Double) evt.getField(aggregateFld);
		} else if (type == FieldType.Integer) {
			val = ((Integer) evt.getField(aggregateFld)) * 1.0;
		} else if (type == FieldType.Long) {
			val = ((Long) evt.getField(aggregateFld)) * 1.0;
		} else if (type == FieldType.Short) {
			val = ((Short) evt.getField(aggregateFld)) * 1.0;
		} else if (type == FieldType.Float) {
			val = ((Float) evt.getField(aggregateFld)) * 1.0;
		}
		if(val == null)
		{
			return null;
		}
		timestamp = System.currentTimeMillis();
		cache.put(timestamp, val);

		return null;
	}

	private GeoEvent createAggregateGeoEvent(Integer count, Double sum, Double mean, Double variance, Double sd)
			throws MessagingException, FieldException {
		try {
			if(gedout == null)
				return null;
			GeoEvent outevt = geoEventCreator.create(gedout.getGuid());
			outevt.setField("AGGREGATE_COUNT", count);
			outevt.setField("AGGREGATE_MEAN", mean);
			outevt.setField("AGGREGATE_STANDARD_DEVIATION", sd);
			outevt.setField("AGGREGATE_SUM", sum);
			outevt.setField("AGGREGATE_VARIANCE", variance);

			long now = System.currentTimeMillis();
			Date time = new Date(now);
			outevt.setField("TIMESTAMP", time);

			outevt.setProperty(GeoEventPropertyName.TYPE, "event");
			outevt.setProperty(GeoEventPropertyName.OWNER_ID, getId());
			outevt.setProperty(GeoEventPropertyName.OWNER_URI, definition.getUri());
			
			return outevt;
		} catch (MessagingException e) {
			throw (e);
		} catch (FieldException e) {
			throw (e);
		}
	}
	private synchronized void updateCache()
	{
		try {
			HashMap<Long, Double> workingCache = cache;
			Integer count = workingCache.size();
			if(count==0)
				return;
			boolean isEmpty = workingCache.isEmpty();
			if (isEmpty) {
				workingCache.clear();
				cache.clear();
				return;
			}
			Set<Long> keys = workingCache.keySet();
			Iterator<Long> it = keys.iterator();
			long now = System.currentTimeMillis();
			Double currentsum = 0.0;
			Double currentMean = 0.0;
			Double varsum = 0.0;

			//int count = 0;
			while (it.hasNext()) {
				long ts = it.next();
				if (now - ts > interval) {
					cache.remove(ts);
					workingCache.remove(ts);
				} else {
					currentsum += workingCache.get(ts);
					//count += 1;
				}
			}
			Double sum = currentsum;
			
			currentMean = sum / count;
			it = keys.iterator();
			while (it.hasNext()) {
				long ts = it.next();
				Double x = workingCache.get(ts);
				varsum += Math.pow((x - currentMean), 2);
			}
			
			Double variance = varsum / count;
			Double sd = Math.sqrt(variance);
			Double mean = currentMean;
			

			GeoEvent msg = createAggregateGeoEvent(count, sum, mean, variance, sd);
			send(msg);
		} catch (MessagingException | FieldException e) {
			LOG.error(e.getMessage());
		}
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
				//Thread.sleep(5);
				if (running) {
					updateCache();
				}
			} catch (Exception e) {

			}
		}

	}

}
	



