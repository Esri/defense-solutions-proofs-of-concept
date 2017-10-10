package com.esri.geoevent.solutions.transport.irc.jerklib.parsers;

import com.esri.geoevent.solutions.transport.irc.jerklib.EventToken;
import com.esri.geoevent.solutions.transport.irc.jerklib.Session;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.IRCEvent;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.impl.NickChangeEventImpl;

public class NickParser implements CommandParser
{
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		Session session = event.getSession();
		return new NickChangeEventImpl
		(
				token.data(), 
				session, 
				token.nick(), // old
				token.arg(0), // new nick
				token.hostName(), // hostname
				token.userName() // username
		); 
	}
}
