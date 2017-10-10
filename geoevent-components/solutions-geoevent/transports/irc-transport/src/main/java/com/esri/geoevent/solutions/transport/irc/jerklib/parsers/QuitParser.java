package com.esri.geoevent.solutions.transport.irc.jerklib.parsers;

import java.util.List;

import com.esri.geoevent.solutions.transport.irc.jerklib.Channel;
import com.esri.geoevent.solutions.transport.irc.jerklib.EventToken;
import com.esri.geoevent.solutions.transport.irc.jerklib.Session;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.IRCEvent;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.QuitEvent;
import com.esri.geoevent.solutions.transport.irc.jerklib.events.impl.QuitEventImpl;


public class QuitParser implements CommandParser
{
	public QuitEvent createEvent(EventToken token, IRCEvent event)
	{
		Session session = event.getSession();
		String nick = token.nick();
		List<Channel> chanList = event.getSession().removeNickFromAllChannels(nick);
		return new QuitEventImpl
		(
			token.data(), 
			session, 
			nick, // who
			token.userName(), // username
			token.hostName(), // hostName
			token.arg(0), // message
			chanList
		);
	}
}
