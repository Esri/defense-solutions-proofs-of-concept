package com.esri.geoevent.solutions.processor.trackidle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
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
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManagerException;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import com.esri.ges.util.Converter;
import com.esri.ges.util.Validator;

public class TrackIdleProcessor extends GeoEventProcessorBase {
	private static final BundleLogger LOGGER = BundleLoggerFactory
			.getLogger(TrackIdleProcessor.class);

	private TrackIdleProcessorNotificationMode notificationMode;
	private long idleLimit;
	private GeoEventCreator geoEventCreator;
	private GeoEventDefinitionManager manager;
	private String outDefName;
	private long tolerance;
	private Boolean keepFields;
	private List<FieldDefinition> fds;
	private Boolean createDef = false;
	private GeoEventDefinition ged;
	private final Map<String, TrackIdleProcessorStart> trackIdles = new ConcurrentHashMap<String, TrackIdleProcessorStart>();

	protected TrackIdleProcessor(GeoEventProcessorDefinition definition)
			throws ComponentException {
		super(definition);
	}

	public void afterPropertiesSet() {
		notificationMode = Validator.valueOfIgnoreCase(
				TrackIdleProcessorNotificationMode.class,
				getProperty("notificationMode").getValueAsString(),
				TrackIdleProcessorNotificationMode.OnChange);
		idleLimit = Converter.convertToInteger(getProperty("idleLimit")
				.getValueAsString(), 300);
		tolerance = Converter.convertToLong(getProperty("tolerance")
				.getValueAsString(), 50l);
		keepFields = (Boolean) getProperty("keepfields").getValue();
		outDefName = getProperty("outdefname").getValueAsString();

	
		fds = new ArrayList<FieldDefinition>();
		
		try {
			//fds.add(new DefaultFieldDefinition("trackId", FieldType.String,
					//"TRACK_ID"));
			fds.add(new DefaultFieldDefinition("idle", FieldType.Boolean));
			fds.add(new DefaultFieldDefinition("idleDuration", FieldType.Double));
			fds.add(new DefaultFieldDefinition("idleStart", FieldType.Date));
			//fds.add(new DefaultFieldDefinition("geometry", FieldType.Geometry));

			if ((ged = manager.searchGeoEventDefinition(outDefName, definition.getUri().toString())) == null)
			{
				createDef = true;
			}
		} catch (ConfigurationException e) {

		}

		// geoEventDefinitions.put(ged.getName(), ged);
	}

	@Override
	public GeoEvent process(GeoEvent geoEvent) throws Exception {
		GeoEvent msg = null;

		if (createDef) {
			createGeoEventDefinition(geoEvent, keepFields);
			createDef=false;
		}

		if (geoEvent.getTrackId() == null || geoEvent.getGeometry() == null) {
			LOGGER.warn("NULL_ERROR");
			return null;
		}
		if (trackIdles == null) {
			LOGGER.warn("TRACK_IDLES_NULL");
			return null;
		}
		try {
			String cacheKey = buildCacheKey(geoEvent);
			TrackIdleProcessorStart idleStart = trackIdles.get(cacheKey);
			Date startTime = (Date)geoEvent.getField("TIME_START");
			long currentStartTime = startTime.getTime();
			if (idleStart != null && idleStart.getGeometry() != null) {
				if (!hasGeometryMoved(geoEvent.getGeometry(),
						idleStart.getGeometry(), tolerance)) {
					
					double idleDuration = (currentStartTime - idleStart
							.getStartTime().getTime()) / 1000.0;
					idleDuration = idleDuration >= 0 ? idleDuration
							: -idleDuration;
					idleDuration = Math.round(idleDuration * 10.0) / 10.0;
					if (idleDuration >= idleLimit) {
						idleStart.setIdleDuration(idleDuration);

						if (notificationMode == TrackIdleProcessorNotificationMode.Continuous)
							msg = createTrackIdleGeoEvent(idleStart, true,
									geoEvent, ged);
						else if (!idleStart.isIdling())
							msg = createTrackIdleGeoEvent(idleStart, true,
									geoEvent, ged);
						idleStart.setIdling(true);

					}
				}
				else
				{
					if (idleStart.isIdling())
					{
						msg = createTrackIdleGeoEvent(idleStart, false, geoEvent, ged);
					}
					idleStart.setGeometry(geoEvent.getGeometry());
					idleStart.setStartTime(geoEvent.getStartTime());
					idleStart.setIdling(false);
				}
			} else {
				trackIdles.put(
						cacheKey,
						new TrackIdleProcessorStart(geoEvent.getTrackId(), startTime, geoEvent.getGeometry()));
			}
		} catch (Exception error) {
			LOGGER.error(error.getMessage(), error);
		}
		return msg;
	}

	@Override
	public void validate() throws ValidationException {
		super.validate();
		List<String> errors = new ArrayList<String>();
		if (idleLimit <= 0)
			errors.add(LOGGER.translate("VALIDATION_GAP_DURATION_INVALID",
					definition.getName()));

		if (errors.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (String message : errors)
				sb.append(message).append("\n");
			throw new ValidationException(LOGGER.translate("VALIDATION_ERROR",
					this.getClass().getName(), sb.toString()));
		}
	}

	private GeoEvent processGeoEvent(GeoEvent geoEvent)
			throws GeoEventDefinitionManagerException {
		GeoEvent geoevent = null;

		if (createDef) {
			createGeoEventDefinition(geoEvent, keepFields);
			createDef=false;
		}

		if (geoEvent.getTrackId() == null || geoEvent.getGeometry() == null) {
			LOGGER.warn("NULL_ERROR");
			return null;
		}
		if (trackIdles == null) {
			LOGGER.warn("TRACK_IDLES_NULL");
			return null;
		}
		try {
			String cacheKey = buildCacheKey(geoEvent);
			TrackIdleProcessorStart idleStart = trackIdles.get(cacheKey);
			Date startTime = (Date)geoEvent.getField("TIME_START");
			long currentStartTime = startTime.getTime();
			if (idleStart != null && idleStart.getGeometry() != null) {
				if (!hasGeometryMoved(geoEvent.getGeometry(),
						idleStart.getGeometry(), tolerance)) {
					
					double idleDuration = (currentStartTime - idleStart
							.getStartTime().getTime()) / 1000.0;
					idleDuration = idleDuration >= 0 ? idleDuration
							: -idleDuration;
					idleDuration = Math.round(idleDuration * 10.0) / 10.0;
					if (idleDuration >= idleLimit) {
						idleStart.setIdleDuration(idleDuration);

						if (notificationMode == TrackIdleProcessorNotificationMode.Continuous)
							geoevent = createTrackIdleGeoEvent(idleStart, true,
									geoEvent, ged);
						else if (!idleStart.isIdling())
							geoevent = createTrackIdleGeoEvent(idleStart, true,
									geoEvent, ged);
						idleStart.setIdling(true);

					}
				}
				else
				{
					if (idleStart.isIdling())
					{
						geoevent = createTrackIdleGeoEvent(idleStart, false, geoEvent, ged);
					}
					idleStart.setGeometry(geoEvent.getGeometry());
					idleStart.setStartTime(geoEvent.getStartTime());
					idleStart.setIdling(false);
				}
			} else {
				trackIdles.put(
						cacheKey,
						new TrackIdleProcessorStart(geoEvent.getTrackId(), startTime, geoEvent.getGeometry()));
			}
		} catch (Exception error) {
			LOGGER.error(error.getMessage(), error);
		}
		return geoevent;
	}
	
	private void createGeoEventDefinition(GeoEvent event, Boolean retainFlds)
	{
		if (keepFields) {
			GeoEventDefinition eventDef = event.getGeoEventDefinition();
			try {
				ged = eventDef.augment(fds);
			} catch (ConfigurationException e) {
				LOGGER.error(e.getLocalizedMessage());
			}
		} else {
			ged = new DefaultGeoEventDefinition();
			FieldDefinition trackidFD = event.getGeoEventDefinition()
					.getFieldDefinition("TRACK_ID");
			fds.add(trackidFD);
			FieldDefinition geoFD = event.getGeoEventDefinition()
					.getFieldDefinition("GEOMETRY");
			fds.add(geoFD);
			ged.setFieldDefinitions(fds);
		}
		ged.setName(outDefName);
		ged.setOwner(definition.getUri().toString());
		try {
			manager.addGeoEventDefinition(ged);
		} catch (GeoEventDefinitionManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private boolean hasGeometryMoved(MapGeometry geom1, MapGeometry geom2,
			double tolerance) {
		if (geom1 != null && geom1.getGeometry() != null
				&& geom1.getGeometry().getType() == Type.Point && geom2 != null
				&& geom2.getGeometry() != null
				&& geom2.getGeometry().getType() == Type.Point) {
			Point corePt1 = (Point) geom1.getGeometry();
			Point corePt2 = (Point) geom2.getGeometry();
			double meters = 0.0;
			try {
				meters = GeometryEngine.geodesicDistanceOnWGS84(corePt1,
						corePt2);
			} catch (Throwable error) {
				LOGGER.error(error.getMessage());
			}

			double feet = meter2feet(meters);
			if (feet >= tolerance)
				return true;
			else
				return false;
		} else {
			throw new RuntimeException(
					LOGGER.translate("INVALID_GEOMETRY_TYPE"));
		}
	}

	private double meter2feet(double meter) {
		return meter * 3.28084;
	}

	

	private GeoEvent createTrackIdleGeoEvent(TrackIdleProcessorStart idleStart,
			boolean isIdle, GeoEvent oEvent, GeoEventDefinition ged)
			throws MessagingException {
		GeoEvent idleEvent = null;
		if (geoEventCreator != null) {
			try {
				idleEvent = geoEventCreator.create(outDefName, definition.getUri().toString());
				// idleEvent.setField("trackId", idleStart.getTrackId());
				idleEvent.setField("idle", isIdle);
				idleEvent.setField("idleDuration", idleStart.getIdleDuration());
				idleEvent.setField("idleStart", idleStart.getStartTime());
				// idleEvent.setField("GEOMETRY", idleStart.getGeometry());
				if (keepFields) {
					for (FieldDefinition fd : oEvent.getGeoEventDefinition()
							.getFieldDefinitions()) {
						idleEvent.setField(fd.getName(),
								oEvent.getField(fd.getName()));
					}

				} else {
					for (FieldDefinition fd : oEvent.getGeoEventDefinition()
							.getFieldDefinitions()) {
						if (fd.getTags().contains("TRACK_ID")
								|| fd.getTags().contains("GEOMETRY")) {
							idleEvent.setField(fd.getName(),
									oEvent.getField(fd.getName()));
						}
					}
				}
				idleEvent.setProperty(GeoEventPropertyName.TYPE, "event");
				idleEvent.setProperty(GeoEventPropertyName.OWNER_ID, getId());
				idleEvent.setProperty(GeoEventPropertyName.OWNER_URI,
						definition.getUri());
			} catch (FieldException error) {
				idleEvent = null;
				LOGGER.error("GEOEVENT_CREATION_ERROR", error.getMessage());
				LOGGER.info(error.getMessage(), error);
			}
		}
		return idleEvent;
	}

	private String buildCacheKey(GeoEvent geoEvent) {
		if (geoEvent != null && geoEvent.getTrackId() != null) {
			GeoEventDefinition definition = geoEvent.getGeoEventDefinition();
			return definition.getOwner() + "/" + definition.getName() + "/"
					+ geoEvent.getTrackId();
		}
		return null;
	}

	public void setMessaging(Messaging messaging) {
		geoEventCreator = messaging.createGeoEventCreator();
	}

	public void setManager(GeoEventDefinitionManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void shutdown()
	{
		super.shutdown();
		//GeoEvent Extension is shutting down
	}
	
	@Override
	public void onServiceStart()
	{
		//GeoEvent Service is starting
	}
	
	@Override
	public void onServiceStop(){
		//GeoEvent Service is stopping
	}
	
	@Override
	public boolean isGeoEventMutator()
	{
		//Must return true if processor is going to 
		//Modify the GeoEvent passed in.
		return false;
	}
	
	
}
