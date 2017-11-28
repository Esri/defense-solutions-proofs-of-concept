/*******************************************************************************
 * Copyright 2012-2015 Esri
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
package com.esri.vehiclecommander.model;

import com.esri.core.map.Graphic;
import com.esri.core.renderer.DictionaryRenderer;
import com.esri.core.symbol.advanced.Message;
import com.esri.core.symbol.advanced.MessageProcessor;
import com.esri.core.symbol.advanced.SymbolDictionary.DictionaryType;
import com.esri.map.GraphicsLayer;
import com.esri.map.Layer;
import com.esri.map.MessageGroupLayer;
import com.esri.vehiclecommander.controller.AppConfigController;
import com.esri.vehiclecommander.controller.AppConfigListener;
import com.esri.vehiclecommander.controller.MapController;
import com.esri.vehiclecommander.controller.MapControllerListenerAdapter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * A layer that displays MIL-STD-2525C messages from an XML string.
 */
public class Mil2525CMessageLayer extends MessageGroupLayer {

    private final AppConfigController appConfig;

    /**
     * Constructs a Mil2525CMessageLayer, assuming that the ArcGIS Runtime deployment
     * is located in the application directory. Use the other constructor if the
     * ArcGIS Runtime deployment is elsewhere.
     * @param xmlMessageFilename the XML file on which this layer is based.
     * @param name the layer name.
     * @param mapController the MapController.
     * @param appConfig the application configuration's controller (cannot be null).
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public Mil2525CMessageLayer(String xmlMessageFilename, String name, MapController mapController, AppConfigController appConfig)
            throws IOException, ParserConfigurationException, SAXException {
        super(DictionaryType.Mil2525C);
        this.appConfig = appConfig;
        init(xmlMessageFilename, name, mapController, null);
    }

    /**
     * Constructs a Mil2525CMessageLayer.
     * @param xmlMessageFilename the XML file on which this layer is based.
     * @param name the layer name.
     * @param mapController the MapController.
     * @param appConfig the application configuration's controller.
     * @param symbolDictionaryPath the directory containing the ArcGIS Runtime deployment.
     *        This must not be null! If it's null, call the other constructor.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public Mil2525CMessageLayer(String xmlMessageFilename, String name, MapController mapController, AppConfigController appConfig, String symbolDictionaryPath)
            throws IOException, ParserConfigurationException, SAXException {
        super(DictionaryType.Mil2525C, symbolDictionaryPath);
        this.appConfig = appConfig;
        init(xmlMessageFilename, name, mapController, symbolDictionaryPath);
    }

    private void init(String xmlMessageFilename, String name, MapController mapController, String symbolDictionaryPath) throws ParserConfigurationException, SAXException, IOException {
        appConfig.addListener(new AppConfigListener() {

            public void decoratedChanged(boolean isDecorated) {
                //Do nothing
            }

            public void showMessageLabelsChanged(boolean showMessageLabels) {
                toggleLabels(showMessageLabels);
            }
        });
        
        this.setName(name);
        final MessageProcessor processor = null == symbolDictionaryPath ?
            new MessageProcessor(DictionaryType.Mil2525C, this, 1.0) :
            new MessageProcessor(DictionaryType.Mil2525C, this, symbolDictionaryPath);
        Mil2525CMessageParser parser = new Mil2525CMessageParser();
        parser.parseMessages(new File(xmlMessageFilename));
        final List<Message> messages = parser.getMessages();
        mapController.addListener(new MapControllerListenerAdapter() {

            @Override
            public void mapReady() {
                synchronized (messages) {
                    for (Message message : messages) {
                        try {
                            //Any other problem simply throws a RuntimeException, but a missing
                            //message ID crashes the JVM. Therefore, we test for that case and
                            //throw our own RuntimeException.
                            if (null == message.getID()) {
                                throw new RuntimeException("Message ID is null");
                            } else {
                                processor.processMessage(message);
                            }
                        } catch (RuntimeException re) {
                            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Bad message in layer\n\tMessage: " + message + "\n\tError: " + re.getMessage());
                        }
                    }
                    //Now that all the sublayers are created, turn labels on or off.
                    toggleLabels(appConfig.isShowMessageLabels());
                }
            }
        });
    }
    
    private void toggleLabels(boolean showLabels) {
        for (Layer layer : getLayers()) {
            GraphicsLayer graphicsLayer = (GraphicsLayer) layer;
            if (graphicsLayer.getRenderer() instanceof DictionaryRenderer) {
                DictionaryRenderer dictionaryRenderer = (DictionaryRenderer) graphicsLayer.getRenderer();
                dictionaryRenderer.setLabelsVisible(showLabels);
                graphicsLayer.setRenderer(dictionaryRenderer);
            }
        }
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

}
