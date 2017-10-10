package com.esri.geoevent.solutions.transport.irc.jerklib.parsers;

import com.esri.geoevent.solutions.transport.irc.jerklib.EventToken;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.IRCEvent;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.impl.NickInUseEventImpl;

public class NickInUseParser implements CommandParser
{
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		return new NickInUseEventImpl
		(
				token.arg(1),
				token.data(), 
				event.getSession()
		); 
	}
}
