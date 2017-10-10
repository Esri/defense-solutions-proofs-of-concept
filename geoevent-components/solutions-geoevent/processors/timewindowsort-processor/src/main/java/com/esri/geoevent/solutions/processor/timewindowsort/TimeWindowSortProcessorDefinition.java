package com.esri.geoevent.solutions.processor.timewindowsort;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.arcgis.interop.Properties;
import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class TimeWindowSortProcessorDefinition extends
		GeoEventProcessorDefinitionBase {
	private static final Log LOG = LogFactory
			.getLog(TimeWindowSortProcessorDefinition.class);
	public TimeWindowSortProcessorDefinition() throws PropertyException
	{
		List<LabeledValue> allowedTypes = new ArrayList<LabeledValue>();
		allowedTypes.add(new LabeledValue("${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.AV_STRING}","string"));
		allowedTypes.add(new LabeledValue("${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.AV_INTEGER}","int"));
		allowedTypes.add(new LabeledValue("${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.AV_LONG}","long"));
		allowedTypes.add(new LabeledValue("${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.AV_SHORT}","short"));
		allowedTypes.add(new LabeledValue("${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.AV_DOUBLE}","double"));
		allowedTypes.add(new LabeledValue("${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.AV_FLOAT}","float"));
		allowedTypes.add(new LabeledValue("${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.AV_DATE}","date"));
		
		propertyDefinitions.put("interval", new PropertyDefinition("interval", PropertyType.Integer, 60000, "${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.LBL_INTERVAL}", "${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.DESC_INTERVAL}", true, false));
		propertyDefinitions.put("orderby", new PropertyDefinition("orderby", PropertyType.String, "", "${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.LBL_SORT_FLD}", "${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.DESC_SORT_FLD}", true, false));
		propertyDefinitions.put("expectedType", new PropertyDefinition("expectedType", PropertyType.String, "", "${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.LBL_EXPECTED}", "${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.DESC_EXPECTED}", true, false, allowedTypes));
	}
	
	@Override
	public String getName() {
		return "TimeWindowSortProcessor";
	}

	@Override
	public String getDomain() {
		return "com.esri.geoevent.solutions.processor.timewindowsort";
	}

	@Override
	public String getVersion() {
		return "10.3.0";
	}

	@Override
	public String getLabel() {
		return "${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription() {
		return "${com.esri.geoevent.solutions.processor.timewindowsort.tws-processor.PROCESSOR_DESC}";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}

}
