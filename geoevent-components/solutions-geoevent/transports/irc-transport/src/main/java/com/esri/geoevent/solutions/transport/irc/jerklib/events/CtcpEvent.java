package com.esri.geoevent.solutions.transport.irc.jerklib.events;

/**
 * Event fired for generic CTCP events
 * 
 * @author mohadib
 *
 */
public interface CtcpEvent extends MessageEvent
{
    /**
     * Returns the CTCP query
     * @return ctcp query
     */
    String getCtcpString();
}
