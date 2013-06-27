/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerInitialisationAdapter;
import org.gudy.azureus2.core3.download.impl.DownloadManagerAdapter;
import org.gudy.azureus2.core3.global.GlobalManager;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeDownloadFactory {

    private VuzeDownloadFactory() {
    }

    public static VuzeDownloadManager create(String torrentFile, final Set<String> fileSelection, String saveDir, VuzeDownloadListener listener) {
        GlobalManager gm = VuzeManager.getInstance().getGlobalManager();
        DownloadManager dm = null;

        if (fileSelection == null || fileSelection.isEmpty()) {
            dm = gm.addDownloadManager(torrentFile, null, saveDir, DownloadManager.STATE_WAITING, true, false, null);
        } else {
            dm = gm.addDownloadManager(torrentFile, null, saveDir, null, DownloadManager.STATE_WAITING, true, false, new DownloadManagerInitialisationAdapter() {
                public void initialised(DownloadManager dm) {
                    setupPartialSelection(dm, fileSelection);
                }
            });
        }

        VuzeDownloadManager vdm = new VuzeDownloadManager(dm);

        setup(dm, vdm, listener);

        return vdm;
    }

    private static void setupPartialSelection(DownloadManager dm, Set<String> fileSelection) {
        DiskManagerFileInfo[] fileInfos = dm.getDiskManagerFileInfoSet().getFiles();

        try {
            dm.getDownloadState().suppressStateSave(true);

            for (DiskManagerFileInfo fileInfo : fileInfos) {
                File f = fileInfo.getFile(true);
                if (!fileSelection.contains(f)) {
                    fileInfo.setSkipped(true);
                }
            }

        } finally {
            dm.getDownloadState().suppressStateSave(false);
        }
    }

    private static void setup(DownloadManager dm, final VuzeDownloadManager vdm, final VuzeDownloadListener listener) {
        dm.addListener(new DownloadManagerAdapter() {

            private AtomicBoolean finished = new AtomicBoolean(false);

            @Override
            public void stateChanged(DownloadManager manager, int state) {
                if (state == DownloadManager.STATE_READY) {
                    manager.startDownload();
                }
            }

            @Override
            public void downloadComplete(DownloadManager manager) {
                if (finished.compareAndSet(false, true)) {
                    if (listener != null) {
                        listener.downloadComplete(vdm);
                    }
                }
            }
        });

        if (dm.getState() != DownloadManager.STATE_STOPPED) {
            dm.initialize();
        }
    }
}
