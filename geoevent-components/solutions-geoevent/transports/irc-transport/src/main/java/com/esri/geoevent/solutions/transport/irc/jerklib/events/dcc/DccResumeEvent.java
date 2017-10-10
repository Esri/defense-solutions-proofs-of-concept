package com.esri.geoevent.solutions.transport.irc.jerklib.events.dcc;

/**
 * DCC RESUME event.
 * 
 * @author Andres N. Kievsky
 */
public interface DccResumeEvent extends DccEvent
{
	String getFilename();

	int getPort();

	long getPosition();

}
