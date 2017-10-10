package com.esri.geoevent.solutions.transport.irc.jerklib.parsers;

import com.esri.geoevent.solutions.transport.irc.jerklib.Channel;
import com.esri.geoevent.solutions.transport.irc.jerklib.EventToken;
import com.esri.geoevent.solutions.transport.irc.jerklib.Session;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.IRCEvent;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.impl.KickEventImpl;

/**
 * @author mohadib
 *
 */
public class KickParser implements CommandParser
{
	public IRCEvent createEvent(EventToken token, IRCEvent event)
	{
		Session session = event.getSession();
		Channel channel = session.getChannel(token.arg(0));
		
		String msg = "";
		if (token.args().size() == 3)
		{
			msg = token.arg(2);
		}
		
		return new KickEventImpl
		(
			token.data(), 
			session, 
			token.nick(), // byWho
			token.userName(), // username
			token.hostName(), // host name
			token.arg(1), // victim
			msg, // message
			channel
		);
	}
}
