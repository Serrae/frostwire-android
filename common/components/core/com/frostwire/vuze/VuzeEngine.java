/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, 2013, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.vuze;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.global.GlobalManagerDownloadRemovalVetoException;
import org.gudy.azureus2.core3.util.HashWrapper;
import org.gudy.azureus2.core3.util.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreRunningListener;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public abstract class VuzeEngine {

    private static final Logger LOG = LoggerFactory.getLogger(VuzeEngine.class);

    private final AzureusCore core;

    public VuzeEngine() {
        initConfiguration();

        this.core = AzureusCoreFactory.create();
        this.core.start();
    }

    public GlobalManager getGlobalManager() {
        return core.getGlobalManager();
    }

    public void pause() {
        core.getGlobalManager().pauseDownloads();
    }

    public void resume() {
        core.getGlobalManager().resumeDownloads();
    }

    public void remove(VuzeDownloadManager dm, boolean removeTorrent, boolean removeData) {
        remove(dm.getDM(), removeTorrent, removeData);
    }

    public void remove(byte[] hash, boolean removeTorrent, boolean removeData) {
        DownloadManager dm = core.getGlobalManager().getDownloadManager(new HashWrapper(hash));
        if (dm != null) {
            remove(dm, removeTorrent, removeData);
        }
    }

    public void execute(final Runnable command) {
        AzureusCoreFactory.addCoreRunningListener(new AzureusCoreRunningListener() {
            @Override
            public void azureusCoreRunning(AzureusCore core) {
                command.run();
            }
        });
    }

    public <V> V execute(final Callable<V> task) {
        final ArrayList<V> holder = new ArrayList<V>(1);

        // default return to null
        holder.add(0, null);

        AzureusCoreFactory.addCoreRunningListener(new AzureusCoreRunningListener() {
            @Override
            public void azureusCoreRunning(AzureusCore core) {
                try {
                    holder.set(0, task.call());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return holder.get(0);
    }

    protected abstract File getVuzePath();

    protected abstract File getTorrentsPath();

    protected void initConfiguration() {
        File azureusPath = getVuzePath();

        System.setProperty("azureus.config.path", azureusPath.getAbsolutePath());
        System.setProperty("azureus.install.path", azureusPath.getAbsolutePath());
        System.setProperty("azureus.loadplugins", "0"); // disable third party azureus plugins

        SystemProperties.APPLICATION_NAME = "azureus";
        SystemProperties.setUserPath(azureusPath.getAbsolutePath());

        COConfigurationManager.setParameter(VuzeKeys.AUTO_ADJUST_TRANSFER_DEFAULTS, false);
        COConfigurationManager.setParameter(VuzeKeys.GENERAL_DEFAULT_TORRENT_DIRECTORY, getTorrentsPath().getAbsolutePath());
    }

    private void remove(DownloadManager dm, boolean removeTorrent, boolean removeData) {
        try {
            core.getGlobalManager().removeDownloadManager(dm, removeTorrent, removeData);
        } catch (GlobalManagerDownloadRemovalVetoException e) {
            LOG.warn("Error removing download manager", e);
        }
    }
}
