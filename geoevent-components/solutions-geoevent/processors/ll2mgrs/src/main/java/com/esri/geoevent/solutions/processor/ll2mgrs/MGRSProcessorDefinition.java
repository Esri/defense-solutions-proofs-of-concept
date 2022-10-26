package com.esri.geoevent.solutions.processor.ll2mgrs;

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class MGRSProcessorDefinition extends GeoEventProcessorDefinitionBase{
	
	
	private String lblGeoField = "${com.esri.geoevent.processor.latlong-mgrs-converter-processors.LBL_GEO_FLD}";
	private String descGeoField= "${com.esri.geoevent.processor.latlong-mgrs-converter-processors.DESC_GEO_FLD}";
	private String lblNewDef = "${com.esri.geoevent.processor.latlong-mgrs-converter-processors.LBL_NEW_DEF}";
	private String descNewDef = "${com.esri.geoevent.processor.latlong-mgrs-converter-processors.DESC_NEW_DEF}";
	private String lblAccuracy = "${com.esri.geoevent.processor.latlong-mgrs-converter-processors.LBL_ACCURACY}";
	private String descAccuracy= "${com.esri.geoevent.processor.latlong-mgrs-converter-processors.DESC_ACCURACY}";
	
	public MGRSProcessorDefinition() {
		try {
			propertyDefinitions.put("geofld", new PropertyDefinition("geofld", PropertyType.String, "GEOMETRY", lblGeoField, descGeoField, true, false));
			propertyDefinitions.put("eventdef", new PropertyDefinition("eventdef", PropertyType.String, "", lblNewDef, descNewDef, true, false));
			propertyDefinitions.put("accuracy", new PropertyDefinition("accuracy", PropertyType.Integer, 5, lblAccuracy, descAccuracy, true, false));
		} catch (PropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Override
	public String getName() {
		return "LL2MGRS";
	}

	@Override
	public String getDomain() {
		return "com.esri.geoevent.solutions.processor.ll2mgrs";
	}

	@Override
	public String getVersion() {
		return "10.6.0";
	}

	@Override
	public String getLabel() {
		return "${com.esri.geoevent.processor.latlong-mgrs-converter-processors.LBL_LL2MGRS_PROCESSOR}";
	}

	@Override
	public String getDescription() {
		return "${com.esri.geoevent.processor.latlong-mgrs-converter-processors.DESC_LL2MGRS_PROCESSOR}";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}

}
