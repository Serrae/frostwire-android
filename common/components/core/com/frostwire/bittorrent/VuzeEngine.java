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

package com.frostwire.bittorrent;

import org.gudy.azureus2.core3.config.COConfigurationManager;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreLifecycleAdapter;

/**
 * @author gubatron
 * @author aldenml
 *
 */
final class VuzeEngine implements BTorrentEngine {

    private final AzureusCore core;

    public VuzeEngine() {
        this.core = AzureusCoreFactory.create();
        this.core.addLifecycleListener(new CoreLifecycleAdapter());

        initConfiguration();
    }

    public void start() {
        core.start();
    }

    public void pause() {

    }

    public void resume() {

    }

    private void initConfiguration() {
        COConfigurationManager.setParameter(VuzeKeys.RESUME_DOWNLOADS_ON_START, true);
    }

    private class CoreLifecycleAdapter extends AzureusCoreLifecycleAdapter {
        @Override
        public void started(AzureusCore core) {
            // do something?
        }
    }
}
