package com.esri.geoevent.solutions.processor.trackidle;

import java.util.ArrayList;
import java.util.List;

import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.DefaultGeoEventDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class TrackIdleProcessorDefinition extends
		GeoEventProcessorDefinitionBase {
	private static final BundleLogger LOGGER = BundleLoggerFactory
			.getLogger(TrackIdleProcessorDefinition.class);

	public TrackIdleProcessorDefinition()
	{
		try {
			List<LabeledValue> allowableValues = new ArrayList<>();
			allowableValues.add(new LabeledValue("OnChange", "OnChange"));
			allowableValues.add(new LabeledValue("Continuous", "Continuous"));

			propertyDefinitions
					.put("notificationMode",
							new PropertyDefinition(
									"notificationMode",
									PropertyType.String,
									"OnChange",
									"${com.esri.geoevent.solutions.processor.trackidle.trackidle-processor.PROCESSOR_NOTIFICATION_MODE_LBL}",
									"${com.esri.geoevent.solutions.processor.trackidle.trackidle-processor.PROCESSOR_NOTIFICATION_MODE_DESC}",
									true, false, allowableValues));
			propertyDefinitions
					.put("idleLimit",
							new PropertyDefinition(
									"idleLimit",
									PropertyType.Long,
									300,
									"${com.esri.geoevent.solutions.processor.trackidle.trackidle-processor.PROCESSOR_IDLE_LIMIT_LBL}",
									"${com.esri.geoevent.solutions.processor.trackidle.trackidle-processor.PROCESSOR_IDLE_LIMIT_DESC}",
									true, false));
			propertyDefinitions
					.put("tolerance",
							new PropertyDefinition(
									"tolerance",
									PropertyType.Long,
									120,
									"${com.esri.geoevent.solutions.processor.trackidle.trackidle-processor.PROCESSOR_TOLERANCE_LBL}",
									"${com.esri.geoevent.solutions.processor.trackidle.trackidle-processor.PROCESSOR_TOLERANCE_DESC}",
									true, false));
			propertyDefinitions
					.put("keepfields",
							new PropertyDefinition(
									"keepfields",
									PropertyType.Boolean,
									false,
									"${com.esri.geoevent.solutions.processor.trackidle.trackidle-processor.PROCESSOR_KEEPFIELDS_LBL}",
									"${com.esri.geoevent.solutions.processor.trackidle.trackidle-processor.PROCESSOR_KEEPFIELDS_DESC}",
									true, false));
			propertyDefinitions
					.put("outdefname",
							new PropertyDefinition(
									"outdefname",
									PropertyType.String,
									"",
									"${com.esri.geoevent.solutions.processor.trackidle.trackidle-processor.PROCESSOR_OUTDEFNAME_LBL}",
									"${com.esri.geoevent.solutions.processor.trackidle.trackidle-processor.PROCESSOR_OUTDEFNAME_DESC}",
									true, false));
		} catch (Exception error) {
			LOGGER.error("INIT_ERROR", error.getMessage());
			LOGGER.info(error.getMessage(), error);
		}
	}

	@Override
	public String getName()
	{
		return "TrackIdleProcessor";
	}

	@Override
	public String getDomain()
	{
		return "com.esri.geoevent.solutions.processor.trackidle";
	}

	@Override
	public String getVersion()
	{
		return "10.5.0";
	}

	@Override
	public String getLabel()
	{
		return "${com.esri.geoevent.solutions.processor.trackidle.trackidle-processor.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription()
	{
		return "${com.esri.geoevent.solutions.processor.trackidle.trackidle-processor.PROCESSOR_DESC}";
	}
	
	@Override
	public String getContactInfo()
	{
		return "patrick_hill@esri.com";
	}
}

