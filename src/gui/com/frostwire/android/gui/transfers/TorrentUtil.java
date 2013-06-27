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

package com.frostwire.android.gui.transfers;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.AsyncDispatcher;
//import com.aelitis.azureus.core.AzureusCoreFactory;
//import com.aelitis.azureus.ui.UIFunctions;
//import com.aelitis.azureus.ui.UIFunctionsManager;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class TorrentUtil {

    private static AsyncDispatcher async = new AsyncDispatcher(2000);

    public static boolean isComplete(DownloadManager dm) {
        /*
        if (!TorrentUtil.getSkippedFiles(dm).isEmpty()) {
            long downloaded = 0;
            long size = 0;
            for (DiskManagerFileInfo fileInfo : getNoSkippedFileInfoSet(dm)) {
                downloaded += fileInfo.getDownloaded();
                size += fileInfo.getLength();
            }
            return downloaded == size;
        } else {
            return dm.getStats().getDownloadCompleted(true) == 1000;
        }*/
        return dm.getAssumedComplete();
    }

    public static void stop(DownloadManager dm) {
        stop(dm, DownloadManager.STATE_STOPPED);
    }

    public static void stop(final DownloadManager dm, final int stateAfterStopped) {
        if (dm == null) {
            return;
        }

        int state = dm.getState();

        if (state == DownloadManager.STATE_STOPPED || state == DownloadManager.STATE_STOPPING || state == stateAfterStopped) {
            return;
        }

        asyncStop(dm, stateAfterStopped);
    }

    public static void asyncStop(final DownloadManager dm, final int stateAfterStopped) {
        async.dispatch(new AERunnable() {
            public void runSupport() {
                dm.stopIt(stateAfterStopped, false, false);
            }
        });
    }
}
