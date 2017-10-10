package com.esri.geoevent.solutions.processor.stwa;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class STWAProcessorDefinition extends
		GeoEventProcessorDefinitionBase {
	private static final Log LOG = LogFactory
			.getLog(STWAProcessorDefinition.class);
	public STWAProcessorDefinition() throws PropertyException
	{
		
		propertyDefinitions.put("interval", new PropertyDefinition("interval", PropertyType.Integer, 60000, "${com.esri.geoevent.solutions.processor.stwa.stwa-processor.LBL_INTERVAL}", "${com.esri.geoevent.solutions.processor.stwa.stwa-processor.DESC_INTERVAL}", true, false));
		propertyDefinitions.put("aggregate", new PropertyDefinition("aggregate", PropertyType.String, "", "${com.esri.geoevent.solutions.processor.stwa.stwa-processor.LBL_AGGREGATE_FLD}", "${com.esri.geoevent.solutions.processor.stwa.stwa-processor.DESC_AGGREGATE_FLD}", true, false));
		
	}
	
	@Override
	public String getName() {
		return "STWAProcessor";
	}

	@Override
	public String getDomain() {
		return "com.esri.geoevent.solutions.processor.stwa";
	}

	@Override
	public String getVersion() {
		return "10.5.0";
	}

	@Override
	public String getLabel() {
		return "${com.esri.geoevent.solutions.processor.stwa.stwa-processor.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription() {
		return "${com.esri.geoevent.solutions.processor.stwa.stwa-processor.PROCESSOR_DESC}";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}

}
