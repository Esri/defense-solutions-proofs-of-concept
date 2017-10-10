package com.esri.geoevent.solutions.transport.irc.jerklib.parsers;

import com.esri.geoevent.solutions.transport.irc.jerklib.EventToken;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.ConnectionCompleteEvent;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.IRCEvent;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.impl.ConnectionCompleteEventImpl;

public class ConnectionCompleteParser implements CommandParser
{
	
	/* :irc.nmglug.org 001 namnar :Welcome to the nmglug.org 
	 	
	 	Lets user know channels can now be joined etc.
	 	
	  Lets user update *records* 
	  A requested connection to irc.freenode.net might actually
	  connect to kubrick.freenode.net etc 
	*/
	
	public ConnectionCompleteEvent createEvent(EventToken token, IRCEvent event)
	{
		return new ConnectionCompleteEventImpl
		(
				token.data(), 
				token.prefix(), // actual hostname
				event.getSession(), 
				event.getSession().getConnectedHostName() // requested hostname
		);
	}
}
