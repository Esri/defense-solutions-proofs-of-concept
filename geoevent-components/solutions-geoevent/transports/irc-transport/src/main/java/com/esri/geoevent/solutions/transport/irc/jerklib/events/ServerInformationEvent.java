package com.esri.geoevent.solutions.transport.irc.jerklib.events;

import com.esri.geoevent.solutions.transport.irc.jerklib.ServerInformation;

/**
 * Event fired when IRC numeric 005 is received - AKA Server Information
 * 
 * @author mohadib
 *
 */
public interface ServerInformationEvent extends IRCEvent
{
    /**
     * Gets the server information object
     * 
     * @return the info
     */
    ServerInformation getServerInformation();
}
