/*******************************************************************************
 * Copyright 2013-2017 Esri
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
package com.esri.squadleader.controller.test;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.militaryapps.model.LayerInfo;
import com.esri.militaryapps.model.LayerType;
import com.esri.squadleader.controller.MapController;
import com.esri.squadleader.util.Utilities;
import com.esri.squadleader.view.SquadLeaderActivity;

import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

public class MapControllerTest extends ActivityInstrumentationTestCase2<SquadLeaderActivity> {

    private SquadLeaderActivity activity;
    private MapController mapController;

    public MapControllerTest() {
        super(SquadLeaderActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        mapController = activity.getMapController();
    }

    /**
     * MapConfig loading tests
     * <p>
     * Here's the priority that Squad Leader should use:
     * 1. Check for existing user preferences and use those.
     * 2. Check for /mnt/sdcard/SquadLeader/mapconfig.xml and parse that with MapConfigReader.
     * 3. Use mapconfig.xml built into the app
     * <p>
     * To make this easier, test #2 first. That should write preferences. Then delete
     * /mnt/sdcard/SquadLeader/mapconfig.xml and test #1. Then delete
     * user preferences and test #3.
     *
     * @throws IOException
     */

    @Test
    public void test001LoadMapConfigFromSdCardFile() throws IOException {
        clearExistingPreferences(mapController.getContext());

        //Create mapconfig.xml on SD card
        File originalMapConfigOnSdCard = new File(activity.getString(com.esri.squadleader.R.string.squad_leader_home_dir),
                activity.getString(com.esri.squadleader.R.string.map_config_filename));
        if (originalMapConfigOnSdCard.exists()) {
            //Back it up
            originalMapConfigOnSdCard.renameTo(new File(originalMapConfigOnSdCard.getAbsolutePath() + ".bak"));
        }
        Utilities.copyAssetToDir(getInstrumentation().getTargetContext().getAssets(),
                activity.getString(com.esri.squadleader.R.string.map_config_filename),
                activity.getString(com.esri.squadleader.R.string.squad_leader_home_dir));

        //Load it
        reloadMapController();
        assertEquals(1, mapController.getNonBasemapLayers().size());
        checkBasemaps(mapController);
    }

    @Test
    public void test002LoadMapConfigFromProfile() {
        //Delete mapconfig.xml on SD card
        new File(activity.getString(com.esri.squadleader.R.string.squad_leader_home_dir),
                activity.getString(com.esri.squadleader.R.string.map_config_filename)).delete();

        //Load it from preferences file
        reloadMapController();
        assertEquals(1, mapController.getNonBasemapLayers().size());
        checkBasemaps(mapController);
    }

    @Test
    public void test003LoadMapConfigFromAppAsset() {
        clearExistingPreferences(mapController.getContext());

        reloadMapController();

        assertEquals(1, mapController.getNonBasemapLayers().size());
        checkBasemaps(mapController);

        //Now that tests are done, restore original MapConfig if any
        File originalMapConfigOnSdCard = new File(activity.getString(com.esri.squadleader.R.string.squad_leader_home_dir),
                activity.getString(com.esri.squadleader.R.string.map_config_filename) + ".bak");
        if (originalMapConfigOnSdCard.exists()) {
            String filename = originalMapConfigOnSdCard.getAbsolutePath();
            if (filename.endsWith(".bak")) {
                originalMapConfigOnSdCard.renameTo(new File(filename.substring(0, filename.length() - ".bak".length())));
            }
        }
    }

    public void test004ResetMap() throws ParserConfigurationException, SAXException, IOException {
        LayerInfo layerInfo = new LayerInfo();
        layerInfo.setDatasetPath("http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer");
        layerInfo.setLayerType(LayerType.TILED_MAP_SERVICE);
        layerInfo.setName("Test Name");
        layerInfo.setVisible(false);
        mapController.addLayer(layerInfo);
        assertEquals(5, mapController.getNonBasemapLayers().size());
        assertEquals(10, mapController.getBasemapLayers().size());

        mapController.reset();
        assertEquals(2, mapController.getNonBasemapLayers().size());
        checkBasemaps(mapController);
    }

    private static void checkBasemaps(MapController mapController) {
        assertEquals(10, mapController.getBasemapLayers().size());
        checkBasemapLayer(mapController, 0, "Imagery", "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer");
        checkBasemapLayer(mapController, 1, "Streets", "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
        checkBasemapLayer(mapController, 2, "Topographic", "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");
        checkBasemapLayer(mapController, 3, "Shaded Relief", "http://services.arcgisonline.com/ArcGIS/rest/services/World_Shaded_Relief/MapServer");
        checkBasemapLayer(mapController, 4, "Physical", "http://services.arcgisonline.com/ArcGIS/rest/services/World_Physical_Map/MapServer");
        checkBasemapLayer(mapController, 5, "Terrain", "http://services.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer");
        checkBasemapLayer(mapController, 6, "USGS Topo", "http://services.arcgisonline.com/ArcGIS/rest/services/USA_Topo_Maps/MapServer");
        checkBasemapLayer(mapController, 7, "Oceans", "http://services.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer");
        checkBasemapLayer(mapController, 8, "Light Gray Canvas", "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer");
        checkBasemapLayer(mapController, 9, "National Geographic", "http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer");

    }

    private static void checkBasemapLayer(MapController mapController, int index, String expectedName, String expectedUrl) {
        assertEquals(expectedName, mapController.getBasemapLayers().get(index).getLayer().getName());
        assertEquals(expectedUrl, mapController.getBasemapLayers().get(index).getLayer().getUrl());
    }

    private void reloadMapController() {
        mapController = new MapController((MapView) activity.findViewById(com.esri.squadleader.R.id.map), activity.getAssets(),
                new OnStatusChangedListener() {

                    @Override
                    public void onStatusChanged(Object source, STATUS status) {

                    }
                },
                activity);
    }

    private void clearExistingPreferences(Context context) {
        context.deleteFile(context.getString(com.esri.squadleader.R.string.map_config_prefname));
    }

    @Override
    protected void tearDown() throws Exception {
        activity.finish();
    }

}
