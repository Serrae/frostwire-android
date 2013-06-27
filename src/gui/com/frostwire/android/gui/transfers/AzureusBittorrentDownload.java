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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.torrent.TOTorrentException;

import com.frostwire.vuze.VuzeDownloadManager;
import com.frostwire.vuze.VuzeFormatter;

/**
 * @author gubatron
 * @author aldenml
 *
 */
final class AzureusBittorrentDownload implements BittorrentDownload {

    private final TransferManager manager;
    private VuzeDownloadManager downloadManager;

    private List<BittorrentDownloadItem> items;
    private String hash;
    private Set<DiskManagerFileInfo> fileInfoSet;
    private final long size;
    private final String displayName;

    public AzureusBittorrentDownload(TransferManager manager, VuzeDownloadManager downloadManager) throws TOTorrentException {
        this.manager = manager;
        this.downloadManager = downloadManager;

        hash = TorrentUtil.hashToString(downloadManager.getHash());

        fileInfoSet = downloadManager.getNoSkippedFileInfoSet();

        this.size = downloadManager.getSize();
        this.displayName = downloadManager.getDisplayName();

        items = new ArrayList<BittorrentDownloadItem>(fileInfoSet.size());
        for (DiskManagerFileInfo fileInfo : fileInfoSet) {
            items.add(new AzureusBittorrentDownloadItem(fileInfo));
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStatus() {
        return downloadManager.getStatus();
    }

    public int getProgress() {
        return downloadManager.getProgress();
    }

    public long getSize() {
        return size;
    }

    public boolean isResumable() {
        return downloadManager.isResumable();
    }

    public boolean isPausable() {
        return downloadManager.isPausable();
    }

    public boolean isComplete() {
        return downloadManager.isComplete();
    }

    public boolean isDownloading() {
        return downloadManager.isDownloading();
    }

    public boolean isSeeding() {
        return downloadManager.isSeeding();
    }

    public List<? extends BittorrentDownloadItem> getItems() {
        if (items.size() == 1) {
            return Collections.emptyList();
        }
        return items;
    }

    public void pause() {
        downloadManager.pause();
    }

    public void resume() {
        downloadManager.resume();
    }

    public File getSavePath() {
        return downloadManager.getSavePath();
    }

    public long getBytesReceived() {
        return downloadManager.getBytesReceived();
    }

    public long getBytesSent() {
        return downloadManager.getBytesSent();
    }

    public long getDownloadSpeed() {
        return downloadManager.getDownloadSpeed();
    }

    public long getUploadSpeed() {
        return downloadManager.getUploadSpeed();
    }

    public long getETA() {
        return downloadManager.getETA();
    }

    public Date getDateCreated() {
        return downloadManager.getCreated();
    }

    public String getPeers() {
        int peers = downloadManager.getPeers();
        int connectedPeers = downloadManager.getConnectedPeers();
        boolean hasStarted = downloadManager.hasStarted();
        boolean hasScrape = downloadManager.hasScrape();
        return VuzeFormatter.formatPeers(peers, connectedPeers, hasStarted, hasScrape);
    }

    public String getSeeds() {
        int seeds = downloadManager.getSeeds();
        int connectedSeeds = downloadManager.getConnectedSeeds();
        boolean hasStarted = downloadManager.hasStarted();
        boolean hasScrape = downloadManager.hasScrape();
        return VuzeFormatter.formatSeeds(seeds, connectedSeeds, hasStarted, hasScrape);
    }

    public String getHash() {
        return hash;
    }

    public String getSeedToPeerRatio() {
        return VuzeFormatter.formatSeedToPeerRatio(downloadManager.getSeeds(), downloadManager.getPeers());
    }

    public String getShareRatio() {
        return VuzeFormatter.formatShareRatio(downloadManager.getShareRatio());
    }

    @Override
    public void cancel() {
        cancel(false);
    }

    public void cancel(boolean deleteData) {
        cancel(deleteData, true);
    }

    public void cancel(boolean deleteData, boolean async) {
        manager.remove(this);
        downloadManager.removeDownload(deleteData, deleteData);
    }

    @Override
    public List<? extends BittorrentDownloadItem> getBittorrentItems() {
        return items;
    }

    @Override
    public String getDetailsUrl() {
        return null;
    }
}
