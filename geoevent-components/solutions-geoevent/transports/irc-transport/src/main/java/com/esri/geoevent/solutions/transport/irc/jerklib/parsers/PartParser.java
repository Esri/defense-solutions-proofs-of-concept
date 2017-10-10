package com.esri.geoevent.solutions.transport.irc.jerklib.parsers;

import com.esri.geoevent.solutions.transport.irc.jerklib.EventToken;
import com.esri.geoevent.solutions.transport.irc.jerklib.Session;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.IRCEvent;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.PartEvent;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.impl.PartEventImpl;

/**
 * @author mohadib
 *
 */
public class PartParser implements CommandParser
{
	public PartEvent createEvent(EventToken token, IRCEvent event)
	{
			Session session = event.getSession();
			return new PartEventImpl
			(
					token.data(), 
					session,
					token.nick(), // who
					token.userName(), // username
					token.hostName(), // host name
					token.arg(0), // channel name
					session.getChannel(token.arg(0)), 
					token.args().size() == 2? token.arg(1) : ""
			);
	}
}
