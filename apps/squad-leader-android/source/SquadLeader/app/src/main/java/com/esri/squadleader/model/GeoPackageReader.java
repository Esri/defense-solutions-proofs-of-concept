/*******************************************************************************
 * Copyright 2016-2017 Esri
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
package com.esri.squadleader.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.RasterLayer;
import com.esri.core.ags.LayerServiceInfo;
import com.esri.core.geodatabase.Geopackage;
import com.esri.core.geodatabase.GeopackageFeatureTable;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Field;
import com.esri.core.raster.FileRasterSource;
import com.esri.core.raster.RasterSource;
import com.esri.core.renderer.RasterRenderer;
import com.esri.core.renderer.Renderer;

import org.codehaus.jackson.JsonFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Opens OGC GeoPackages and gets their data and layers.
 */
public class GeoPackageReader {

    private static final String TAG = GeoPackageReader.class.getSimpleName();

    private static GeoPackageReader instance = new GeoPackageReader();

    /**
     * <b>Be sure to call dispose() if you get and use the GeoPackageReader instance when you're done
     * with the GeoPackages it opened for you!</b>
     *
     * @return
     */
    public static GeoPackageReader getInstance() {
        return instance;
    }

    private final HashSet<Geopackage> geopackages = new HashSet<Geopackage>();
    private final HashSet<RasterSource> rasterSources = new HashSet<RasterSource>();

    private GeoPackageReader() {

    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    /**
     * Disposes any resources created by opening GeoPackages. You must call dispose() when you're done
     * with any resources that GeoPackageReader created for you!
     */
    public void dispose() {
        for (Geopackage gpkg : geopackages) {
            gpkg.dispose();
        }
        geopackages.clear();
        for (RasterSource src : rasterSources) {
            src.dispose();
        }
        rasterSources.clear();
    }

    /**
     * Reads the tables in a GeoPackage, makes a layer from each table, and returns a list containing
     * those layers.
     *
     * @param gpkgPath       the full path to the .gpkg file.
     * @param sr             the spatial reference to which any raster layers should be projected, typically the
     *                       spatial reference of your map.
     * @param showVectors    if true, this method will include the GeoPackage's vector layers.
     * @param showRasters    if true, this method will include the GeoPackage's raster layer.
     * @param rasterRenderer the renderer to be used for raster layers. One simple option is an RGBRenderer.
     * @param markerRenderer the renderer to be used for point layers.
     * @param lineRenderer   the renderer to be used for polyline layers.
     * @param fillRenderer   the renderer to be used for polygon layers.
     * @return a list of the layers created for all tables in the GeoPackage.
     * @throws IOException if gpkgPath cannot be read. Possible reasons include the file not
     *                     existing, failure to request READ_EXTERNAL_STORAGE or
     *                     WRITE_EXTERNAL_STORAGE permission, or the GeoPackage containing an
     *                     invalid spatial reference.
     */
    public List<Layer> readGeoPackageToLayerList(String gpkgPath,
                                                 SpatialReference sr,
                                                 boolean showVectors,
                                                 boolean showRasters,
                                                 RasterRenderer rasterRenderer,
                                                 Renderer markerRenderer,
                                                 Renderer lineRenderer,
                                                 Renderer fillRenderer) throws IOException {
        List<Layer> layers = new ArrayList<Layer>();

        if (showRasters) {
            // Check to see if there are any rasters before loading them
            SQLiteDatabase sqliteDb = null;
            Cursor cursor = null;
            try {
                sqliteDb = SQLiteDatabase.openDatabase(gpkgPath, null, SQLiteDatabase.OPEN_READONLY);
                cursor = sqliteDb.rawQuery("SELECT COUNT(*) FROM gpkg_contents WHERE data_type = ?", new String[]{"tiles"});
                if (cursor.moveToNext()) {
                    if (0 < cursor.getInt(0)) {
                        cursor.close();
                        sqliteDb.close();
                        FileRasterSource src = new FileRasterSource(gpkgPath);
                        rasterSources.add(src);
                        if (null != sr) {
                            src.project(sr);
                        }
                        RasterLayer rasterLayer = new RasterLayer(src);
                        rasterLayer.setRenderer(rasterRenderer);
                        rasterLayer.setName((gpkgPath.contains("/") ? gpkgPath.substring(gpkgPath.lastIndexOf("/") + 1) : gpkgPath) + " (raster)");
                        layers.add(rasterLayer);
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, "Could not read raster(s) from GeoPackage", t);
            } finally {
                if (null != cursor) {
                    cursor.close();
                }
                if (null != sqliteDb) {
                    sqliteDb.close();
                }
            }
        }

        if (showVectors) {
            Geopackage gpkg;
            try {
                gpkg = new Geopackage(gpkgPath);
            } catch (RuntimeException ex) {
                throw new IOException(
                        null != ex.getMessage() && ex.getMessage().contains("unknown wkt") ?
                                "Geopackage " + gpkgPath + " contains an invalid spatial reference." :
                                null,
                        ex);
            }
            geopackages.add(gpkg);
            List<GeopackageFeatureTable> tables = gpkg.getGeopackageFeatureTables();
            if (0 < tables.size()) {
                //First pass: polygons and unknowns
                HashSet<Geometry.Type> types = new HashSet<Geometry.Type>();
                types.add(Geometry.Type.ENVELOPE);
                types.add(Geometry.Type.POLYGON);
                types.add(Geometry.Type.UNKNOWN);
                layers.addAll(getTablesAsLayers(tables, types, fillRenderer));

                //Second pass: lines
                types.clear();
                types.add(Geometry.Type.LINE);
                types.add(Geometry.Type.POLYLINE);
                layers.addAll(getTablesAsLayers(tables, types, lineRenderer));

                //Third pass: points
                types.clear();
                types.add(Geometry.Type.MULTIPOINT);
                types.add(Geometry.Type.POINT);
                layers.addAll(getTablesAsLayers(tables, types, markerRenderer));
            }
        }

        return layers;
    }

    private static List<Layer> getTablesAsLayers(List<GeopackageFeatureTable> tables, Set<Geometry.Type> types, Renderer renderer) {
        List<Layer> layers = new ArrayList<Layer>(tables.size());
        for (GeopackageFeatureTable table : tables) {
            if (types.contains(table.getGeometryType())) {
                final FeatureLayer layer = new FeatureLayer(table);
                JSONObject jsonObject = new JSONObject();
                JSONArray fieldsArray = new JSONArray();
                try {
                    for (Field field : table.getFields()) {
                        fieldsArray.put(new JSONObject(Field.toJson(field)));
                    }
                    jsonObject.put("fields", fieldsArray);
                    LayerServiceInfo layerInfo = LayerServiceInfo.fromJson(new JsonFactory().createJsonParser(jsonObject.toString()));
                    layer.getPopupInfo(0).setLayer(layerInfo);
                } catch (Exception e) {
                    Log.e(TAG, "Could not create LayerServiceInfo for FeatureLayer for table " + table.getTableName(), e);
                }
                layer.setRenderer(renderer);
                layer.setName(table.getTableName());
                layers.add(layer);
            }
        }
        return layers;
    }

}
