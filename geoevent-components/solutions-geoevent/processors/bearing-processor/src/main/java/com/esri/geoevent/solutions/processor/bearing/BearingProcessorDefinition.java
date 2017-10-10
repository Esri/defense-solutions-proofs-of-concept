package com.esri.geoevent.solutions.processor.bearing;
import java.util.ArrayList;
import java.util.List;

import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;


public class BearingProcessorDefinition extends GeoEventProcessorDefinitionBase {
	private String srcGeoLbl = "${com.esri.geoevent.solutions.processor.bearing.bearing-processor.SRC_GEO_LBL}";
	private String srcCoordLbl = "${com.esri.geoevent.solutions.processor.bearing.bearing-processor.SRC_COORD_LBL}";
	private String lblOSrc="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.LBL_ORIGIN_SOURCE}";
	private String descOSrc="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DESC_ORIGIN_SRC}";
	private String lblOFld="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.LBL_ORIGIN_FIELD}";
	private String descOFld="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DESC_ORIGIN_FIELD}";
	private String lblOX="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.LBL_ORIGIN_X}";
	private String descOX="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DESC_ORIGIN_X}";
	private String lblOY="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.LBL_ORIGIN_Y}";
	private String descOY="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DESC_ORIGIN_Y}";
	private String lblDSrc="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.LBL_DESTINATION_SOURCE}";
	private String descDSrc="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DESC_DESTINATION_SRC}";
	private String lblDFld="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.LBL_DESTINATION_FIELD}";
	private String descDFld="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DESC_DESTINATION_FIELD}";
	private String lblDX="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.LBL_DESTINATION_X}";
	private String descDX="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DESC_DESTINATION_X}";
	private String lblDY="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.LBL_DESTINATION_Y}";
	private String descDY="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DESC_DESTINATION_Y}";
	private String defaultSrc="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DEFAULT_SRC}";
	private String lblNewFld="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.LBL_NEW_FIELD}";
	private String descNewFld="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DESC_NEW_FLD}";
	private String lblGenerateGeo="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.LBL_GENERATE_GEO}";
	private String descGenerateGeo="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DESC_GENERATE_GEO}";
	private String lblEventDefName="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.LBL_GEOEVENT_DEFINITION_NAME}";
	private String descEventDefName="${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DESC_GEOEVENT_DEFINITION_NAME}";
	private String lblWkidOut = "${com.esri.geoevent.solutions.processor.bearing.bearing-processor.LBL_WKID}";
	private String descWkidOut = "${com.esri.geoevent.solutions.processor.bearing.bearing-processor.DESC_WKID}";

	public BearingProcessorDefinition() throws PropertyException {
		
		List<LabeledValue> oSrcValues = new ArrayList<LabeledValue>();
		oSrcValues.add(new LabeledValue(srcGeoLbl, "geo"));
		oSrcValues.add(new LabeledValue(srcCoordLbl, "coord"));
		
		List<LabeledValue> dSrcValues = new ArrayList<LabeledValue>();
		dSrcValues.add(new LabeledValue(srcGeoLbl, "geo"));
		dSrcValues.add(new LabeledValue(srcCoordLbl, "coord"));
		
		PropertyDefinition pdOriginSource = new PropertyDefinition("osrc", PropertyType.String, srcGeoLbl, lblOSrc, descOSrc, true, false, oSrcValues);
		propertyDefinitions.put(pdOriginSource.getPropertyName(), pdOriginSource);

		PropertyDefinition pdOGeoFld = new PropertyDefinition("oGeoFld", PropertyType.String, "ORIGIN_GEOMETRY_FIELD", lblOFld, descOFld, false, false);
		pdOGeoFld.setDependsOn("osrc=geo");
		propertyDefinitions.put(pdOGeoFld.getPropertyName(), pdOGeoFld);
		
		PropertyDefinition pdOXField = new PropertyDefinition("oxFld", PropertyType.String, "ORIGIN_X_FIELD", lblOX, descOX, false, false);
		pdOXField.setDependsOn("osrc=coord");
		propertyDefinitions.put(pdOXField.getPropertyName(), pdOXField);
		
		PropertyDefinition pdOYField = new PropertyDefinition("oyFld", PropertyType.String, "ORIGIN_Y_FIELD", lblOY, descOY, false, false);
		pdOYField.setDependsOn("osrc=coord");
		propertyDefinitions.put(pdOYField.getPropertyName(),pdOYField);
		
		PropertyDefinition pdDestinationSource = new PropertyDefinition("dsrc", PropertyType.String, defaultSrc, lblDSrc, "", true, false, dSrcValues);
		propertyDefinitions.put(pdDestinationSource.getPropertyName(),pdDestinationSource);
				
		PropertyDefinition pdDGeoFld = new PropertyDefinition("dGeoFld", PropertyType.String, "DESTINATION_GEOMETRY_FIELD", lblDFld, descDFld, false, false);
		pdDGeoFld.setDependsOn("dsrc=geo");
		propertyDefinitions.put(pdDGeoFld.getPropertyName(),pdDGeoFld);
		
		PropertyDefinition pdDXField = new PropertyDefinition("dxFld", PropertyType.String, "DESTINATION_X_FIELD", lblDX, descDX, false, false);
		pdDXField.setDependsOn("dsrc=coord");
		propertyDefinitions.put(pdDXField.getPropertyName(),pdDXField);
		
		PropertyDefinition pdDYField = new PropertyDefinition("dyFld", PropertyType.String, "DESTINATION_Y_FIELD", lblDY, descDY, false, false);
		pdDYField.setDependsOn("dsrc=coord");
		propertyDefinitions.put(pdDYField.getPropertyName(),pdDYField);
		
		PropertyDefinition pdBearingField = new PropertyDefinition("newfld", PropertyType.String, "bearing", lblNewFld, descNewFld, true, false);
		propertyDefinitions.put(pdBearingField.getPropertyName(), pdBearingField);
		
		PropertyDefinition pdGenerateGeometry = new PropertyDefinition("generateGeo", PropertyType.Boolean, true, lblGenerateGeo, descGenerateGeo, true, false);
		propertyDefinitions.put(pdGenerateGeometry.getPropertyName(), pdGenerateGeometry);
		
		PropertyDefinition pdOutWkid = new PropertyDefinition("wkidout", PropertyType.Integer, 4326, lblWkidOut, descWkidOut, "generateGeo=true", false, false);
		propertyDefinitions.put(pdOutWkid.getPropertyName(), pdOutWkid);
		
		PropertyDefinition pdNewGeoDef = new PropertyDefinition("newdef", PropertyType.String, "calculate_bearing", lblEventDefName, descEventDefName, true, false);
		propertyDefinitions.put(pdNewGeoDef.getPropertyName(), pdNewGeoDef);
	}
	@Override
	public String getName(){
		return "BearingProcessor";
	}
	
	@Override
	public String getDomain() {
		return "com.esri.geoevent.solutions.processor.bearing";
	}

	@Override
	public String getVersion() {
		return "10.5.0";
	}

	@Override
	public String getLabel() {
		return "${com.esri.geoevent.solutions.processor.bearing.bearing-processor.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription() {
		return "${com.esri.geoevent.solutions.processor.bearing.bearing-processor.PROCESSOR_DESCRIPTION}";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}
}
