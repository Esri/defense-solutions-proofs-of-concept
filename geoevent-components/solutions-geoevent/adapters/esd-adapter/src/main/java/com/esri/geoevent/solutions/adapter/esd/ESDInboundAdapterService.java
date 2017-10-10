package com.esri.geoevent.solutions.adapter.esd;

import com.esri.ges.adapter.Adapter;
import com.esri.ges.adapter.AdapterServiceBase;
import com.esri.ges.core.component.ComponentException;

public class ESDInboundAdapterService extends AdapterServiceBase
{
	public ESDInboundAdapterService()
	{
//		final Log LOG = LogFactory.getLog(ESDInboundAdapterService.class);
//		LOG.debug("This is a debug log message ... ");
//		LOG.info("This is an informational log message ... ");
//		LOG.error("This is an error log message ... ");
		
		
	  definition = new ESDInboundAdapterDefinition();
	}
	
	public Adapter createAdapter() throws ComponentException
	{
	  return new ESDInboundAdapter(definition);
	}
}