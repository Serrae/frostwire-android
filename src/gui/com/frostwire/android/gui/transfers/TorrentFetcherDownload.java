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
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloader;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderCallBackInterface;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frostwire.android.R;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class TorrentFetcherDownload implements BittorrentDownload {
    
    private static final Logger LOG = LoggerFactory.getLogger(TorrentFetcherDownload.class);
            
    private final TransferManager manager;
    private final TorrentDownloadInfo info;
    private final Date dateCreated;

    private int statusResId;
    private final TorrentDownloader torrentDownloader;

    private BittorrentDownload delegate;

    private boolean removed;

    public TorrentFetcherDownload(TransferManager manager, TorrentDownloadInfo info) {
        this.manager = manager;
        this.info = info;
        this.dateCreated = new Date();

        this.statusResId = R.string.torrent_fetcher_download_status_downloading_torrent;
        this.torrentDownloader = TorrentDownloaderFactory.create(new TorrentDownloaderListener(), info.getTorrentUrl(), info.getDetailsUrl(), null);
        this.torrentDownloader.start();
    }

    public BittorrentDownload getDelegate() {
        return delegate;
    }

    public String getDisplayName() {
        return delegate != null ? delegate.getDisplayName() : info.getDisplayName();
    }

    public String getStatus() {
        return delegate != null ? delegate.getStatus() : String.valueOf(statusResId);
    }

    public int getProgress() {
        return delegate != null ? delegate.getProgress() : 0;
    }

    public long getSize() {
        return delegate != null ? delegate.getSize() : info.getSize();
    }

    public Date getDateCreated() {
        return delegate != null ? delegate.getDateCreated() : dateCreated;
    }

    public List<? extends TransferItem> getItems() {
        return delegate != null ? delegate.getItems() : new ArrayList<TransferItem>();
    }

    public File getSavePath() {
        return delegate != null ? delegate.getSavePath() : null;
    }

    public long getBytesReceived() {
        return delegate != null ? delegate.getBytesReceived() : 0;
    }

    public long getBytesSent() {
        return delegate != null ? delegate.getBytesSent() : 0;
    }

    public long getDownloadSpeed() {
        return delegate != null ? delegate.getDownloadSpeed() : 0;
    }

    public long getUploadSpeed() {
        return delegate != null ? delegate.getUploadSpeed() : 0;
    }

    public long getETA() {
        return delegate != null ? delegate.getETA() : 0;
    }

    public String getHash() {
        return delegate != null ? delegate.getHash() : info.getHash();
    }

    public String getPeers() {
        return delegate != null ? delegate.getPeers() : "";
    }

    public String getSeeds() {
        return delegate != null ? delegate.getSeeds() : "";
    }

    public String getSeedToPeerRatio() {
        return delegate != null ? delegate.getSeedToPeerRatio() : "";
    }

    public String getShareRatio() {
        return delegate != null ? delegate.getShareRatio() : "";
    }

    public boolean isResumable() {
        return delegate != null ? delegate.isResumable() : false;
    }

    public boolean isPausable() {
        return delegate != null ? delegate.isPausable() : false;
    }

    public boolean isComplete() {
        return delegate != null ? delegate.isComplete() : false;
    }

    @Override
    public boolean isDownloading() {
        return delegate != null ? delegate.isDownloading() : true;
    }

    @Override
    public boolean isSeeding() {
        return delegate != null ? delegate.isSeeding() : false;
    }

    @Override
    public void cancel() {
        cancel(false);
    }

    @Override
    public void cancel(boolean deleteData) {
        cancel(false, true);
    }

    @Override
    public void cancel(boolean deleteData, boolean async) {
        statusResId = R.string.torrent_fetcher_download_status_canceled;

        if (delegate != null) {
            delegate.cancel(deleteData, async);
        } else {
            removed = true;
            try {
                torrentDownloader.cancel();
            } catch (Throwable e) {
                // ignore, I can't do anything
                LOG.error("Error canceling torrent downloader",e);
            }
            try {
                torrentDownloader.getFile().delete();
            } catch (Throwable e) {
                // ignore, I can't do anything
                LOG.error("Error deleting file of torrent downloader",e);
            }
        }
        manager.remove(this);
    }

    public void pause() {
        if (delegate != null) {
            delegate.pause();
        }
    }

    public void resume() {
        if (delegate != null) {
            delegate.resume();
        }
    }

    private final class TorrentDownloaderListener implements TorrentDownloaderCallBackInterface {

        private AtomicBoolean finished = new AtomicBoolean(false);

        public void TorrentDownloaderEvent(int state, final TorrentDownloader inf) {
            if (removed) {
                return;
            }
            if (state == TorrentDownloader.STATE_FINISHED && finished.compareAndSet(false, true)) {
                try {

                    delegate = BittorrentDownloadCreator.create(manager, inf.getFile().getAbsolutePath(), null, info.getRelativePath());

                    if (delegate instanceof InvalidBittorrentDownload) {
                        cancel();
                    }

                    if (delegate == null) {
                        LOG.error("Error creating the actual torrent download, delegate after creation is null");
                    }

                } catch (Throwable e) {
                    statusResId = R.string.torrent_fetcher_download_status_error;
                    LOG.error("Error creating the actual torrent download", e);
                }
            } else if (state == TorrentDownloader.STATE_ERROR) {
                statusResId = R.string.torrent_fetcher_download_status_error;
            }
        }
    }

    @Override
    public List<? extends BittorrentDownloadItem> getBittorrentItems() {
        return (delegate != null) ? delegate.getBittorrentItems() : new ArrayList<BittorrentDownloadItem>(0);
    }

    @Override
    public String getDetailsUrl() {
        return info.getDetailsUrl();
    }
}