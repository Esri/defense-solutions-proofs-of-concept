package com.esri.geoevent.solutions.processor.unitconversion;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class UnitConversionProcessorDefinition extends
		GeoEventProcessorDefinitionBase {

	private String lblVelInputType = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.LBL_VELOCITY_INPUT_TYPE}";
	private String descVelInputType = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.DESC_VELOCITY_INPUT_TYPE}";
	private String lblVelField = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.LBL_VELOCITY_FIELD}";
	private String descVelField = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.DESC_VELOCITY_FIELD}";
	private String lblVelInputUnit = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.LBL_VELOCITY_INPUT_UNIT}";
	private String descVelInputUnit = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.DESC_VELOCITY_INPUT_UNIT}";
	private String lblVelOutputUnit = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.LBL_VELOCITY_OUTPUT_UNIT}";
	private String descVelOutputUnit = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.DESC_VELOCITY_OUT_UNIT}";
	private String lblAltInputType = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.LBL_ALTITUDE_INPUT_TYPE}";
	private String descAltInputType = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.DESC_ALTITUDE_INPUT_TYPE}";
	private String lblAltField = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.LBL_ALTITUDE_FIELD}";
	private String descAltField = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.DESC_ALTITUDE_FIELD}";
	private String lblAltInputUnit = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.LBL_ALTITUDE_INPUT_UNIT}";
	private String descAltInputUnit = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.DESC_ALTITUDE_INPUT_UNIT}";
	private String lblAltOutputUnit = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.LBL_ALTITUDE_OUTPUT_UNIT}";
	private String descAltOutputUnit = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.DESC_ALTITUDE_OUT_UNIT}";
	private String lblFreqInputType = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.LBL_FREQUENCY_INPUT_TYPE}";
	private String descFreqInputType = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.DESC_FREQUENCY_INPUT_TYPE}";
	private String lblFreqField = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.LBL_FREQUENCY_FIELD}";
	private String descFreqField = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.DESC_FREQUENCY_FIELD}";
	private String lblFreqInputUnit = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.LBL_FREQUENCY_INPUT_UNIT}";
	private String descFreqInputUnit = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.DESC_FREQUENCY_INPUT_UNIT}";
	private String lblFreqOutputUnit = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.LBL_FREQUENCY_OUTPUT_UNIT}";
	private String descFreqOutputUnit = "${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.DESC_FREQUENCY_OUT_UNIT}";
	private static final Log LOG = LogFactory
			.getLog(UnitConversionProcessorDefinition.class);

	public UnitConversionProcessorDefinition() throws PropertyException {
		try {
			List<LabeledValue> allowedIn = new ArrayList<LabeledValue>();
			allowedIn
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.INPUT_FIELD_LBL}",
							"Field"));
			allowedIn
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.INPUT_NA_LBL}",
							"NA"));

			PropertyDefinition pdVInputType = new PropertyDefinition("input-v",
					PropertyType.String, "", lblVelInputType, descVelInputType,
					true, false, allowedIn);
			propertyDefinitions.put(pdVInputType.getPropertyName(),
					pdVInputType);

			PropertyDefinition pdVMan = new PropertyDefinition(
					"velocity-manual", PropertyType.String, "", lblVelField,
					descVelField, false, false);
			pdVMan.setDependsOn("input-v=Field");
			propertyDefinitions.put(pdVMan.getPropertyName(), pdVMan);

			List<LabeledValue> allowedVelocities = new ArrayList<LabeledValue>();
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_MILES_PER_HOUR}",
							"Miles/Hour"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_METERS_PER_HOUR}",
							"Meters/Hour"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_KILOMETERS_PER_HOUR}",
							"Kilometers/Hour"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_HECTOMETERS_PER_HOUR}",
							"Hectometers/Hour"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_KNOTS}",
							"Knots"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_FEET_PER_HOUR}",
							"Feet/Hour"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_MILES_PER_MINUTE}",
							"Miles/Minute"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_METERS_PER_MINUTE}",
							"Meters/Minute"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_KILOMETERS_PER_MINUTE}",
							"Kilometers/Minute"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_HECTOMETERS_PER_MINUTE}",
							"Hectometers/Minute"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_NAUTICAL_MILES_PER_MINUTE}",
							"Nautical Miles/Minute"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_FEET_PER_MINUTE}",
							"Feet/Minute"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_MILES_PER_SECOND}",
							"Miles/Second"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_METERS_PER_SECOND}",
							"Meters/Second"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_KILOMETERS_PER_SECOND}",
							"Kilometers/Second"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_HECTOMETERS_PER_SECOND}",
							"Hectometers/Second"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_NAUTICAL_MILES_PER_SECOND}",
							"Nautical Miles/Second"));
			allowedVelocities
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.VELOCITY_FEET_PER_SECOND}",
							"Feet/Second"));
			PropertyDefinition pdVIn = new PropertyDefinition("vin",
					PropertyType.String, "", lblVelInputUnit, descVelInputUnit,
					false, false, allowedVelocities);
			propertyDefinitions.put(pdVIn.getPropertyName(), pdVIn);

			PropertyDefinition pdVOut = new PropertyDefinition("vout",
					PropertyType.String, "", lblVelOutputUnit,
					descVelOutputUnit, false, false, allowedVelocities);
			propertyDefinitions.put(pdVOut.getPropertyName(), pdVOut);

			PropertyDefinition pdAInputType = new PropertyDefinition("input-a",
					PropertyType.String, "", lblAltInputType, descAltInputType,
					false, false, allowedIn);
			propertyDefinitions.put(pdAInputType.getPropertyName(),
					pdAInputType);

			PropertyDefinition pdAMan = new PropertyDefinition("alt-manual",
					PropertyType.String, "", lblAltField, descAltField, false,
					false);
			pdAMan.setDependsOn("input-a=Field");
			propertyDefinitions.put(pdAMan.getPropertyName(), pdAMan);

			List<LabeledValue> allowedAltitudes = new ArrayList<LabeledValue>();
			allowedAltitudes
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.ALTITUDE_METERS}",
							"Meters"));
			allowedAltitudes
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.ALTITUDE_FEET}",
							"Feet"));
			allowedAltitudes
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.ALTITUDE_MILES}",
							"Miles"));
			allowedAltitudes
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.ALTITUDE_KILOMETERS}",
							"Kilometers"));
			allowedAltitudes
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.ALTITUDE_HECTOMETERS}",
							"Hectometers"));
			allowedAltitudes
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.ALTITUDE_NM}",
							"Nautical Miles"));

			PropertyDefinition pdAltIn = new PropertyDefinition("altin",
					PropertyType.String, "", lblAltInputUnit, descAltInputUnit,
					false, false, allowedAltitudes);
			propertyDefinitions.put(pdAltIn.getPropertyName(), pdAltIn);

			PropertyDefinition pdAltOut = new PropertyDefinition("altout",
					PropertyType.String, "", lblAltOutputUnit,
					descAltOutputUnit, false, false, allowedAltitudes);
			propertyDefinitions.put(pdAltOut.getPropertyName(), pdAltOut);

			PropertyDefinition pdFInputType = new PropertyDefinition("input-f",
					PropertyType.String, "", lblFreqInputType,
					descFreqInputType, true, false, allowedIn);
			propertyDefinitions.put(pdFInputType.getPropertyName(),
					pdFInputType);

			PropertyDefinition pdFMan = new PropertyDefinition("freq-manual",
					PropertyType.String, null, lblFreqField, descFreqField,
					false, false);
			pdFMan.setDependsOn("input-f=Field");
			propertyDefinitions.put(pdFMan.getPropertyName(), pdFMan);

			List<LabeledValue> allowedFrequencies = new ArrayList<LabeledValue>();
			allowedFrequencies
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.FREQUENCY_HERTZ}",
							"Hz"));
			allowedFrequencies
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.FREQUENCY_KILOHERTZ}",
							"KHz"));
			allowedFrequencies
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.FREQUENCY_MEGAHERTZ}",
							"MHz"));
			allowedFrequencies
					.add(new LabeledValue(
							"${com.esri.geoevent.solutions.processor.unitconversion.unitConversion-processor.FREQUENCY_GIGAHERTZ}",
							"GHz"));
			PropertyDefinition pdFIn = new PropertyDefinition("freqin",
					PropertyType.String, "", lblFreqInputUnit,
					descFreqInputUnit, false, false, allowedFrequencies);
			propertyDefinitions.put(pdFIn.getPropertyName(), pdFIn);

			PropertyDefinition pdFOut = new PropertyDefinition("freqout",
					PropertyType.String, "", lblFreqOutputUnit,
					descFreqOutputUnit, false, false, allowedFrequencies);
			propertyDefinitions.put(pdFOut.getPropertyName(), pdFOut);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
	
	@Override
	public String getName()
	{
		return "UnitConversionProcessor";
	}

	@Override
	public String getDomain()
	{
		return "com.esri.geoevent.solutions.processor.unitconversion";
	}

	@Override
	public String getVersion()
	{
		return "10.5.0";
	}

	@Override
	public String getLabel()
	{
		return "Unit Conversion Processor";
	}

	@Override
	public String getDescription()
	{
		return "Converts units in a geoevent definition";
	}

	@Override
	public String getContactInfo()
	{
		return "geoeventprocessor@esri.com";
	}

}
