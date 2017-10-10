package com.esri.geoevent.solutions.processor.timetolong;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.esri.ges.core.ConfigurationException;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManagerException;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

public class TimeToLongProcessor extends GeoEventProcessorBase {
	private GeoEventDefinitionManager manager;
	public Messaging messaging;

	public TimeToLongProcessor(GeoEventProcessorDefinition definition)
			throws ComponentException {
		super(definition);
		// TODO Auto-generated constructor stub
	}

	private static final BundleLogger LOGGER = BundleLoggerFactory
			.getLogger(TimeToLongProcessor.class);
	private String timefld;
	private String numfld;
	private String newdefname;
	private Boolean useexisting;
	private String outputtype;
	private GeoEventDefinition ged;
	private Boolean createDef = false;
	private List<FieldDefinition> fds;

	@Override
	public boolean isGeoEventMutator() {
		return true;
	}

	@Override
	public GeoEvent process(GeoEvent evt) throws Exception {
		if (createDef) {
			createGeoEventDefinition(evt);
			createDef = false;
		}
		GeoEvent geOut = null;
		Date dt = (Date) evt.getField(timefld);
		long longdate = dt.getTime();
		if (useexisting) {
			geOut = evt;
			if (outputtype.equals("long")) {
				evt.setField(numfld, longdate);
			} else if (outputtype.equals("float")) {
				float floatdate = (float) longdate;
				evt.setField(numfld, floatdate);
			} else if (outputtype.equals("double")) {
				Double dbldate = longdate * 1.0;
				evt.setField(numfld, dbldate);
			}else if (outputtype.equals("string")) {
				String strdate = ((Long) longdate).toString();
				evt.setField(numfld, strdate);
			} 
		} else {

			if (outputtype.equals("long")) {

				GeoEventCreator geoEventCreator = messaging
						.createGeoEventCreator();
				geOut = geoEventCreator.create(ged.getGuid(), new Object[] {
						evt.getAllFields(), longdate });

			} else if (outputtype.equals("float")) {
				float floatdate = (float) longdate;
				GeoEventCreator geoEventCreator = messaging
						.createGeoEventCreator();
				geOut = geoEventCreator.create(ged.getGuid(), new Object[] {
						evt.getAllFields(), floatdate });
			} else if (outputtype.equals("double")) {
				Double dbldate = longdate * 1.0;
				GeoEventCreator geoEventCreator = messaging
						.createGeoEventCreator();
				geOut = geoEventCreator.create(ged.getGuid(), new Object[] {
						evt.getAllFields(), dbldate });
			} else if (outputtype.equals("string")) {
				String strdate = ((Long) longdate).toString();
				GeoEventCreator geoEventCreator = messaging
						.createGeoEventCreator();
				geOut = geoEventCreator.create(ged.getGuid(), new Object[] {
						evt.getAllFields(), strdate });
			}
			geOut.setProperty(GeoEventPropertyName.TYPE, "message");
			geOut.setProperty(GeoEventPropertyName.OWNER_ID, getId());
			geOut.setProperty(GeoEventPropertyName.OWNER_ID,
					definition.getUri());
		}
		return geOut;

	}

	@Override
	public void afterPropertiesSet() {
		timefld = properties.get("timefld").getValueAsString();
		numfld = properties.get("longfld").getValueAsString();
		newdefname = properties.get("newdef").getValueAsString();
		useexisting = (Boolean) properties.get("useexisting").getValue();
		outputtype = properties.get("outputtype").getValueAsString();

		if (!useexisting) {
			try {
				FieldDefinition fd = null;
				if (outputtype.equals("long")) {
					fd = new DefaultFieldDefinition(numfld, FieldType.Long);
				} else if (outputtype.equals("float")) {
					fd = new DefaultFieldDefinition(numfld, FieldType.Float);
				} else if (outputtype.equals("double")) {
					fd = new DefaultFieldDefinition(numfld, FieldType.Double);
				} else if (outputtype.equals("string")) {
					fd = new DefaultFieldDefinition(numfld, FieldType.String);
				}

				fds = new ArrayList<FieldDefinition>();
				fds.add(fd);
				if ((ged = manager.searchGeoEventDefinition(newdefname,
						definition.getUri().toString())) == null) {
					createDef = true;
				}
			} catch (ConfigurationException e) {
				LOGGER.error(e.getMessage());
			}
		}

	}

	// getters and setters
	public void setMessaging(Messaging messaging) {
		this.messaging = messaging;
	}

	public void setManager(GeoEventDefinitionManager manager) {
		this.manager = manager;
	}

	private void createGeoEventDefinition(GeoEvent event) {

		GeoEventDefinition eventDef = event.getGeoEventDefinition();
		try {
			ged = eventDef.augment(fds);
		} catch (ConfigurationException e) {
			LOGGER.error(e.getLocalizedMessage());
		}
		ged.setName(newdefname);
		ged.setOwner(definition.getUri().toString());
		try {
			manager.addGeoEventDefinition(ged);
		} catch (GeoEventDefinitionManagerException e) {
			LOGGER.error(e.getMessage());
		}
	}

}
