/**
 * Created on May 12, 2010
 *
 * Copyright 2008 Vuze, Inc.  All rights reserved.
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License only.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA 
 */
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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfoSet;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.gudy.azureus2.core3.util.FileUtil;

import com.frostwire.util.FileUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeUtils {

    private VuzeUtils() {
    }

    public static Set<File> getIgnorableFiles() {
        Set<File> set = getIncompleteFiles();
        set.addAll(getSkipedFiles());
        return set;
    }

    public static TOTorrent convert(com.frostwire.torrent.TOTorrent t) throws IOException {
        try {
            return TOTorrentFactory.deserialiseFromMap(t.serialiseToMap());
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    static Set<File> getSkippedFiles(DownloadManager dm) {
        Set<File> set = new HashSet<File>();
        DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            if (fileInfo.isSkipped()) {
                set.add(fileInfo.getFile(false));
            }
        }
        return set;
    }

    /**
     * Deletes incomplete and skipped files.
     */
    static void finalCleanup(DownloadManager dm) {
        Set<File> filesToDelete = getSkippedFiles(dm);
        filesToDelete.addAll(getIncompleteFiles(dm));

        for (File f : filesToDelete) {
            f.delete();
        }

        FileUtils.deleteEmptyDirectoryRecursive(dm.getSaveLocation());
    }

    static void setSkipped(DownloadManager dm, DiskManagerFileInfo[] fileInfos) {
        boolean paused = setSkipped(dm, fileInfos, true);

        if (paused) {
            dm.resume();
        }
    }

    private static Set<File> getSkipedFiles() {
        Set<File> set = new HashSet<File>();

        try {
            List<DownloadManager> dms = VuzeManager.getInstance().getDownloadManagers().get(5, TimeUnit.SECONDS); // don't block forever
            for (DownloadManager dm : dms) {
                set.addAll(getSkippedFiles(dm));
            }
        } catch (Throwable e) {
            // ignore
        }

        return set;
    }

    private static Set<File> getIncompleteFiles() {
        Set<File> set = new HashSet<File>();

        try {
            List<DownloadManager> dms = VuzeManager.getInstance().getDownloadManagers().get(5, TimeUnit.SECONDS); // don't block forever
            for (DownloadManager dm : dms) {
                set.addAll(getIncompleteFiles(dm));
            }
        } catch (Throwable e) {
            // ignore
        }

        return set;
    }

    private static Set<File> getIncompleteFiles(DownloadManager dm) {
        Set<File> set = new HashSet<File>();

        DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            if (getDownloadPercent(fileInfo) < 100) {
                set.add(fileInfo.getFile(false));
            }
        }

        return set;
    }

    private static int getDownloadPercent(DiskManagerFileInfo fileInfo) {
        long length = fileInfo.getLength();
        if (length == 0 || fileInfo.getDownloaded() == length) {
            return 100;
        } else {
            return (int) (fileInfo.getDownloaded() * 100 / length);
        }
    }

    // Taken from FilesViewMenuUtil
    /**
     * @author TuxPaper
     * @created May 12, 2010
     *
     */
    // Returns true if it was paused here.
    private static boolean setSkipped(DownloadManager manager, DiskManagerFileInfo[] infos, boolean skipped) {
        // if we're not managing the download then don't do anything other than
        // change the file's priority

        if (!manager.isPersistent()) {
            for (int i = 0; i < infos.length; i++) {
                infos[i].setSkipped(skipped);
            }
            return false;
        }
        int[] existing_storage_types = manager.getStorageType(infos);
        int nbFiles = manager.getDiskManagerFileInfoSet().nbFiles();
        boolean[] setLinear = new boolean[nbFiles];
        boolean[] setCompact = new boolean[nbFiles];
        boolean[] setReorder = new boolean[nbFiles];
        boolean[] setReorderCompact = new boolean[nbFiles];
        int compactCount = 0;
        int linearCount = 0;
        int reorderCount = 0;
        int reorderCompactCount = 0;

        if (infos.length > 1) {

        }
        // This should hopefully reduce the number of "exists" checks.
        File save_location = manager.getAbsoluteSaveLocation();
        boolean root_exists = save_location.isDirectory() || (infos.length <= 1 && save_location.exists());

        boolean type_has_been_changed = false;
        boolean requires_pausing = false;

        for (int i = 0; i < infos.length; i++) {
            int existing_storage_type = existing_storage_types[i];
            int compact_target;
            int non_compact_target;
            if (existing_storage_type == DiskManagerFileInfo.ST_COMPACT || existing_storage_type == DiskManagerFileInfo.ST_LINEAR) {
                compact_target = DiskManagerFileInfo.ST_COMPACT;
                non_compact_target = DiskManagerFileInfo.ST_LINEAR;
            } else {
                compact_target = DiskManagerFileInfo.ST_REORDER_COMPACT;
                non_compact_target = DiskManagerFileInfo.ST_REORDER;
            }
            int new_storage_type;
            if (skipped) {

                // Check to see if the file exists, but try to avoid doing an
                // actual disk check if possible.
                File existing_file = infos[i].getFile(true);

                // Avoid performing existing_file.exists if we know that it is meant
                // to reside in the default save location and that location does not
                // exist.
                boolean perform_check;
                if (root_exists) {
                    perform_check = true;
                } else if (FileUtil.isAncestorOf(save_location, existing_file)) {
                    perform_check = false;
                } else {
                    perform_check = true;
                }

                if (perform_check && existing_file.exists()) {
                    {

                        // compact only currently supports first+last piece and therefore is not
                        // good for handling partial DND files (as other partial pieces will be discarded....)

                        new_storage_type = non_compact_target;
                    }
                }
                // File does not exist.
                else {
                    new_storage_type = compact_target;
                }
            } else {
                new_storage_type = non_compact_target;
            }

            boolean has_changed = existing_storage_type != new_storage_type;

            type_has_been_changed |= has_changed;

            if (has_changed) {

                requires_pausing |= (new_storage_type == DiskManagerFileInfo.ST_COMPACT || new_storage_type == DiskManagerFileInfo.ST_REORDER_COMPACT);

                if (new_storage_type == DiskManagerFileInfo.ST_COMPACT) {
                    setCompact[infos[i].getIndex()] = true;
                    compactCount++;
                } else if (new_storage_type == DiskManagerFileInfo.ST_LINEAR) {
                    setLinear[infos[i].getIndex()] = true;
                    linearCount++;
                } else if (new_storage_type == DiskManagerFileInfo.ST_REORDER) {
                    setReorder[infos[i].getIndex()] = true;
                    reorderCount++;
                } else if (new_storage_type == DiskManagerFileInfo.ST_REORDER_COMPACT) {
                    setReorderCompact[infos[i].getIndex()] = true;
                    reorderCompactCount++;
                }
            }
        }

        boolean ok = true;
        boolean paused = false;
        if (type_has_been_changed) {
            if (requires_pausing)
                paused = manager.pause();
            if (linearCount > 0)
                ok &= Arrays.equals(setLinear, manager.getDiskManagerFileInfoSet().setStorageTypes(setLinear, DiskManagerFileInfo.ST_LINEAR));
            if (compactCount > 0)
                ok &= Arrays.equals(setCompact, manager.getDiskManagerFileInfoSet().setStorageTypes(setCompact, DiskManagerFileInfo.ST_COMPACT));
            if (reorderCount > 0)
                ok &= Arrays.equals(setReorder, manager.getDiskManagerFileInfoSet().setStorageTypes(setReorder, DiskManagerFileInfo.ST_REORDER));
            if (reorderCompactCount > 0)
                ok &= Arrays.equals(setReorderCompact, manager.getDiskManagerFileInfoSet().setStorageTypes(setReorderCompact, DiskManagerFileInfo.ST_REORDER_COMPACT));
        }

        if (ok) {
            for (int i = 0; i < infos.length; i++) {
                infos[i].setSkipped(skipped);
            }
        }

        return paused;
    }
}
