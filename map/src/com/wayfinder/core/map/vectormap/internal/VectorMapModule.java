/*******************************************************************************
 * Copyright (c) 1999-2010, Vodafone Group Services
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above 
 *       copyright notice, this list of conditions and the following 
 *       disclaimer in the documentation and/or other materials provided 
 *       with the distribution.
 *     * Neither the name of Vodafone Group Services nor the names of its 
 *       contributors may be used to endorse or promote products derived 
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 ******************************************************************************/
package com.wayfinder.core.map.vectormap.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.internal.SharedSystems;
import com.wayfinder.core.map.MapDownloadListener;
import com.wayfinder.core.map.MapErrorListener;
import com.wayfinder.core.map.MapKeyInterface;
import com.wayfinder.core.map.MapObject;
import com.wayfinder.core.map.MapObjectImage;
import com.wayfinder.core.map.MapObjectListener;
import com.wayfinder.core.map.MapStartupListener;
import com.wayfinder.core.map.util.ScreenInfo;
import com.wayfinder.core.map.vectormap.MapDetailedConfigInterface;
import com.wayfinder.core.map.vectormap.MapDrawerInterface;
import com.wayfinder.core.map.vectormap.MapInitialConfig;
import com.wayfinder.core.map.vectormap.PreInstalledMapsListener;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.map.vectormap.internal.control.TileMapControlThread;
import com.wayfinder.core.map.vectormap.internal.control.TileMapLoader;
import com.wayfinder.core.map.vectormap.internal.drawer.Camera;
import com.wayfinder.core.map.vectormap.internal.drawer.MapDrawer;
import com.wayfinder.core.map.vectormap.internal.drawer.MapKeyInterfaceImpl;
import com.wayfinder.core.map.vectormap.internal.drawer.MapRenderTask;
import com.wayfinder.core.map.vectormap.internal.drawer.MapUpdaterThread;
import com.wayfinder.core.map.vectormap.internal.drawer.RenderManager;
import com.wayfinder.core.map.vectormap.internal.drawer.Utils;
import com.wayfinder.core.map.vectormap.internal.route.SmoothZoomHandler;
import com.wayfinder.core.map.vectormap.internal.route.PredictRouteTileHandler;
import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.persistence.PersistenceRequest;
import com.wayfinder.core.shared.internal.settings.InternalSettingsInterface;
import com.wayfinder.core.shared.internal.settings.GeneralSettingsHolder.GeneralSettingsInternal;
import com.wayfinder.core.shared.internal.settings.InternalSettingsInterface.InternalSettingsListener;
import com.wayfinder.core.shared.internal.settings.language.LangTypes;
import com.wayfinder.core.shared.internal.settings.language.LanguageInternal;
import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.core.shared.settings.Language;
import com.wayfinder.core.network.internal.InternalNetworkInterface;
import com.wayfinder.pal.PAL;
import com.wayfinder.pal.concurrency.ConcurrencyLayer;
import com.wayfinder.pal.graphics.WFGraphicsFactory;
import com.wayfinder.pal.persistence.PersistenceLayer;
import com.wayfinder.pal.persistence.SettingsConnection;

/**
 * 
 * 
 */
public final class VectorMapModule implements VectorMapInterface, PersistenceRequest {

    private static final Logger LOG = LogFactory
            .getLoggerForClass(VectorMapModule.class);
    
    final private MapUpdaterThread m_UpdaterThread;
    final private RenderManager m_RenderManager;
    final private Camera m_Camera;  
    final private MapDrawer m_MapDrawer;
    
    private MapStartupListener m_MapStartupListener;
    final private MapKeyInterfaceImpl m_MapKeyInterface;
    final private WFTileMapHolder m_TileMapHolder;
    final private TileMapControlThread m_TileMapControlThread;
    final private MapDetailedConfigInterfaceImpl m_MapDetailedConfigInterface;
//    final private PersistenceModule m_PersistenceModule;
    final private WorkScheduler m_WorkScheduler; 
    final private PersistenceLayer m_PersistenceLayer;
    final private ConcurrencyLayer m_concurrencyLayer;
    private PredictRouteTileHandler m_PredictRouteTileHandler;
    
    private boolean m_isMapStarted = false;
    private boolean m_hasBeenInitialized = false;
    private boolean m_IsLoadingPreInstalledMaps = false;
    private boolean m_IsWaitingForPreInstalledMaps = false;
    private String m_StartupString = null;
    
    private InternalSettingsInterface m_settingsIfc;
    
    public static VectorMapInterface createVectorMapInterface(SharedSystems systems, InternalNetworkInterface network) {
        return new VectorMapModule(systems, network);
    }
    
    private VectorMapModule(SharedSystems systems, InternalNetworkInterface network) {
        
        m_WorkScheduler = systems.getWorkScheduler();
        m_PersistenceLayer = systems.getPAL().getPersistenceLayer();
        m_concurrencyLayer = systems.getPAL().getConcurrencyLayer();
//        m_PersistenceModule = systems.getPersistentModule();
        m_RenderManager = new RenderManager();
        final PAL pal = systems.getPAL();
        m_TileMapControlThread = new TileMapControlThread(pal.getPersistenceLayer(), pal.getUtilFactory());
        m_TileMapControlThread.addTileMapRequester(systems, network);
        m_Camera = new Camera();
        m_UpdaterThread = new MapUpdaterThread(m_Camera);
        m_TileMapHolder = new WFTileMapHolder(m_RenderManager);
        m_MapDrawer = new MapDrawer(m_RenderManager);
        m_MapKeyInterface = 
            new MapKeyInterfaceImpl(m_UpdaterThread, m_Camera, m_RenderManager);
        m_MapDetailedConfigInterface = 
            new MapDetailedConfigInterfaceImpl(m_TileMapControlThread, m_RenderManager, m_Camera, this);
        
        m_settingsIfc = systems.getSettingsIfc();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#initializeMap()
     */
    public synchronized void initializeMap(WFGraphicsFactory factory, MapDrawerInterface drawerInterface,
            MapInitialConfig initialConfig) {
        
        if(m_hasBeenInitialized) {
            return;
        }
        
        // Check if we have pre-installed map file(s) to load. 
        if(initialConfig.internalGetPreInstalledMapsFolders() != null) {
            TileMapLoader tileMapLoader = m_TileMapControlThread.getTileMapLoader();
            PreInstalledMapsListener mapListener = initialConfig.getPreInstalledMapsListener();
            m_IsLoadingPreInstalledMaps = true;
            String []dirs = initialConfig.internalGetPreInstalledMapsFolders();            
            PreInstalledMapLoader mLoader = 
                new PreInstalledMapLoader(dirs, tileMapLoader, this, m_PersistenceLayer, mapListener);
            m_WorkScheduler.schedule(mLoader);
        }
        
        Utils.createUtils().init(factory);
        
        // load the language form core settings instead of using the initialConfig
        // old code String language = initialConfig.getLanguageAsISO693_3();
        //          int lang = LangTypes.getLangType(language);
        int lang = m_settingsIfc.getGeneralSettings().getInternalLanguage().getLangType(); 
        
        m_Camera.init(initialConfig);        
        m_TileMapHolder.init(this, initialConfig.getWidth(), initialConfig.getHeight());        
        
        m_TileMapControlThread.init(m_concurrencyLayer, false, lang);        
        m_TileMapControlThread.startCache(initialConfig.getCacheConfigurations());        
        m_TileMapControlThread.setTileMapExtractionListener(m_TileMapHolder);
        m_TileMapControlThread.setUpdateOverviewMaps(true);

        SmoothZoomHandler zoomHandler = new SmoothZoomHandler(m_TileMapControlThread, this);
        // Check if we should enable the route pre-fetcher function. 
        if(initialConfig.useRouteTileDownloader()) {
            m_PredictRouteTileHandler = 
                new PredictRouteTileHandler(m_WorkScheduler, m_Camera, m_TileMapControlThread, this);            
        }
        
        //TODO: the PredictRouteTileHandler cannot function without a TMFD 
        m_MapDetailedConfigInterface.init(m_PredictRouteTileHandler);
        m_RenderManager.init(factory, m_Camera, m_MapDrawer, m_TileMapControlThread, 
                m_TileMapHolder, m_MapKeyInterface, m_UpdaterThread, 
                zoomHandler, m_PredictRouteTileHandler, initialConfig);
        
        m_UpdaterThread.init(drawerInterface, m_RenderManager, m_MapKeyInterface, m_concurrencyLayer);
        
//        m_PersistenceModule.pendingReadPersistenceRequest(this, PersistenceModule.SETTING_MAP_DATA);
        
        //TODO: Register to settings changes in order to change language at runtime
        //something like this m_settingsIfc.registerSettingsListener(this);
        // 
        m_hasBeenInitialized = true;        
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#startMapComponent(java.lang.String)
     */
    public synchronized void startMapComponent(String string) {
        
        if(string == null || string.equals("")) {
            throw new IllegalArgumentException("Must provide a valid client type!");
        }
        if(!m_hasBeenInitialized) {
            throw new IllegalArgumentException("The map must been initialized before it can be started!");
        }
         
        if(!m_IsLoadingPreInstalledMaps) {
            // If we don't waiting for pre-installed maps to be loaded we start the map. 
            sendMapStartupRequest(string);
        } else {
            // We are loading pre-installed maps. The startup request will be send from
            // the preInstalledMaps() method when the maps has been loaded. 
            m_IsWaitingForPreInstalledMaps = true;
            m_StartupString = string;
        }
    }
    
    private void sendMapStartupRequest(String string) {
        String clientType = string.replace('-', '\\');
        m_TileMapControlThread.setTileMapFormatDescIDString(clientType, clientType);
        m_TileMapControlThread.loadTileMapFormatDescCRC();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setMapDownloadListener(com.wayfinder.core.map.MapDownloadListener)
     */
    public void setMapDownloadListener(MapDownloadListener downloadListener) {
        m_TileMapControlThread.setTileMapDownloadListener(downloadListener);
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setStartupListener(com.wayfinder.core.map.MapStartupListener)
     */
    public synchronized void setStartupListener(MapStartupListener startupListener) {
        m_MapStartupListener = startupListener;
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#isMapStarted()
     */
    public synchronized boolean isMapStarted() {
        return m_isMapStarted;
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#requestMapUpdate()
     */
    public synchronized void requestMapUpdate() {
        if(m_hasBeenInitialized)
            m_UpdaterThread.updateMap();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#closeMapComponent()
     */
    public void closeMapComponent() {
        purgeData();
        m_UpdaterThread.close();
        m_TileMapControlThread.closeMapComponent();
//        m_PersistenceModule.pendingWritePersistenceRequest(this, PersistenceModule.SETTING_MAP_DATA);
    }

    /**
     * @see VectorMapInterface#getActivePosition()
     */
    public Position getActivePosition() {        
        final int lat = m_RenderManager.getActiveWorldLat();
        final int lon = m_RenderManager.getActiveWorldLon();
        return new Position(lat,lon);
    }

    /* (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#getActivePositionName()
     */
    public String getActivePositionName() {
        return m_RenderManager.getActiveObjectName();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#getMapDetailedConfigInterface()
     */
    public MapDetailedConfigInterface getMapDetailedConfigInterface() {
        return m_MapDetailedConfigInterface;
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#getMapKeyInterface()
     */
    public MapKeyInterface getMapKeyInterface() {
        return m_MapKeyInterface;
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#purgeData()
     */
    public void purgeData() {
        m_TileMapControlThread.purgeData();
        m_TileMapHolder.purgeData();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#addMapObject(com.wayfinder.core.map.MapObject)
     */
    public boolean addMapObject(MapObject mapObject, MapObjectImage mapObjectImage) {
        if(mapObject == null)
            throw new IllegalArgumentException("mapObject can't be null!");
        if(mapObjectImage == null)
            throw new IllegalArgumentException("mapObjectImage can't be null!");
        
        m_RenderManager.requestAddMapObject(mapObject, mapObjectImage);
        requestMapUpdate();        
        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#removeAllMapObjects()
     */
    public void removeAllMapObjects() {
        m_RenderManager.requestRemoveAllMapObjects();
        requestMapUpdate();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#removeMapObject(com.wayfinder.core.map.MapObject)
     */
    public void removeMapObject(MapObject mapObject) {
        m_RenderManager.requestRemoveMapObject(mapObject);
        requestMapUpdate();
    }

    public void setSelectedMapObject(MapObject selectedMapObject) {
       m_RenderManager.setSelectedMapObject(selectedMapObject);
       requestMapUpdate();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#set3DMode(boolean)
     */
    public void set3DMode(boolean use3DMode) {
        MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
        task.requestSet3DMode(use3DMode);                
        requestMapUpdate();                
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setCenter(int, int)
     */
    public void setCenter(int lat, int lon) {
        MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
        task.requestSetCenter(lat, lon);
        requestMapUpdate();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setDrawArea(int, int, int, int)
     */
    public void setDrawArea(int x, int y, int width, int height) {
        MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
        task.requestSetDrawArea(x, y, width, height);
        requestMapUpdate();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setFollowGpsPosition(boolean)
     */
    public void setFollowGpsPosition(boolean shouldTrack) {
        MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
        task.requestFollowGpsPosition(shouldTrack);
        requestMapUpdate();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#isFollowGpsPosition()
     */
    public boolean isFollowGpsPosition() {       
        return m_RenderManager.isFollowGpsPosition();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setGpsPosition(int, int, float)
     */
    public void setGpsPosition(int gpsLat, int gpsLon, float angle) {
        
        angle = (float)Math.toRadians(angle);
        angle += (float)(Math.PI/2);
        if(angle<0){
            angle+= (float)(2*Math.PI);
        }
        MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
        task.requestSetGpsPositions(gpsLat, gpsLon, angle);        
        requestMapUpdate();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setMapDrawerInterface(com.wayfinder.core.map.vectormap.MapDrawerInterface)
     */
    public void setMapDrawerInterface(MapDrawerInterface drawerInterface) {
        m_UpdaterThread.setMapDrawerInterface(drawerInterface);
        
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setMapErrorListener(com.wayfinder.core.map.MapErrorListener)
     */
    public void setMapErrorListener(MapErrorListener errorListener) {
        throw new IllegalArgumentException("Not implemented yet!");
        // 
        
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setMapObjectListener(com.wayfinder.core.map.MapObjectListener)
     */
    public void setMapObjectListener(MapObjectListener mapObjectListener) {
        m_RenderManager.setMapObjectListener(mapObjectListener);        
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setNightMode(boolean)
     */
    public void setNightMode(boolean useNightColors) {
        m_TileMapControlThread.setNightMode(useNightColors);
        requestMapUpdate();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#isNightMode()
     */
    public boolean isNightMode() {
        return m_TileMapControlThread.isInNightMode();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setRotation(float)
     */
    public void setRotation(float angle) {
        
        angle = (float)Math.toRadians(angle);
        angle += (float)(Math.PI/2);
        if(angle<0){
            angle+= (float)(2*Math.PI);
        }
        MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
        task.requestSetRotation(angle);
        requestMapUpdate();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#getRotation()
     */
    public float getRotation() {
        float angle = (float)Math.toDegrees(m_Camera.getRotation());
        if(angle < 0) {
            angle += 360;
        }
        
        return angle;
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setScale(float)
     */
    public void setScale(float scale) {
        MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
        task.requestSetScale(scale);
        requestMapUpdate();
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#getScale()
     */
    public float getScale() {
        return m_Camera.getZoomLevel();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setTrackingPoint(int, int)
     */
    public boolean setActiveScreenPoint(int screenX, int screenY) {
        if(m_RenderManager.insideScreen(screenX, screenY)) {
            MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
            task.requestSetActiveScreenPoint(screenX, screenY);
            requestMapUpdate();
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setVisible(boolean)
     */
    public synchronized void setVisible(boolean isVisible) {
        
        if(!m_hasBeenInitialized)
            return;
        
        m_UpdaterThread.setVisible(isVisible);
        m_TileMapControlThread.setVisible(isVisible);
        
        if(m_PredictRouteTileHandler != null)
            m_PredictRouteTileHandler.setVisible(isVisible);
            
        m_MapKeyInterface.resetKeyEvents();
        
        if(isVisible) {
            // Force a update of the map when the map become visible.             
            m_Camera.mapChangeNotify();
            requestMapUpdate();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#getSearchRadiusMeters()
     */
    public long getSearchRadiusMeters() {
        // camera bounding box: {minX, maxX, minY, maxY}
        int[] camBB = m_Camera.getCameraBoundingBox();
        int heightMC2 = Math.abs(camBB[3] - camBB[2]);
        int widthMC2 = Math.abs(camBB[1] - camBB[0]);
        //on most devices would usually be the height:
        long radiusMC2 = (heightMC2 > widthMC2) ? heightMC2 / 2 : widthMC2 / 2;        
        return (long)(radiusMC2 * Utils.MC2SCALE_TO_METER);
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#hasEnoughMapPaintContent()
     */
    public boolean hasEnoughMapPaintContent() {
        throw new IllegalArgumentException("Not implemented yet!");
        // 
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#isIn3DMode()
     */
    public boolean isIn3DMode() {
        return m_Camera.isIn3DMode();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setLanguageAsISO639_3(java.lang.String)
     */
    public void setLanguageAsISO639_3(String language) {
        int lang = LangTypes.getLangType(language);
        m_TileMapControlThread.setLanguage(lang);
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.map.vectormap.VectorMapInterface#setWorldBox(int, int, int, int)
     */
    public void setWorldBox(int cornerLat1, int cornerLon1, int cornerLat2, int cornerLon2) {
        
        BoundingBox bb = new BoundingBox(cornerLat1, cornerLat2, cornerLon1, cornerLon2);
        ScreenInfo screenInfo =
            new ScreenInfo(m_RenderManager.getScreenWidth(), m_RenderManager.getScreenHeight(), true);
        
        MapRenderTask task = new MapRenderTask(m_RenderManager, m_Camera);
        task.requestSetWorldBox(bb, screenInfo);
        requestMapUpdate();        
    }
    
    // --------------------------------------------------------------------------------------------
    public synchronized void updateMapStartedStatus() {
        if(!m_isMapStarted) {
            m_isMapStarted = true;
            if(m_MapStartupListener != null) {
                m_MapStartupListener.mapStartupComplete(MapStartupListener.STARTUP_SUCCESS);
            }
        }        
        requestMapUpdate();        
    }
    
    /**
     * Called when the pre-installed maps has been fully loaded. If no pre-installed maps
     * are added this method will never be called. 
     */
    synchronized void preInstalledMapsLoaded() {        
        m_IsLoadingPreInstalledMaps = false;
        
        // If we have tried to start the map component before the pre-installed 
        // maps are loaded we send the startup request when the maps are loaded. 
        if(m_IsWaitingForPreInstalledMaps) {
            sendMapStartupRequest(m_StartupString);            
        }
        
    }
    
    // -------------------------------------------------------------------------------------
    // PersistenceRequest methods

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.persistent.PersistentRequest#error(com.wayfinder.core.shared.error.CoreError)
     */
    public void error(CoreError coreError) {
        
        if(LOG.isError()) {
            LOG.error("VectorMapModule.error()", "ERROR= "+coreError.getInternalMsg());
        }
        
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.persistent.PersistentRequest#readPersistentData(com.wayfinder.pal.persistence.SettingsConnection)
     */
    public void readPersistenceData(SettingsConnection sConnection)
            throws IOException {
        
        DataInputStream in = sConnection.getDataInputStream(1);       
        
        int zoom = in.readInt();
        int lat = in.readInt();
        int lon = in.readInt();
        in.close();
        
        setCenter(lat, lon);
        setScale(zoom);
        
        if(LOG.isInfo()) {
            LOG.info("VectorMapModule.readPersistenceData()", 
            		" lat= "+lat+
                    " lon= "+lon+
                    " zoom= "+zoom);
        }
        
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.persistent.PersistentRequest#writePersistentData(com.wayfinder.pal.persistence.SettingsConnection)
     */
    public void writePersistenceData(SettingsConnection sConnection)
            throws IOException {
                
        DataOutputStream dout = sConnection.getOutputStream(1);
        
        int zoom = (int)m_Camera.getZoomLevel();
        int lat = (int)m_Camera.getLatitude();
        int lon = (int)m_Camera.getLongitude();
        
        dout.writeInt(zoom);
        dout.writeInt(lat);
        dout.writeInt(lon);                
        dout.close();       
        
        if(LOG.isInfo()) {
            LOG.info("VectorMapModule.writePersistenceData()",
                    " lat= "+lat+
                    " lon= "+lon+
                    " zoom= "+zoom);
        }        
    }
    
    // Only used for debugging the map
    public void printDebug() {
        m_TileMapHolder.printDebug((int)m_Camera.getZoomLevel());
    }

//    public void settingsUpdated(GeneralSettingsInternal settings) {
//        if(LOG.isError()) {
//            LOG.error("VectorMapModule.settingsUpdated()", settings.getInternalLanguage().toString());
//        }
//        int langType = settings.getInternalLanguage().getLangType();
//        m_TileMapControlThread.setLanguage(langType);
//    }

}
