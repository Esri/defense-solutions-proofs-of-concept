package com.esri.geoevent.solutions.transport.irc.jerklib.parsers;

import com.esri.geoevent.solutions.transport.irc.jerklib.EventToken;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.IRCEvent;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.impl.NumericEventImpl;

public class NumericErrorParser implements CommandParser
{
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		return new NumericEventImpl
		(
				token.arg(0), 
				token.data(), 
				token.numeric(), 
				event.getSession()
		); 
	}
}
