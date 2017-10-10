package com.esri.geoevent.solutions.transport.irc.jerklib.parsers;

import com.esri.geoevent.solutions.transport.irc.jerklib.events.IRCEvent;

public interface InternalEventParser
{
	public IRCEvent receiveEvent(IRCEvent e);
}
