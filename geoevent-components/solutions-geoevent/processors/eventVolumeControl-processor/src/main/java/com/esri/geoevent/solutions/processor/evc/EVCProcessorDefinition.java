package com.esri.geoevent.solutions.processor.evc;

import java.util.ArrayList;
import java.util.List;

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;
import com.esri.ges.core.property.LabeledValue;
public class EVCProcessorDefinition extends GeoEventProcessorDefinitionBase
{
	private String allowedValInterval = "${com.esri.geoevent.solutions.processor.evc.eventVolumeControl-processor.INTERVAL_ALLOWEDVALUE_INTERVAL}";
	private String allowedValMaxPerInterval = "${com.esri.geoevent.solutions.processor.evc.eventVolumeControl-processor.INTERVAL_ALLOWEDVALUE_MAX_PER_INTERVAL}";
	private String lblFilterType = "${com.esri.geoevent.solutions.processor.evc.eventVolumeControl-processor.LBL_FILTER_TYPE}";
	private String descFilterType = "${com.esri.geoevent.solutions.processor.evc.eventVolumeControl-processor.DESC_FILTER_TYPE}";
	private String lblMaxPerInterval = "${com.esri.geoevent.solutions.processor.evc.eventVolumeControl-processor.LBL_EVENTS_PER_INTERVAL}";
	private String descMaxPerInterval = "${com.esri.geoevent.solutions.processor.evc.eventVolumeControl-processor.DESC_EVENTS_PER_INTERVAL}";
	public EVCProcessorDefinition()
	{
		try {
			PropertyDefinition pdInterval = new PropertyDefinition("interval", PropertyType.Long, 10000, "Interval (miliseconds)", "Amount of time between which new messages with the same track id will be dropped", true, false);
			propertyDefinitions.put(pdInterval.getPropertyName(), pdInterval);
			
			List<LabeledValue> allowedVals = new ArrayList<LabeledValue>();
			allowedVals.add(new LabeledValue(allowedValInterval,"byInterval" ));
			allowedVals.add(new LabeledValue(allowedValMaxPerInterval, "maxPerInterval"));
			PropertyDefinition pdFilterType = new PropertyDefinition("filterType", PropertyType.String, allowedValInterval, lblFilterType, descFilterType, true, false, allowedVals);

			propertyDefinitions.put(pdFilterType.getPropertyName(), pdFilterType);
			
			PropertyDefinition pdEPI = new PropertyDefinition("epi", PropertyType.Long, 100, lblMaxPerInterval, descMaxPerInterval, true, false);
			pdEPI.setDependsOn("filterType=maxPerInterval");
			propertyDefinitions.put(pdEPI.getPropertyName(), pdEPI);
			
		} catch (PropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getName()
	{
		return "EVCProcessor";
	}

	@Override
	public String getDomain()
	{
		return "com.esri.geoevent.solutions.processor.evc";
	}

	@Override
	public String getVersion()
	{
		return "10.5.0";
	}

	@Override
	public String getLabel()
	{
		return "${com.esri.geoevent.solutions.processor.evc.eventVolumeControl-processor.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription()
	{
		return "${com.esri.geoevent.solutions.processor.evc.eventVolumeControl-processor.PROCESSOR_DESCRIPTION}";
	}

	@Override
	public String getContactInfo()
	{
		return "geoeventprocessor@esri.com";
	}
}
