package com.esri.geoevent.solutions.transport.irc.jerklib.events;


import java.util.Date;

import com.esri.geoevent.solutions.transport.irc.jerklib.Channel;


/**
 * 
 * Event fired when topic is received
 * @author mohadib
 * @see Channel
 */
public interface TopicEvent extends IRCEvent
{
    /**
     * Gets the topic
     *
     * @return the topic
     */
    public String getTopic();

    /**
     * Gets who set the topic
     *
     * @return topic setter
     */
    public String getSetBy();

    /**
     * Gets when topic was set
     *
     * @return when
     */
    public Date getSetWhen();

    /**
     * Gets Channel
     *
     * @return Channel
     * @see Channel
     */
    public Channel getChannel();

}
