package com.esri.geoevent.solutions.transport.tcpsquirt;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.transport.TransportDefinitionBase;
import com.esri.ges.transport.TransportType;

public class TcpSquirtOutboundTransportDefinition extends TransportDefinitionBase
{
	final static private Log LOG = LogFactory.getLog(TcpSquirtOutboundTransportDefinition.class);

	public TcpSquirtOutboundTransportDefinition()
	{
		super(TransportType.OUTBOUND);
		try
		{
			propertyDefinitions.put("host", new PropertyDefinition("host", PropertyType.String, "localhost", "${com.esri.geoevent.solutions.transport.tcpsquirt.tcpsquirt-transport.LBL_HOST}", "${com.esri.geoevent.solutions.transport.tcpsquirt.tcpsquirt-transport.DESC_HOST}", false, false));
			propertyDefinitions.put("port", new PropertyDefinition("port", PropertyType.Integer, new Integer(5000), "${com.esri.geoevent.solutions.transport.tcpsquirt.tcpsquirt-transport.LBL_PORT}", "${com.esri.geoevent.solutions.transport.tcpsquirt.tcpsquirt-transport.DESC_PORT}", true, false));
			List<LabeledValue> allowedModes = new ArrayList<LabeledValue>();
			allowedModes.add(new LabeledValue("${com.esri.geoevent.solutions.transport.tcpsquirt.tcpsquirt-transport.ALLOWED_MODE_SERVER}", "SERVER"));
			allowedModes.add(new LabeledValue("${com.esri.geoevent.solutions.transport.tcpsquirt.tcpsquirt-transport.ALLOWED_MODE_CLIENT}", "CLIENT"));
			propertyDefinitions.put("mode", new PropertyDefinition("mode", PropertyType.String, "${com.esri.geoevent.solutions.transport.tcpsquirt.tcpsquirt-transport.MODE_SERVER_LBL}", "${com.esri.geoevent.solutions.transport.tcpsquirt.tcpsquirt-transport.LBL_MODE}", "${com.esri.geoevent.solutions.transport.tcpsquirt.tcpsquirt-transport.DESC_MODE}", true, false, allowedModes));
			propertyDefinitions.put("clientConnectionTimeout", new PropertyDefinition("clientConnectionTimeout", PropertyType.Integer, new Integer(60), "{com.esri.geoevent.solutions.transport.tcpsquirt.tcpsquirt-transport.LBL_TIMEOUT}", "{com.esri.geoevent.solutions.transport.tcpsquirt.tcpsquirt-transport.DESC_TIMEOUT}", "mode=CLIENT", false, false));
		}
		catch (PropertyException e)
		{
			LOG.error("Failed to define properties of TCPSquirtOutboundTransportDefinition: ", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName()
	{
		return "TCPSquirt";
	}
	
	@Override
  public String getLabel()
  {
    return "TCP-Squirt Outbound Transport";
  }
	
	@Override
	public String getDomain()
	{
		return "com.esri.geoevent.solutions.transport.tcpsquirt.outbound";
	}

	@Override
	public String getDescription()
	{
		return "ESRI TCP-Squirt Transport for outbound streams.";
	}
}