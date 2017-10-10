package com.esri.geoevent.solutions.processor.timetolong;

import java.util.ArrayList;
import java.util.List;

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;
import com.esri.ges.core.property.LabeledValue;

public class TimeToLongDefinition extends GeoEventProcessorDefinitionBase{
	String timefldlbl = "${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.TIME_FLD_LBL}";
	String timeflddesc="${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.TIME_FLD_DESC}";
	String useexistinglbl = "${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.USE_EXISTING_LBL}";
	String useexistingdesc = "${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.USE_EXISTING_DESC}";
	String longfldlbl="${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.LONG_FLD_LBL}";
	String longflddesc = "${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.LONG_FLD_DESC}";
	String newdeflbl="${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.EVENT_DEF_LBL}";
	String newdefdesc="${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.EVENT_DEF_DESC}";
	String converttolbl="${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.CONVERT_TO_LBL}";
	String converttodesc="${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.CONVERT_TO_DESC}";
	String lvlong="${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.TYPE_LONG}";
	String lvfloat="${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.TYPE_FLOAT}";
	String lvdouble="${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.TYPE_DOUBLE}";
	String lvstring="${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.TYPE_STRING}";

	public TimeToLongDefinition() throws PropertyException
	{
		List<LabeledValue> allowedValues = new ArrayList<LabeledValue>();
		allowedValues.add(new LabeledValue(lvlong, "long"));
		allowedValues.add(new LabeledValue(lvfloat, "float"));
		allowedValues.add(new LabeledValue(lvdouble, "double"));
		allowedValues.add(new LabeledValue(lvstring, "string"));
		
		propertyDefinitions.put("timefld", new PropertyDefinition("timefld", PropertyType.String, "", timefldlbl, timeflddesc, true, false));
		propertyDefinitions.put("outputtype", new PropertyDefinition("outputtype", PropertyType.String, "", converttolbl, converttodesc, true, false, allowedValues));
		propertyDefinitions.put("useexisting", new PropertyDefinition("useexisting", PropertyType.Boolean, true, useexistinglbl, useexistingdesc, true, false));
		propertyDefinitions.put("newdef", new PropertyDefinition("newdef", PropertyType.String, "DateTimeToNumber", newdeflbl, newdefdesc, "useexisting=false",true, false));
		propertyDefinitions.put("longfld", new PropertyDefinition("longfld", PropertyType.String, "", longfldlbl, longflddesc, true, false));
	}
	@Override
	public String getName() {
		return "TimeToLongProcessor";
	}

	@Override
	public String getDomain() {
		return "com.esri.geoevent.solutions.processor.timetolong";
	}

	@Override
	public String getVersion() {
		return "10.5.0";
	}

	@Override
	public String getLabel() {
		return "${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription() {
		return "${com.esri.geoevent.solutions.processor.timetolong.timetolong-processor.PROCESSOR_DESCRIPTION}";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}

}
