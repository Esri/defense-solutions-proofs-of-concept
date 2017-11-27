/*******************************************************************************
 * Copyright 2015 Esri
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

import android.content.res.AssetManager;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.advanced.Message;
import com.esri.core.symbol.advanced.MessageGroupLayer;
import com.esri.core.symbol.advanced.SymbolDictionary.DictionaryType;
import com.esri.militaryapps.controller.MapControllerListener;
import com.esri.squadleader.controller.AdvancedSymbolController;
import com.esri.squadleader.controller.MapController;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

/**
 * A layer that displays MIL-STD-2525C messages from an XML string.
 */
public class Mil2525CMessageLayer extends MessageGroupLayer {

    private static final String TAG = Mil2525CMessageLayer.class.getSimpleName();

    private boolean showLabels = false;

    /**
     * Constructs a Mil2525CMessageLayer.
     * @param xmlMessageFilename the XML file on which this layer is based.
     * @param name the layer name.
     * @param mapController the MapController.
     * @param symbolDictionaryDirname the name of the directory where the symbol dictionary copy should
     *                                reside. Normally you should pass<code>getContext().getString(R.string.sym_dict_dirname)</code>.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static Mil2525CMessageLayer newInstance(
            String xmlMessageFilename,
            String name,
            MapController mapController,
            String symbolDictionaryDirname,
            AssetManager assetManager)
            throws IOException, ParserConfigurationException, SAXException {
        File symbolDictionaryPath = AdvancedSymbolController.copySymbolDictionaryToDisk(assetManager, symbolDictionaryDirname);
        return new Mil2525CMessageLayer(xmlMessageFilename, name, mapController, symbolDictionaryPath.getAbsolutePath());
    }

    private Mil2525CMessageLayer(String xmlMessageFilename, String name, MapController mapController, String symbolDictionaryPath)
            throws IOException, ParserConfigurationException, SAXException {
        super(DictionaryType.MIL2525C, symbolDictionaryPath);
        init(xmlMessageFilename, name, mapController);
    }

    private void init(String xmlMessageFilename, String name, MapController mapController) throws ParserConfigurationException, SAXException, IOException {
        this.setName(name);
        Mil2525CMessageParser parser = new Mil2525CMessageParser();
        parser.parseMessages(new File(xmlMessageFilename));
        final List<Message> messages = parser.getMessages();
        final MapControllerListener listener = new MapControllerListener() {

            @Override
            public void mapReady() {
                synchronized (messages) {
                    for (Message message : messages) {
                        try {
                            //Any other problem simply throws a RuntimeException, but a missing
                            //message ID crashes the app. Therefore, we test for that case and
                            //throw our own RuntimeException.
                            if (null == message.getID()) {
                                throw new RuntimeException("Message ID is null");
                            } else {
                                getMessageProcessor().processMessage(message);
                            }
                        } catch (RuntimeException re) {
                            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Bad message in layer\n\tMessage: " + message + "\n\tError: " + re.getMessage());
                        }
                    }
                    //Now that all the sublayers are created, turn labels on or off.
                    toggleLabels(isShowLabels());
                }
            }

            public void layersChanged(boolean isOverlay) {}
        };
        Thread readyThread = new Thread() {
            @Override
            public void run() {
                listener.mapReady();
            }
        };
        if (mapController.isReady()) {
            readyThread.start();
        } else {
            /**
             * TODO potential race condition. If the map wasn't ready when we checked mapController.isReady()
             *      but it is ready now, the listener won't get called and the messages won't be processed
             *      and added to the map. This behavior doesn't seem to happen, probably because some
             *      other layer is almost always added to the map before this one, but if this becomes
             *      a problem, figure out how to resolve it.
             */
            mapController.addListener(listener);
        }
    }

    private void toggleLabels(boolean showLabels) {
        /**
         * TODO this is the right way to toggle the labels, but there is a bug in the Runtime SDK for
         * Android that causes it not to work (NIM102986). When that bug is fixed, uncomment this code
         * and test it.
         */
//        for (Layer layer : getLayers()) {
//            GraphicsLayer graphicsLayer = (GraphicsLayer) layer;
//            if (graphicsLayer.getRenderer() instanceof DictionaryRenderer) {
//                DictionaryRenderer dictionaryRenderer = (DictionaryRenderer) graphicsLayer.getRenderer();
//                dictionaryRenderer.setLabelsVisible(showLabels);
//                graphicsLayer.setRenderer(dictionaryRenderer);
//            }
//        }
    }

    /**
     * Performs an identify on this layer.
     * @param screenX
     * @param screenY
     * @param tolerance
     * @return
     */
    public IdentifyResultList identify(float screenX, float screenY, int tolerance) {
        IdentifyResultList results = new IdentifyResultList();
        Layer[] layers = getLayers();
        for (Layer layer : layers) {
            if (layer instanceof GraphicsLayer) {
                IdentifyResultList theseResults = identify((GraphicsLayer) layer, screenX, screenY, tolerance);
                for (int i = 0; i < theseResults.size(); i++) {
                    results.add(theseResults.get(i), layer);
                }
            }
        }
        return results;
    }

    /**
     * Performs an identify on a GraphicsLayer and returns the results.
     * @param graphicsLayer
     * @param screenX
     * @param screenY
     * @param tolerance
     * @return
     */
    public static IdentifyResultList identify(GraphicsLayer graphicsLayer, float screenX, float screenY, int tolerance) {
        IdentifyResultList results = new IdentifyResultList();
        int[] graphicIds = graphicsLayer.getGraphicIDs(screenX, screenY, tolerance);
        for (int id : graphicIds) {
            Graphic graphic = graphicsLayer.getGraphic(id);
            IdentifiedItem item = new IdentifiedItem(
                    graphic.getGeometry(),
                    -1,
                    graphic.getAttributes(),
                    graphicsLayer.getName() + " " + graphic.getUid());
            results.add(item, graphicsLayer);
        }
        return results;
    }

    /**
     * Returns true if this layer is displaying labels and false otherwise. NOTE: currently, labels
     * cannot be toggled, due to bug NIM102986, so this method always returns false.
     * @return true if this layer is displaying labels and false otherwise.
     */
    public boolean isShowLabels() {
        return showLabels;
    }

    /**
     * Sets whether labels should display. TODO make this method public when bug NIM102986 is fixed.
     * @param showLabels true if labels should display and false otherwise.
     */
    private void setShowLabels(boolean showLabels) {
        if (this.showLabels != showLabels) {
            toggleLabels(showLabels);
        }
        this.showLabels = showLabels;
    }
}
