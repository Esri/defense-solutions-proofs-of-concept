package com.esri.geoevent.solutions.transport.irc.jerklib.events;


import java.util.List;

import com.esri.geoevent.solutions.transport.irc.jerklib.Channel;

/**
 * 
 * Event fired when nick list event comes from server
 * @author mohadib
 *         
 */
public interface NickListEvent extends IRCEvent
{

    /**
     * Gets the channel the nick list came from
     *
     * @return Channel
     * @see Channel
     */
    public Channel getChannel();


    /**
     * Gets the nick list for the Channel
     *
     * @return List of nicks in channel
     */
    public List<String> getNicks();
}
