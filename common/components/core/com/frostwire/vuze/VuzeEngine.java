/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, 2013, FrostWire(TM). All rights reserved.
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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.util.SystemProperties;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreLifecycleAdapter;
import com.frostwire.android.gui.util.SystemUtils;
import com.frostwire.concurrent.AsyncExecutor;
import com.frostwire.concurrent.AsyncExecutors;
import com.frostwire.concurrent.AsyncFuture;
import com.frostwire.concurrent.Futures;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeEngine {

    private static final AsyncExecutor executor = AsyncExecutors.newSingleThreadExecutor();

    private final AzureusCore core;
    private final CountDownLatch coreStarted;

    public VuzeEngine() {
        initConfiguration();

        this.core = AzureusCoreFactory.create();
        this.core.addLifecycleListener(new CoreLifecycleAdapter());

        this.coreStarted = new CountDownLatch(1);
    }

    public AzureusCore getCore() {
        return core;
    }

    public void start() {
        core.start();
    }

    public void pause() {

    }

    public void resume() {

    }

    public AsyncFuture<List<DownloadManager>> getDownloadManagers() {
        if (core.isStarted()) {
            return Futures.successful(getDownloadManagersSupport());
        }

        return executor.submit(new Callable<List<DownloadManager>>() {
            @Override
            public List<DownloadManager> call() throws Exception {
                coreStarted.await();
                return getDownloadManagersSupport();
            }
        });
    }

    private void initConfiguration() {
        File azureusPath = SystemUtils.getAzureusDirectory();

        System.setProperty("azureus.config.path", azureusPath.getAbsolutePath());
        System.setProperty("azureus.install.path", azureusPath.getAbsolutePath());
        System.setProperty("azureus.loadplugins", "0"); // disable third party azureus plugins

        SystemProperties.APPLICATION_NAME = "azureus";
        SystemProperties.setUserPath(azureusPath.getAbsolutePath());

        COConfigurationManager.setParameter(VuzeKeys.AUTO_ADJUST_TRANSFER_DEFAULTS, false);
        COConfigurationManager.setParameter(VuzeKeys.RESUME_DOWNLOADS_ON_START, true);
        COConfigurationManager.setParameter(VuzeKeys.GENERAL_DEFAULT_TORRENT_DIRECTORY, SystemUtils.getTorrentsDirectory().getAbsolutePath());
        
        // network parameters, fine tunning for android
        /*
        COConfigurationManager.setParameter("network.tcp.write.select.time", 1000);
        COConfigurationManager.setParameter("network.tcp.write.select.min.time", 1000);
        COConfigurationManager.setParameter("network.tcp.read.select.time", 1000);
        COConfigurationManager.setParameter("network.tcp.read.select.min.time", 1000);
        COConfigurationManager.setParameter("network.control.write.idle.time", 1000);
        COConfigurationManager.setParameter("network.control.read.idle.time", 1000);
        */
    }

    private List<DownloadManager> getDownloadManagersSupport() {
        GlobalManager gm = core.getGlobalManager();
        return new ArrayList<DownloadManager>(gm.getDownloadManagers());
    }

    private class CoreLifecycleAdapter extends AzureusCoreLifecycleAdapter {
        @Override
        public void started(AzureusCore core) {
            coreStarted.countDown();
        }
    }
}
