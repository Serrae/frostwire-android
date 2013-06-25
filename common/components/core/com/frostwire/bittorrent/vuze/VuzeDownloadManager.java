/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, 2013, FrostWire(R). All rights reserved.
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

package com.frostwire.bittorrent.vuze;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.torrent.TOTorrentException;

import com.frostwire.bittorrent.BTorrentDownloadManager;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class VuzeDownloadManager implements BTorrentDownloadManager {

    private final DownloadManager dm;

    public VuzeDownloadManager(DownloadManager dm) {
        this.dm = dm;
    }

    public DownloadManager getDownloadManager() {
        return dm;
    }

    @Override
    public byte[] getHash() {
        try {
            return dm.getTorrent().getHash();
        } catch (TOTorrentException e) {
            throw new RuntimeException(e); // can't recover from this
        }
    }
}
