package com.esri.geoevent.solutions.processor.ll2mgrs;

import java.util.ArrayList;
import java.util.List;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.ges.core.ConfigurationException;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

import com.esri.sde.sdk.pe.factory.PeFactory;

import com.esri.sde.sdk.pe.engine.PeAngunit;
import com.esri.sde.sdk.pe.engine.PeGeogcs;
import com.esri.sde.sdk.pe.engine.PeNotationMgrs;
import com.esri.sde.sdk.pe.engine.PeNotationUtm;
import com.esri.sde.sdk.pe.engine.PePrimem;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;

public class LatLongProcessor extends GeoEventProcessorBase
{
  private              GeoEventDefinitionManager manager;
  public               Messaging                 messaging;
  private              Integer                   accuracy;
  private              String                    newdef;
  private              String                    geofld;
  private              String                    mgrs;
  private              Boolean                   returnBB;
  private              Boolean                   overwrite;
  private              List<FieldDefinition>     fds;
  static final private BundleLogger              LOGGER = BundleLoggerFactory.getLogger(LatLongProcessor.class);

  public LatLongProcessor(GeoEventProcessorDefinition definition) throws ComponentException
  {
    super(definition);
    geoEventMutator = true;
  }

  public void setManager(GeoEventDefinitionManager manager)
  {
    this.manager = manager;
  }

  public void setMessaging(Messaging messaging)
  {
    this.messaging = messaging;
  }

  @Override
  public void afterPropertiesSet()
  {
    mgrs = properties.get("mgrs").getValueAsString();
    overwrite = (Boolean) properties.get("overwrite").getValue();
    returnBB = (Boolean) properties.get("returnbb").getValue();
    if (overwrite)
      geofld = "GEOMETRY";
    else
    {
      geofld = properties.get("geofld").getValueAsString();
      try
      {
        FieldDefinition fd = new DefaultFieldDefinition(geofld, FieldType.Geometry);
        fds = new ArrayList<FieldDefinition>();
        fds.add(fd);
      }
      catch (ConfigurationException e)
      {
        LOGGER.warn("Error while trying to create new geometry field.", e);
      }
    }
    newdef = properties.get("eventdef").getValueAsString();
    //accuracy = (Integer) properties.get("accuracy").getValue();

  }

  @Override
  public boolean isGeoEventMutator()
  {
    return true;
  }

  @Override
  public GeoEvent process(GeoEvent evt) throws Exception
  {

    String mgrsval = (String) evt.getField(mgrs);
    mgrsval.replace(" ", ""); //remove spaces
    String[] mgrsvals = { mgrsval };

    PeGeogcs peGeoCS = PeFactory.geogcs(4326);
    // PeNotationMgrs mgrs = new PeNotationMgrs();
    double[] pts = new double[2];
    PeNotationMgrs.mgrs_to_geog(peGeoCS, 1, mgrsvals, pts);
    Point pt = new Point(pts[0], pts[1]);
    MapGeometry mapGeo = null;
    if (returnBB)
    {
      int accuracy = FindAccuracy(mgrsval);
      Geometry geo = findBoundingBox(mgrsval, accuracy, pt);
      mapGeo = new MapGeometry(geo, SpatialReference.create(4326));
    }
    else
    {
      mapGeo = new MapGeometry(pt, SpatialReference.create(4326));

    }
    GeoEvent geOut = null;
    if (!overwrite)
    {
      GeoEventDefinition edOut;
      GeoEventDefinition geoDef = evt.getGeoEventDefinition();
      if ((edOut = manager.searchGeoEventDefinition(newdef, getId())) == null)
      {
        try
        {
          edOut = geoDef.augment(fds);
          edOut.setOwner(getId());
          edOut.setName(newdef);
          manager.addGeoEventDefinition(edOut);
        }
        catch (Exception e)
        {
          LOGGER.warn("Error while trying to create new GeoEvent definition.", e);
        }


      }
      GeoEventCreator geoEventCreator = messaging.createGeoEventCreator();

      geOut = geoEventCreator.create(edOut.getGuid(), new Object[] { evt.getAllFields(), mapGeo });
      geOut.setProperty(GeoEventPropertyName.TYPE, "message");
      geOut.setProperty(GeoEventPropertyName.OWNER_ID, getId());
      geOut.setProperty(GeoEventPropertyName.OWNER_ID, definition.getUri());
      geOut.setField(geofld, mapGeo);
    }
    else
    {
      geOut = evt;
      geOut.setGeometry(mapGeo);
    }

    return geOut;

  }

  private int FindAccuracy(String mgrs)
  {
    char[] charArray = mgrs.toCharArray();
    int accuracy = 0;
    boolean startSearching = false;
    int index = 0;
    for (int i = 0; i < charArray.length; ++i)
    {
      char c = charArray[i];
      if (Character.isLetter(c))
      {
        index = i;
        break;
      }
    }
    String dropZone = mgrs.substring(index);
    accuracy = dropZone.length() - 3;
    return accuracy / 2;
  }

  private Geometry findBoundingBox(String mgrs, int accuracy, Point mgrsPt)
  {
    String mgrsZone = mgrs.substring(0, 1);
    int wkid = 0;
    String utm = null;
    if (mgrsZone.equals("A") || mgrsZone.equals("B"))
    {
      wkid = 32761;
    }
    else if (mgrsZone.equals("Y") || mgrsZone.equals("Z"))
    {
      wkid = 32661;
    }
    else
    {
      PeGeogcs peGeoCS = PeFactory.geogcs(4326);
      double[] coordArray = { mgrsPt.getX(), mgrsPt.getY() };
      String[] utmArray = new String[1];
      PeNotationUtm.geog_to_utm(peGeoCS, 1, coordArray, PeNotationUtm.PE_UTM_OPTS_NS, utmArray);
      utm = utmArray[0];
      wkid = GetWkidFromUTM(utm);

    }
    SpatialReference pcs = SpatialReference.create(wkid);
    SpatialReference gcs = SpatialReference.create(4326);
    Geometry projGeo = GeometryEngine.project(mgrsPt, gcs, pcs);//projec local
    Point projPt = (Point) projGeo;
    double dist = getDistFromAccuracy(accuracy);
    double xmin, ymin, xmax, ymax;
    xmin = projPt.getX();
    ymin = projPt.getY();
    xmax = xmin + dist;
    ymax = ymin + dist;
    Polygon pbb = new Polygon();
    pbb.startPath(xmin, ymin);
    pbb.lineTo(xmax, ymin);
    pbb.lineTo(xmax, ymax);
    pbb.lineTo(xmin, ymax);
    pbb.lineTo(xmin, ymin);
    pbb.closeAllPaths();
    Geometry bb = GeometryEngine.project(pbb, pcs, gcs);//project back to wgs84
    return bb;
  }

  private double getDistFromAccuracy(int accuracy)
  {
    double dist = 0;
    if (accuracy == 0)
      dist = 100000;
    else if (accuracy == 1)
      dist = 10000;
    else if (accuracy == 2)
      dist = 1000;
    else if (accuracy == 3)
      dist = 100;
    else if (accuracy == 4)
      dist = 10;
    else if (accuracy == 5)
      dist = 1;
    else if (accuracy == 6)
      dist = 0.1;
    else if (accuracy == 7)
      dist = 0.01;
    else if (accuracy == 8)
      dist = 0.001;

    return dist;
  }

  private int GetWkidFromUTM(String utm)
  {
    int wkid = 0;
    int nbase = 32600;
    int sbase = 32700;
    int base = 0;
    char[] cArray = utm.toCharArray();
    int len = cArray.length;
    int index = 0;
    for (int i = 0; i < len; ++i)
    {
      char c = cArray[i];
      if (Character.isLetter(c))
      {
        index = i;
        break;
      }
    }
    if (utm.substring(index, index + 1).equals("N"))
    {
      base = nbase;//north
    }
    else
    {
      base = sbase;//south
    }
    String zone = utm.substring(0, index);
    Integer zoneNum = Integer.parseInt(zone);
    wkid = base + zoneNum;
    return wkid;
  }

}
