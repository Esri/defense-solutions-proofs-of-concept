package com.esri.geoevent.solutions.processor.trackidle;

import java.util.Date;

import com.esri.core.geometry.MapGeometry;

public class TrackIdleProcessorStart
{
	private String			trackId;
	private Date				startTime;
	private MapGeometry	geometry;
	private boolean			isIdling;
	private double			idleDuration;

	public TrackIdleProcessorStart(String trackId, Date startTime, MapGeometry geometry)
	{
		this.trackId = trackId;
		this.startTime = startTime;
		this.geometry = geometry;
		this.setIdling(false);
		this.idleDuration = 0;
	}

	public String getTrackId()
	{
		return trackId;
	}

	public void setTrackId(String trackId)
	{
		this.trackId = trackId;
	}

	public Date getStartTime()
	{
		return startTime;
	}

	public void setStartTime(Date startTime)
	{
		this.startTime = startTime;
	}

	public MapGeometry getGeometry()
	{
		return geometry;
	}

	public void setGeometry(MapGeometry geometry)
	{
		this.geometry = geometry;
	}

	public boolean isIdling()
	{
		return isIdling;
	}

	public void setIdling(boolean isIdling)
	{
		this.isIdling = isIdling;
	}

	public double getIdleDuration()
	{
		return idleDuration;
	}

	public void setIdleDuration(double idleDuration)
	{
		this.idleDuration = idleDuration;
	}

}

