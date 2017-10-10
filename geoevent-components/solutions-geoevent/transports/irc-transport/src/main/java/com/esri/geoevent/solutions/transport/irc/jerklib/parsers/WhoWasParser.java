package com.esri.geoevent.solutions.transport.irc.jerklib.parsers;

import com.esri.geoevent.solutions.transport.irc.jerklib.EventToken;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.IRCEvent;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.impl.WhowasEventImpl;

public class WhoWasParser implements CommandParser
{
	
	/* :kubrick.freenode.net 314 scripy1 ty n=ty 71.237.206.180 * :ty
	 "<nick> <user> <host> * :<real name>" */
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		return new WhowasEventImpl
		(
				token.arg(3), 
				token.arg(2), 
				token.arg(1), 
				token.arg(5), 
				token.data(), 
				event.getSession()
		); 
	}
}
