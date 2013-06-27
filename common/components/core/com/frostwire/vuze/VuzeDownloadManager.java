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
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfoSet;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.util.DisplayFormatters;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeDownloadManager {

    private final DownloadManager dm;
    private final Set<DiskManagerFileInfo> noSkippedFileInfoSet;
    private final Set<File> skippedFiles;
    private final String displayName;
    private final boolean partial;
    private final long size;
    private final byte[] hash;

    public VuzeDownloadManager(DownloadManager dm) {
        this.dm = dm;
        this.noSkippedFileInfoSet = calculateNoSkippedFileInfoSet(dm);
        this.skippedFiles = calculateSkippedFiles(dm);
        this.displayName = calculateDisplayName();
        this.partial = !skippedFiles.isEmpty();
        this.size = calculateSize();
        this.hash = calculateHash(dm);
    }

    public DownloadManager getDM() {
        return dm;
    }

    public Set<DiskManagerFileInfo> getNoSkippedFileInfoSet() {
        return noSkippedFileInfoSet;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPartial() {
        return partial;
    }

    public long getSize() {
        return size;
    }

    public byte[] getHash() {
        return hash;
    }

    public boolean isComplete() {
        return dm.getAssumedComplete();
    }

    public boolean isPausable() {
        return ManagerUtils.isStopable(dm);
    }

    public boolean isResumable() {
        return ManagerUtils.isStartable(dm);
    }

    public boolean isDownloading() {
        return dm.getState() == DownloadManager.STATE_DOWNLOADING;
    }

    public boolean isSeeding() {
        return dm.getState() == DownloadManager.STATE_SEEDING;
    }

    public String getStatus() {
        return DisplayFormatters.formatDownloadStatus(dm);
    }

    public void pause() {
        if (isPausable()) {
            ManagerUtils.stop(dm);
        }
    }

    public void resume() {
        if (isResumable()) {
            ManagerUtils.start(dm);
        }
    }

    private Set<DiskManagerFileInfo> calculateNoSkippedFileInfoSet(DownloadManager dm) {
        Set<DiskManagerFileInfo> set = new HashSet<DiskManagerFileInfo>();
        DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            if (!fileInfo.isSkipped()) {
                set.add(fileInfo);
            }
        }
        return set;
    }

    private Set<File> calculateSkippedFiles(DownloadManager dm) {
        Set<File> set = new HashSet<File>();
        DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            if (fileInfo.isSkipped()) {
                set.add(fileInfo.getFile(false));
            }
        }
        return set;
    }

    private String calculateDisplayName() {
        String displayName = null;

        if (noSkippedFileInfoSet.size() == 1) {
            displayName = FilenameUtils.getBaseName(noSkippedFileInfoSet.toArray(new DiskManagerFileInfo[0])[0].getFile(false).getName());
        } else {
            displayName = dm.getDisplayName();
        }

        return displayName;
    }

    private long calculateSize() {
        long size = 0;

        if (partial) {
            if (noSkippedFileInfoSet.isEmpty()) {
                size = dm.getSize();
            } else {
                size = 0;
                for (DiskManagerFileInfo fileInfo : noSkippedFileInfoSet) {
                    size += fileInfo.getLength();
                }
            }
        } else {
            size = dm.getSize();
        }

        return size;
    }

    private byte[] calculateHash(DownloadManager dm) {
        try {
            return dm.getTorrent().getHash();
        } catch (TOTorrentException e) {
            throw new RuntimeException(e);
        }
    }
}
