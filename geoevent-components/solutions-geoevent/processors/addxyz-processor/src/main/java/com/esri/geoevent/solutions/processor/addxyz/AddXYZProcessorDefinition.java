package com.esri.geoevent.solutions.processor.addxyz;

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class AddXYZProcessorDefinition extends GeoEventProcessorDefinitionBase {

	public AddXYZProcessorDefinition() throws PropertyException {
		PropertyDefinition pdGeometryFieldName = new PropertyDefinition("geofield", PropertyType.String, "", "${com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.LBL_GEO_FIELD}", "${com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.DESC_GEO_FIELD}", true, false);
		propertyDefinitions.put(pdGeometryFieldName.getPropertyName(), pdGeometryFieldName);
		
		PropertyDefinition pdGEDName = new PropertyDefinition("gedName", PropertyType.String, "AddXYZDef", "${com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.LBL_GEOEVENT_DEFINITION_NAME}", "${com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.DESC_GEOEVENT_DEFINITION_NAME}", true, false);
		propertyDefinitions.put(pdGEDName.getPropertyName(), pdGEDName);
		
		PropertyDefinition pdXField = new PropertyDefinition("xfield", PropertyType.String, "x", "${com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.LBL_X_FIELD}", "${com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.DESC_X_FIELD}", true, false);
		propertyDefinitions.put(pdXField.getPropertyName(), pdXField);
		
		PropertyDefinition pdYField = new PropertyDefinition("yfield", PropertyType.String, "y", "${com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.LBL_Y_FIELD}", "${com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.DESC_Y_FIELD}", true, false);
		propertyDefinitions.put(pdYField.getPropertyName(), pdYField);
		
		PropertyDefinition pdZField = new PropertyDefinition("zfield", PropertyType.String, "z", "${com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.LBL_Z_FIELD}", "${com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.DESC_Z_FIELD}", false, false);
		propertyDefinitions.put(pdZField.getPropertyName(), pdZField);
	}
	
	@Override
	public String getName() {
		return "AddXYZProcessor";
	}

	@Override
	public String getDomain() {
		return "com.esri.geoevent.solutions.processor.addxyz";
	}

	@Override
	public String getVersion() {
		return "10.5.0";
	}

	@Override
	public String getLabel() {
		return "${com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription() {
		return "${com.esri.geoevent.solutions.processor.addxyz.addxyz-processor.PROCESSOR_DESCRIPTION}";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}


}
