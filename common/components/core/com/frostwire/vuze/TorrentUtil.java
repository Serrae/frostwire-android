/*
 * Created on 9 Jul 2007
 * Created by Allan Crooks
 * Copyright (C) 2007 Aelitis, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * AELITIS, SAS au capital de 46,603.30 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 */

package com.frostwire.vuze;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.plugins.download.Download;

class TorrentUtil {

    public static void removeDownloads(DownloadManager[] dms, AERunnable deleteFailed) {
        removeDownloads(dms, deleteFailed, false);
    }

    public static void removeDownloads(final DownloadManager[] dms, final AERunnable deleteFailed, final boolean deleteData) {
        if (dms == null) {
            return;
        }

        // confusing code:
        // for loop goes through erasing published and low noise torrents until
        // it reaches a normal one.  We then prompt the user, and stop the loop.
        // When the user finally chooses an option, we act on it.  If the user
        // chose to act on all, we do immediately all and quit.  
        // If the user chose an action just for the one torrent, we do that action, 
        // remove that item from the array (by nulling it), and then call 
        // removeDownloads again so we can prompt again (or erase more published/low noise torrents)
        for (int i = 0; i < dms.length; i++) {
            DownloadManager dm = dms[i];
            if (dm == null) {
                continue;
            }

            boolean deleteTorrent = COConfigurationManager.getBooleanParameter("def.deletetorrent");

            removeDownloadsPrompterClosed(dms, i, deleteFailed, deleteData ? 1 : 2, true, deleteTorrent);
        }
    }

    private static void removeDownloadsPrompterClosed(DownloadManager[] dms, int index, AERunnable deleteFailed, int result, boolean doAll, boolean deleteTorrent) {
        if (result == -1) {
            // user pressed ESC (as opposed to clicked Cancel), cancel whole
            // list
            return;
        }
        if (doAll) {
            if (result == 1 || result == 2) {

                for (int i = index; i < dms.length; i++) {
                    DownloadManager dm = dms[i];
                    boolean deleteData = result == 2 ? false : !dm.getDownloadState().getFlag(Download.FLAG_DO_NOT_DELETE_DATA_ON_REMOVE);
                    ManagerUtils.asyncStopDelete(dm, DownloadManager.STATE_STOPPED, deleteTorrent, deleteData, deleteFailed);
                }
            } //else cancel
        } else { // not remembered
            if (result == 1 || result == 2) {
                DownloadManager dm = dms[index];
                boolean deleteData = result == 2 ? false : !dm.getDownloadState().getFlag(Download.FLAG_DO_NOT_DELETE_DATA_ON_REMOVE);

                ManagerUtils.asyncStopDelete(dm, DownloadManager.STATE_STOPPED, deleteTorrent, deleteData, null);
            }
            // remove the one we just did and go through loop again
            dms[index] = null;
            if (index != dms.length - 1) {
                removeDownloads(dms, deleteFailed, true);
            }
        }
    }
}
