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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.util.Arrays;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfoSet;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.tracker.client.TRTrackerScraperResponse;
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
    private final File savePath;
    private final Date created;

    public VuzeDownloadManager(DownloadManager dm) {
        this.dm = dm;
        this.noSkippedFileInfoSet = calculateNoSkippedFileInfoSet(dm);
        this.skippedFiles = VuzeUtils.getSkippedFiles(dm);
        this.displayName = calculateDisplayName();
        this.partial = !skippedFiles.isEmpty();
        this.size = calculateSize();
        this.hash = calculateHash(dm);
        this.savePath = dm.getSaveLocation();
        this.created = new Date(dm.getCreationTime());
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

    public File getSavePath() {
        return savePath;
    }

    public Date getCreated() {
        return created;
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

    public int getProgress() {
        int progress;

        if (isComplete()) {
            progress = 100;
        }

        if (partial) {
            long downloaded = 0;
            for (DiskManagerFileInfo fileInfo : noSkippedFileInfoSet) {
                downloaded += fileInfo.getDownloaded();
            }
            progress = (int) ((downloaded * 100) / size);
        } else {
            progress = dm.getStats().getDownloadCompleted(true) / 10;
        }

        return progress;
    }

    public long getBytesReceived() {
        return dm.getStats().getTotalGoodDataBytesReceived();
    }

    public long getBytesSent() {
        return dm.getStats().getTotalDataBytesSent();
    }

    public long getDownloadSpeed() {
        return dm.getStats().getDataReceiveRate();
    }

    public long getUploadSpeed() {
        return dm.getStats().getDataSendRate();
    }

    public long getETA() {
        return dm.getStats().getETA();
    }

    public int getShareRatio() {
        return dm.getStats().getShareRatio();
    }

    public int getPeers() {
        int peers;

        TRTrackerScraperResponse response = dm.getTrackerScrapeResponse();

        if (response != null && response.isValid()) {
            int trackerPeerCount = response.getPeers();
            peers = dm.getNbPeers();
            if (peers == 0 || trackerPeerCount > peers) {
                if (trackerPeerCount <= 0) {
                    peers = dm.getActivationCount();
                } else {
                    peers = trackerPeerCount;
                }
            }
        } else {
            peers = dm.getNbPeers();
        }

        return peers;
    }

    public int getSeeds() {
        int seeds;

        TRTrackerScraperResponse response = dm.getTrackerScrapeResponse();

        if (response != null && response.isValid()) {
            seeds = Math.max(dm.getNbSeeds(), response.getSeeds());
        } else {
            seeds = dm.getNbSeeds();
        }

        return seeds;
    }

    public int getConnectedPeers() {
        return dm.getNbPeers();
    }

    public int getConnectedSeeds() {
        return dm.getNbSeeds();
    }

    public boolean hasStarted() {
        int state = dm.getState();
        return state == DownloadManager.STATE_SEEDING || state == DownloadManager.STATE_DOWNLOADING;
    }

    public boolean hasScrape() {
        TRTrackerScraperResponse response = dm.getTrackerScrapeResponse();
        return response != null && response.isValid();
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

    public void removeDownload(boolean deleteTorrent, boolean deleteData) {
        ManagerUtils.asyncStopDelete(dm, DownloadManager.STATE_STOPPED, deleteTorrent, deleteData, null);
    }

    @Override
    public boolean equals(Object o) {
        boolean equals = false;

        if (o instanceof VuzeDownloadManager) {
            VuzeDownloadManager other = (VuzeDownloadManager) o;
            if (dm.equals(other.dm) || Arrays.areEqual(getHash(), other.getHash())) {
                equals = true;
            }
        }

        return equals;
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
