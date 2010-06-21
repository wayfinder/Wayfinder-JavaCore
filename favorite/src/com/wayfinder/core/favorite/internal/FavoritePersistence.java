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
package com.wayfinder.core.favorite.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.wayfinder.core.shared.internal.debug.LogFactory;
import com.wayfinder.core.shared.internal.debug.Logger;
import com.wayfinder.core.shared.internal.threadpool.Work;
import com.wayfinder.core.shared.internal.threadpool.WorkScheduler;
import com.wayfinder.core.shared.util.LinkedList;
import com.wayfinder.pal.error.PermissionsException;
import com.wayfinder.pal.persistence.PersistenceLayer;
import com.wayfinder.pal.persistence.WFFileConnection;

/**
 * Favorite and changes are kept in 2 different files,
 * Changes file will remain open, if that get broken the favorite list will 
 * remain intact and only the latest changes will be lost    
 * 
 */
public class FavoritePersistence implements Work {
    
    private static final Logger LOG =
        LogFactory.getLoggerForClass(FavoritePersistence.class);
    
    public static final String FILE_NAME_LIST = "favorites.bin";
    public static final String FILE_NAME_CHANGES = "favchanges.bin";
    
    /**
     * when this number of changes is reached a clean and a full write will 
     * be done 
     */
    public final int NBR_OF_CHANGES_LIMIT = 20;
    
    static final int OPERATION_SET_CRC = 1;
    static final int OPERATION_ADD_FAV = 2;
    static final int OPERATION_SET_FAV_STATUS = 3;
    
    private WorkScheduler workScheduler;
    
    private PersistenceLayer persistenceLayer;
    
    //TODO: unnecessary usage of synchronized LinkedList
    private LinkedList taskList = new LinkedList();
    
    private Runnable currentTask = null;

    private int nextIndex;
    
    private WFFileConnection changesFile;
    
    private WriteExceptionHandler exHandler;
    
    public FavoritePersistence(WorkScheduler ws, PersistenceLayer ps, 
            WriteExceptionHandler handler) {
        workScheduler = ws;
        persistenceLayer = ps;
        exHandler = handler;
    }
    
//--- run persistence operation serially using WorkScheduler and rescheduled 
// worker when possible    
    
    synchronized void addTask(Runnable task) {
         if (currentTask == null) {
             currentTask = task;
             workScheduler.schedule(this);
         } else { 
             taskList.add(task); 
         }
    }
    
    private synchronized Runnable getCurrentTask() {
        return currentTask;
    }
    
    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.threadpool.Work#run()
     */
    public void run() {
        //certain to return no null value at this moment 
        getCurrentTask().run();
    }

    /*
     * (non-Javadoc)
     * @see com.wayfinder.core.shared.internal.threadpool.Work#shouldBeRescheduled()
     */
    synchronized public boolean shouldBeRescheduled() {
        //currentTask has ended extract the next one if any.
        currentTask = (Runnable) taskList.removeFirst();
        return (currentTask!=null);
    }

    public int getPriority() {
        return WorkScheduler.PRIORITY_LOW;
    }
    
    private WFFileConnection openChangesFile() throws IOException, PermissionsException {
        if (changesFile == null) { 
            String path =  persistenceLayer.getBaseFileDirectory()+ "/"+ FILE_NAME_CHANGES;
            changesFile = persistenceLayer.openFile(path);
        }
        return changesFile;
    }
    
    private WFFileConnection openListFile() throws IOException, PermissionsException {
        String path =  persistenceLayer.getBaseFileDirectory()+ "/"+ FILE_NAME_LIST;
        return persistenceLayer.openFile(path);
    } 
    
    private void saveFavList(FavoriteList favList) throws IOException, PermissionsException {
        DataOutputStream out = null;
        WFFileConnection listFile = null;
        try {
            //TODO check the order to be safer;
            listFile = openListFile(); 
            out = listFile.openDataOutputStream(false);//this will overwrite the old changesFile
            favList.removePhantoms();
            favList.write(out);
            out.flush();
            openChangesFile().delete();
        } finally {
            try {
                if (out != null) out.close();
                if (listFile != null) listFile.close();
            } catch (IOException e) {
                if (LOG.isError()) {
                    LOG.error("FavoritePersistence.closeOut", e);
                }
            }
        }
    }
    
    /**
     * @return true if the favorites need to be saved 
     * @throws IOException
     * @throws PermissionsException
     */
    private boolean restoreFavList(FavoriteList favList) throws IOException, PermissionsException {
        
        //first restore the list any error here is fatal
        DataInputStream listIn = null;
        WFFileConnection listFile = null;
        try {
            listFile = openListFile(); 
            if (listFile.exists()) {
                listIn = listFile.openDataInputStream();
                favList.read(listIn);
            }
        } finally {
            try {
                if (listIn != null) listIn.close();
                if (listFile != null) listFile.close();
            } catch (IOException e) {
                if (LOG.isError()) {
                    LOG.error("FavoritePersistence.restoreFavList()", e);
                }
            }
        }
        
        //restore the changes errors here are allowed 
        DataInputStream changesIn = null;
        try { 
            WFFileConnection changesFile = openChangesFile();
            if (changesFile.exists()) {
                changesIn = openChangesFile().openDataInputStream();
                return (favList.readChanges(changesIn) > NBR_OF_CHANGES_LIMIT);
            } else {
                return false;
            }
        } catch (IOException e) {
            if (LOG.isError()) {
                LOG.error("FavoritePersistence.restoreFavList()", e);
            }
            //if errors try to keep what we have 
            //but order a saving and reset of CRC so we force a server synch
            favList.resetCrc();
            return true;
        } finally {
            try {
                if (changesIn != null) changesIn.close();
            } catch (IOException e) {
                if (LOG.isError()) {
                    LOG.error("FavoritePersistence.restoreFavList()", e);
                }
            }
        }
    }    
    
//--- favorite persistence asynch operation
    
    public void postAddFav(final FavoriteInternal fav) {
        this.addTask(new Runnable() {
            public void run() {
                DataOutputStream out = null;
                try {
                    //the changes will be appended
                    out = openChangesFile().openDataOutputStream(true);
                    out.writeInt(OPERATION_ADD_FAV);
                    fav.setPersistanceIndex(nextIndex++);
                    //TODO status could be changed by now
                    fav.write(out);
                    out.flush();
                } catch (Exception e) {
                    if(LOG.isError()) {
                        LOG.error("FavoritePersistence.postAddFav()", e);
                    }
                    exHandler.handleWriteChangeError(fav, e);
                } finally {
                    try {
                        if (out != null) out.close();
                    } catch (IOException e) {
                        if(LOG.isError()) {
                            LOG.error("FavoritePersistence.postAddFav()", e);
                        }
                    }
                }
            }                    
        });
    }
    
    public void postDelFav(final FavoriteInternal fav) {
        this.addTask(new Runnable() {
            public void run() {
                DataOutputStream out = null;
                try {
                    //the changes will be appended
                    out = openChangesFile().openDataOutputStream(true);
                    out.writeInt(OPERATION_SET_FAV_STATUS);
                    out.writeInt(fav.getPersistanceIndex());
                    out.writeInt(fav.getStatus());
                    out.flush();
                } catch (Exception e) {
                    if(LOG.isError()) {
                        LOG.error("FavoritePersistence.postDelFav()", e);
                    }
                    exHandler.handleWriteChangeError(fav, e);
                } finally {
                    try {
                        if (out != null) out.close();
                    } catch (IOException e) {
                        if(LOG.isError()) {
                            LOG.error("FavoritePersistence.closeOut", e);
                        }
                    }
                }
            }        
        });
    }

    public void postReplaceFav(final FavoriteInternal oldFav, 
            final  FavoriteInternal newFav) {
        this.addTask(new Runnable() {
            public void run() {
                DataOutputStream out = null;
                try {
                    //the changes will be appended
                    out = openChangesFile().openDataOutputStream(true);
                    
                    //delete the old one
                    out.writeInt(OPERATION_SET_FAV_STATUS);
                    out.writeInt(oldFav.getPersistanceIndex());
                    out.writeInt(oldFav.getStatus());
                    
                    //add the new one
                    out.writeInt(OPERATION_ADD_FAV);
                    newFav.setPersistanceIndex(nextIndex++);
                    newFav.write(out);
                    out.flush();
                } catch (Exception e) {
                    if(LOG.isError()) {
                        LOG.error("FavoritePersistence.postReplaceFav()", e);
                    }
                    exHandler.handleWriteChangeError(oldFav, e);
                } finally {
                    try {
                        if (out != null) out.close();
                    } catch (IOException e) {
                        if(LOG.isError()) {
                            LOG.error("FavoritePersistence.closeOut", e);
                        }
                    }
                }
            }        
        });
    }
    

    public void postSynchChanges(final FavoriteSynchData data) {
        this.addTask(new Runnable() {
            public void run() {
                DataOutputStream out = null;
                try {
                    //the changes will be appended
                    out = openChangesFile().openDataOutputStream(true);
                    //FIXME finish this
                    for(int i=0; i < data.clientDeletedFavs.length ; i++) {
                        FavoriteInternal fav = data.clientDeletedFavs[i];
                        out.writeInt(OPERATION_SET_FAV_STATUS);
                        out.writeInt(fav.getPersistanceIndex());
                        out.writeInt(FavoriteInternal.STATUS_PHANTOM);
                    }
                    
                    for(int i=0; i < data.clientNewFavs.length ; i++) {
                        FavoriteInternal fav = data.clientNewFavs[i];
                        out.writeInt(OPERATION_SET_FAV_STATUS);
                        out.writeInt(fav.getPersistanceIndex());
                        out.writeInt(FavoriteInternal.STATUS_PHANTOM);
                    }

                    for(int i=0; i < data.serverDeletedFavs.length ; i++) {
                        FavoriteInternal fav = data.serverDeletedFavs[i];
                        out.writeInt(OPERATION_SET_FAV_STATUS);
                        out.writeInt(fav.getPersistanceIndex());
                        out.writeInt(FavoriteInternal.STATUS_PHANTOM);
                    }

                    for(int i=0; i < data.serverNewFavs.length ; i++) {
                        FavoriteInternal fav = data.serverNewFavs[i];
                        out.writeInt(OPERATION_ADD_FAV);
                        fav.setPersistanceIndex(nextIndex++);
                        //TODO status could be changed by now
                        fav.write(out);
                    }
                    
                    //at end save the crc
                    out.writeInt(OPERATION_SET_CRC);
                    out.writeUTF(data.serverCrc);
                    
                    out.flush();
                } catch (Exception e) {
                    if(LOG.isError()) {
                        LOG.error("FavoritePersistence.postSynchChanges()", e);
                    }
                    exHandler.handleSavingSynchError(e);
                } finally {
                    try {
                        if (out != null) out.close();
                    } catch (IOException e) {
                        if(LOG.isError()) {
                            LOG.error("FavoritePersistence.closeOut", e);
                        }
                    }
                }
            }        
        });
        
    }
   
    /**
     * Read clean up and save if necessary 
     * @param loadHandler
     */
    public void postInitFav(final FavoriteLoadHandler loadHandler) {
        this.addTask(new Runnable() {
            public void run() {
                try {
                    FavoriteList favList = new FavoriteList();
                    if (restoreFavList(favList)) {
                        saveFavList(favList);
                    }
                    loadHandler.handleList(favList.getInternalArray(), favList.getServerCrc());
                    nextIndex = favList.size();
                } catch (IOException e) {
                    //old version error when reading
                    if(LOG.isError()) {
                        LOG.error("FavoritePersistence.postInitFav().run()", e);
                    }
                    loadHandler.handleException(e); 
                    deleteAll();
                } catch (PermissionsException e) {
                    if(LOG.isError()) {
                        LOG.error("FavoritePersistence.postInitFav().run()", e);
                    }
                    loadHandler.handleException(e); 
                    //cannot do much;
                } catch (RuntimeException e) {
                    //this is can be a programming error 
                    //or undetected corrupted data 
                    if(LOG.isError()) {
                        LOG.error("FavoritePersistence.postInitFav().run()", e);
                    }
                    loadHandler.handleException(e); 
                    deleteAll();
                }
            }
        });
    }

    private void deleteAll() {
        try {
            openChangesFile().delete();
        } catch (IOException e) {
            if(LOG.isError()) {
                LOG.error("FavoritePersistence.deleteAll()", e);
            }
        } catch (PermissionsException e) {
            if(LOG.isError()) {
                LOG.error("FavoritePersistence.deleteAll()", e);
            }
        }
        try {
            openListFile().delete();
        } catch (IOException e) {
            if(LOG.isError()) {
                LOG.error("FavoritePersistence.deleteAll()", e);
            }
        } catch (PermissionsException e) {
            if(LOG.isError()) {
                LOG.error("FavoritePersistence.deleteAll()", e);
            }
        }
    }

}
