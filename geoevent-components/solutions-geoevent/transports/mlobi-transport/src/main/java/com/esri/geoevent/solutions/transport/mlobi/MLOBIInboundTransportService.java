package com.esri.geoevent.solutions.transport.mlobi;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.http.GeoEventHttpClientService;
import com.esri.ges.transport.Transport;
import com.esri.ges.transport.TransportServiceBase;
import com.esri.ges.transport.util.XmlTransportDefinition;

public class MLOBIInboundTransportService extends TransportServiceBase{

	GeoEventHttpClientService httpService;
	
	public MLOBIInboundTransportService(){
		definition = new XmlTransportDefinition(getResourceAsStream("mlobi-inbound-transport-definition.xml"));
	}
	@Override
	public Transport createTransport() throws ComponentException {
		return new MLOBIInboundTransport(definition, httpService);
	}
	
	public void setHttpClientService(GeoEventHttpClientService service)
	{
		this.httpService=service;
	}

}
