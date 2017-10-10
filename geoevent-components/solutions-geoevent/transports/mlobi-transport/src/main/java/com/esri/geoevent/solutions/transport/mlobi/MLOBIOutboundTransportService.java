package com.esri.geoevent.solutions.transport.mlobi;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.transport.Transport;
import com.esri.ges.transport.TransportServiceBase;
import com.esri.ges.transport.util.XmlTransportDefinition;
import com.esri.ges.core.http.GeoEventHttpClientService;

public class MLOBIOutboundTransportService extends TransportServiceBase {
	protected GeoEventHttpClientService httpClientService;
	public MLOBIOutboundTransportService()
	{
		definition = new XmlTransportDefinition( getResourceAsStream( "mlobi-outbound-transport-definition.xml" ) );
	}
	@Override
	public Transport createTransport() throws ComponentException {
		return new MLOBIOutboundTransport(definition, httpClientService);
	}
	
	public void setHttpClientService( GeoEventHttpClientService service )
	{
	  this.httpClientService = service;
	}

}
