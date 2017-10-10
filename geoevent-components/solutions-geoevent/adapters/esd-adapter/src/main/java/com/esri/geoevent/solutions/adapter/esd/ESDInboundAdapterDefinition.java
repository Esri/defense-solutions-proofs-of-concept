package com.esri.geoevent.solutions.adapter.esd;

import java.util.ArrayList;
import java.util.List;

import com.esri.ges.adapter.AdapterDefinitionBase;
import com.esri.ges.adapter.AdapterType;
import com.esri.ges.core.ConfigurationException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.DefaultGeoEventDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEventDefinition;

public class ESDInboundAdapterDefinition extends AdapterDefinitionBase
{
	public ESDInboundAdapterDefinition()
	{
		super(AdapterType.INBOUND);
		try
		{
			GeoEventDefinition md = new DefaultGeoEventDefinition();
			md.setName("ESDGeoEventDefinition");
			List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();
			fieldDefinitions.add(new DefaultFieldDefinition("track_id", FieldType.Long));  					// 0
			fieldDefinitions.add(new DefaultFieldDefinition("target_location", FieldType.Geometry));		// 1
			fieldDefinitions.add(new DefaultFieldDefinition("target_width", FieldType.Long));				// 2
			fieldDefinitions.add(new DefaultFieldDefinition("slant_range", FieldType.Long));				// 3
			fieldDefinitions.add(new DefaultFieldDefinition("sensor_pointing_azimuth", FieldType.Float));	// 4		
			fieldDefinitions.add(new DefaultFieldDefinition("sensor_elevation_angle", FieldType.Float));	// 5
			fieldDefinitions.add(new DefaultFieldDefinition("field_of_view", FieldType.Float));				// 6
			fieldDefinitions.add(new DefaultFieldDefinition("sensor_altitude", FieldType.Long));			// 7
			fieldDefinitions.add(new DefaultFieldDefinition("sensor_location", FieldType.Geometry));		// 8
			fieldDefinitions.add(new DefaultFieldDefinition("sensor_name_enum", FieldType.Short));			// 9
			fieldDefinitions.add(new DefaultFieldDefinition("collection_time", FieldType.Date));			// 10
			fieldDefinitions.add(new DefaultFieldDefinition("mission_number", FieldType.Long));				// 11
			fieldDefinitions.add(new DefaultFieldDefinition("mission_start_time", FieldType.Date));			// 12
			fieldDefinitions.add(new DefaultFieldDefinition("security_classification", FieldType.String));	// 13
			fieldDefinitions.add(new DefaultFieldDefinition("platform_code", FieldType.Short));				// 14
			md.setFieldDefinitions(fieldDefinitions);
			geoEventDefinitions.put(md.getName(), md);
		}
		catch (ConfigurationException ex)
		{
			;
		}
	}

	@Override
	public String getName()
	{
		return "Exploitation_Support_Data";
	}
	
	@Override
  public String getLabel()
  {
    return "${com.esri.geoevent.solutions.adapter.esd.esd-adapter.ADAPTER_LABEL}";
  }
	
	@Override
	public String getDomain()
	{
		return "com.esri.geoevent.solutions.adapter.esd.inbound";
	}

	@Override
	public String getDescription()
	{
		return "${com.esri.geoevent.solutions.adapter.esd.esd-adapter.ADAPTER_DESCRIPTION}";
	}

	@Override
	public String getContactInfo()
	{
		return "cbailey@esri.com";
	}
}