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
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.tracker.client.TRTrackerScraperResponse;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.DisplayFormatters;

import com.frostwire.vuze.VuzeDownloadManager;

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
        if (isComplete()) {
            return 100;
        }

        if (downloadManager.isPartial()) {
            long downloaded = 0;
            for (DiskManagerFileInfo fileInfo : fileInfoSet) {
                downloaded += fileInfo.getDownloaded();
            }
            return (int) ((downloaded * 100) / size);
        } else {
            return downloadManager.getDM().getStats().getDownloadCompleted(true) / 10;
        }
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
        return downloadManager.getDM().getSaveLocation();
    }

    public long getBytesReceived() {
        return downloadManager.getDM().getStats().getTotalGoodDataBytesReceived();
    }

    public long getBytesSent() {
        return downloadManager.getDM().getStats().getTotalDataBytesSent();
    }

    public long getDownloadSpeed() {
        return downloadManager.getDM().getStats().getDataReceiveRate();// / 1000;
    }

    public long getUploadSpeed() {
        return downloadManager.getDM().getStats().getDataSendRate() / 1000;
    }

    public long getETA() {
        return downloadManager.getDM().getStats().getETA();
    }

    public Date getDateCreated() {
        return new Date(downloadManager.getDM().getCreationTime());
    }

    public String getPeers() {
        long lTotalPeers = -1;
        long lConnectedPeers = 0;
        if (downloadManager != null) {
            lConnectedPeers = downloadManager.getDM().getNbPeers();

            if (lTotalPeers == -1) {
                TRTrackerScraperResponse response = downloadManager.getDM().getTrackerScrapeResponse();
                if (response != null && response.isValid()) {
                    lTotalPeers = response.getPeers();
                }
            }
        }

        long totalPeers = lTotalPeers;
        if (totalPeers <= 0) {
            DownloadManager dm = downloadManager.getDM();
            if (dm != null) {
                totalPeers = dm.getActivationCount();
            }
        }

        //        long value = lConnectedPeers * 10000000;
        //        if (totalPeers > 0)
        //            value = value + totalPeers;

        int state = downloadManager.getDM().getState();
        boolean started = state == DownloadManager.STATE_SEEDING || state == DownloadManager.STATE_DOWNLOADING;
        boolean hasScrape = lTotalPeers >= 0;

        String tmp;
        if (started) {
            tmp = hasScrape ? (lConnectedPeers > lTotalPeers ? "%1" : "%1 " + "/" + " %2") : "%1";
        } else {
            tmp = hasScrape ? "%2" : "";
        }

        tmp = tmp.replaceAll("%1", String.valueOf(lConnectedPeers));
        tmp = tmp.replaceAll("%2", String.valueOf(totalPeers));

        return tmp;
    }

    public String getSeeds() {
        long lTotalSeeds = -1;
        //long lTotalPeers = 0;
        long lConnectedSeeds = 0;
        DownloadManager dm = downloadManager.getDM();
        if (dm != null) {
            lConnectedSeeds = dm.getNbSeeds();

            if (lTotalSeeds == -1) {
                TRTrackerScraperResponse response = dm.getTrackerScrapeResponse();
                if (response != null && response.isValid()) {
                    lTotalSeeds = response.getSeeds();
                    //lTotalPeers = response.getPeers();
                }
            }
        }

        //        // Allows for 2097151 of each type (connected seeds, seeds, peers)
        //        long value = (lConnectedSeeds << 42);
        //        if (lTotalSeeds > 0)
        //            value += (lTotalSeeds << 21);
        //        if (lTotalPeers > 0)
        //            value += lTotalPeers;

        //boolean bCompleteTorrent = dm == null ? false : dm.getAssumedComplete();

        int state = dm.getState();
        boolean started = (state == DownloadManager.STATE_SEEDING || state == DownloadManager.STATE_DOWNLOADING);
        boolean hasScrape = lTotalSeeds >= 0;
        String tmp;

        if (started) {
            tmp = hasScrape ? (lConnectedSeeds > lTotalSeeds ? "%1" : "%1 " + "/" + " %2") : "%1";
        } else {
            tmp = hasScrape ? "%2" : "";
        }
        tmp = tmp.replaceAll("%1", String.valueOf(lConnectedSeeds));
        String param2 = "?";
        if (lTotalSeeds != -1) {
            param2 = String.valueOf(lTotalSeeds);
        }
        tmp = tmp.replaceAll("%2", param2);

        return tmp;
    }

    public String getHash() {
        return hash;
    }

    public String getSeedToPeerRatio() {
        float ratio = -1;

        DownloadManager dm = downloadManager.getDM();
        if (dm != null) {
            TRTrackerScraperResponse response = dm.getTrackerScrapeResponse();
            int seeds;
            int peers;

            if (response != null && response.isValid()) {
                seeds = Math.max(dm.getNbSeeds(), response.getSeeds());

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
                seeds = dm.getNbSeeds();
                peers = dm.getNbPeers();
            }

            if (peers < 0 || seeds < 0) {
                ratio = 0;
            } else {
                if (peers == 0) {
                    if (seeds == 0)
                        ratio = 0;
                    else
                        ratio = Float.POSITIVE_INFINITY;
                } else {
                    ratio = (float) seeds / peers;
                }
            }
        }

        if (ratio == -1) {
            return "";
        } else if (ratio == 0) {
            return "??";
        } else {
            return DisplayFormatters.formatDecimal(ratio, 3);
        }
    }

    public String getShareRatio() {
        DownloadManager dm = downloadManager.getDM();

        int sr = (dm == null) ? 0 : dm.getStats().getShareRatio();

        if (sr == Integer.MAX_VALUE) {
            sr = Integer.MAX_VALUE - 1;
        }
        if (sr == -1) {
            sr = Integer.MAX_VALUE;
        }

        String shareRatio = "";

        if (sr == Integer.MAX_VALUE) {
            shareRatio = Constants.INFINITY_STRING;
        } else {
            shareRatio = DisplayFormatters.formatDecimal((double) sr / 1000, 3);
        }

        return shareRatio;
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
        TorrentUtil.removeDownload(downloadManager.getDM(), deleteData, deleteData, async);
    }

    DownloadManager getDownloadManager() {
        return downloadManager.getDM();
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
