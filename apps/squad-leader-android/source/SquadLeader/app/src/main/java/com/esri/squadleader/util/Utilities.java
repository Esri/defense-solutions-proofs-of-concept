/*******************************************************************************
 * Copyright 2013-2015 Esri
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.squadleader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.esri.core.geometry.AngularUnit;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.militaryapps.model.Location;
import com.esri.militaryapps.model.MapConfig;
import com.esri.militaryapps.model.MapConfigReader;
import com.esri.squadleader.R;

/**
 * A class for useful static methods that don't really belong anywhere else.
 */
public class Utilities extends com.esri.militaryapps.util.Utilities {
    
    private static final String TAG = Utilities.class.getSimpleName();
    
    /**
     * The number of milliseconds to wait between animation-like activities.
     * It's not imperative that you use exactly this number; this constant is
     * provided for convenience.
     */
    public static final int ANIMATION_PERIOD_MS = 1000 / 24;
    
    /**
     * The Web Mercator spatial reference, based on wkid 3857.
     */
    public static final SpatialReference WEB_MERCATOR_3857 = SpatialReference.create(3857);
    
    public static final AngularUnit DEGREES = (AngularUnit) AngularUnit.create(AngularUnit.Code.DEGREE);
    
    /**
     * Copies the specified asset to a destination directory, whether the asset is a file or a directory.<br/>
     * <br/>
     * For example, if assetName is foo and destDir is /mnt/sdcard/bar, then a copy of foo will be made
     * and will be called /mnt/sdcard/bar/foo, whether foo is a file or a directory. 
     * @param assetManager the AssetManager from which to copy the asset.
     * @param assetName the name of the asset, as a relative path from the assets directory. The asset
     *                  can be a file or a directory. If it is a directory, all contents will be copied.
     * @param destDir the destination of the files.
     * @throws IOException 
     */
    public static void copyAssetToDir(AssetManager assetManager, String assetName, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String dirLastName = assetName;
        int lastIndex = dirLastName.lastIndexOf(File.separator);
        if (-1 < lastIndex) {
            dirLastName = dirLastName.substring(lastIndex + 1);
        }
        String destSubDir = destDir + File.separator + dirLastName;

        String[] assets = assetManager.list(assetName);
        if (0 == assets.length) {
            //It's a file
            copyFileAsset(assetManager, assetName, destDir);
        } else {
            //It's a directory
            for (String asset : assets) {
                copyAssetToDir(assetManager, assetName + File.separator + asset, destSubDir);
            }
        }
    }
    
    private static void copyFileAsset(AssetManager assetManager, String assetName, String destDir) throws IOException {
        String assetLastName = assetName;
        int lastIndex = assetLastName.lastIndexOf(File.separator);
        if (-1 < lastIndex) {
            assetLastName = assetLastName.substring(lastIndex + 1);
        }
        
        InputStream in = assetManager.open(assetName);
        String newFileName = destDir + File.separator + assetLastName;
        OutputStream out = new FileOutputStream(newFileName);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        in = null;
        out.flush();
        out.close();
        out = null;
    }
    
    /**
     * Calculates the distance in meters between two locations, using the Web Mercator spatial
     * reference (wkid 3857).
     * @param location1 a location.
     * @param location2 a location.
     * @return the distance in meters between the two locations.
     */
    public static double calculateDistanceInMeters(Location location1, Location location2) {
        return calculateDistanceInMeters(location1, location2, WEB_MERCATOR_3857);
    }
    
    /**
     * Calculates the distance in meters between two locations, using the Web Mercator spatial
     * reference (wkid 3857).
     * @param location1 a longitude/latitude location.
     * @param location2 a longitude/latitude location.
     * @param the spatial reference to use for the distance calculation.
     * @return the distance in meters between the two locations.
     */
    public static double calculateDistanceInMeters(Location location1, Location location2, SpatialReference sr) {
        Point pt1 = GeometryEngine.project(location1.getLongitude(), location1.getLatitude(), sr);
        Point pt2 = GeometryEngine.project(location2.getLongitude(), location2.getLatitude(), sr);
        return GeometryEngine.distance(pt1, pt2, sr);
    }

    /**
     * Reads the MapConfig from a file (first on disk, then from the app's resources if necessary) without modifying any user preferences.
     * @param context the Context from which to get the app home directory and map config filename.
     * @param assetManager the app's assets.
     * @return the MapConfig.
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws IOException 
     */
    public static MapConfig readMapConfig(Context context, AssetManager assetManager) throws IOException, ParserConfigurationException, SAXException {
        //Read mapconfig from the SD card
        InputStream mapConfigInputStream = null;
        File mapConfigFile = new File(
                context.getString(R.string.squad_leader_home_dir),
                context.getString(R.string.map_config_filename));
        if (mapConfigFile.exists() && mapConfigFile.isFile()) {
            Log.d(TAG, "Loading mapConfig from " + mapConfigFile.getAbsolutePath());
            try {
                mapConfigInputStream = new FileInputStream(mapConfigFile);
            } catch (FileNotFoundException e) {
                //Swallow and let it load built-in mapconfig.xml
            }
        }
        if (null == mapConfigInputStream) {
            Log.d(TAG, "Loading mapConfig from app's " + context.getString(R.string.map_config_filename) + " asset");
            try {
                mapConfigInputStream = assetManager.open(context.getString(R.string.map_config_filename));
            } catch (IOException e) {
                Log.e(TAG, "Couldn't load any " + context.getString(R.string.map_config_filename) + ", including the one built into the app", e);
            }
        }
        try {
            return MapConfigReader.readMapConfig(mapConfigInputStream);
        } finally {
            if (null != mapConfigInputStream) {
                mapConfigInputStream.close();
            }
        }
    }

    /**
     * Returns the abbreviation for the specified angular unit. Sometimes getAbbreviation doesn't
     * return the abbreviation you might expect. For example, it might return "deg" for degrees
     * instead of returning the degrees symbol. This method offers better abbreviations for some
     * angular units by calling com.esri.militaryapps.util.Utilities.getAngularUnitAbbreviation(int, String).
     * @param angularUnit the angular unit
     * @return the angular unit's preferred abbreviation.
     */
    public static String getAngularUnitAbbreviation(AngularUnit angularUnit) {
        return getAngularUnitAbbreviation(angularUnit.getID(), angularUnit.getAbbreviation());
    }

}
