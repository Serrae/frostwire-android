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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfoSet;
import org.gudy.azureus2.core3.download.DownloadManager;

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
}
