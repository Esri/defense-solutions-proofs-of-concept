package com.esri.geoevent.solutions.adapter.esd;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.ges.adapter.AdapterDefinition;
import com.esri.ges.adapter.InboundAdapterBase;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.messaging.MessagingException;


public class ESDInboundAdapter extends InboundAdapterBase
{
	private static final Log LOG = LogFactory.getLog(ESDInboundAdapter.class);
	private String currentSentence = "";
	private boolean started=false;
	
  public ESDInboundAdapter(AdapterDefinition definition) throws ComponentException
  {
    super(definition);
  }


  @Override
public void receive(ByteBuffer buffer, String channelId) {
	// TODO Auto-generated method stub
	super.receive(buffer, channelId);
}


public GeoEvent adapt(ByteBuffer buffer, String channelId)
  {

    try
    {

    	String sentence = "";
    	final int TOOLONG = 2048;
    	boolean ended=false;
    	while (!ended)
    	{
    		
    		char character = (char)buffer.get(); // get the char but don't advance the mark
			if (!started)
			{
	    		if (currentSentence.contentEquals("T") && character == 'a') // start
	    		{
	    			started = true;
	    			currentSentence += 'a';
	    		}
	    		else if (character == 'T')
	    		{
	    			currentSentence = "T";
	    		} 
	    		else
	    		{
	    			currentSentence = "";
	    		}

			}
			else
			{
	    		if (currentSentence.endsWith("T") && character == 'a') // start of next sentence
	    		{
	    				ended = true;
	    				sentence = currentSentence.substring(0, currentSentence.length()-1);
	    				currentSentence = "Ta"; // start of next sentence leave started = true
	    		}
	    		else
	    		{
	    			currentSentence += character;
	    		}
				
			}
    		
    		// if the incoming data is malformed we need to eventually give up and clear the buffer
    		if(currentSentence.length() > TOOLONG)  
    		{
    			currentSentence = "";
    			started = false;
    			LOG.info("Received 2408 characters without two \"Ta\" fields, probably not ESD data.");
    			return null;  			
    		}
    	}
    	
    	final String tagsRegex = "Ta|To|Te|Tw|Sr|Sp|Se|Fv|Sl|Sa|So|Sn|Ic|Cd|Ct|Mn|Md|Mt|Cl|Pc|Iv|Ir|Ip|Ih";
    	final String notTagsRegex = "[^"+tagsRegex+"]+";
    	String[] fields = sentence.split(tagsRegex);  // get array of the fields divided by the tags
    	String[] tags = sentence.split(notTagsRegex); // get array of the tags


      GeoEvent msg = createGeoEvent(fields, tags);
      return msg;
    }
    catch (Exception ex)
    {
    	// not enough data for a complete sentence yet so try again later
      return null;
    }
  }

  //ESD format has the following tags 
  //Ta - target latitude (deg)
  //To - target longitude (deg)
  //Tw - target_width
  //Sr - slant range
  //Sp - sensor pointing azimuth (deg) 
  //Se - sensor depression angle (deg) 
  //Fv - field of view (deg) 
  //Sl - sensor altitude (ft) 
  //Sa - sensor latitude (deg) 
  //So - sensor longitude (deg) 
  //Sn - sensor name (this is a number 1-5)
  //Ic - image coordinate system // currently ignored (Always assume geographic coordinated not geocentric
  //Cd - date of collection YYYYMMDD
  //Ct - time of collection HHMMSS
  //Mn - mission number
  //Md - mission start date
  //Mt - mission start time
  //Cl - classification
  //Pc - project id code (platform code)
  //Iv - ESD icd version
  //Ir - undocumented token
  //Ip - undocumented token
  //Ih - undocumented token 
  private GeoEvent createGeoEvent(String[] fields, String[] tags) {
	  
	  if (fields.length < 15)
	  {
		  return null;
	  }
	  // Create an instance of the message using the guid that we generated when
	  // we started up.
	  GeoEvent msg;
	  try
	  {
		  msg = geoEventCreator.create(((AdapterDefinition)definition).getGeoEventDefinition("ESDGeoEventDefinition").getGuid());
	  }
	  catch (MessagingException e)
	  {
		  return null;
	  }

	  double targetLat=-999.0;
	  double targetLon=-999.0;
	  double targetElevation=0.0;
	  double sensorLat=-999.0;
	  double sensorLon=-999.0;
	  double sensorAlt=-999.0;
	  String collectionDate="";
	  String collectionTime="";
	  String missionDate = "";
	  String missionTime="";
	  for(int i = 1; i<fields.length ; i++) // the 0th element is any junk before the first tag
	  {
		  try {
			if (tags[i-1].equals("Ta")) //PDDMMSST
			  {
				if(fields[i].length()==8)
				{
					
					int dd = Integer.parseInt(fields[i].substring(1, 3));
					int mm = Integer.parseInt(fields[i].substring(3, 5));
					int ss = Integer.parseInt(fields[i].substring(5, 7));	
					int t = Integer.parseInt(fields[i].substring(7, 8));
					
					targetLat = dd + (mm+((ss+(t/10))/60.0))/60.0;
					if (fields[i].charAt(0) == '-') 
						targetLat = targetLat*(-1);
				}
				else if(fields[i].length()==7) // some version leave off the "+"
				{
					int dd = Integer.parseInt(fields[i].substring(0, 2));
					int mm = Integer.parseInt(fields[i].substring(2, 4));
					int ss = Integer.parseInt(fields[i].substring(4, 6));	
					int t = Integer.parseInt(fields[i].substring(6, 7));
					targetLat = dd + (mm+((ss+(t/10))/60.0))/60.0; 
				}			  }
			  else if (tags[i-1].equals("To")) //PDDDMMSST
			  {
					if(fields[i].length()==9)
					{
						int ddd = Integer.parseInt(fields[i].substring(1, 4));
						int mm = Integer.parseInt(fields[i].substring(4, 6));
						int ss = Integer.parseInt(fields[i].substring(6, 8));	
						int t = Integer.parseInt(fields[i].substring(8, 9));
						targetLon = ddd + (mm+((ss+(t/10))/60.0))/60.0;
						
						if (fields[i].charAt(0) == '-') 
							targetLon = targetLon*(-1);
					}
					else if(fields[i].length()==8) // some version leave off the "+"
					{
						int dd = Integer.parseInt(fields[i].substring(0, 3));
						int mm = Integer.parseInt(fields[i].substring(3, 5));
						int ss = Integer.parseInt(fields[i].substring(5, 7));	
						int t = Integer.parseInt(fields[i].substring(7, 8));
						targetLon = dd + (mm+((ss+(t/10))/60.0))/60.0; 
					}					
			  }
			  else if (tags[i-1].equals("Te")) // target_width
			  {
				  float targetFeet = Float.parseFloat(fields[i]); 
				  targetElevation = targetFeet / 3.28084;  // store TargetElevation in meters to use in geometry field
			  }
			  else if (tags[i-1].equals("Tw")) // target_width
			  {
				  msg.setField(2, (long)Float.parseFloat(fields[i])); 
			  }    		     	
			  else if (tags[i-1].equals("Sr")) //slant range
			  {
				  msg.setField(3, (long)Float.parseFloat(fields[i])); 
			  } 
			  else if (tags[i-1].equals("Sp")) //sensor pointing azimuth
			  {
				  msg.setField(4, Float.parseFloat(fields[i])); 
			  }
			  else if (tags[i-1].equals("Se")) //sensor depression angle
			  {
				  msg.setField(5, Float.parseFloat(fields[i]));; 
			  }
			  else if (tags[i-1].equals("Fv")) //sensor_field_of_view
			  {
				  msg.setField(6, Float.parseFloat(fields[i]));; 
			  }
			  else if (tags[i-1].equals("Sl")) // Sensor_altitude
			  {
				  long sensorFeet = Long.parseLong(fields[i]);
				  sensorAlt = sensorFeet / 3.28084;  // store sensor Altitude in meters to use in geometry field
				  msg.setField(7, sensorFeet ); 
			  }    		     	
			  else if (tags[i-1].equals("Sa")) // these get converted to a geometry for sensor location in field 8
			  {
				if(fields[i].length()==8)
				{
					int dd = Integer.parseInt(fields[i].substring(1, 3)); //eg +34 skip the sign
					int mm = Integer.parseInt(fields[i].substring(3, 5));
					int ss = Integer.parseInt(fields[i].substring(5, 7));	
					int t = Integer.parseInt(fields[i].substring(7, 8));
					
					sensorLat = dd + (mm+((ss+(t/10))/60.0))/60.0;
					if (fields[i].charAt(0) == '-') 
						sensorLat = sensorLat*(-1);
				}
				else if(fields[i].length()==7) // some version leave off the "+"
				{
					int dd = Integer.parseInt(fields[i].substring(0, 2));
					int mm = Integer.parseInt(fields[i].substring(2, 4));
					int ss = Integer.parseInt(fields[i].substring(4, 6));	
					int t = Integer.parseInt(fields[i].substring(6, 7));
					sensorLat = dd + (mm+((ss+(t/10))/60.0))/60.0; 
				}			
			  }
			  else if (tags[i-1].equals("So")) //PDDDMMSST
			  {
					if(fields[i].length()==9)
					{
						int ddd = Integer.parseInt(fields[i].substring(1, 4)); //eg -117 skip sign
						int mm = Integer.parseInt(fields[i].substring(4, 6));
						int ss = Integer.parseInt(fields[i].substring(6, 8));	
						int t = Integer.parseInt(fields[i].substring(8, 9));
						sensorLon = ddd + (mm+((ss+(t/10))/60.0))/60.0;
						if (fields[i].charAt(0) == '-') 
							sensorLon = sensorLon*(-1);
					}
					else if(fields[i].length()==8) // some version leave off the "+"
					{
						int dd = Integer.parseInt(fields[i].substring(0, 3)); //DDD
						int mm = Integer.parseInt(fields[i].substring(3, 5)); //MM
						int ss = Integer.parseInt(fields[i].substring(5, 7)); //SS	
						int t = Integer.parseInt(fields[i].substring(7, 8));  //T
						sensorLon = dd + (mm+((ss+(t/10))/60.0))/60.0; 
					}
			  }			  
			  else if (tags[i-1].equals("Sn")) // sensor name enum
			  {
				  msg.setField(9, Short.parseShort(fields[i])); 
			  }    		     	    	  
			  else if (tags[i-1].equals("Cd")) // Collection date yyyymmdd next two fieild get combined into 10
			  {
				  collectionDate = fields[i]; 
			  }
			  else if (tags[i-1].equals("Ct")) // collection time HHMMSS
			  {
				  collectionTime = fields[i]; 
			  }
			  else if (tags[i-1].equals("Mn")) //Mission Number
			  {
				  msg.setField(11, Long.parseLong(fields[i])); 
			  } 
			  else if (tags[i-1].equals("Md")) // mission date yyyymmdd next two fields get combined into 12
			  {
				  missionDate = fields[i]; 
			  }
			  else if (tags[i-1].equals("Mt")) // mission time HHMMSS
			  {
				  missionTime = fields[i]; 
			  }
			  else if (tags[i-1].equals("Cl")) // Security Classification
			  {
				  msg.setField(13,fields[i]); 
			  }
			  else if (tags[i-1].equals("Pc")) // Project/platform code
			  {
				  msg.setField(14, Short.parseShort(fields[i])); 
			  }
		} catch (NumberFormatException e) {
			LOG.debug("parse exception fields "+tags[i-1]+fields[i]);

			//return null;  	
		} catch (FieldException e) {
			LOG.debug("FieldException fields "+tags[i-1]+fields[i]);
		}    		     	    	  

	  }
	  
	  try {
		  msg.setField(0, 1);
		  // for now set the track ID to 1 ultimately we might want to utilize the channel ID or the mission number

		  if (targetLat>=-90 && targetLat<= 90 && targetLon <= 180 && targetLon >= -180)
		  {
			  int wkid = 4326;
			  SpatialReference sr = SpatialReference.create(wkid);
			  Point p = new Point(targetLon, targetLat);
			  MapGeometry mapGeo = new MapGeometry(p, sr);
			  msg.setField(1, mapGeo);
		  }
		  if (sensorLat>=-90 && sensorLat<= 90 && sensorLon <= 180 && sensorLon >= -180)
		  {
			  int wkid = 4326;
			  SpatialReference sr = SpatialReference.create(wkid);
			  Point p = new Point(sensorLon, sensorLat, sensorAlt);
			  MapGeometry mapGeo = new MapGeometry(p, sr);
			  msg.setField(8, mapGeo);
		  }  
		  if (collectionDate.length() == 8 && collectionTime.length() == 6 )
		  {

			  DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); 
			  Date date;
			  try {
				  date = (Date)formatter.parse(collectionDate+collectionTime);
			  } catch (ParseException e) {

				  date=null;
			  } 
			  if (date!=null)
			  {
				  msg.setField(10,date);
			  }
		  }
		  if (missionDate.length() == 8 && missionTime.length() == 6 )
		  {

			  DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); 
			  Date date;
			  try {
				  date = (Date)formatter.parse(missionDate+missionTime);
			  } catch (ParseException e) {

				  date=null;
			  } 
			  if (date!=null)
			  {
				  msg.setField(12,date);
			  }
		  }      
		  return msg;
	  } catch (FieldException e1) {
		  
			LOG.debug("FieldException: "+e1.getMessage());
		  return null;
	  }
  }
}