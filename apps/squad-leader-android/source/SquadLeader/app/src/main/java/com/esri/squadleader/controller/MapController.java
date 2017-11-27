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
package com.esri.squadleader.controller;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Grid.GridType;
import com.esri.android.map.Layer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.LocationDisplayManager.AutoPanMode;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISImageServiceLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.popup.Popup;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.geometry.CoordinateConversion;
import com.esri.core.geometry.CoordinateConversion.MGRSConversionMode;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Feature;
import com.esri.core.map.Graphic;
import com.esri.core.map.popup.PopupFieldInfo;
import com.esri.core.renderer.RGBRenderer;
import com.esri.core.renderer.Renderer;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.militaryapps.controller.LocationController.LocationMode;
import com.esri.militaryapps.model.BasemapLayerInfo;
import com.esri.militaryapps.model.LayerInfo;
import com.esri.militaryapps.model.LocationProvider.LocationProviderState;
import com.esri.militaryapps.model.MapConfig;
import com.esri.squadleader.R;
import com.esri.squadleader.model.BasemapLayer;
import com.esri.squadleader.model.GeoPackageReader;
import com.esri.squadleader.model.Mil2525CMessageLayer;
import com.esri.squadleader.util.Utilities;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.xml.parsers.ParserConfigurationException;

/**
 * A controller for the MapView object used in the application.
 */
public class MapController extends com.esri.militaryapps.controller.MapController {

    private static class LocationChangeHandler extends Handler {

        public static final String KEY_MAPX = "mapx";
        public static final String KEY_MAPY = "mapy";

        private final WeakReference<MapController> mapControllerRef;

        LocationChangeHandler(MapController mapController) {
            mapControllerRef = new WeakReference<MapController>(mapController);
        }

        @Override
        public void handleMessage(Message msg) {
            final MapController mapController = mapControllerRef.get();
            Bundle bundle = msg.getData();
            final Point mapPoint = new Point(bundle.getDouble(KEY_MAPX), bundle.getDouble(KEY_MAPY));

            if (mapController.isAutoPan()) {
                mapController.panTo(mapPoint);
            }

            //If we're using the device's location service, we don't need to add the graphic.
            if (LocationMode.SIMULATOR == mapController.getLocationController().getMode()) {
                if (-1 == mapController.locationGraphicId) {
                    try {
                        mapController.locationGraphicId = mapController.locationGraphicsLayer.addGraphic(
                                new Graphic(mapPoint, mapController.mapView.getLocationDisplayManager().getDefaultSymbol()));
                    } catch (Exception e) {
                        Log.e(TAG, "Couldn't add location graphic", e);
                    }
                } else {
                    mapController.locationGraphicsLayer.updateGraphic(mapController.locationGraphicId, mapPoint);
                }
            } else {
                mapController.locationGraphicsLayer.removeAll();
                mapController.locationGraphicId = -1;
            }
        }

    }

    private static final String TAG = MapController.class.getSimpleName();

    private static final RGBRenderer RGB_RENDERER = new RGBRenderer();
    private static final SimpleRenderer FILL_RENDERER = new SimpleRenderer(new SimpleFillSymbol(Color.RED));
    private static final SimpleRenderer LINE_RENDERER = new SimpleRenderer(new SimpleLineSymbol(Color.rgb(128, 64, 0), 5f));
    private static final SimpleRenderer MARKER_RENDERER = new SimpleRenderer(new SimpleMarkerSymbol(Color.BLUE, 10, SimpleMarkerSymbol.STYLE.CIRCLE));

    private final MapView mapView;
    private final AssetManager assetManager;
    private final OnStatusChangedListener layerListener;
    private final Activity targetActivity;
    private final List<BasemapLayer> basemapLayers = new ArrayList<BasemapLayer>();
    private final List<Layer> nonBasemapLayers = new ArrayList<Layer>();
    private final GraphicsLayer locationGraphicsLayer = new GraphicsLayer();
    private final LocationChangeHandler locationChangeHandler = new LocationChangeHandler(this);
    private final Object lastLocationLock = new Object();
    private final HashSet<ShapefileFeatureTable> shapefileFeatureTables = new HashSet<ShapefileFeatureTable>();
    private boolean autoPan = false;
    private int locationGraphicId = -1;
    private Point lastLocation = null;
    private MapConfig lastMapConfig = null;
    private SpatialReference lastSpatialReference = null;

    /**
     * Creates a new MapController. <b>Call dispose() on each MapController you create when you are done!</b>
     *
     * @param mapView        the MapView being controlled by the new MapController.
     * @param assetManager   the application's AssetManager.
     * @param layerListener  an OnStatusChangedListener that will be set for each layer that is added to
     *                       the map through this MapController. If null, each layer will keep its existing
     *                       or default listener.
     * @param targetActivity the target Activity, which is used to check and request permissions.
     *                       The Activity's onRequestPermissionsResult method will be called.
     *                       If targetActivity is null, LocationController will not do anything
     *                       useful.
     */
    public MapController(
            final MapView mapView,
            AssetManager assetManager,
            OnStatusChangedListener layerListener,
            Activity targetActivity) {
        super(LocationController.getLocationModeFromPreferences(
                mapView.getContext().getSharedPreferences(
                        LocationController.PREFS_NAME,
                        Context.MODE_PRIVATE)),
                mapView.getContext().getString(R.string.gpx_resource_path),
                new File(Environment.getExternalStorageDirectory(), mapView.getContext().getString(R.string.gpx_deployment_path)).getAbsolutePath());
        this.layerListener = layerListener;
        this.targetActivity = targetActivity;
        LocationController locationController = (LocationController) getLocationController();
        locationController.setSharedPreferences(mapView.getContext().getSharedPreferences(LocationController.PREFS_NAME, Context.MODE_PRIVATE));
        locationController.setTargetActivity(targetActivity);
        locationController.setLocationService(mapView.getLocationDisplayManager());
        try {
            locationController.start();
        } catch (Throwable t) {
            Log.w(TAG, "Couldn't start LocationController", t);
        }
        this.mapView = mapView;
        mapView.setOnStatusChangedListener(new OnStatusChangedListener() {

            private static final long serialVersionUID = 3362997958525760249L;

            @Override
            public void onStatusChanged(Object source, STATUS status) {
                if (source == mapView && STATUS.INITIALIZED == status) {
                    if (null != getSpatialReference()) {
                        lastSpatialReference = getSpatialReference();
                    }
                    fireMapReady();
                }
            }

        });

        setAutoPan(autoPan);

        mapView.setAllowRotationByPinch(true);

        mapView.getGrid().setType(GridType.MGRS);
        mapView.getGrid().setVisibility(false);
        this.assetManager = assetManager;
        reloadMapConfig();
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    /**
     * Releases certain resources. Be sure to call this method when you're done with a MapController.
     */
    public void dispose() {
        for (Layer layer : mapView.getLayers()) {
            layer.recycle();
        }
        mapView.removeAll();
        for (ShapefileFeatureTable table : shapefileFeatureTables) {
            table.dispose();
        }
        shapefileFeatureTables.clear();
        GeoPackageReader.getInstance().dispose();
    }

    /**
     * Loads a map configuration using one of the following approaches, trying each approach in order
     * until one of them works.
     * <ol>
     * <li>Check for existing user preferences and use those.</li>
     * <li>Check for /mnt/sdcard/SquadLeader/mapconfig.xml and parse that with MapConfigReader.</li>
     * <li>Use mapconfig.xml built into the app.</li>
     * </ol>
     */
    public void reloadMapConfig() {
        reloadMapConfig(true);
    }

    private void reloadMapConfig(boolean useExistingPreferences) {
        mapView.removeAll();

        /**
         * Load a map configuration using one of these approaches. Try the first on the list and try each
         * approach until one of them works.
         * - Check for existing user preferences and use those.
         * - Check for /mnt/sdcard/SquadLeader/mapconfig.xml and parse that with MapConfigReader.
         * - Use mapconfig.xml built into the app
         */
        MapConfig mapConfig = null;
        Context context = mapView.getContext();
        FileInputStream serializedMapConfigStream = null;
        if (useExistingPreferences) {
            try {
                serializedMapConfigStream = context.openFileInput(context.getString(R.string.map_config_prefname));
            } catch (FileNotFoundException e) {
                //Swallow
            }
        }
        if (null != serializedMapConfigStream) {
            Log.d(TAG, "Loading mapConfig previously saved on device");
            try {
                mapConfig = (MapConfig) new ObjectInputStream(serializedMapConfigStream).readObject();
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Couldn't load the class for deserialized object", e);
            } catch (IOException e) {
                Log.e(TAG, "Couldn't deserialize object", e);
            }
        }
        if (null == mapConfig) {
            try {
                mapConfig = Utilities.readMapConfig(context, assetManager);
                if (null != mapConfig) {
                    //Write mapConfig to preferences
                    FileOutputStream out = context.openFileOutput(context.getString(R.string.map_config_prefname), Context.MODE_PRIVATE);
                    new ObjectOutputStream(out).writeObject(mapConfig);
                    out.close();
                } else {
                    Log.e(TAG, "Read MapConfig from stream but it came back null");
                }
            } catch (Exception e) {
                Log.e(TAG, "Couldn't read MapConfig", e);
            }
        }
        if (null != mapConfig) {
            fireMapConfigRead(mapConfig);
            lastMapConfig = mapConfig;
            //Load map layers from mapConfig
            for (BasemapLayerInfo layerInfo : mapConfig.getBasemapLayers()) {
                List<Layer> layers = createLayers(layerInfo);
                for (Layer layer : layers) {
                    addBasemapLayer(new BasemapLayer(layer, layerInfo.getThumbnailUrl()));
                }
            }

            for (LayerInfo layerInfo : mapConfig.getNonBasemapLayers()) {
                List<Layer> layers = createLayers(layerInfo);
                for (Layer layer : layers) {
                    addLayer(layer);
                }
            }

            if (0 != mapConfig.getScale()) {
                zoomToScale(mapConfig.getScale(), mapConfig.getCenterX(), mapConfig.getCenterY());
            }
            setRotation(mapConfig.getRotation());
        }

        addLayer(locationGraphicsLayer, true);
    }

    /**
     * Returns the last MapConfig that this MapController successfully read. Note that making changes
     * to this MapConfig object has no effect on this MapController.
     *
     * @return the last MapConfig that this MapController successfully read.
     */
    public MapConfig getLastMapConfig() {
        return lastMapConfig;
    }

    /**
     * Set a listener that fires when the map is single-tapped. Set to null to remove the current listener.
     *
     * @param listener the listener.
     */
    public void setOnSingleTapListener(OnSingleTapListener listener) {
        if (null != mapView) {
            mapView.setOnSingleTapListener(listener);
        }
    }

    @Override
    public void reset() throws ParserConfigurationException, SAXException,
            IOException {
        super.reset();
        removeAllLayers();
        reloadMapConfig(false);
    }

    /**
     * Removes a layer from the map.
     *
     * @param layer the layer to remove.
     * @return true if the layer was present in the map and hence was removed.
     */
    public boolean removeLayer(Layer layer) {
        boolean removed = basemapLayers.remove(layer);
        removed |= nonBasemapLayers.remove(layer);
        try {
            mapView.removeLayer(layer);
        } catch (Throwable t) {
            removed = false;
        }
        return removed;
    }

    public void removeAllLayers() {
        basemapLayers.clear();
        nonBasemapLayers.clear();
        mapView.removeAll();
    }

    public List<BasemapLayer> getBasemapLayers() {
        return basemapLayers;
    }

    public int getVisibleBasemapLayerIndex() {
        for (int i = 0; i < basemapLayers.size(); i++) {
            if (basemapLayers.get(i).getLayer().isVisible()) {
                return i;
            }
        }
        return -1;
    }

    public void setVisibleBasemapLayerIndex(final int index) {
        int oldIndex = getVisibleBasemapLayerIndex();
        if (index != oldIndex) {
            basemapLayers.get(index).getLayer().setVisible(true);
            basemapLayers.get(oldIndex).getLayer().setVisible(false);
        }
    }

    public List<Layer> getNonBasemapLayers() {
        return nonBasemapLayers;
    }

    public Context getContext() {
        return mapView.getContext();
    }

    private List<Layer> createLayers(LayerInfo layerInfo) {
        List<Layer> layerList = null;
        Layer singleLayer = null;
        switch (layerInfo.getLayerType()) {
            case TILED_MAP_SERVICE: {
                singleLayer = new ArcGISTiledMapServiceLayer(layerInfo.getDatasetPath());
                break;
            }
            case DYNAMIC_MAP_SERVICE: {
                singleLayer = new ArcGISDynamicMapServiceLayer(layerInfo.getDatasetPath());
                break;
            }
            case TILED_CACHE: {
                singleLayer = new ArcGISLocalTiledLayer(layerInfo.getDatasetPath());
                break;
            }
            case MIL2525C_MESSAGE: {
                try {
                    singleLayer = Mil2525CMessageLayer.newInstance(
                            layerInfo.getDatasetPath(), layerInfo.getName(), this,
                            getContext().getString(R.string.sym_dict_dirname), assetManager);
                } catch (Exception e) {
                    Log.e(TAG, "Couldn't create Mil2525CMessageLayer", e);
                }
                break;
            }
            case FEATURE_SERVICE: {
                singleLayer = new ArcGISFeatureLayer(layerInfo.getDatasetPath(), MODE.ONDEMAND);
                break;
            }
            case IMAGE_SERVICE: {
                singleLayer = new ArcGISImageServiceLayer(layerInfo.getDatasetPath(), null);
                break;
            }
            case GEOPACKAGE: {
                try {
                    final String path = layerInfo.getDatasetPath();
                    layerList = GeoPackageReader.getInstance().readGeoPackageToLayerList(
                            path,
                            mapView.getSpatialReference(),
                            layerInfo.isShowVectors(),
                            layerInfo.isShowRasters(),
                            RGB_RENDERER,
                            MARKER_RENDERER,
                            LINE_RENDERER,
                            FILL_RENDERER);
                } catch (IOException e) {
                    Log.e(TAG, "Couldn't read GeoPackage file " + layerInfo.getDatasetPath(), e);
                    Toast.makeText(
                            getContext(),
                            "Couldn't read GeoPackage file " + layerInfo.getDatasetPath() + ": " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
                break;
            }
            case SHAPEFILE: {
                try {
                    final ShapefileFeatureTable table = new ShapefileFeatureTable(layerInfo.getDatasetPath());
                    shapefileFeatureTables.add(table);
                    FeatureLayer featureLayer = new FeatureLayer(table);
                    Renderer renderer = null;
                    switch (table.getGeometryType()) {
                        case ENVELOPE:
                        case POLYGON:
                            renderer = FILL_RENDERER;
                            break;

                        case LINE:
                        case POLYLINE:
                            renderer = LINE_RENDERER;
                            break;

                        case MULTIPOINT:
                        case POINT:
                            renderer = MARKER_RENDERER;
                    }
                    featureLayer.setRenderer(renderer);
                    singleLayer = featureLayer;
                } catch (Throwable t) {
                    Log.e(TAG, "Could not add shapefile " + layerInfo.getDatasetPath(), t);
                    Toast.makeText(getContext(), "Could not add shapefile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default: {
                Log.i(TAG, "Layer " + layerInfo.getName() + " is of a type not yet implemented in ArcGIS Runtime for Android.");
            }
        }
        if (null != layerList) {
            for (Layer layer : layerList) {
                layer.setName(layerInfo.getName() + " : " + layer.getName());
                layer.setVisible(layerInfo.isVisible());
            }
        } else if (null != singleLayer) {
            singleLayer.setName(layerInfo.getName());
            singleLayer.setVisible(layerInfo.isVisible());
            layerList = new ArrayList<Layer>(1);
            layerList.add(singleLayer);
        } else {
            layerList = new ArrayList<Layer>();
        }

        return layerList;
    }

    /**
     * Adds a non-basemap layer to the map.
     *
     * @param layer the layer to add to the map.
     */
    public void addLayer(Layer layer) {
        addLayer(layer, false);
    }

    /**
     * Adds a non-basemap layer to the map.
     * TODO make this method public when overlay layers are implemented
     *
     * @param layer     the layer to add to the map.
     * @param isOverlay true if the layer is an overlay that can be turned on and
     *                  off, and false otherwise.
     */
    private void addLayer(Layer layer, boolean isOverlay) {
        //TODO do something with isOverlay (i.e. implement overlay layers)
        if (null != layerListener) {
            layer.setOnStatusChangedListener(layerListener);
        }
        mapView.addLayer(layer);
        nonBasemapLayers.add(layer);
        fireLayersChanged(isOverlay);
    }

    /**
     * Adds a basemap layer to the map.
     *
     * @param basemapLayer the layer to add to the map.
     */
    public void addBasemapLayer(BasemapLayer basemapLayer) {
        addBasemapLayer(basemapLayer, false);
    }

    /**
     * Adds a basemap layer to the map.
     * TODO make this method public when overlay layers are implemented
     *
     * @param basemapLayer the layer to add to the map.
     * @param isOverlay    true if the layer is an overlay that can be turned on and
     *                     off, and false otherwise.
     */
    private void addBasemapLayer(BasemapLayer basemapLayer, boolean isOverlay) {
        //TODO do something with isOverlay (i.e. implement overlay layers)
        if (null != layerListener) {
            basemapLayer.getLayer().setOnStatusChangedListener(layerListener);
        }
        mapView.addLayer(basemapLayer.getLayer(), basemapLayers.size());
        basemapLayers.add(basemapLayer);
        if (basemapLayer.getLayer().isVisible()) {
            setVisibleBasemapLayerIndex(basemapLayers.size() - 1);
        }
        fireLayersChanged(isOverlay);
    }

    /**
     * Adds a layer to the map based on a LayerInfo object.
     *
     * @param layerInfo the LayerInfo object that specifies the layer to add to the map.
     *                  Whether the layer will be a basemap layer or not depends on whether
     *                  the LayerInfo object is also of type BasemapLayerInfo.
     */
    public void addLayer(LayerInfo layerInfo) {
        addLayer(layerInfo, false);
    }

    /**
     * Adds a layer to the map based on a LayerInfo object.
     * TODO make this method public when overlay layers are implemented
     *
     * @param layerInfo the LayerInfo object that specifies the layer to add to the map.
     *                  Whether the layer will be a basemap layer or not depends on whether
     *                  the LayerInfo object is also of type BasemapLayerInfo.
     * @param isOverlay true if the layer is an overlay that can be turned on and
     *                  off, and false otherwise.
     */
    private void addLayer(LayerInfo layerInfo, boolean isOverlay) {
        //TODO do something with isOverlay (i.e. implement overlay layers)
        List<Layer> layers = createLayers(layerInfo);
        for (Layer layer : layers) {
            if (layerInfo instanceof BasemapLayerInfo) {
                BasemapLayer basemapLayer = new BasemapLayer(layer, ((BasemapLayerInfo) layerInfo).getThumbnailUrl());
                addBasemapLayer(basemapLayer, isOverlay);
            } else {
                addLayer(layer, isOverlay);
            }
        }
        //TODO emit an error if layer was null (Exception, Toast, something)
    }

    /**
     * Zooms in on the map, just like calling MapView.zoomIn().
     */
    @Override
    public void zoomIn() {
        mapView.zoomin();
    }

    /**
     * Zooms out on the map, just like calling MapView.zoomOut().
     */
    @Override
    public void zoomOut() {
        mapView.zoomout();
    }

    /**
     * Call pause() when the activity/application is paused, so that the MapView
     * gets paused.
     */
    public void pause() {
        getLocationController().pause();
        mapView.pause();
    }

    /**
     * Call unpause() when the activity/application is unpaused, so that the
     * MapView gets unpaused.
     */
    public void unpause() {
        mapView.unpause();
        try {
            getLocationController().unpause();
        } catch (Exception e) {
            Log.d(TAG, "Couldn't unpause LocationController", e);
        }
    }

    @Override
    public void zoom(double factor) {
        mapView.setScale(factor);
    }

    @Override
    public void setRotation(double degrees) {
        mapView.setRotationAngle(degrees);
    }

    @Override
    public double getRotation() {
        return mapView.getRotationAngle();
    }

    @Override
    protected void _zoomToScale(double scale, double centerPointX, double centerPointY) {
        mapView.zoomToScale(new Point(centerPointX, centerPointY), scale);
    }

    @Override
    public int getWidth() {
        return mapView.getWidth();
    }

    @Override
    public int getHeight() {
        return mapView.getHeight();
    }

    @Override
    public void panTo(double centerX, double centerY) {
        panTo(new Point(centerX, centerY));
    }

    public void panTo(Point newCenter) {
        mapView.centerAt(newCenter, true);
    }

    /**
     * Pans the map to a new center point, if a valid MGRS string is provided.
     *
     * @param newCenterMgrs the map's new center point, as an MGRS string.
     * @return if the string was valid, the point to which the map was panned; null otherwise
     */
    public Point panTo(String newCenterMgrs) {
        newCenterMgrs = Utilities.convertToValidMgrs(newCenterMgrs,
                pointToMgrs(mapView.getMaxExtent().getCenter()));
        if (null != newCenterMgrs) {
            Point pt = mgrsToPoint(newCenterMgrs);
            if (null != pt) {
                panTo(pt);
                return pt;
            } else {
                Log.w(TAG, "MGRS string " + newCenterMgrs + " could not be converted to a point");
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the map point for the screen coordinates provided.
     *
     * @param screenX
     * @param screenY
     * @return a Point object in map coordinates for the specified screen X and Y,
     * or null if the map is not initialized and hence can't convert to map coordinates.
     */
    public Point toMapPointObject(int screenX, int screenY) {
        return mapView.toMapPoint(screenX, screenY);
    }

    @Override
    public double[] toMapPoint(int screenX, int screenY) {
        Point pt = toMapPointObject(screenX, screenY);
        if (null == pt) {
            return null;
        } else {
            return new double[]{pt.getX(), pt.getY()};
        }
    }

    @Override
    public double[] toScreenPoint(double mapX, double mapY) {
        final Point screenPoint = mapView.toScreenPoint(new Point(mapX, mapY));
        return null == screenPoint ? null : new double[]{screenPoint.getX(), screenPoint.getY()};
    }

    @Override
    public void setGridVisible(boolean visible) {
        mapView.getGrid().setVisibility(visible);
    }

    @Override
    public boolean isGridVisible() {
        return mapView.getGrid().getVisibility();
    }

    /**
     * Converts an array of map points to MGRS strings.
     *
     * @param points the points, in map coordinates, to convert to MGRS strings.
     * @return an array of MGRS strings corresponding to the input points.
     * @deprecated use pointsToMgrs instead.
     */
    public String[] toMilitaryGrid(Point[] points) {
        SpatialReference sr = getSpatialReference();
        if (null == sr) {
            //Assume Web Mercator (3857)
            sr = SpatialReference.create(3857);
        }
        return toMilitaryGrid(points, sr);
    }

    /**
     * Converts an array of points in a known spatial reference to MGRS strings.
     *
     * @param points the points to convert to MGRS strings.
     * @param fromSr the spatial reference of all of the points.
     * @return an array of MGRS strings corresponding to the input points.
     * @deprecated use pointsToMgrs instead.
     */
    public String[] toMilitaryGrid(Point[] points, SpatialReference fromSr) {
        List<String> mgrsStrings = pointsToMgrs(Arrays.asList(points), fromSr);
        return mgrsStrings.toArray(new String[mgrsStrings.size()]);
    }

    /**
     * Converts a list of map points to MGRS strings.
     *
     * @param points the points, in map coordinates, to convert to MGRS strings.
     * @return a list of MGRS strings corresponding to the input points.
     */
    public List<String> pointsToMgrs(List<Point> points) {
        SpatialReference sr = getSpatialReference();
        if (null == sr) {
            //Assume Web Mercator (3857)
            sr = SpatialReference.create(3857);
        }
        return pointsToMgrs(points, sr);
    }

    /**
     * Converts a list of map points to MGRS strings.
     *
     * @param points the points to convert to MGRS strings.
     * @param fromSr the spatial reference of all of the points.
     * @return a list of MGRS strings corresponding to the input points.
     */
    public List<String> pointsToMgrs(List<Point> points, SpatialReference fromSr) {
        return CoordinateConversion.pointsToMgrs(points, fromSr, MGRSConversionMode.AUTO, 5, false, true);
    }

    /**
     * Converts a map point to an MGRS string.
     *
     * @param point the point, in map coordinates, to convert to an MGRS string.
     * @return an MGRS string corresponding to the input point, or null if the point
     * cannot be converted.
     */
    public String pointToMgrs(Point point) {
        SpatialReference sr = getSpatialReference();
        if (null == sr) {
            //Assume Web Mercator (3857)
            sr = SpatialReference.create(3857);
        }
        return pointToMgrs(point, sr);
    }

    /**
     * Converts a map point to an MGRS string.
     *
     * @param point  the point to convert to an MGRS string.
     * @param fromSr the spatial reference of the point.
     * @return an MGRS string corresponding to the input point, or null if the point
     * cannot be converted.
     */
    public String pointToMgrs(Point point, SpatialReference fromSr) {
        try {
            return CoordinateConversion.pointToMgrs(point, fromSr, MGRSConversionMode.AUTO, 5, false, true);
        } catch (Throwable t) {
            Log.e(TAG, "Could not convert " + point, t);
            return null;
        }
    }

    @Override
    public String pointToMgrs(double x, double y, int wkid) {
        return pointToMgrs(new Point(x, y), SpatialReference.create(wkid));
    }

    /**
     * Converts an array of MGRS strings to map points.
     *
     * @param mgrsStrings the MGRS strings to convert to map points.
     * @return an array of map points in the coordinate system of the map.
     * @deprecated use mgrsToPoints instead.
     */
    public Point[] fromMilitaryGrid(String[] mgrsStrings) {
        List<Point> pointsList = mgrsToPoints(Arrays.asList(mgrsStrings));
        return pointsList.toArray(new Point[pointsList.size()]);
    }

    /**
     * Converts a list of MGRS strings to map points.
     *
     * @param mgrsStrings the MGRS strings to convert to map points.
     * @return a list of map points in the coordinate system of the map.
     */
    public List<Point> mgrsToPoints(List<String> mgrsStrings) {
        SpatialReference sr = getSpatialReference();
        if (null == sr) {
            //Assume Web Mercator (3857)
            sr = SpatialReference.create(3857);
        }
        return CoordinateConversion.mgrsToPoints(mgrsStrings, sr, MGRSConversionMode.AUTO);
    }

    /**
     * Converts an MGRS string to a map point.
     *
     * @param mgrsString the MGRS string to convert to a map point.
     * @return a map point in the coordinate system of the map.
     */
    public Point mgrsToPoint(String mgrsString) {
        SpatialReference sr = getSpatialReference();
        if (null == sr) {
            //Assume Web Mercator (3857)
            sr = SpatialReference.create(3857);
        }
        return CoordinateConversion.mgrsToPoint(mgrsString, sr, MGRSConversionMode.AUTO);
    }

    @Override
    public void onLocationChanged(com.esri.militaryapps.model.Location location) {
        if (null != location) {
            final Point mapPoint = GeometryEngine.project(location.getLongitude(), location.getLatitude(), getSpatialReference());
            Bundle bundle = new Bundle();
            bundle.putDouble(LocationChangeHandler.KEY_MAPX, mapPoint.getX());
            bundle.putDouble(LocationChangeHandler.KEY_MAPY, mapPoint.getY());
            Message msg = new Message();
            msg.setData(bundle);
            locationChangeHandler.sendMessage(msg);
            new Thread() {
                public void run() {
                    synchronized (lastLocationLock) {
                        lastLocation = mapPoint;
                    }
                }

                ;
            }.start();
        }
    }

    @Override
    public void onStateChanged(LocationProviderState state) {

    }

    @Override
    protected LocationController createLocationController(String builtInGpxPath, LocationMode locationMode,
                                                          String gpxDeploymentPath) {
        File file = null;
        if (null == locationMode) {
            file = new File(gpxDeploymentPath);
            if (file.exists()) {
                locationMode = LocationMode.SIMULATOR;
            } else {
                locationMode = LocationMode.LOCATION_SERVICE;
                file = null;
            }
        }
        try {
            LocationController locationController = new LocationController(
                    builtInGpxPath,
                    locationMode);
            if (null != file) {
                locationController.setGpxFile(file, false);
            }
            return locationController;
        } catch (Exception e) {
            Log.e(TAG, "Couldn't instantiate LocationController", e);
            return null;
        }
    }

    @Override
    public void setAutoPan(boolean autoPan) {
        if (autoPan) {
            synchronized (lastLocationLock) {
                if (null != lastLocation) {
                    panTo(lastLocation);
                }
            }
        }
        this.autoPan = autoPan;
        LocationDisplayManager locationDisplayManager = mapView.getLocationDisplayManager();
        if (null != locationDisplayManager) {
            locationDisplayManager.setAutoPanMode(autoPan ? AutoPanMode.LOCATION : AutoPanMode.OFF);
        }
    }

    @Override
    public boolean isAutoPan() {
        return autoPan;
    }

    /**
     * Returns the spatial reference of the MapView that this controller controls.
     *
     * @return the spatial reference of the MapView that this controller controls.
     */
    public SpatialReference getSpatialReference() {
        SpatialReference sr = mapView.getSpatialReference();
        if (null == sr) {
            sr = lastSpatialReference;
        }
        return sr;
    }

    public double[] projectPoint(double x, double y, int fromWkid, int toWkid) {
        Point pt = (Point) GeometryEngine.project(new Point(x, y), SpatialReference.create(fromWkid), SpatialReference.create(toWkid));
        return new double[]{pt.getX(), pt.getY()};
    }

    public Callout getCallout() {
        return mapView.getCallout();
    }

    public void setShowMagnifierOnLongPress(boolean showMagnifier) {
        mapView.setShowMagnifierOnLongPress(showMagnifier);
    }

    public FutureTask<List<Popup>> queryFeatureLayer(final FeatureLayer featureLayer, final QueryParameters queryParameters) {
        return new FutureTask<>(new Callable<List<Popup>>() {
            @Override
            public List<Popup> call() throws Exception {
                ArrayList<Popup> popups = new ArrayList<>();
                try {
                    final long[] featureIds = featureLayer.getFeatureTable().queryIds(queryParameters, null).get();
                    for (long featureId : featureIds) {
                        popups.add(createPopup(featureLayer, featureLayer.getFeature(featureId)));
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Could not query layer " + featureLayer.getName(), t);
                }
                return popups;
            }
        });
    }

    public FutureTask<List<Popup>> identifyFeatureLayers(final float screenX, final float screenY) {
        return new FutureTask<>(new Callable<List<Popup>>() {
            @Override
            public List<Popup> call() throws Exception {
                ArrayList<Popup> popups = new ArrayList<>();
                final List<Layer> layers = getNonBasemapLayers();
                for (Layer layer : layers) {
                    if (layer instanceof FeatureLayer) {
                        FeatureLayer featureLayer = (FeatureLayer) layer;
                        try {
                            final long[] featureIds = featureLayer.getFeatureIDs(screenX, screenY, 5);
                            for (long featureId : featureIds) {
                                popups.add(createPopup(featureLayer, featureLayer.getFeature(featureId)));
                            }
                        } catch (Throwable t) {
                            Log.w(TAG, "Could not identify on layer " + featureLayer.getName(), t);
                        }
                    }
                }
                return popups;
            }
        });
    }

    private Popup createPopup(Layer layer, Feature feature) {
        final Popup popup = layer.createPopup(mapView, 0, feature);

        // In the popup, if a field's label is missing, set it to be the field name.
        final Map<String, PopupFieldInfo> fieldInfos = popup.getPopupInfo().getFieldInfos();
        for (String key : fieldInfos.keySet()) {
            final PopupFieldInfo fieldInfo = fieldInfos.get(key);
            if (null == fieldInfo.getLabel() || fieldInfo.getLabel().trim().isEmpty()) {
                fieldInfo.setLabel(fieldInfo.getFieldName());
            }
            // Hide the geometry field.
            if ("geom".equals(fieldInfo.getFieldName())) {
                fieldInfo.setVisible(false);
            }
        }

        return popup;
    }

}
