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
    private final VuzeDownloadManager dm;

    private final List<BittorrentDownloadItem> items;
    private final String hash;
    private final long size;
    private final String displayName;

    public AzureusBittorrentDownload(TransferManager manager, VuzeDownloadManager dm) throws TOTorrentException {
        this.manager = manager;
        this.dm = dm;

        this.hash = TorrentUtil.hashToString(dm.getHash());

        this.size = dm.getSize();
        this.displayName = dm.getDisplayName();

        Set<DiskManagerFileInfo> fileInfoSet = dm.getNoSkippedFileInfoSet();
        items = new ArrayList<BittorrentDownloadItem>(fileInfoSet.size());
        for (DiskManagerFileInfo fileInfo : fileInfoSet) {
            items.add(new AzureusBittorrentDownloadItem(fileInfo));
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStatus() {
        return dm.getStatus();
    }

    public int getProgress() {
        return dm.getProgress();
    }

    public long getSize() {
        return size;
    }

    public boolean isResumable() {
        return dm.isResumable();
    }

    public boolean isPausable() {
        return dm.isPausable();
    }

    public boolean isComplete() {
        return dm.isComplete();
    }

    public boolean isDownloading() {
        return dm.isDownloading();
    }

    public boolean isSeeding() {
        return dm.isSeeding();
    }

    public List<? extends BittorrentDownloadItem> getItems() {
        if (items.size() == 1) {
            return Collections.emptyList();
        }
        return items;
    }

    public void pause() {
        dm.pause();
    }

    public void resume() {
        dm.resume();
    }

    public File getSavePath() {
        return dm.getSavePath();
    }

    public long getBytesReceived() {
        return dm.getBytesReceived();
    }

    public long getBytesSent() {
        return dm.getBytesSent();
    }

    public long getDownloadSpeed() {
        return dm.getDownloadSpeed();
    }

    public long getUploadSpeed() {
        return dm.getUploadSpeed();
    }

    public long getETA() {
        return dm.getETA();
    }

    public Date getDateCreated() {
        return dm.getCreated();
    }

    public String getPeers() {
        int peers = dm.getPeers();
        int connectedPeers = dm.getConnectedPeers();
        boolean hasStarted = dm.hasStarted();
        boolean hasScrape = dm.hasScrape();
        return VuzeFormatter.formatPeers(peers, connectedPeers, hasStarted, hasScrape);
    }

    public String getSeeds() {
        int seeds = dm.getSeeds();
        int connectedSeeds = dm.getConnectedSeeds();
        boolean hasStarted = dm.hasStarted();
        boolean hasScrape = dm.hasScrape();
        return VuzeFormatter.formatSeeds(seeds, connectedSeeds, hasStarted, hasScrape);
    }

    public String getHash() {
        return hash;
    }

    public String getSeedToPeerRatio() {
        return VuzeFormatter.formatSeedToPeerRatio(dm.getSeeds(), dm.getPeers());
    }

    public String getShareRatio() {
        return VuzeFormatter.formatShareRatio(dm.getShareRatio());
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
        dm.removeDownload(deleteData, deleteData);
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
