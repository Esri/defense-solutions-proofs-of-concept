
package com.esri.geoevent.solutions.transport.irc.jerklib.listeners;

import com.esri.geoevent.solutions.transport.irc.jerklib.events.IRCEvent;

/**
 * IRCEventListener - Listener to receive IRCEvents
 *
 * @author mohadib
 */
public interface IRCEventListener
{

    /**
     * recieveEvent() - receive IRCEvents
     *
     * @param e <code>IRCEvent<code> the event
     */
    public void receiveEvent(IRCEvent e);
}
