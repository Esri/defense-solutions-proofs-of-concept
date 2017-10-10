package com.esri.geoevent.solutions.transport.irc.jerklib.events.impl;

import com.esri.geoevent.solutions.transport.irc.jerklib.Session;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.ConnectionLostEvent;

/**
 * @author mohadib
 * @see ConnectionLostEvent
 */
public class ConnectionLostEventImpl implements ConnectionLostEvent
{
	private Session session;
	
	
	public ConnectionLostEventImpl(Session session)
	{
		this.session = session;
	}
	
	/* (non-Javadoc)
	 * @see com.esri.ges.transport.Irc.jerklib.events.IRCEvent#getRawEventData()
	 */
	public String getRawEventData()
	{
		return "";
	}

	/* (non-Javadoc)
	 * @see com.esri.ges.transport.Irc.jerklib.events.IRCEvent#getSession()
	 */
	public Session getSession()
	{
		return session;
	}

	/* (non-Javadoc)
	 * @see com.esri.ges.transport.Irc.jerklib.events.IRCEvent#getType()
	 */
	public Type getType()
	{
		return Type.CONNECTION_LOST;
	}

}
